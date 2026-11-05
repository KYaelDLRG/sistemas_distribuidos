/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class AsteroidShapeFactory {
    private static final Random RANDOM = new Random();
    private static final Set<String> GENERATED_SIGNATURES = Collections.synchronizedSet(new HashSet<String>());

    private AsteroidShapeFactory() {
    }

    public static AsteroidShape createUniqueShape() {
        while (true) {
            int numVertices = 9 + RANDOM.nextInt(6); // 9 a 14 vértices aleatorios
            double baseRadius = 24.0 + RANDOM.nextDouble() * 22.0; // Radio base aleatorio entre 24 y 46 px

            double[] angles = new double[numVertices];
            double step = (2.0 * Math.PI) / numVertices;
            for (int i = 0; i < numVertices; i++) {
                double jitter = (RANDOM.nextDouble() - 0.5) * (step * 0.55);
                angles[i] = i * step + jitter;
            }
            Arrays.sort(angles);

            double[] radii = new double[numVertices];
            double[] relX = new double[numVertices];
            double[] relY = new double[numVertices];
            double maxRadius = 0.0;
            StringBuilder sigBuilder = new StringBuilder();
            sigBuilder.append("V:").append(numVertices).append(";");

            for (int i = 0; i < numVertices; i++) {
                double radVariance = (RANDOM.nextDouble() - 0.5) * (baseRadius * 0.70);
                double r = Math.max(12.0, baseRadius + radVariance);
                radii[i] = r;
                if (r > maxRadius) {
                    maxRadius = r;
                }

                double px = r * Math.cos(angles[i]);
                double py = r * Math.sin(angles[i]);
                relX[i] = px;
                relY[i] = py;

                // Firma matemática de alta precisión para verificación de unicidad
                sigBuilder.append(String.format(Locale.US, "[%.3f,%.3f];", px, py));
            }

            String signature = sigBuilder.toString();
            // Comprobación atómica de no-repetición
            if (GENERATED_SIGNATURES.add(signature)) {
                Path2D.Double path = new Path2D.Double();
                path.moveTo(relX[0], relY[0]);

                for (int i = 1; i < numVertices; i++) {
                    path.lineTo(relX[i], relY[i]);
                }
                path.closePath();

                return new AsteroidShape(path, maxRadius, signature, relX, relY);
            }
        }
    }

    public static int getGeneratedSignaturesCount() {
        return GENERATED_SIGNATURES.size();
    }
}
