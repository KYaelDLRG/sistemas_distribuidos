import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * CLASE 16: PROGRAMADORES Y ON-LINE
 * Práctica: Servidor HTTP Concurrente en Java
 * 
 * Ejercicio 1:
 * - Análisis de concurrencia (8 threads vs 1 thread) con sleep de 5 segundos.
 * 
 * Ejercicio 2:
 * - Solicitud POST en el endpoint /task.
 * - Impresión en consola de la cantidad de bytes y el contenido del cuerpo HTTP:
 *     Numero de bytes en el cuerpo del mensaje: <bytes>
 *     Cuerpo del mensaje <<contenido>>
 * - Cálculo de la multiplicación enviada en el cuerpo (ej. "1757600,34334")
 *   y envío de la respuesta: "El resultado de la multiplicación es <resultado>"
 * ============================================================================
 */
public class ServidorHttp {

    // Puerto en el que escuchará el servidor HTTP (8080 según el ejercicio)
    private static final int PUERTO = 8080;

    // Duración de la pausa en milisegundos para simular carga de trabajo del Ejercicio 1
    private static final int TIEMPO_ESPERA_MS = 5000;

    public static void main(String[] args) throws IOException {
        // Tamaño del ThreadPool (por defecto 8, configurable por argumento)
        int numHilos = 8;
        if (args.length > 0) {
            try {
                numHilos = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argumento inválido. Usando valor predeterminado de 8 hilos.");
                numHilos = 8;
            }
        }

        // Creación del servidor HTTP en el puerto 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(PUERTO), 0);

        // Configuración del Pool de Hilos (Executor)
        server.setExecutor(Executors.newFixedThreadPool(numHilos));

        // Registro del endpoint /task (Ejercicio 2 y Ejercicio 1)
        server.createContext("/task", new TaskHandler());

        // Alias /tarea y / para compatibilidad
        server.createContext("/tarea", new TaskHandler());
        server.createContext("/", new RaizHandler());

        // Inicio del servidor
        server.start();

        System.out.println("Servidor escuchando en el puerto " + PUERTO);
    }

    /**
     * Manejador para el endpoint "/task" (y "/tarea")
     */
    static class TaskHandler implements HttpHandler {

        private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // EJERCICIO 4: Inicio del cronómetro en nanosegundos
            long nanoInicio = System.nanoTime();

            String metodo = exchange.getRequestMethod();

            /*
             * ================================================================
             * EJERCICIO 3: Imprimir cantidad y pares (key - value) de Headers
             * ================================================================
             */
            Headers headers = exchange.getRequestHeaders();
            System.out.println("Numero de headers recibidos: " + headers.size());
            System.out.println("Headers recibidos (key - value):");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                System.out.println("  " + entry.getKey() + " : " + entry.getValue());
            }

            // EJERCICIO 4: Verificar si el header personalizado "X-Debug" es "true"
            String xDebugValor = headers.getFirst("X-Debug");
            boolean esDebug = "true".equalsIgnoreCase(xDebugValor);

            /*
             * ================================================================
             * EJERCICIO 2: Solicitud POST en /task
             * ================================================================
             */
            if ("POST".equalsIgnoreCase(metodo)) {
                // 1. Leer los bytes del cuerpo de la petición HTTP
                InputStream is = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] datosTemp = new byte[1024];
                int leidos;
                while ((leidos = is.read(datosTemp, 0, datosTemp.length)) != -1) {
                    buffer.write(datosTemp, 0, leidos);
                }
                byte[] cuerpoBytes = buffer.toByteArray();

                // 2. Convertir el contenido del cuerpo a String
                String cuerpoMensaje = new String(cuerpoBytes, StandardCharsets.UTF_8).trim();

                // 3. Imprimir en el servidor el número de bytes y el contenido (según Ejercicio 2)
                System.out.println("Numero de bytes en el cuerpo del mensaje: " + cuerpoBytes.length);
                System.out.println("Cuerpo del mensaje <" + cuerpoMensaje + ">");

                // 4. Procesar la multiplicación de los dos números separados por coma
                String respuestaCliente;
                try {
                    String[] factores = cuerpoMensaje.split(",");
                    BigInteger factor1 = new BigInteger(factores[0].trim());
                    BigInteger factor2 = new BigInteger(factores[1].trim());
                    BigInteger resultado = factor1.multiply(factor2);

                    respuestaCliente = "El resultado de la multiplicación es " + resultado + "\n";
                } catch (Exception e) {
                    respuestaCliente = "Error al procesar los números: " + e.getMessage() + "\n";
                }

                // EJERCICIO 4: Si X-Debug es true, agregar cabecera con el tiempo total
                aplicarDebug(exchange, nanoInicio, esDebug);

                // 5. Enviar respuesta HTTP 200 OK al cliente curl
                byte[] bytesRespuesta = respuestaCliente.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytesRespuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytesRespuesta);
                os.close();
                return;
            }

            /*
             * ================================================================
             * EJERCICIO 1: Solicitud GET (Simulación de trabajo con sleep de 5s)
             * ================================================================
             */
            String horaInicio = sdf.format(new Date());
            String nombreHilo = Thread.currentThread().getName();

            try {
                Thread.sleep(TIEMPO_ESPERA_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String horaFin = sdf.format(new Date());

            String respuesta = String.format(
                    "Respuesta exitosa del Servidor HTTP\n" +
                    "Hilo asignado: %s | Inicio: %s | Fin: %s\n",
                    nombreHilo, horaInicio, horaFin
            );

            // EJERCICIO 4: Si X-Debug es true, agregar cabecera con el tiempo total
            aplicarDebug(exchange, nanoInicio, esDebug);

            byte[] bytesRespuesta = respuesta.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytesRespuesta.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytesRespuesta);
            os.close();
        }

        /**
         * EJERCICIO 4: Calcula el tiempo total en nanosegundos, segundos y milisegundos,
         * y añade el header de respuesta "X-Debug-Info" además de imprimirlo en consola.
         */
        private void aplicarDebug(HttpExchange exchange, long nanoInicio, boolean esDebug) {
            if (esDebug) {
                long nanoFin = System.nanoTime();
                long duracionNanos = nanoFin - nanoInicio;
                long segundos = duracionNanos / 1_000_000_000L;
                long milisegundos = (duracionNanos % 1_000_000_000L) / 1_000_000L;

                // Formato exacto requerido:
                // La operacion tomo 2266869700 nanosegundos = 2 segundos con 266 milisegundos.
                String debugInfo = String.format(
                        "La operacion tomo %d nanosegundos = %d segundos con %d milisegundos.",
                        duracionNanos, segundos, milisegundos
                );

                // Agregar el header a la respuesta HTTP
                exchange.getResponseHeaders().set("X-Debug-Info", debugInfo);

                // Imprimir en la consola del servidor
                System.out.println("X-debug-info: " + debugInfo);
            }
        }
    }

    /**
     * Manejador de la raíz "/"
     */
    static class RaizHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String respuesta = "Servidor HTTP Clase 16 activo. Endpoint disponible: /task\n";
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
