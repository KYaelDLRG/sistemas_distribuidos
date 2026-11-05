/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsteroidHttpServer implements HttpHandler {
    private final int port;
    private final AsteroidSimulation simulation;
    private HttpServer server;

    public AsteroidHttpServer(int port, AsteroidSimulation simulation) {
        this.port = port;
        this.simulation = simulation;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", this);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("[AsteroidHttpServer] Servidor HTTP escuchando en el puerto " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[AsteroidHttpServer] Servidor HTTP detenido.");
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Cabeceras CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendResponse(exchange, 204, "");
            return;
        }

        try {
            if ("/api/asteroid".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCreateManual(exchange);
            } else if ("/api/asteroids/random".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCreateRandom(exchange);
            } else if ("/api/asteroids/count".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetCount(exchange);
            } else if ("/api/status".equals(path) && "GET".equalsIgnoreCase(method)) {
                sendResponse(exchange, 200, "{\"status\":\"online\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Ruta no encontrada: " + path + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Error interno: " + e.getMessage() + "\"}");
        }
    }

    private void handleCreateManual(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseParameters(exchange.getRequestURI().getQuery(), body);

        double speed = parseDouble(params.get("speed"), 3.0);
        double dirX = parseDouble(params.get("dirX"), 1.0);
        double dirY = parseDouble(params.get("dirY"), 0.0);
        double angularVelocity = parseDouble(params.get("angularVelocity"), 2.0);

        Asteroid created = simulation.addManualAsteroid(speed, dirX, dirY, angularVelocity);

        String jsonResponse = String.format(
            "{\"status\":\"ok\",\"action\":\"manual\",\"id\":%d,\"totalCreated\":%d,\"active\":%d}",
            created.getId(), simulation.getTotalCreatedCount(), simulation.getActiveCount()
        );
        sendResponse(exchange, 200, jsonResponse);
    }

    private void handleCreateRandom(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseParameters(exchange.getRequestURI().getQuery(), body);

        int count = parseInt(params.get("count"), 1);
        if (count < 1) count = 1;

        int added = simulation.addRandomAsteroids(count);

        String jsonResponse = String.format(
            "{\"status\":\"ok\",\"action\":\"random\",\"added\":%d,\"totalCreated\":%d,\"active\":%d}",
            added, simulation.getTotalCreatedCount(), simulation.getActiveCount()
        );
        sendResponse(exchange, 200, jsonResponse);
    }

    private void handleGetCount(HttpExchange exchange) throws IOException {
        String jsonResponse = String.format(
            "{\"status\":\"ok\",\"totalCreated\":%d,\"active\":%d}",
            simulation.getTotalCreatedCount(), simulation.getActiveCount()
        );
        sendResponse(exchange, 200, jsonResponse);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString().trim();
    }

    private Map<String, String> parseParameters(String query, String body) {
        Map<String, String> map = new HashMap<String, String>();

        // Parsear query string
        if (query != null && !query.isEmpty()) {
            parseFormEncoded(query, map);
        }

        // Parsear body (JSON o Form Encoded)
        if (body != null && !body.isEmpty()) {
            if (body.startsWith("{") && body.endsWith("}")) {
                parseJsonSimple(body, map);
            } else {
                parseFormEncoded(body, map);
            }
        }

        return map;
    }

    private void parseFormEncoded(String data, Map<String, String> target) {
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx).trim();
                String val = pair.substring(idx + 1).trim();
                target.put(key, val);
            }
        }
    }

    private void parseJsonSimple(String json, Map<String, String> target) {
        Pattern pattern = Pattern.compile("\"([a-zA-Z0-9_]+)\"\\s*:\\s*([0-9.\\-+eE]+|\"[^\"]*\")");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = matcher.group(2).replace("\"", "").trim();
            target.put(key, val);
        }
    }

    private double parseDouble(String str, double def) {
        if (str == null || str.isEmpty()) return def;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private int parseInt(String str, int def) {
        if (str == null || str.isEmpty()) return def;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
