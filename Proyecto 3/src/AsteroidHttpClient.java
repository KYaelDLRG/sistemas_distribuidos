/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsteroidHttpClient {
    private final String baseUrl;
    private int timeoutMs = 3000;

    public AsteroidHttpClient(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
    }

    public boolean testConnection() {
        try {
            String response = sendGet(baseUrl + "/api/status");
            return response != null && response.contains("online");
        } catch (Exception e) {
            return false;
        }
    }

    public int fetchTotalCreatedCount() {
        try {
            String response = sendGet(baseUrl + "/api/asteroids/count");
            return extractIntField(response, "totalCreated", -1);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean createManualAsteroid(double speed, double dirX, double dirY, double angularVelocity) {
        try {
            String jsonPayload = String.format(
                "{\"speed\":%.4f,\"dirX\":%.4f,\"dirY\":%.4f,\"angularVelocity\":%.4f}",
                speed, dirX, dirY, angularVelocity
            );
            String response = sendPost(baseUrl + "/api/asteroid", jsonPayload);
            return response != null && response.contains("\"status\":\"ok\"");
        } catch (Exception e) {
            System.err.println("[AsteroidHttpClient] Error al enviar asteroide manual: " + e.getMessage());
            return false;
        }
    }

    public int createRandomAsteroids(int count) {
        try {
            String jsonPayload = String.format("{\"count\":%d}", count);
            String response = sendPost(baseUrl + "/api/asteroids/random", jsonPayload);
            if (response != null && response.contains("\"status\":\"ok\"")) {
                return extractIntField(response, "added", count);
            }
            return 0;
        } catch (Exception e) {
            System.err.println("[AsteroidHttpClient] Error al enviar asteroides aleatorios: " + e.getMessage());
            return 0;
        }
    }

    private String sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            return readStream(conn);
        }
        return null;
    }

    private String sendPost(String urlStr, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setDoOutput(true);

        byte[] outputBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        OutputStream os = conn.getOutputStream();
        os.write(outputBytes);
        os.close();

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            return readStream(conn);
        }
        return null;
    }

    private String readStream(HttpURLConnection conn) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private int extractIntField(String json, String fieldName, int def) {
        if (json == null) return def;
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
