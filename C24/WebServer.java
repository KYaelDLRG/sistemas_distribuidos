import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Servidor Web RESTful (micro-SAES) en Java con soporte JSON (Google Gson) y Códigos de Estado HTTP.
 * 
 * Implementa la especificación completa solicitada en la Clase 24:
 * 1. Endpoint inexistente o incorrecto (/chetito) -> 404 Not Found con JSON de error.
 * 2. Identificador no numérico (GET /alumnos/p, DELETE /alumnos/f) -> 400 Bad Request con {"error": "ID invalido"}.
 * 3. POST /alumnos con JSON de entrada -> 201 Created con JSON del nuevo alumno (incluye ID asignado).
 * 4. GET /alumnos/{id}: si existe -> 200 OK con JSON del alumno; si no existe -> 404 Not Found con {"error": "Alumno no encontrado"}.
 * 5. PUT /alumnos/{id} con JSON de actualización: si existe -> 200 OK con {"mensaje": "Alumno actualizado"}; si no existe -> 404 Not Found con {"error": "Alumno no encontrado"}.
 * 6. DELETE /alumnos/{id}: si existe -> 204 No Content (sin cuerpo); si no existe -> 404 Not Found con {"error": "Alumno no encontrado"}.
 * 
 * Mantiene activo el endpoint /status para compatibilidad y health check.
 * Sistemas Distribuidos - ESCOM IPN
 */
