/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.awt.geom.Path2D;
import java.util.Arrays;

public class AsteroidShape {
    private final Path2D.Double path;
    private final double maxRadius;
    private final String signature;
    private final double[] relX;
    private final double[] relY;

    public AsteroidShape(Path2D.Double path, double maxRadius, String signature, double[] relX, double[] relY) {
        this.path = path;
        this.maxRadius = maxRadius;
        this.signature = signature;
        this.relX = Arrays.copyOf(relX, relX.length);
        this.relY = Arrays.copyOf(relY, relY.length);
    }

    public Path2D.Double getPath() {
        return path;
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    public String getSignature() {
        return signature;
    }

    public double[] getRelX() {
        return Arrays.copyOf(relX, relX.length);
    }

    public double[] getRelY() {
        return Arrays.copyOf(relY, relY.length);
    }

    public int getVertexCount() {
        return relX.length;
    }

    /**
     * Comprueba rigurosamente si otra forma es geométricamente idéntica en vértices.
     */
    public boolean isGeometricallyIdentical(AsteroidShape other) {
        if (other == null) return false;
        if (this.relX.length != other.relX.length) return false;

        for (int i = 0; i < this.relX.length; i++) {
            if (Math.abs(this.relX[i] - other.relX[i]) > 1e-4 ||
                Math.abs(this.relY[i] - other.relY[i]) > 1e-4) {
                return false;
            }
        }
        return true;
    }
}
