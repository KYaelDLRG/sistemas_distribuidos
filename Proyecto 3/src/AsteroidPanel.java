/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class AsteroidPanel extends JPanel implements Runnable {
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;

    private static final Font HUD_FONT = new Font("Monospaced", Font.BOLD, 14);
    private static final Color HUD_COLOR = new Color(120, 220, 255);

    private final AsteroidSimulation simulation;
    private volatile boolean running = true;
    private Thread gameThread;

    private int currentFps = 0;
    private int frameCounter = 0;
    private long lastFpsUpdate = System.currentTimeMillis();

    public AsteroidPanel(AsteroidSimulation simulation) {
        this.simulation = simulation;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setMaximumSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
    }

    public synchronized void startGameLoop() {
        if (gameThread == null || !gameThread.isAlive()) {
            running = true;
            gameThread = new Thread(this, "Asteroid-GameLoop-Thread");
            gameThread.setDaemon(true);
            gameThread.start();
        }
    }

    public synchronized void stopGameLoop() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerFrame = 1000000000.0 / 60.0; // 60 FPS objetivo

        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1000000000.0;
            lastTime = now;

            // Limitar dt para evitar saltos bruscos en caso de pausa del sistema
            if (dt > 0.1) {
                dt = 0.1;
            }

            simulation.update(dt);
            repaint();

            // Cálculo de FPS
            frameCounter++;
            long currentTimeMs = System.currentTimeMillis();
            if (currentTimeMs - lastFpsUpdate >= 1000) {
                currentFps = frameCounter;
                frameCounter = 0;
                lastFpsUpdate = currentTimeMs;
            }

            // Regular a 60 FPS
            long elapsedNs = System.nanoTime() - now;
            long sleepNs = (long) (nsPerFrame - elapsedNs);
            if (sleepNs > 0) {
                try {
                    Thread.sleep(sleepNs / 1000000, (int) (sleepNs % 1000000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo negro
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Renderizar asteroides
        simulation.render(g2);

        // HUD informativo discreto estilo arcade
        g2.setFont(HUD_FONT);
        g2.setColor(HUD_COLOR);
        g2.drawString(String.format("ESPACIO: %dx%d px | FPS: %d", WIDTH, HEIGHT, currentFps), 18, 28);
        g2.drawString(String.format("ASTEROIDES ACTIVOS: %d | TOTAL CREADOS: %d", simulation.getActiveCount(), simulation.getTotalCreatedCount()), 18, 48);
    }
}
