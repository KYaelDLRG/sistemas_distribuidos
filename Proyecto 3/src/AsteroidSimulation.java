/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AsteroidSimulation {
    private final List<Asteroid> activeAsteroids = new ArrayList<Asteroid>();
    private final Queue<Asteroid> pendingAsteroids = new ConcurrentLinkedQueue<Asteroid>();
    private final AtomicLong idSequence = new AtomicLong(0);
    private final AtomicInteger totalCreatedCount = new AtomicInteger(0);

    public AsteroidSimulation() {
    }

    public Asteroid addManualAsteroid(double speed, double dirX, double dirY, double angularVelocityDeg) {
        AsteroidShape shape = AsteroidShapeFactory.createUniqueShape();
        SpawnPosition spawn = AsteroidSpawnManager.calculateManualSpawn(dirX, dirY, shape.getMaxRadius());

        long id = idSequence.incrementAndGet();
        Asteroid asteroid = new Asteroid(id, spawn.getX(), spawn.getY(), speed, dirX, dirY, angularVelocityDeg, shape);

        pendingAsteroids.add(asteroid);
        totalCreatedCount.incrementAndGet();
        return asteroid;
    }

    public int addRandomAsteroids(int count) {
        if (count <= 0) return 0;
        int added = 0;
        for (int i = 0; i < count; i++) {
            AsteroidShape shape = AsteroidShapeFactory.createUniqueShape();
            SpawnPosition spawn = AsteroidSpawnManager.calculateRandomSpawn(shape.getMaxRadius());
            double[] dir = AsteroidSpawnManager.calculateDirectionToInterior(spawn.getX(), spawn.getY());
            double speed = AsteroidSpawnManager.generateRandomSpeed();
            double angularVel = AsteroidSpawnManager.generateRandomAngularVelocity();

            long id = idSequence.incrementAndGet();
            Asteroid asteroid = new Asteroid(id, spawn.getX(), spawn.getY(), speed, dir[0], dir[1], angularVel, shape);

            pendingAsteroids.add(asteroid);
            totalCreatedCount.incrementAndGet();
            added++;
        }
        return added;
    }

    public void update(double dt) {
        // Transferir asteroides pendientes a la lista activa
        Asteroid pending;
        while ((pending = pendingAsteroids.poll()) != null) {
            synchronized (activeAsteroids) {
                activeAsteroids.add(pending);
            }
        }

        // Actualizar física y aplicar reaparición en bordes opuestos (wrap-around)
        synchronized (activeAsteroids) {
            int size = activeAsteroids.size();
            for (int i = 0; i < size; i++) {
                Asteroid a = activeAsteroids.get(i);
                a.update(dt);
                a.wrapAround(0.0, 0.0, AsteroidSpawnManager.CANVAS_WIDTH, AsteroidSpawnManager.CANVAS_HEIGHT);
            }
        }
    }

    public void render(Graphics2D g2) {
        synchronized (activeAsteroids) {
            int size = activeAsteroids.size();
            for (int i = 0; i < size; i++) {
                activeAsteroids.get(i).render(g2);
            }
        }
    }

    public int getTotalCreatedCount() {
        return totalCreatedCount.get();
    }

    public int getActiveCount() {
        synchronized (activeAsteroids) {
            return activeAsteroids.size();
        }
    }
}
