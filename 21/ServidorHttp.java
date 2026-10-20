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
import java.security.MessageDigest;
import java.util.concurrent.Executors;

/**
 * Ejercicio 3: Servidor HTTP Concurrente para Búsqueda Distribuida de Hashes SHA-256.
 * Endpoint /task: recibe "<hashObjetivo>,<primeraLetra>" vía POST.
 * Examina el espacio de combinaciones desde <primeraLetra>aaaa hasta <primeraLetra>zzzz.
 * Devuelve "La contraseña es: <palabra>\n" o "NULL" si no la encuentra.
 * Cabecera X-debug-info: tiempo transcurrido en nanosegundos, segundos y milisegundos.
 */
public class ServidorHttp {

    private static final int PUERTO_DEFAULT = 8080;
    private static final int NUM_HILOS = 8;

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

        server.createContext("/task", new TaskHandler());
        server.createContext("/", new RaizHandler());

        server.start();
        System.out.println("Servidor HTTP (Ejercicio 3) iniciado en http://localhost:" + puerto + "/task");
        System.out.println("Esperando solicitudes POST...");
    }

    static class TaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long nanoInicio = System.nanoTime();

            String metodo = exchange.getRequestMethod();
            Headers headers = exchange.getRequestHeaders();

            // Detectar cabecera X-Debug: true
            String xDebugValor = headers.getFirst("X-Debug");
            boolean esDebug = "true".equalsIgnoreCase(xDebugValor);

            if ("POST".equalsIgnoreCase(metodo)) {
                // Leer cuerpo de la petición
                InputStream is = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int leidos;
                while ((leidos = is.read(temp, 0, temp.length)) != -1) {
                    buffer.write(temp, 0, leidos);
                }
                String cuerpo = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();

                System.out.println("\n[POST /task] Petición recibida: <" + cuerpo + ">");

                String respuestaCliente;
                String[] partes = cuerpo.split(",");

                if (partes.length < 2) {
                    respuestaCliente = "NULL";
                } else {
                    String hashObjetivo = partes[0].trim();
                    String letraStr = partes[1].trim();

                    if (letraStr.isEmpty()) {
                        respuestaCliente = "NULL";
                    } else {
                        char primeraLetra = letraStr.toLowerCase().charAt(0);
                        String encontrada = buscarClave(hashObjetivo, primeraLetra);

                        if (encontrada != null) {
                            // Cuando encuentra la contraseña:
                            // Formato según especificación: "La contraseña es: escom\n" (25 bytes)
                            respuestaCliente = "La contraseña es: " + encontrada + "\n";
                            System.out.println(">> ¡Contraseña encontrada!: " + encontrada);
                        } else {
                            // Cuando NO encuentra la contraseña:
                            // Formato según especificación: "NULL" (4 bytes, sin salto de línea)
                            respuestaCliente = "NULL";
                            System.out.println(">> Contraseña NO encontrada en rango '" + primeraLetra + "aaaa' - '" + primeraLetra + "zzzz'. Retornando NULL.");
                        }
                    }
                }

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
                    exchange.getResponseHeaders().set("X-debug-info", debugInfo);
                    System.out.println(">> " + debugInfo);
                }

                byte[] bytesRespuesta = respuestaCliente.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytesRespuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytesRespuesta);
                os.close();
                return;
            }

            // Si no es POST, devolver 405 Method Not Allowed
            String noPermitido = "Método no permitido. Utilice POST en /task\n";
            byte[] bytesError = noPermitido.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(405, bytesError.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytesError);
            os.close();
        }

        /**
         * Búsqueda por fuerza bruta de 4 caracteres para una primera letra fija.
         * Rango: <primeraLetra>aaaa hasta <primeraLetra>zzzz (26^4 = 456,976 combinaciones).
         */
        private String buscarClave(String hashObjetivo, char primeraLetra) {
            char a = primeraLetra;

            try {
                for (char b = 'a'; b <= 'z'; b++) {
                    for (char c = 'a'; c <= 'z'; c++) {
                        for (char d = 'a'; d <= 'z'; d++) {
                            for (char e = 'a'; e <= 'z'; e++) {
                                String intento = "" + a + b + c + d + e;
                                String hash = sha256(intento);
                                if (hash.equalsIgnoreCase(hashObjetivo)) {
                                    return intento;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error en cálculo hash: " + ex.getMessage());
            }

            return null;
        }

        /**
         * Cálculo de hash SHA-256 idéntico al del Ejercicio 1 / TestSHA
         */
        public static String sha256(String texto) throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
    }

    static class RaizHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String respuesta = "Servidor HTTP Ejercicio 21 activo. Envíe peticiones POST a /task\n";
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
