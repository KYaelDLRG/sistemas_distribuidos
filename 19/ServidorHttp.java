import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP Concurrente en Java.
 * 
 * Implementa el endpoint /searchtoken (Ejercicio 5 de la Clase 16).
 * Recibe solicitudes POST con cuerpo "<cantidad>,<TOKEN>", genera <cantidad>
 * tokens aleatorios de 3 caracteres ('A' - 'Z') y retorna el conteo de apariciones del token.
 */
public class ServidorHttp {

    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido, usando puerto por defecto: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        int numHilos = 8;
        if (args.length > 1) {
            try {
                numHilos = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(numHilos));

        // Registro del endpoint principal /searchtoken
        server.createContext("/searchtoken", new SearchTokenHandler(port));

        // Endpoint raíz para chequeo de salud
        server.createContext("/", exchange -> {
            byte[] resp = ("Servidor HTTP activo en el puerto " + server.getAddress().getPort()).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        server.start();
        System.out.println("Servidor HTTP iniciado exitosamente en el puerto " + port + " con pool de " + numHilos + " hilos.");
    }

    /**
     * Manejador del endpoint /searchtoken
     */
    private static class SearchTokenHandler implements HttpHandler {
        private final int port;

        public SearchTokenHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Método no permitido
                return;
            }

            // Lectura del cuerpo de la petición
            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            String body = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim();

            // Formato esperado: "<cantidad>,<TOKEN>" (e.g. "1757600,IPN")
            String[] parts = body.split(",");
            if (parts.length < 2) {
                byte[] errorResp = "Formato inválido. Debe ser: <cantidad>,<TOKEN>".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, errorResp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorResp);
                }
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(parts[0].trim());
            } catch (NumberFormatException e) {
                byte[] errorResp = "Cantidad numérica inválida".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, errorResp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorResp);
                }
                return;
            }

            String token = parts[1].trim().toUpperCase();

            // Conteo de apariciones del token en tokens generados aleatoriamente
            int apariciones = contarApariciones(cantidad, token);

            // Respuesta con el número de apariciones
            byte[] responseBytes = String.valueOf(apariciones).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        /**
         * Genera 'cantidad' combinaciones aleatorias de 3 letras ('A'-'Z') y cuenta
         * cuántas veces coincide con el token buscado.
         */
        private int contarApariciones(int cantidad, String token) {
            if (token == null || token.length() != 3) {
                return 0;
            }

            Random rand = new Random();
            char t0 = token.charAt(0);
            char t1 = token.charAt(1);
            char t2 = token.charAt(2);
            int count = 0;

            for (int i = 0; i < cantidad; i++) {
                char c0 = (char) ('A' + rand.nextInt(26));
                char c1 = (char) ('A' + rand.nextInt(26));
                char c2 = (char) ('A' + rand.nextInt(26));
                if (c0 == t0 && c1 == t1 && c2 == t2) {
                    count++;
                }
            }
            return count;
        }
    }
}
