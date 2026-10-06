import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Práctica: Sistemas Distribuidos - Cliente HTTP Moderno en Java 11+
 * Ejercicio 2:
 * 1. Mantiene la impresión de headers originales usando Iterator (Ejercicio 1).
 * 2. Copia el mapa inmutable de encabezados a un nuevo HashMap mutable.
 * 3. Añade los encabezados Set-Cookie: "Max-Age=0", "id=123", "theme=dark".
 * 4. Imprime el contenido del nuevo mapa utilizando el método forEach con expresiones Lambda.
 */
public class Ejercicio2 {

    // Cliente HTTP reutilizable configurado con HTTP/1.1 y timeout de 10 segundos
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {

        // Construcción de la solicitud GET hacia httpbin.org
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://httpbin.org/get"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();

        // Envío síncrono de la solicitud y recepción de la respuesta en String
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Obtención de las cabeceras devueltas por el servidor
        HttpHeaders headers = response.headers();

        // =========================================================================
        // Ejercicio 1: Impresión de los headers originales mediante un Iterator
        // =========================================================================
        // headers.map() devuelve un Map<String, List<String>> inmutable
        Map<String, List<String>> mapHeaders = headers.map();
        Iterator<Map.Entry<String, List<String>>> iterator = mapHeaders.entrySet().iterator();

        System.out.println("--- Headers originales recibidos (usando Iterator) ---");
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            String nombreHeader = entry.getKey();
            List<String> valoresHeader = entry.getValue();

            System.out.println(nombreHeader + ":" + valoresHeader);
        }

        // =========================================================================
        // Ejercicio 2: Copiar a un nuevo mapa mutable y agregar headers Set-Cookie
        // =========================================================================
        // NOTA: headers.map() es inmutable; intentar modificarlo lanzaría UnsupportedOperationException.
        // Por ello, se copia cada entrada a un nuevo HashMap con listas mutables independientes.
        Map<String, List<String>> nuevoMapaHeaders = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : mapHeaders.entrySet()) {
            nuevoMapaHeaders.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // Se agregan los headers "Set-Cookie" solicitados
        // Cada header HTTP puede contener una lista con múltiples valores
        List<String> cookies = nuevoMapaHeaders.computeIfAbsent("Set-Cookie", k -> new ArrayList<>());
        cookies.add("Max-Age=0");
        cookies.add("id=123");
        cookies.add("theme=dark");

        // Impresión del nuevo mapa utilizando el método forEach con expresión Lambda
        System.out.println("\n--- Nuevo mapa con Set-Cookie agregado (usando forEach) ---");
        nuevoMapaHeaders.forEach((clave, valores) -> System.out.println(clave + ":" + valores));

        // Impresión del código de estado y cuerpo de la respuesta HTTP
        System.out.println("\nStatus Code: " + response.statusCode());
        System.out.println("Response Body:\n" + response.body());
    }
}
