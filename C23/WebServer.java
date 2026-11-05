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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Servidor Web RESTful (micro-SAES) en Java.
 * Implementa el endpoint /alumnos con soporte para los cuatro métodos REST:
 * - GET: Consulta de alumno por ID (/alumnos/{id})
 * - POST: Inserción de nuevo alumno (/alumnos)
 * - PUT: Actualización de datos de alumno (/alumnos/{id})
 * - DELETE: Eliminación de alumno (/alumnos/{id})
 * 
 * Mantiene activo el endpoint /status para verificación de disponibilidad.
 * Sistemas Distribuidos - ESCOM IPN
 */
public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private HttpServer server;

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
        System.out.println(" Servidor RESTful micro-SAES iniciado en el puerto: " + port);
        System.out.println(" Endpoint de estado:  http://localhost:" + port + "/status");
        System.out.println(" Endpoint de alumnos: http://localhost:" + port + "/alumnos");
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
     * Handler para el endpoint /alumnos
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
     * Mapea cada método HTTP (GET, POST, PUT, DELETE) con su respectiva operación.
     */
    private void handleAlumnosRequest(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        String metodo = exchange.getRequestMethod();
        System.out.println("Ruta solicitada: " + ruta);
        System.out.println("Método solicitado: " + metodo);

        String[] partes = ruta.split("/");

        if (metodo.equalsIgnoreCase("get")) {
            // Código del método GET: Lista al alumno con el ID especificado en la ruta (ej. /alumnos/1)
            if (partes.length >= 3) {
                try {
                    int idBuscado = Integer.parseInt(partes[2].trim());
                    Alumno alumno = alumnos.get(idBuscado);
                    if (alumno != null) {
                        enviarRespuesta(exchange, alumno.toString());
                    } else {
                        // Si el alumno no existe, se cierra la conexión para respuesta vacía (curl 52)
                        exchange.close();
                        return;
                    }
                } catch (NumberFormatException e) {
                    exchange.close();
                    return;
                }
            } else {
                exchange.close();
                return;
            }
        } else if (metodo.equalsIgnoreCase("post")) {
            // Código del método POST: Inserta un nuevo alumno a la lista y obtiene su ID
            String cuerpo = leerCuerpo(exchange);
            if (cuerpo.isEmpty()) {
                exchange.close();
                return;
            }

            try {
                String[] datos = cuerpo.split(",");
                String nombre = datos[0].trim();
                String apellido1 = datos[1].trim();
                String apellido2 = datos[2].trim();
                double promedio = Double.parseDouble(datos[3].trim());

                int nuevoId = ++ultimoId;
                Alumno nuevoAlumno = new Alumno(nuevoId, nombre, apellido1, apellido2, promedio);
                alumnos.put(nuevoId, nuevoAlumno);

                enviarRespuesta(exchange, nuevoAlumno.toString());
            } catch (Exception e) {
                exchange.close();
                return;
            }
        } else if (metodo.equalsIgnoreCase("put")) {
            // Código del método PUT: Actualiza al alumno con el ID especificado (ej. /alumnos/2)
            if (partes.length >= 3) {
                try {
                    int idActualizar = Integer.parseInt(partes[2].trim());
                    String cuerpo = leerCuerpo(exchange);
                    if (cuerpo.isEmpty()) {
                        exchange.close();
                        return;
                    }

                    String[] datos = cuerpo.split(",");
                    String nombre = datos[0].trim();
                    String apellido1 = datos[1].trim();
                    String apellido2 = datos[2].trim();
                    double promedio = Double.parseDouble(datos[3].trim());

                    Alumno alumnoActualizado = new Alumno(idActualizar, nombre, apellido1, apellido2, promedio);
                    alumnos.put(idActualizar, alumnoActualizado);

                    enviarRespuesta(exchange, alumnoActualizado.toString());
                } catch (Exception e) {
                    exchange.close();
                    return;
                }
            } else {
                exchange.close();
                return;
            }
        } else if (metodo.equalsIgnoreCase("delete")) {
            // Código del método DELETE: Borra al alumno con el ID especificado (ej. /alumnos/1)
            if (partes.length >= 3) {
                try {
                    int idBorrar = Integer.parseInt(partes[2].trim());
                    alumnos.remove(idBorrar);

                    String mensaje = "Alumno con ID: " + idBorrar + " removido de la lista de alumnos.\n";
                    enviarRespuesta(exchange, mensaje);
                } catch (NumberFormatException e) {
                    exchange.close();
                    return;
                }
            } else {
                exchange.close();
                return;
            }
        } else {
            exchange.close();
            return;
        }
    }

    /**
     * Lee de forma completa el cuerpo de datos del mensaje HTTP entrante.
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
     * Envía la respuesta HTTP 200 OK con el cuerpo de texto en codificación UTF-8.
     */
    private void enviarRespuesta(HttpExchange exchange, String respuesta) throws IOException {
        byte[] bytesRespuesta = respuesta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytesRespuesta.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytesRespuesta);
        }
    }

    /**
     * Handler para el endpoint /status.
     * Mantiene compatibilidad para comprobación de que el servicio está activo.
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
     * Handler para la ruta raíz /.
     */
    static class RootHandler implements HttpHandler {
        private final int port;

        public RootHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Servidor RESTful micro-SAES activo en el puerto " + port + ". Endpoints: /status, /alumnos\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
