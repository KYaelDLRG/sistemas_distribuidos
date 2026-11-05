import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ejercicio 1 (Item 1) - Práctica Cloud Translation API:
 * 
 * Consumo de una API REST pública sin token de validación: JokeAPI.
 * Utiliza el cliente HTTP síncrono nativo de Java (HttpClient) para enviar una
 * solicitud GET y obtener un chiste de la categoría deseada.
 * 
 * Materia: Sistemas Distribuidos - ESCOM IPN
 * Alumno : Kaleb Yael De La Rosa Gutiérrez
 * Grupo  : 7CM1
 */
public class ClienteJokeAPI {

    // Endpoint público oficial de JokeAPI v2 (categoría Programming)
    private static final String JOKE_API_URL = "https://v2.jokeapi.dev/joke/Programming?format=json";

    public static void main(String[] args) {
        String endpoint = JOKE_API_URL;
        if (args.length > 0) {
            endpoint = args[0];
        }

        System.out.println("================================================================================");
        System.out.println("   EJERCICIO 1: CONSUMO DE API REST PUBLICA SIN AUTENTICACION (JokeAPI)         ");
        System.out.println("   Alumno: Kaleb Yael De La Rosa Gutiérrez | Grupo: 7CM1                        ");
        System.out.println("================================================================================");
        System.out.println(">>> Preparando cliente HTTP síncrono...");
        System.out.println(">>> URL Destino: " + endpoint);

        // 1. Construcción del cliente HTTP síncrono
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        // 2. Construcción de la solicitud HTTP GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(endpoint))
                .header("Accept", "application/json")
                .header("User-Agent", "Java11-HttpClient-ESCOM/1.0")
                .build();

        try {
            System.out.println("\n>>> Enviando solicitud HTTP GET a JokeAPI...");
            long tiempoInicio = System.currentTimeMillis();

            // 3. Envío síncrono y bloqueo hasta recibir respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long latencia = System.currentTimeMillis() - tiempoInicio;

            // 4. Despliegue de resultados
            System.out.println("\n=========================== RESPUESTA DEL SERVIDOR ============================");
            System.out.println("Código de Estado HTTP: " + response.statusCode() + " OK");
            System.out.println("Tiempo de Respuesta  : " + latencia + " ms");
            System.out.println("Content-Type         : " + response.headers().firstValue("Content-Type").orElse("N/A"));
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Cuerpo JSON recibido (Raw Payload):");
            System.out.println(response.body());
            System.out.println("================================================================================\n");

        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Ocurrió una excepción al comunicarse con la API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
