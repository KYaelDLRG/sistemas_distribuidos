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
 * Ejercicio 3:
 * 1. Retoma el código del Ejercicio 2 (copia a mapa mutable y agregado de cookies).
 * 2. Utiliza entrySet() para obtener las parejas clave-valor del mapa.
 * 3. Aplica la API de Streams con sorted() (utilizando Map.Entry.comparingByKey())
 *    para ordenar los encabezados alfabéticamente por su nombre (clave).
 * 4. Imprime los encabezados ordenados utilizando forEach().
 */
public class Ejercicio3 {

    // Cliente HTTP reutilizable con HTTP/1.1 y timeout de conexión de 10 segundos
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

        // Envío síncrono y recepción de la respuesta
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Obtención de las cabeceras de respuesta (HttpHeaders)
        HttpHeaders headers = response.headers();

        // =========================================================================
        // Ejercicio 1: Impresión de headers originales mediante un Iterator
        // =========================================================================
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
        Map<String, List<String>> nuevoMapaHeaders = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : mapHeaders.entrySet()) {
            nuevoMapaHeaders.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // Adición de los encabezados Set-Cookie
        List<String> setCookies = nuevoMapaHeaders.computeIfAbsent("Set-Cookie", k -> new ArrayList<>());
        setCookies.add("Max-Age=0");
        setCookies.add("id=123");
        setCookies.add("theme=dark");

        // Impresión sin ordenar (del Ejercicio 2)
        System.out.println("\n--- Mapa modificado con Set-Cookie (sin ordenar, usando forEach) ---");
        nuevoMapaHeaders.forEach((clave, valores) -> System.out.println(clave + ":" + valores));

        // =========================================================================
        // Ejercicio 3: Ordenamiento alfabético mediante entrySet(), Stream.sorted() y forEach()
        // =========================================================================
        // 1. entrySet(): Devuelve el Set<Map.Entry<String, List<String>>> del mapa.
        // 2. stream(): Abre el flujo secuencial sobre los elementos del conjunto.
        // 3. sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)):
        //    Ordena alfabéticamente por la clave (A -> Z), ignorando diferencias entre
        //    mayúsculas y minúsculas para que "access-control" (A) quede antes que "Set-Cookie" (S).
        // 4. forEach(): Operación terminal que itera sobre cada entrada ya ordenada.
        System.out.println("\n--- Headers ordenados alfabéticamente por llave (Stream sorted y forEach) ---");
        nuevoMapaHeaders.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> System.out.println(entry.getKey() + ":" + entry.getValue()));

        // Impresión de código de estado y cuerpo de la respuesta HTTP
        System.out.println("\nStatus Code: " + response.statusCode());
        System.out.println("Response Body:\n" + response.body());
    }
}
