/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

public class Asteroid {
    private static final Stroke ASTEROID_STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final long id;
    private double x;
    private double y;
    private final double speed;
    private final double dirX;
    private final double dirY;
    private final double vx;
    private final double vy;
    private final double angularVelocityDeg;
    private final double angularVelocityRad;
    private double currentAngleRad;
    private final AsteroidShape shape;

    public Asteroid(long id, double initialX, double initialY, double speed, double dirX, double dirY, double angularVelocityDeg, AsteroidShape shape) {
        this.id = id;
        this.x = initialX;
        this.y = initialY;
        this.speed = Math.max(0.1, speed);
        this.angularVelocityDeg = angularVelocityDeg;
        this.angularVelocityRad = Math.toRadians(angularVelocityDeg);
        this.currentAngleRad = 0.0;
        this.shape = shape;

        // Normalizar vector director
        double len = Math.hypot(dirX, dirY);
        if (len < 1e-6) {
            this.dirX = 1.0;
            this.dirY = 0.0;
        } else {
            this.dirX = dirX / len;
            this.dirY = dirY / len;
        }

        // Velocidad en píxeles por frame estándar (60 FPS)
        this.vx = this.dirX * this.speed;
        this.vy = this.dirY * this.speed;
    }

    public void update(double dt) {
        // Factor de escala temporal relativo a 60 FPS
        double frameScale = dt * 60.0;

        // Traslación constante
        this.x += this.vx * frameScale;
        this.y += this.vy * frameScale;

        // Rotación constante con signo (+ sentido horario, - sentido antihorario)
        this.currentAngleRad += this.angularVelocityRad * frameScale;
        if (this.currentAngleRad > Math.PI * 2) {
            this.currentAngleRad -= Math.PI * 2;
        } else if (this.currentAngleRad < -Math.PI * 2) {
            this.currentAngleRad += Math.PI * 2;
        }
    }

    public void wrapAround(double minX, double minY, double maxX, double maxY) {
        double r = this.shape.getMaxRadius();

        // Salida completa por la derecha -> reaparece por la izquierda
        if (this.x - r > maxX) {
            this.x = minX - r;
        }
        // Salida completa por la izquierda -> reaparece por la derecha
        else if (this.x + r < minX) {
            this.x = maxX + r;
        }

        // Salida completa por abajo -> reaparece por arriba
        if (this.y - r > maxY) {
            this.y = minY - r;
        }
        // Salida completa por arriba -> reaparece por abajo
        else if (this.y + r < minY) {
            this.y = maxY + r;
        }
    }

    public void render(Graphics2D g2) {
        AffineTransform previousTransform = g2.getTransform();

        g2.translate(this.x, this.y);
        g2.rotate(this.currentAngleRad);

        g2.setColor(Color.WHITE);
        g2.setStroke(ASTEROID_STROKE);
        g2.draw(this.shape.getPath());

        g2.setTransform(previousTransform);
    }

    public long getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDirX() {
        return dirX;
    }

    public double getDirY() {
        return dirY;
    }

    public double getAngularVelocityDeg() {
        return angularVelocityDeg;
    }

    public AsteroidShape getShape() {
        return shape;
    }
}
