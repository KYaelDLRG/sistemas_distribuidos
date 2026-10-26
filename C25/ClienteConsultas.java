import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ejercicio 3: Cliente de Consultas avanzadas con Query Parameters y Paginacion.
 * 
 * Permite ejecutar y visualizar las respuestas del servidor para:
 * 1. Filtro por minPromedio y prefijo de nombre (GET /alumnos?minPromedio=8&nombre=C)
 * 2. Paginacion explicita Pagina 1 (GET /alumnos?page=1&size=20)
 * 3. Paginacion explicita Pagina 2 (GET /alumnos?page=2&size=20)
 * 4. Peticion sin parametros para validar la proteccion por defecto contra devolucion completa por omision (GET /alumnos)
 */
public class ClienteConsultas {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("================================================================================");
        System.out.println("        EJERCICIO 3: PRUEBAS DE QUERY PARAMETERS Y PAGINACION                   ");
        System.out.println("================================================================================");

        // Prueba 1: Filtro minPromedio=8 y nombre=C
        ejecutarConsulta(
                "PRUEBA 1: Filtro de Alumnos con minPromedio >= 8 y nombre que inicia con 'C'",
                "http://localhost:8080/alumnos?minPromedio=8&nombre=C"
        );

        // Prueba 2: Paginacion Pagina 1, tamano 20
        ejecutarConsulta(
                "PRUEBA 2: Paginacion - Pagina 1 (size=20)",
                "http://localhost:8080/alumnos?page=1&size=20"
        );

        // Prueba 3: Paginacion Pagina 2, tamano 20
        ejecutarConsulta(
                "PRUEBA 3: Paginacion - Pagina 2 (size=20)",
                "http://localhost:8080/alumnos?page=2&size=20"
        );

        // Prueba 4: Omision de parametros (Valores por defecto page=1, size=20)
        ejecutarConsulta(
                "PRUEBA 4: Peticion sin parametros (Validacion de valores por defecto)",
                "http://localhost:8080/alumnos"
        );
    }

    private static void ejecutarConsulta(String titulo, String url) throws IOException, InterruptedException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(">>> " + titulo);
        System.out.println("URL Enviada : " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code : " + response.statusCode());
        System.out.println("Respuesta del Servidor:\n" + response.body());
    }
}
