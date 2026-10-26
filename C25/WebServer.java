import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WebServer {
    private static final String ALUMNOS_ENDPOINT = "/alumnos";
    private static final String STATUS_ENDPOINT = "/status";

    private final int port;
    private HttpServer server;
   
    private static Map<Integer, Alumno> alumnos = new HashMap<>();
    private static int contadorId = 1;
    private static Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }

    public WebServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext alumnosContext = server.createContext(ALUMNOS_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        alumnosContext.setHandler(this::handleAlumnosRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleAlumnosRequest(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        String metodo = exchange.getRequestMethod();
       
        String[] partes = ruta.split("/");
       
        // Punto 1: Si la ruta exacta no es /alumnos ni /alumnos/{id}, el recurso no existe (404)
        if (partes.length > 3 || (partes.length == 2 && !ruta.equals("/alumnos"))) {
            enviarRespuestaJson(exchange, 404, "{\"error\": \"Recurso no encontrado\"}");
            return;
        }

        String responseMessage = "";
        int statusCode = 200;

        if (metodo.equalsIgnoreCase("get")) {
            if (partes.length >= 3) {
                // Consulta por ID: /alumnos/{id}
                try {
                    int id = Integer.parseInt(partes[2]);
                    Alumno a = alumnos.get(id);
                    if (a != null) {
                        statusCode = 200;
                        responseMessage = gson.toJson(a);
                    } else {
                        statusCode = 404;
                        responseMessage = "{\"error\": \"Alumno no encontrado\"}";
                    }
                } catch (NumberFormatException e) {
                    // Punto 2: ID no numerico (ej. /alumnos/p) -> 400 Bad Request
                    statusCode = 400;
                    responseMessage = "{\"error\": \"ID invalido\"}";
                }
            } else {
                // Consulta general o filtrada: /alumnos con query parameters
                // Ejercicio 1: Si la lista esta vacia, devuelve codigo 200 y JSON "{}"
                if (alumnos.isEmpty()) {
                    statusCode = 200;
                    responseMessage = "{}";
                } else {
                    // Procesamiento de query parameters (Ejercicio 3)
                    Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

                    List<Alumno> filtrados = new ArrayList<>(alumnos.values());
                    // Ordenar por ID para consistencia y determinismo
                    filtrados.sort(Comparator.comparingInt(a -> a.id));

                    // 1. Filtro por minPromedio (ej. minPromedio=8)
                    if (queryParams.containsKey("minPromedio")) {
                        try {
                            double minProm = Double.parseDouble(queryParams.get("minPromedio"));
                            filtrados = filtrados.stream()
                                    .filter(a -> a.promedio >= minProm)
                                    .collect(Collectors.toList());
                        } catch (NumberFormatException ignored) {}
                    }

                    // 2. Filtro por nombre (ej. nombre=C o nombre=Carlos)
                    if (queryParams.containsKey("nombre")) {
                        String nomFiltro = queryParams.get("nombre").trim().toLowerCase();
                        if (!nomFiltro.isEmpty()) {
                            filtrados = filtrados.stream()
                                    .filter(a -> a.nombre != null && a.nombre.toLowerCase().startsWith(nomFiltro))
                                    .collect(Collectors.toList());
                        }
                    }

                    // 3. Paginacion con page y size (Ejercicio 3)
                    // "En el caso de que el usuario no introduzca parametros tendran que asignarse unos por defecto para evitar que se devuelva la tabla completa por omision."
                    int page = 1;
                    int size = 20;

                    if (queryParams.containsKey("page")) {
                        try {
                            page = Math.max(1, Integer.parseInt(queryParams.get("page")));
                        } catch (NumberFormatException ignored) {}
                    }

                    if (queryParams.containsKey("size")) {
                        try {
                            size = Math.max(1, Integer.parseInt(queryParams.get("size")));
                        } catch (NumberFormatException ignored) {}
                    }

                    // Aplicar paginacion
                    int total = filtrados.size();
                    int fromIndex = (page - 1) * size;
                    List<Alumno> paginados;

                    if (fromIndex >= total) {
                        paginados = Collections.emptyList();
                    } else {
                        int toIndex = Math.min(fromIndex + size, total);
                        paginados = filtrados.subList(fromIndex, toIndex);
                    }

                    statusCode = 200;
                    responseMessage = gson.toJson(paginados);
                }
            }
        } else if (metodo.equalsIgnoreCase("post")) {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            String bodyString = new String(requestBytes).trim();
           
            try {
                Alumno nuevoRecibido = gson.fromJson(bodyString, Alumno.class);
                int id = contadorId++;
                Alumno nuevoAlumno = new Alumno(id, nuevoRecibido.nombre, nuevoRecibido.apellido1, nuevoRecibido.apellido2, nuevoRecibido.promedio);
                alumnos.put(id, nuevoAlumno);

                statusCode = 201; // Punto 3: Creado con exito
                responseMessage = gson.toJson(nuevoAlumno);
            } catch (Exception e) {
                statusCode = 400;
                responseMessage = "{\"error\": \"Datos JSON incorrectos\"}";
            }
        } else if (metodo.equalsIgnoreCase("put")) {
            if (partes.length >= 3) {
                try {
                    int id = Integer.parseInt(partes[2]);
                    byte[] requestBytes = exchange.getRequestBody().readAllBytes();
                    String bodyString = new String(requestBytes).trim();

                    Alumno alumnoExistente = alumnos.get(id);
                    if (alumnoExistente != null) {
                        Alumno datosActualizados = gson.fromJson(bodyString, Alumno.class);
                        alumnoExistente.nombre = datosActualizados.nombre;
                        alumnoExistente.apellido1 = datosActualizados.apellido1;
                        alumnoExistente.apellido2 = datosActualizados.apellido2;
                        alumnoExistente.promedio = datosActualizados.promedio;

                        statusCode = 200; // Punto 5
                        responseMessage = "{\"mensaje\": \"Alumno actualizado\"}";
                    } else {
                        statusCode = 404;
                        responseMessage = "{\"error\": \"Alumno no encontrado\"}";
                    }
                } catch (NumberFormatException e) {
                    statusCode = 400;
                    responseMessage = "{\"error\": \"ID invalido\"}";
                }
            } else {
                statusCode = 400;
                responseMessage = "{\"error\": \"Peticion PUT incorrecta\"}";
            }
        } else if (metodo.equalsIgnoreCase("delete")) {
            if (partes.length >= 3) {
                try {
                    int id = Integer.parseInt(partes[2]);
                    Alumno removed = alumnos.remove(id);
                    if (removed != null) {
                        statusCode = 204; // Punto 6: Sin contenido en respuesta
                        responseMessage = "";
                    } else {
                        statusCode = 404;
                        responseMessage = "{\"error\": \"Alumno no encontrado\"}";
                    }
                } catch (NumberFormatException e) {
                    statusCode = 400;
                    responseMessage = "{\"error\": \"ID invalido\"}";
                }
            } else {
                statusCode = 400;
                responseMessage = "{\"error\": \"Peticion DELETE incorrecta\"}";
            }
        } else {
            exchange.close();
            return;
        }

        enviarRespuestaJson(exchange, statusCode, responseMessage);
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return queryParams;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0].trim(), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1].trim(), StandardCharsets.UTF_8);
                    queryParams.put(key, value);
                } catch (Exception ignored) {}
            } else if (keyValue.length == 1) {
                try {
                    String key = URLDecoder.decode(keyValue[0].trim(), StandardCharsets.UTF_8);
                    queryParams.put(key, "");
                } catch (Exception ignored) {}
            }
        }
        return queryParams;
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
        enviarRespuestaJson(exchange, 200, "{\"status\": \"Servidor activo\"}");
    }

    private void enviarRespuestaJson(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] responseBytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        if (responseBytes.length > 0) {
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.flush();
            outputStream.close();
        }
        exchange.close();
    }
}
