import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/**
 * Ejercicio 2: Cliente HTTP Sincrono.
 * 
 * Lee nombres.txt y apellidos.txt, genera las 125 combinaciones con calificaciones
 * pseudoaleatorias (semilla 12345), envia cada registro mediante HTTP POST al endpoint /alumnos
 * utilizando String.format y HttpRequest.BodyPublishers.ofString(json), e imprime
 * cada solicitud y respuesta, finalizando con la consulta GET /alumnos.
 */
public class ClienteSincrono {

    private static final String BASE_URL = "http://localhost:8080/alumnos";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {
        String urlServidor = BASE_URL;
        if (args.length > 0) {
            urlServidor = args[0];
        }

        System.out.println("================================================================================");
        System.out.println("                   PRACTICA C25 - CLIENTE HTTP SINCRONO                         ");
        System.out.println("================================================================================");

        // 1. Verificacion Ejercicio 1: Consultar GET /alumnos cuando la lista esta vacia
        System.out.println("\n>>> [PASO 1] Verificacion Ejercicio 1: GET /alumnos inicial (lista vacia)");
        HttpRequest requestGetVacio = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(urlServidor))
                .setHeader("Accept", "application/json")
                .build();

        HttpResponse<String> responseGetVacio = httpClient.send(requestGetVacio, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status Code: " + responseGetVacio.statusCode());
        System.out.println("Respuesta del servidor: " + responseGetVacio.body());
        System.out.println("--------------------------------------------------------------------------------");

        // 2. Cargar archivos de nombres y apellidos
        ArrayList<String> nombres = leerArchivo("nombres.txt");
        ArrayList<String> apellidos = leerArchivo("apellidos.txt");

        System.out.println("\n>>> [PASO 2] Archivos cargados exitosamente:");
        System.out.println("Nombres   (" + nombres.size() + "): " + nombres);
        System.out.println("Apellidos (" + apellidos.size() + "): " + apellidos);
        System.out.println("Total de combinaciones a generar: " + (nombres.size() * apellidos.size() * apellidos.size()));
        System.out.println("--------------------------------------------------------------------------------");

        // 3. Generar combinaciones y enviar solicitudes POST (Ejercicio 2)
        System.out.println("\n>>> [PASO 3] Enviando solicitudes HTTP POST con cada registro...");
        Random random = new Random(12345); // Semilla fija requerida
        int contador = 0;

        for (String nombre : nombres) {
            for (String ap1 : apellidos) {
                for (String ap2 : apellidos) {
                    contador++;

                    double numero = random.nextDouble() * 100;
                    numero = Math.round(numero * 10) / 100.0;

                    // Construccion del JSON con String.format y Locale.US (punto decimal)
                    String json = String.format(
                            Locale.US,
                            "{\"nombre\":\"%s\",\"apellido1\":\"%s\",\"apellido2\":\"%s\",\"promedio\":%.2f}",
                            nombre, ap1, ap2, numero
                    );

                    // Envio mediante HttpRequest.BodyPublishers.ofString(json)
                    HttpRequest requestPost = HttpRequest.newBuilder()
                            .uri(URI.create(urlServidor))
                            .header("Content-Type", "application/json; charset=UTF-8")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> responsePost = httpClient.send(requestPost, HttpResponse.BodyHandlers.ofString());

                    System.out.printf("[%03d/125] SOLICITUD POST -> %s%n", contador, urlServidor);
                    System.out.println("   JSON Enviado    : " + json);
                    System.out.println("   Status Code     : " + responsePost.statusCode());
                    System.out.println("   Respuesta Server:\n" + responsePost.body());
                    System.out.println("--------------------------------------------------------------------------------");
                }
            }
        }

        // 4. Verificacion final Ejercicio 2: Consulta GET /alumnos tras las inserciones
        System.out.println("\n>>> [PASO 4] Consulta final: GET /alumnos (Respuesta del servidor tras insertar 125 registros)");
        HttpRequest requestGetFinal = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(urlServidor))
                .setHeader("Accept", "application/json")
                .build();

        HttpResponse<String> responseGetFinal = httpClient.send(requestGetFinal, HttpResponse.BodyHandlers.ofString());
        System.out.println("URL consultada         : " + urlServidor);
        System.out.println("Status Code            : " + responseGetFinal.statusCode());
        System.out.println("Respuesta del servidor :\n" + responseGetFinal.body());
        System.out.println("================================================================================");
    }

    public static ArrayList<String> leerArchivo(String nombreArchivo) {
        ArrayList<String> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    lista.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + nombreArchivo);
            e.printStackTrace();
        }
        return lista;
    }
}
