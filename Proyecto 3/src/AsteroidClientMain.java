/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

public class AsteroidClientMain {
    public static final String DEFAULT_SERVER_URL = "http://localhost:8080";

    public static void main(String[] args) {
        String serverUrl = DEFAULT_SERVER_URL;
        if (args.length > 0) {
            serverUrl = args[0];
            if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                serverUrl = "http://" + serverUrl;
            }
        }

        System.out.println("=================================================");
        System.out.println(" PROYECTO 3: CLIENTE TUI DE ASTEROIDES (LANTERNA)");
        System.out.println(" Alumno: Kaleb Yael De La Rosa Gutiérrez | 7CM1");
        System.out.println(" Servidor objetivo: " + serverUrl);
        System.out.println("=================================================");

        try {
            AsteroidHttpClient httpClient = new AsteroidHttpClient(serverUrl);
            AsteroidClientUI ui = new AsteroidClientUI(httpClient);
            ui.start();
        } catch (Exception e) {
            System.err.println("[Error Fatal en Cliente] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
