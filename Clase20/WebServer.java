import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Servidor Web para la Práctica de NGINX (Reverse Proxy y Balanceador de Carga).
 * Sistemas Distribuidos - ESCOM IPN
 */
public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Endpoints requeridos por la práctica
        server.createContext("/status", new StatusHandler(port));
        server.createContext("/task", new TaskHandler());
        server.createContext("/", new RootHandler(port));

        // Pool de hilos para manejar conexiones concurrentes
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Servidor iniciado exitosamente y escuchando en el puerto: " + port);
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido proporcionado. Usando puerto por defecto: " + DEFAULT_PORT);
            }
        }

        try {
            WebServer webServer = new WebServer(port);
            webServer.start();
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor en el puerto " + port + ": " + e.getMessage());
        }
    }

    /**
     * Handler para el endpoint /status
     * Requerido para el Ejercicio 2 (Balanceador de Carga).
     * Devuelve el mensaje con el puerto en el que está respondiendo.
     */
    static class StatusHandler implements HttpHandler {
        private final int port;

        public StatusHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Formato exacto solicitado en el PDF (pág. 7)
            String response = "El servidor está vivo en el puerto " + port + "\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /**
     * Handler para el endpoint /task
     * Requerido para el Ejercicio 1 (Multiplicación de números).
     * Soporta solicitudes POST (cuerpo de texto, JSON o formulario) y GET (query params).
     */
    static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String rawInput = "";

            if ("POST".equalsIgnoreCase(method)) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    rawInput = sb.toString().trim();
                }
            } else if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    rawInput = query;
                }
            }

            // Extraer la lista de números si viene formateada como JSON o form param
            String numbersString = extractNumbers(rawInput);

            String responseMessage;
            int statusCode = 200;

            if (numbersString.isEmpty()) {
                responseMessage = "Por favor envía los números separados por comas (ejemplo: 3,4,5,6).";
                statusCode = 400;
            } else {
                try {
                    BigInteger result = calculateMultiplication(numbersString);
                    // Formato solicitado en la captura de pantalla del Ejercicio 1 (pág. 6):
                    // "El resultado de la multiplicación es 360"
                    responseMessage = "El resultado de la multiplicación es " + result;
                } catch (IllegalArgumentException e) {
                    responseMessage = "Error en el formato de entrada: " + e.getMessage();
                    statusCode = 400;
                }
            }

            byte[] responseBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private String extractNumbers(String input) {
            if (input == null || input.isEmpty()) {
                return "";
            }

            // Si viene codificado en URL como numeros=3%2C4...
            try {
                input = URLDecoder.decode(input, StandardCharsets.UTF_8.name());
            } catch (Exception ignored) {}

            // Si contiene "numeros=" o "numbers="
            if (input.contains("numeros=")) {
                int idx = input.indexOf("numeros=");
                input = input.substring(idx + "numeros=".length());
                int amp = input.indexOf("&");
                if (amp != -1) {
                    input = input.substring(0, amp);
                }
            }

            // Si viene en formato JSON: {"numeros": "3,4,5,6"}
            if (input.startsWith("{") && input.endsWith("}")) {
                int valIdx = input.indexOf(":");
                if (valIdx != -1) {
                    input = input.substring(valIdx + 1).replace("\"", "").replace("}", "").trim();
                }
            }

            return input.trim();
        }

        private BigInteger calculateMultiplication(String input) {
            String[] tokens = input.split(",");
            if (tokens.length == 0) {
                throw new IllegalArgumentException("No se especificaron números");
            }

            BigInteger product = BigInteger.ONE;
            boolean atLeastOne = false;

            for (String token : tokens) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        BigInteger num = new BigInteger(trimmed);
                        product = product.multiply(num);
                        atLeastOne = true;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Valor no numérico encontrado: '" + trimmed + "'");
                    }
                }
            }

            if (!atLeastOne) {
                throw new IllegalArgumentException("La cadena no contenía números válidos");
            }

            return product;
        }
    }

    /**
     * Handler general para la raíz o rutas no mapeadas.
     */
    static class RootHandler implements HttpHandler {
        private final int port;

        public RootHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String response = "Backend Java activo en el puerto " + port + ". Endpoints disponibles: /status, /task\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
    }
}
