package networking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Cliente HTTP Asíncrono en Java.
 * Utiliza HTTP/1.1 para conexión persistente y pool de conexiones.
 * Despacha solicitudes POST asíncronas devolviendo un CompletableFuture<String>.
 */
public class WebClient {
    private HttpClient client;

    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * Envía una tarea al worker especificado de forma asíncrona.
     * 
     * @param url Dirección del servidor trabajador (ej. http://localhost:8081/task)
     * @param requestPayload Datos de la tarea en bytes ("hash,letra")
     * @return CompletableFuture<String> con el cuerpo de la respuesta ("NULL" o "La contraseña es: <palabra>\n")
     */
    public CompletableFuture<String> sendTask(String url, byte[] requestPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
                .uri(URI.create(url))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
