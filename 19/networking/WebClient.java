/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package networking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Cliente HTTP Asíncrono en Java.
 * 
 * Explicación según el video:
 * 1. Mantiene una colección de conexiones abiertas TCP que permite reutilizarlas para
 *    varias transacciones HTTP sin necesidad de reconectarse en cada transacción (Connection Pooling).
 * 2. Utiliza el protocolo HTTP versión 1.1 para conexiones persistentes.
 * 3. En lugar de bloquearse esperando la respuesta del servidor, despacha solicitudes de
 *    manera asíncrona mediante client.sendAsync(), devolviendo un CompletableFuture<String>
 *    que se resolverá cuando los datos estén disponibles en el futuro.
 */
public class WebClient {
    private HttpClient client;

    /**
     * El constructor crea un objeto HttpClient llamado 'client' configurado
     * para utilizar el protocolo HTTP versión 1.1 (habilita connection pooling).
     */
    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * Envía una tarea al servidor especificado de forma asíncrona.
     * 
     * @param url Dirección del servidor trabajador (e.g. http://localhost:8081/searchtoken)
     * @param requestPayload Datos de la tarea en bytes (e.g. "1757600,IPN".getBytes())
     * @return CompletableFuture<String> que contendrá la respuesta del servidor en el futuro
     */
    public CompletableFuture<String> sendTask(String url, byte[] requestPayload) {
        // Se crea un objeto HttpRequest con el método POST y la dirección destino
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
                .uri(URI.create(url))
                .build();

        // Se envía la solicitud de manera asíncrona con sendAsync
        // thenApply extrae el cuerpo (body) de la respuesta cuando esté lista
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