public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private HttpServer server;

    // Instancia de Google Gson para serialización y deserialización JSON
    private final Gson gson = new Gson();

    // Estructura de almacenamiento en memoria para los alumnos
    private final Map<Integer, Alumno> alumnos = new HashMap<>();
    private int ultimoId = 0;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Registro de endpoints del servicio
        server.createContext("/status", new StatusHandler(port));
        server.createContext("/alumnos", new AlumnosHandler());
        server.createContext("/", new RootHandler(port));

        // Pool de hilos para atención concurrente de solicitudes HTTP
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("=================================================================");
        System.out.println(" Servidor RESTful micro-SAES (Clase 24 - Gson) iniciado en: " + port);
        System.out.println(" Endpoint de estado:  http://localhost:" + port + "/status");
        System.out.println(" Endpoint de alumnos: http://localhost:" + port + "/alumnos");
        System.out.println(" Formato de datos:    JSON (application/json)");
        System.out.println("=================================================================");
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
     * Handler para el endpoint /alumnos.
     * Delega el procesamiento al método estructurado handleAlumnosRequest.
     */
    class AlumnosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleAlumnosRequest(exchange);
        }
    }

    /**
     * Método principal de despacho REST para el endpoint /alumnos.
     * Mapea cada método HTTP (GET, POST, PUT, DELETE) con su respectiva operación y código de estado.
     */
    private void handleAlumnosRequest(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        String metodo = exchange.getRequestMethod().toUpperCase();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("[PETICIÓN] Método: " + metodo + " | Ruta: " + ruta);

        // Validar que la ruta sea exactamente /alumnos o empiece con /alumnos/
        if (!ruta.equals("/alumnos") && !ruta.startsWith("/alumnos/")) {
            System.out.println("[RESPUESTA 404] Endpoint incorrecto: " + ruta);
            enviarErrorJson(exchange, 404, "Recurso no encontrado");
            return;
        }

        String[] partes = ruta.split("/");
        // ruta = "/alumnos"   -> partes: ["", "alumnos"] (longitud 2)
        // ruta = "/alumnos/1" -> partes: ["", "alumnos", "1"] (longitud 3)

        switch (metodo) {
            case "GET":
                handleGet(exchange, partes);
                break;
            case "POST":
                handlePost(exchange, partes);
                break;
            case "PUT":
                handlePut(exchange, partes);
                break;
            case "DELETE":
                handleDelete(exchange, partes);
                break;
            default:
                System.out.println("[RESPUESTA 405] Método no permitido: " + metodo);
                enviarErrorJson(exchange, 405, "Método no permitido");
                break;
        }
    }

    /**
     * Procesa peticiones GET:
     * - GET /alumnos       -> Retorna lista completa de alumnos en JSON (200 OK)
     * - GET /alumnos/{id}  -> Retorna alumno en JSON (200 OK) o 404 Not Found si no existe.
     * - GET /alumnos/p     -> Retorna 400 Bad Request con {"error": "ID invalido"} (Inciso 2)
     */
    private void handleGet(HttpExchange exchange, String[] partes) throws IOException {
        // Caso: GET /alumnos o GET /alumnos/
        if (partes.length == 2 || (partes.length == 3 && partes[2].trim().isEmpty())) {
            synchronized (alumnos) {
                String jsonLista = gson.toJson(new ArrayList<>(alumnos.values()));
                System.out.println("[RESPUESTA 200] Listando " + alumnos.size() + " alumno(s)");
                enviarRespuestaJson(exchange, 200, jsonLista);
            }
            return;
        }

        // Caso: GET /alumnos/{id}
        if (partes.length == 3) {
            int idBuscado;
            try {
                idBuscado = Integer.parseInt(partes[2].trim());
            } catch (NumberFormatException e) {
                // Inciso 2: Identificador no numérico
                System.out.println("[RESPUESTA 400] Identificador no numérico: " + partes[2]);
                enviarErrorJson(exchange, 400, "ID invalido");
                return;
            }

            synchronized (alumnos) {
                Alumno alumno = alumnos.get(idBuscado);
                if (alumno != null) {
                    // Inciso 4: Alumno existe -> 200 OK y JSON con datos
                    String json = gson.toJson(alumno);
                    System.out.println("[RESPUESTA 200] Alumno encontrado ID=" + idBuscado + " | " + json);
                    enviarRespuestaJson(exchange, 200, json);
                } else {
                    // Inciso 4: Alumno no existe -> 404 Not Found y JSON {"error": "Alumno no encontrado"}
                    System.out.println("[RESPUESTA 404] Alumno no encontrado ID=" + idBuscado);
                    enviarErrorJson(exchange, 404, "Alumno no encontrado");
                }
            }
            return;
        }

        System.out.println("[RESPUESTA 404] Ruta inválida bajo /alumnos");
        enviarErrorJson(exchange, 404, "Recurso no encontrado");
    }

    /**
     * Procesa peticiones POST /alumnos:
     * - Lee payload en formato JSON (Inciso 3).
     * - Asigna ID autoincremental.
     * - Guarda en el Map.
     * - Retorna código 201 Created y el JSON del alumno con su nuevo ID (Inciso 3).
     */
    private void handlePost(HttpExchange exchange, String[] partes) throws IOException {
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            System.out.println("[RESPUESTA 400] POST no admite identificador en la URI");
            enviarErrorJson(exchange, 400, "POST debe enviarse a /alumnos sin ID en la ruta");
            return;
        }

        String cuerpo = leerCuerpo(exchange);
        if (cuerpo.isEmpty()) {
            System.out.println("[RESPUESTA 400] POST recibido con cuerpo vacío");
            enviarErrorJson(exchange, 400, "Cuerpo de solicitud vacío");
            return;
        }

        Alumno nuevoAlumno;
        try {
            nuevoAlumno = gson.fromJson(cuerpo, Alumno.class);
        } catch (JsonSyntaxException e) {
            System.out.println("[RESPUESTA 400] Sintaxis JSON inválida en POST: " + e.getMessage());
            enviarErrorJson(exchange, 400, "Formato JSON inválido");
            return;
        }

        if (nuevoAlumno == null || nuevoAlumno.getNombre() == null) {
            System.out.println("[RESPUESTA 400] Datos de alumno incompletos en POST");
            enviarErrorJson(exchange, 400, "Datos del alumno incompletos");
            return;
        }

        synchronized (alumnos) {
            int nuevoId = ++ultimoId;
            nuevoAlumno.setId(nuevoId);
            alumnos.put(nuevoId, nuevoAlumno);

            // Inciso 3: Respuesta 201 Created con JSON completo
            String jsonCreado = gson.toJson(nuevoAlumno);
            System.out.println("[RESPUESTA 201] Creado nuevo Alumno ID=" + nuevoId + " | " + jsonCreado);
            enviarRespuestaJson(exchange, 201, jsonCreado);
        }
    }

    /**
     * Procesa peticiones PUT /alumnos/{id}:
     * - Recibe JSON con datos actualizados (Inciso 5).
     * - Si ID no es numérico -> 400 Bad Request {"error": "ID invalido"} (Inciso 2).
     * - Si alumno no existe -> 404 Not Found {"error": "Alumno no encontrado"} (Inciso 5).
     * - Si existe y se actualiza -> 200 OK {"mensaje": "Alumno actualizado"} (Inciso 5).
     */
    private void handlePut(HttpExchange exchange, String[] partes) throws IOException {
        if (partes.length < 3 || partes[2].trim().isEmpty()) {
            System.out.println("[RESPUESTA 400] PUT sin ID en la ruta");
            enviarErrorJson(exchange, 400, "ID no proporcionado en la ruta");
            return;
        }

        int idActualizar;
        try {
            idActualizar = Integer.parseInt(partes[2].trim());
        } catch (NumberFormatException e) {
            // Inciso 2: Identificador no numérico
            System.out.println("[RESPUESTA 400] ID no numérico en PUT: " + partes[2]);
            enviarErrorJson(exchange, 400, "ID invalido");
            return;
        }

        synchronized (alumnos) {
            if (!alumnos.containsKey(idActualizar)) {
                // Inciso 5: Si no existe devuelve 404 y JSON {"error": "Alumno no encontrado"}
                System.out.println("[RESPUESTA 404] Alumno no encontrado para actualizar ID=" + idActualizar);
                enviarErrorJson(exchange, 404, "Alumno no encontrado");
                return;
            }

            String cuerpo = leerCuerpo(exchange);
            if (cuerpo.isEmpty()) {
                System.out.println("[RESPUESTA 400] PUT con cuerpo vacío");
                enviarErrorJson(exchange, 400, "Cuerpo de solicitud vacío");
                return;
            }

            Alumno datosActualizados;
            try {
                datosActualizados = gson.fromJson(cuerpo, Alumno.class);
            } catch (JsonSyntaxException e) {
                System.out.println("[RESPUESTA 400] Sintaxis JSON inválida en PUT: " + e.getMessage());
                enviarErrorJson(exchange, 400, "Formato JSON inválido");
                return;
            }

            if (datosActualizados == null) {
                System.out.println("[RESPUESTA 400] Datos nulos en PUT");
                enviarErrorJson(exchange, 400, "Datos del alumno inválidos");
                return;
            }

            // Actualizar datos del alumno conservando el ID
            datosActualizados.setId(idActualizar);
            alumnos.put(idActualizar, datosActualizados);

            // Inciso 5: Respuesta 200 OK con {"mensaje": "Alumno actualizado"}
            Map<String, String> respuestaOk = new HashMap<>();
            respuestaOk.put("mensaje", "Alumno actualizado");
            String jsonOk = gson.toJson(respuestaOk);
            System.out.println("[RESPUESTA 200] Alumno ID=" + idActualizar + " actualizado | " + jsonOk);
            enviarRespuestaJson(exchange, 200, jsonOk);
        }
    }

    /**
     * Procesa peticiones DELETE /alumnos/{id}:
     * - Si ID no es numérico -> 400 Bad Request {"error": "ID invalido"} (Inciso 2).
     * - Si alumno no existe -> 404 Not Found {"error": "Alumno no encontrado"} (Inciso 6).
     * - Si alumno existe -> 204 No Content sin cuerpo (Inciso 6).
     */
    private void handleDelete(HttpExchange exchange, String[] partes) throws IOException {
        if (partes.length < 3 || partes[2].trim().isEmpty()) {
            System.out.println("[RESPUESTA 400] DELETE sin ID en la ruta");
            enviarErrorJson(exchange, 400, "ID no proporcionado en la ruta");
            return;
        }

        int idBorrar;
        try {
            idBorrar = Integer.parseInt(partes[2].trim());
        } catch (NumberFormatException e) {
            // Inciso 2: Identificador no numérico (ej. DELETE /alumnos/f)
            System.out.println("[RESPUESTA 400] ID no numérico en DELETE: " + partes[2]);
            enviarErrorJson(exchange, 400, "ID invalido");
            return;
        }

        synchronized (alumnos) {
            if (!alumnos.containsKey(idBorrar)) {
                // Inciso 6: Si no existe para ser borrado devuelve 404 y JSON {"error": "Alumno no encontrado"}
                System.out.println("[RESPUESTA 404] Alumno no encontrado para eliminar ID=" + idBorrar);
                enviarErrorJson(exchange, 404, "Alumno no encontrado");
                return;
            }

            alumnos.remove(idBorrar);
            // Inciso 6: Si la solicitud es correcta devuelve sólo el código 204
            System.out.println("[RESPUESTA 204] Alumno ID=" + idBorrar + " eliminado exitosamente");
            enviarRespuestaSinCuerpo(exchange, 204);
        }
    }

    /**
     * Lee de forma completa el cuerpo de datos del mensaje HTTP entrante en UTF-8.
     */
    private String leerCuerpo(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int leidos;
        while ((leidos = is.read(temp, 0, temp.length)) != -1) {
            buffer.write(temp, 0, leidos);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();
    }

    /**
     * Envía respuesta HTTP con encabezado Content-Type: application/json; charset=UTF-8
     * y el cuerpo JSON especificado.
     */
    private void enviarRespuestaJson(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytesRespuesta = (jsonResponse + "\n").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytesRespuesta.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytesRespuesta);
        }
    }

    /**
     * Envía respuesta HTTP sin cuerpo (ej. 204 No Content).
     */
    private void enviarRespuestaSinCuerpo(HttpExchange exchange, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    /**
     * Helper para enviar respuestas de error en formato JSON {"error": "..."}
     */
    private void enviarErrorJson(HttpExchange exchange, int statusCode, String mensajeError) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensajeError);
        String json = gson.toJson(error);
        enviarRespuestaJson(exchange, statusCode, json);
    }

    /**
     * Handler para el endpoint /status.
     * Mantiene comprobación de disponibilidad activa del servicio.
     */
    static class StatusHandler implements HttpHandler {
        private final int port;

        public StatusHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
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
     * Handler para rutas no registradas o la raíz "/".
     * Cumple con el Inciso 1: para cualquier endpoint no existente o incorrecto (ej. /chetito),
     * devuelve el código de estado 404 puesto que ese recurso no existe en el servidor.
     */
    static class RootHandler implements HttpHandler {
        private final int port;
        private final Gson gson = new Gson();

        public RootHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String metodo = exchange.getRequestMethod();
            System.out.println("-----------------------------------------------------------------");
            System.out.println("[ROOT HANDLER] Método: " + metodo + " | Ruta: " + path);

            // Inciso 1: Si el endpoint no existe o es incorrecto (ej. /chetito) -> 404 Not Found
            if (!path.equals("/")) {
                System.out.println("[RESPUESTA 404] Endpoint inexistente o incorrecto: " + path);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Recurso no encontrado");
                byte[] bytes = (gson.toJson(error) + "\n").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // Para la raíz "/" informativa
            Map<String, Object> info = new HashMap<>();
            info.put("servicio", "Servidor RESTful micro-SAES (Clase 24)");
            info.put("puerto", port);
            info.put("endpoints_disponibles", new String[]{"/alumnos", "/status"});
            byte[] bytes = (gson.toJson(info) + "\n").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
