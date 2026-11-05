/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class AsteroidServerMain {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[Advertencia] Puerto inválido, usando puerto por defecto: " + DEFAULT_PORT);
            }
        }

        final int serverPort = port;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("=================================================");
                    System.out.println(" PROYECTO 3: SERVIDOR DE SIMULACIÓN DE ASTEROIDES");
                    System.out.println(" Alumno: Kaleb Yael De La Rosa Gutiérrez | 7CM1");
                    System.out.println("=================================================");

                    AsteroidSimulation simulation = new AsteroidSimulation();
                    AsteroidHttpServer httpServer = new AsteroidHttpServer(serverPort, simulation);
                    httpServer.start();

                    JFrame frame = new JFrame("Simulación de Asteroides - Servidor Gráfico (1000x1000 px)");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    AsteroidPanel panel = new AsteroidPanel(simulation);
                    frame.getContentPane().add(panel);
                    frame.pack(); // Ajusta exactamente al tamaño preferido (1000x1000)
                    frame.setResizable(false);
                    frame.setLocationRelativeTo(null);

                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            panel.stopGameLoop();
                            httpServer.stop();
                        }
                    });

                    frame.setVisible(true);
                    panel.startGameLoop();

                    System.out.println("[Servidor] Lienzo gráfico inicializado en 1000x1000 px (fondo negro).");
                    System.out.println("[Servidor] Escuchando peticiones HTTP en http://localhost:" + serverPort + "/api");
                    System.out.println("[Servidor] Esperando comandos desde el cliente Lanterna...");
                } catch (Exception e) {
                    System.err.println("[Error Fatal] No se pudo inicializar el servidor: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
