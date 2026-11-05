/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.util.Random;

public class AsteroidSpawnManager {
    public static final double CANVAS_WIDTH = 1000.0;
    public static final double CANVAS_HEIGHT = 1000.0;
    private static final Random RANDOM = new Random();

    private AsteroidSpawnManager() {
    }

    /**
     * Calcula una posición de aparición estrictamente fuera de la pantalla (1000x1000)
     * para un vector de dirección dado, de modo que el asteroide entre gradualmente
     * a la vista siguiendo su trayectoria.
     */
    public static SpawnPosition calculateManualSpawn(double dirX, double dirY, double radius) {
        double len = Math.hypot(dirX, dirY);
        double ux = (len < 1e-6) ? 1.0 : (dirX / len);
        double uy = (len < 1e-6) ? 0.0 : (dirY / len);

        double margin = radius + 25.0; // Distancia segura fuera del marco

        // Seleccionar un punto objetivo interior aleatorio para garantizar cruce
        double targetX = 150.0 + RANDOM.nextDouble() * (CANVAS_WIDTH - 300.0);
        double targetY = 150.0 + RANDOM.nextDouble() * (CANVAS_HEIGHT - 300.0);

        // Retroceder a lo largo de la línea directriz hasta salir completamente de la pantalla
        double tMin = 1e9;

        if (ux > 1e-6) {
            double t = (targetX + margin) / ux;
            if (t > 0 && t < tMin) tMin = t;
        } else if (ux < -1e-6) {
            double t = (CANVAS_WIDTH + margin - targetX) / (-ux);
            if (t > 0 && t < tMin) tMin = t;
        }

        if (uy > 1e-6) {
            double t = (targetY + margin) / uy;
            if (t > 0 && t < tMin) tMin = t;
        } else if (uy < -1e-6) {
            double t = (CANVAS_HEIGHT + margin - targetY) / (-uy);
            if (t > 0 && t < tMin) tMin = t;
        }

        if (tMin > 1e8) {
            tMin = Math.max(CANVAS_WIDTH, CANVAS_HEIGHT) + margin;
        }

        double spawnX = targetX - tMin * ux;
        double spawnY = targetY - tMin * uy;

        return new SpawnPosition(spawnX, spawnY);
    }

    /**
     * Genera parámetros aleatorios para un nuevo asteroide (posición exterior, vector hacia el interior,
     * rapidez y velocidad angular con signo).
     */
    public static SpawnPosition calculateRandomSpawn(double radius) {
        double margin = radius + 25.0;
        int edge = RANDOM.nextInt(4); // 0: Top, 1: Bottom, 2: Left, 3: Right
        double x = 0;
        double y = 0;

        switch (edge) {
            case 0: // Superior
                x = RANDOM.nextDouble() * CANVAS_WIDTH;
                y = -margin;
                break;
            case 1: // Inferior
                x = RANDOM.nextDouble() * CANVAS_WIDTH;
                y = CANVAS_HEIGHT + margin;
                break;
            case 2: // Izquierda
                x = -margin;
                y = RANDOM.nextDouble() * CANVAS_HEIGHT;
                break;
            case 3: // Derecha
                x = CANVAS_WIDTH + margin;
                y = RANDOM.nextDouble() * CANVAS_HEIGHT;
                break;
        }

        return new SpawnPosition(x, y);
    }

    /**
     * Calcula un vector de dirección hacia un punto interior aleatorio a partir de la posición de spawn.
     */
    public static double[] calculateDirectionToInterior(double spawnX, double spawnY) {
        double targetX = 150.0 + RANDOM.nextDouble() * (CANVAS_WIDTH - 300.0);
        double targetY = 150.0 + RANDOM.nextDouble() * (CANVAS_HEIGHT - 300.0);

        double dx = targetX - spawnX;
        double dy = targetY - spawnY;
        return new double[]{dx, dy};
    }

    public static double generateRandomSpeed() {
        return 1.5 + RANDOM.nextDouble() * 4.0; // Rapidez entre 1.5 y 5.5 px/frame
    }

    public static double generateRandomAngularVelocity() {
        // Generar velocidad angular no nula con signo aleatorio (+ o -)
        double magnitude = 0.8 + RANDOM.nextDouble() * 3.5; // Entre 0.8 y 4.3 grados/frame
        boolean positive = RANDOM.nextBoolean();
        return positive ? magnitude : -magnitude;
    }
}
