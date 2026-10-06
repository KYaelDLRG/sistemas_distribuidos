import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://httpbin.org/get"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Obtención de las cabeceras de respuesta (HttpHeaders)
        HttpHeaders headers = response.headers();

        // -------------------------------------------------------------------------
        // Ejercicio 1: Sustitución de headers.map().forEach(...) por un Iterator
        // -------------------------------------------------------------------------
        Map<String, List<String>> mapHeaders = headers.map();
        Iterator<Map.Entry<String, List<String>>> iterator = mapHeaders.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            String nombreHeader = entry.getKey();
            List<String> valoresHeader = entry.getValue();

            // Imprime cada header con su lista de valores en el mismo formato
            System.out.println(nombreHeader + ":" + valoresHeader);
        }

        // Imprimir código de estado
        System.out.println("\nStatus Code: " + response.statusCode());

        // Imprimir cuerpo de la respuesta
        System.out.println("Response Body:\n" + response.body());
    }
}
