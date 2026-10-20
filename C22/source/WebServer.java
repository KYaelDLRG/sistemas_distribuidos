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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Ejercicio 2: Servidor Web HTTP Despachador (WebServer).
 * 
 * Actúa como nodo coordinador (Master / Dispatcher):
 * 1. Expone el endpoint /dispatcher en el puerto 8080.
 * 2. Recibe el hashObjetivo (o hashObjetivo,letra) vía POST.
 * 3. Genera la lista de 26 tareas (hashObjetivo,a ... hashObjetivo,z).
 * 4. Envía las tareas de manera concurrente a los dos workers (puertos 8081 y 8082 en /task)
 *    utilizando la clase Aggregator con balanceo dinámico y terminación temprana.
 * 5. Mide el tiempo total transcurrido e incluye la cabecera X-debug-info si se solicita X-Debug.
 * 6. Devuelve la respuesta final encontrada ("La contraseña es: <palabra>\n") al cliente HTTP.
 */
public class WebServer {

    private static final int PUERTO_DEFAULT = 8080;
    private static final int NUM_HILOS = 8;
    private static final String WORKER_ADDRESS_1 = "http://localhost:8081/task";
    private static final String WORKER_ADDRESS_2 = "http://localhost:8082/task";

    public static void main(String[] args) throws IOException {
        int puerto = PUERTO_DEFAULT;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Puerto no válido. Usando puerto por defecto: " + PUERTO_DEFAULT);
                puerto = PUERTO_DEFAULT;
            }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.setExecutor(Executors.newFixedThreadPool(NUM_HILOS));

        server.createContext("/dispatcher", new DispatcherHandler());
        server.createContext("/", new StatusHandler());

        server.start();
        System.out.println("=================================================================");
        System.out.println(" Servidor Despachador (WebServer) iniciado en el puerto " + puerto);
        System.out.println(" Endpoint disponible: http://localhost:" + puerto + "/dispatcher");
        System.out.println(" Workers configurados:");
        System.out.println("   - Worker 1: " + WORKER_ADDRESS_1);
        System.out.println("   - Worker 2: " + WORKER_ADDRESS_2);
        System.out.println(" Esperando solicitudes de clientes...");
        System.out.println("=================================================================");
    }

    static class DispatcherHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long nanoInicio = System.nanoTime();

            String metodo = exchange.getRequestMethod();
            Headers headers = exchange.getRequestHeaders();

            // Detectar cabecera X-Debug: true
            String xDebugValor = headers.getFirst("X-Debug");
            boolean esDebug = "true".equalsIgnoreCase(xDebugValor);

            // Soporte para CORS (útil para integración con Frontend Web en Ejercicio 3)
            Headers resHeaders = exchange.getResponseHeaders();
            resHeaders.set("Access-Control-Allow-Origin", "*");
            resHeaders.set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            resHeaders.set("Access-Control-Allow-Headers", "Content-Type, X-Debug");

            if ("OPTIONS".equalsIgnoreCase(metodo)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(metodo)) {
                // Leer el cuerpo de la petición
                InputStream is = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int leidos;
                while ((leidos = is.read(temp, 0, temp.length)) != -1) {
                    buffer.write(temp, 0, leidos);
                }
                String cuerpo = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();

                System.out.println("\n[POST /dispatcher] Solicitud recibida: <" + cuerpo + ">");

                // Extraer el hashObjetivo (soporta tanto 'hash' como 'hash,letra')
                String hashObjetivo;
                if (cuerpo.contains(",")) {
                    hashObjetivo = cuerpo.split(",")[0].trim();
                } else {
                    hashObjetivo = cuerpo.trim();
                }

                if (hashObjetivo.isEmpty()) {
                    String error = "Error: Cuerpo de petición vacío.\n";
                    byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(400, errorBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorBytes);
                    os.close();
                    return;
                }

                System.out.println(">> Iniciando despacho de 26 tareas para hash: " + hashObjetivo);

                // Instanciar Aggregator y generar la lista de 26 tareas
                Aggregator aggregator = new Aggregator();
                List<String> listaTareas = new ArrayList<>();
                for (char c = 'a'; c <= 'z'; c++) {
                    listaTareas.add(hashObjetivo + "," + c);
                }

                // Enviar las tareas distribuidas hacia los dos workers
                List<String> results = aggregator.sendTasksToWorkers(
                        Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2),
                        listaTareas);

                // Encontrar la solución entre los resultados obtenidos
                String respuestaCliente = "NULL";
                for (String r : results) {
                    if (r != null && !r.trim().equalsIgnoreCase("NULL") && !r.trim().isEmpty()) {
                        respuestaCliente = r.trim();
                        break;
                    }
                }

                // Asegurar formato estándar de respuesta con salto de línea final
                if (!respuestaCliente.endsWith("\n")) {
                    respuestaCliente += "\n";
                }

                System.out.println(">> Resultado obtenido: " + respuestaCliente.trim());

                // Si se solicitó X-Debug, calcular duración y adjuntar cabecera
                if (esDebug) {
                    long nanoFin = System.nanoTime();
                    long duracionNanos = nanoFin - nanoInicio;
                    long segundos = duracionNanos / 1_000_000_000L;
                    long milisegundos = (duracionNanos % 1_000_000_000L) / 1_000_000L;

                    String debugInfo = String.format(
                            "La operacion tomo %d nanosegundos = %d segundos con %d milisegundos",
                            duracionNanos, segundos, milisegundos
                    );
                    resHeaders.set("X-debug-info", debugInfo);
                    System.out.println(">> " + debugInfo);
                }

                byte[] bytesRespuesta = respuestaCliente.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytesRespuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytesRespuesta);
                os.close();
                return;
            }

            // Para otros métodos (ej. GET)
            String msg = "Servidor Despachador HTTP activo. Envíe una solicitud POST a /dispatcher con el hashObjetivo.\n";
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, msgBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(msgBytes);
            os.close();
        }
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String msg = "Servidor Despachador Activo. Endpoint: /dispatcher\n";
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, msgBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(msgBytes);
            os.close();
        }
    }
}
