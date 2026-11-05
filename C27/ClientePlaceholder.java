import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ejercicio 2 (Item 2) - Práctica Cloud Translation API:
 * 
 * Creación de un recurso ficticio mediante solicitud HTTP POST a JSONPlaceholder.
 * Envía un payload en formato JSON con la cabecera 'Content-Type: application/json; charset=UTF-8',
 * conteniendo el Grupo, Nombre del Alumno y userId.
 * 
 * Materia: Sistemas Distribuidos - ESCOM IPN
 * Alumno : Kaleb Yael De La Rosa Gutiérrez
 * Grupo  : 7CM1
 */
public class ClientePlaceholder {

    private static final String POSTS_ENDPOINT = "https://jsonplaceholder.typicode.com/posts";

    public static void main(String[] args) {
        String grupo = "7CM1";
        String alumno = "Kaleb Yael De La Rosa Gutiérrez";
        int userId = 1;

        if (args.length >= 2) {
            grupo = args[0];
            alumno = args[1];
        }

        System.out.println("================================================================================");
        System.out.println("   EJERCICIO 2: CREACION DE RECURSO CON SOLICITUD HTTP POST (JSONPlaceholder)   ");
        System.out.println("   Alumno: " + alumno + " | Grupo: " + grupo);
        System.out.println("================================================================================");
        System.out.println(">>> Endpoint Destino: " + POSTS_ENDPOINT);

        // 1. Construcción del cuerpo JSON requerido por la práctica
        String jsonPayload = String.format(
                "{\n  \"title\": \"%s\",\n  \"body\": \"%s\",\n  \"userId\": %d\n}",
                grupo, alumno, userId
        );

        System.out.println("\n>>> Cuerpo del mensaje HTTP (JSON) a enviar:");
        System.out.println(jsonPayload);

        // 2. Construcción del cliente HTTP síncrono
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 3. Configuración de la solicitud POST con cabecera Content-Type recomendada
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POSTS_ENDPOINT))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            System.out.println("\n>>> Enviando solicitud HTTP POST...");
            long inicio = System.currentTimeMillis();

            // 4. Envío síncrono
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long latencia = System.currentTimeMillis() - inicio;

            // 5. Presentación de resultados y verificación de ID generado
            System.out.println("\n=========================== RESPUESTA DEL SERVIDOR ============================");
            System.out.println("Código de Estado HTTP : " + response.statusCode() + " (" + (response.statusCode() == 201 ? "201 Created - ÉXITO" : "Respuesta recibida") + ")");
            System.out.println("Tiempo de Respuesta   : " + latencia + " ms");
            System.out.println("Encabezado Content-Type: " + response.headers().firstValue("Content-Type").orElse("N/A"));
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Respuesta JSON obtenida (con ID autogenerado por el backend):");
            System.out.println(response.body());
            System.out.println("================================================================================\n");

        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Error durante el envío de la solicitud POST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
