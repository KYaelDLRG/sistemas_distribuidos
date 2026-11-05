import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Servidor Web HTTP para la práctica de Google Cloud Shell y Compute Engine.
 * 
 * Diseñado para ser autónomo (sin dependencias externas), compilable directamente
 * con javac y empaquetable en WebServer.jar para ejecución en instancias virtuales en GCP.
 * 
 * Endpoints:
 * - /status : Retorna "El servidor está vivo" (requerido para Item 7).
 * - /       : Endpoint raíz de bienvenida e información del servidor.
 * 
 * Materia: Sistemas Distribuidos - ESCOM IPN
 * Alumno: Kaleb Yael De La Rosa Gutiérrez (7CM1)
 */
public class WebServer {

    private static final int PUERTO_DEFAULT = 8080;
    private final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // Enlazar al puerto especificado en todas las interfaces de red (0.0.0.0)
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Registro de manejadores de contexto HTTP
        server.createContext("/status", new StatusHandler());
        server.createContext("/", new RootHandler(port));

        // Pool de 8 hilos para atención concurrente
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("=================================================================");
        System.out.println(" Servidor HTTP iniciado exitosamente");
        System.out.println(" Alumno: Kaleb Yael De La Rosa Gutiérrez | Grupo: 7CM1");
        System.out.println(" Servidor escuchando en el puerto " + port);
        System.out.println(" Endpoint de estado: http://localhost:" + port + "/status");
        System.out.println("=================================================================");
    }

    public static void main(String[] args) {
        int puerto = PUERTO_DEFAULT;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: Puerto inválido ('" + args[0] + "'). Usando puerto por defecto: " + PUERTO_DEFAULT);
                puerto = PUERTO_DEFAULT;
            }
        }

        try {
            WebServer webServer = new WebServer(puerto);
            webServer.start();
        } catch (IOException e) {
            System.err.println("Error crítico al iniciar el servidor en el puerto " + puerto + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manejador del endpoint /status.
     * Retorna el texto exacto solicitado en la práctica: "El servidor está vivo"
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String respuesta = "El servidor está vivo\n";
            byte[] bytesRespuesta = respuesta.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytesRespuesta.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytesRespuesta);
            }
        }
    }

    /**
     * Manejador para la raíz / con información del servicio y alumno.
     */
    static class RootHandler implements HttpHandler {
        private final int port;

        public RootHandler(int port) {
            this.port = port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                // Si piden otra ruta desconocida
                String error = "Recurso no encontrado. Endpoint disponible: /status\n";
                byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(404, errorBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorBytes);
                }
                return;
            }

            String respuesta = "=====================================================\n"
                    + " Servidor Web HTTP - Sistemas Distribuidos (ESCOM IPN)\n"
                    + " Alumno: Kaleb Yael De La Rosa Gutiérrez\n"
                    + " Grupo : 7CM1\n"
                    + " Puerto: " + port + "\n"
                    + " Endpoint de comprobación: /status\n"
                    + "=====================================================\n";
            byte[] bytesRespuesta = respuesta.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytesRespuesta.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytesRespuesta);
            }
        }
    }
}
