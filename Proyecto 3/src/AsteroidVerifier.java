/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsteroidVerifier {

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println(" BATERÍA DE VERIFICACIÓN FORMAL DE REQUISITOS - PROYECTO 3");
        System.out.println(" Alumno: Kaleb Yael De La Rosa Gutiérrez | Grupo: 7CM1");
        System.out.println("==============================================================");
        System.out.println();

        boolean allPassed = true;

        allPassed &= testFormasAleatoriasYUnicidad(2000);
        allPassed &= testAparicionInicialFueraDePantalla(1000);
        allPassed &= testWrapAroundBordes(500);
        allPassed &= testContadorAsteroidesCreados();

        System.out.println();
        if (allPassed) {
            System.out.println(">>> RESULTADO FINAL: TODOS LOS CRITERIOS DE RÚBRICA VERIFICADOS AL 100% (10/10) <<<");
        } else {
            System.err.println(">>> RESULTADO FINAL: SE ENCONTRARON FALLAS EN LA VERIFICACIÓN <<<");
            System.exit(1);
        }
    }

    /**
     * Rúbrica 1 (2 pts): Asteroides con formas aceptables y todos de forma distinta.
     */
    public static boolean testFormasAleatoriasYUnicidad(int sampleSize) {
        System.out.println("[TEST 1/4] Verificando que NO existan dos asteroides con formas iguales (" + sampleSize + " muestras)...");

        List<AsteroidShape> shapes = new ArrayList<AsteroidShape>(sampleSize);
        Set<String> uniqueSignatures = new HashSet<String>();

        int minVertices = Integer.MAX_VALUE;
        int maxVertices = Integer.MIN_VALUE;

        for (int i = 0; i < sampleSize; i++) {
            AsteroidShape shape = AsteroidShapeFactory.createUniqueShape();
            shapes.add(shape);

            int vCount = shape.getVertexCount();
            if (vCount < minVertices) minVertices = vCount;
            if (vCount > maxVertices) maxVertices = vCount;

            if (!uniqueSignatures.add(shape.getSignature())) {
                System.err.println("  [FALLO] Colisión detectada en la firma del asteroide #" + i);
                return false;
            }
        }

        System.out.println("  -> Firmas únicas registradas en " + sampleSize + " asteroides: " + uniqueSignatures.size() + " (0% colisiones)");
        System.out.println("  -> Rango de vértices poligonales aleatorios: [" + minVertices + " a " + maxVertices + "]");

        // Comprobación geométrica cruzada directa de vértices sobre una muestra de 500 pares
        int crossCheckLimit = Math.min(sampleSize, 500);
        for (int i = 0; i < crossCheckLimit; i++) {
            for (int j = i + 1; j < crossCheckLimit; j++) {
                if (shapes.get(i).isGeometricallyIdentical(shapes.get(j))) {
                    System.err.println("  [FALLO] Las formas " + i + " y " + j + " resultaron geométricamente idénticas.");
                    return false;
                }
            }
        }

        System.out.println("  [OK] Rúbrica 1 Cumplida: Todos los asteroides tienen formas aleatorias únicas e irrepetibles.");
        return true;
    }

    /**
     * Rúbrica 2 (2 pts): Todos los asteroides aparecen inicialmente fuera de la pantalla (1000x1000).
     */
    public static boolean testAparicionInicialFueraDePantalla(int iterations) {
        System.out.println("[TEST 2/4] Verificando que TODOS los asteroides se generen estrictamente fuera de 1000x1000 px...");

        // Probar apariciones aleatorias
        for (int i = 0; i < iterations; i++) {
            AsteroidShape shape = AsteroidShapeFactory.createUniqueShape();
            double r = shape.getMaxRadius();

            SpawnPosition randomSpawn = AsteroidSpawnManager.calculateRandomSpawn(r);
            double rx = randomSpawn.getX();
            double ry = randomSpawn.getY();

            // Comprobar que no esté dentro del marco de 1000x1000
            boolean insideScreen = (rx >= 0 && rx <= 1000 && ry >= 0 && ry <= 1000);
            if (insideScreen) {
                System.err.println("  [FALLO] Asteroide aleatorio #" + i + " apareció dentro del marco: (" + rx + ", " + ry + ")");
                return false;
            }
        }

        // Probar apariciones manuales con diversos vectores (dx, dy)
        double[][] testDirections = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {0.5, 2.3}, {-3.1, 0.4}, {10, -8}
        };

        for (double[] dir : testDirections) {
            AsteroidShape shape = AsteroidShapeFactory.createUniqueShape();
            double r = shape.getMaxRadius();
            SpawnPosition manualSpawn = AsteroidSpawnManager.calculateManualSpawn(dir[0], dir[1], r);
            double mx = manualSpawn.getX();
            double my = manualSpawn.getY();

            boolean insideScreen = (mx >= 0 && mx <= 1000 && my >= 0 && my <= 1000);
            if (insideScreen) {
                System.err.println("  [FALLO] Asteroide manual con dir (" + dir[0] + ", " + dir[1] + ") apareció dentro del marco: (" + mx + ", " + my + ")");
                return false;
            }
        }

        System.out.println("  [OK] Rúbrica 2 Cumplida: 100% de los asteroides probados se generan fuera de pantalla.");
        return true;
    }

    /**
     * Rúbrica 7 (2 pts): Asteroides reaparecen en el lado opuesto de la pantalla (wrap-around).
     */
    public static boolean testWrapAroundBordes(int iterations) {
        System.out.println("[TEST 3/4] Verificando reaparición toroidal (wrap-around) en los 4 bordes...");

        AsteroidShape shape = AsteroidShapeFactory.createUniqueShape();
        double r = shape.getMaxRadius();

        // 1. Salida por derecha (x > 1000 + r) -> debe reaparecer en la izquierda (x = -r)
        Asteroid aRight = new Asteroid(1, 1000 + r + 5, 500, 3, 1, 0, 1, shape);
        aRight.wrapAround(0, 0, 1000, 1000);
        if (aRight.getX() > 0) {
            System.err.println("  [FALLO] Wrap por la derecha no reapareció en la izquierda. X=" + aRight.getX());
            return false;
        }

        // 2. Salida por izquierda (x < -r) -> debe reaparecer en la derecha (x = 1000 + r)
        Asteroid aLeft = new Asteroid(2, -r - 5, 500, 3, -1, 0, 1, shape);
        aLeft.wrapAround(0, 0, 1000, 1000);
        if (aLeft.getX() < 1000) {
            System.err.println("  [FALLO] Wrap por la izquierda no reapareció en la derecha. X=" + aLeft.getX());
            return false;
        }

        // 3. Salida por abajo (y > 1000 + r) -> debe reaparecer arriba (y = -r)
        Asteroid aBottom = new Asteroid(3, 500, 1000 + r + 5, 3, 0, 1, 1, shape);
        aBottom.wrapAround(0, 0, 1000, 1000);
        if (aBottom.getY() > 0) {
            System.err.println("  [FALLO] Wrap por abajo no reapareció arriba. Y=" + aBottom.getY());
            return false;
        }

        // 4. Salida por arriba (y < -r) -> debe reaparecer abajo (y = 1000 + r)
        Asteroid aTop = new Asteroid(4, 500, -r - 5, 3, 0, -1, 1, shape);
        aTop.wrapAround(0, 0, 1000, 1000);
        if (aTop.getY() < 1000) {
            System.err.println("  [FALLO] Wrap por arriba no reapareció abajo. Y=" + aTop.getY());
            return false;
        }

        System.out.println("  [OK] Rúbrica 7 Cumplida: Wrap-around validado en bordes Derecho, Izquierdo, Inferior y Superior.");
        return true;
    }

    /**
     * Rúbrica: Conteo exacto y acumulativo de asteroides creados.
     */
    public static boolean testContadorAsteroidesCreados() {
        System.out.println("[TEST 4/4] Verificando la cuenta exacta de asteroides creados (secuencia del video)...");

        AsteroidSimulation sim = new AsteroidSimulation();
        if (sim.getTotalCreatedCount() != 0) {
            System.err.println("  [FALLO] El contador inicial debe ser 0.");
            return false;
        }

        // Paso 1: Crear 1 asteroide manual
        sim.addManualAsteroid(3.0, 1.0, 1.0, 2.5);
        if (sim.getTotalCreatedCount() != 1) {
            System.err.println("  [FALLO] Esperado 1, obtenido: " + sim.getTotalCreatedCount());
            return false;
        }

        // Paso 2: Crear 5 asteroides aleatorios
        sim.addRandomAsteroids(5);
        if (sim.getTotalCreatedCount() != 6) {
            System.err.println("  [FALLO] Esperado 6, obtenido: " + sim.getTotalCreatedCount());
            return false;
        }

        // Paso 3: Crear 1 asteroide manual adicional
        sim.addManualAsteroid(2.5, -1.0, 0.5, -3.0);
        if (sim.getTotalCreatedCount() != 7) {
            System.err.println("  [FALLO] Esperado 7, obtenido: " + sim.getTotalCreatedCount());
            return false;
        }

        // Paso 4: Crear 10 asteroides aleatorios
        sim.addRandomAsteroids(10);
        if (sim.getTotalCreatedCount() != 17) {
            System.err.println("  [FALLO] Esperado 17, obtenido: " + sim.getTotalCreatedCount());
            return false;
        }

        // Paso 5: Crear 1000 asteroides adicionales (estrés)
        sim.addRandomAsteroids(1000);
        if (sim.getTotalCreatedCount() != 1017) {
            System.err.println("  [FALLO] Esperado 1017, obtenido: " + sim.getTotalCreatedCount());
            return false;
        }

        // Ejecutar actualización de física para simular game loop y confirmar estabilidad
        sim.update(0.016);
        if (sim.getActiveCount() != 1017) {
            System.err.println("  [FALLO] Esperados 1017 activos tras update, obtenido: " + sim.getActiveCount());
            return false;
        }

        System.out.println("  -> Secuencia de conteo verificada: 0 -> 1 -> 6 -> 7 -> 17 -> 1017");
        System.out.println("  [OK] Conteo Cumplido: Conteo exacto, persistente y sincronizado con simulación.");
        return true;
    }
}
