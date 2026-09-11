public class Rectangulo extends Figura {
    private double base;
    private double altura;

    // Constructor principal: recibe centro, base y altura
    public Rectangulo(Coordenada centro, double base, double altura) {
        super(centro, 4);
        this.base = base;
        this.altura = altura;
        calcularVertices();
    }

    // Constructor centrado en el origen (0, 0)
    public Rectangulo(double base, double altura) {
        this(new Coordenada(0.0, 0.0), base, altura);
    }

    // Constructor por defecto
    public Rectangulo() {
        this(new Coordenada(0.0, 0.0), 0.0, 0.0);
    }

    // Constructores de compatibilidad con ejercicios previos
    public Rectangulo(double xSupIzq, double ySupIzq, double xInfDer, double yInfDer) {
        super(new Coordenada((xSupIzq + xInfDer) / 2.0, (ySupIzq + yInfDer) / 2.0), 4);
        this.base = xInfDer - xSupIzq;
        this.altura = ySupIzq - yInfDer;
        calcularVertices();
    }

    public Rectangulo(Coordenada supIzq, Coordenada infDer) {
        super(new Coordenada((supIzq.abcisa() + infDer.abcisa()) / 2.0,
                             (supIzq.ordenada() + infDer.ordenada()) / 2.0), 4);
        if (supIzq.abcisa() >= infDer.abcisa() || supIzq.ordenada() <= infDer.ordenada()) {
            throw new IllegalArgumentException("La primer coordenada no se encuentra arriba y a la izquierda de la segunda");
        }
        this.base = infDer.abcisa() - supIzq.abcisa();
        this.altura = supIzq.ordenada() - infDer.ordenada();
        calcularVertices();
    }

    // Calcula los 4 vértices del rectángulo
    private void calcularVertices() {
        double cx = centro.abcisa();
        double cy = centro.ordenada();
        double dx = base / 2.0;
        double dy = altura / 2.0;

        // Vértice 1: Esquina Superior Izquierda
        this.vertices[0] = new Coordenada(cx - dx, cy + dy);
        // Vértice 2: Esquina Superior Derecha
        this.vertices[1] = new Coordenada(cx + dx, cy + dy);
        // Vértice 3: Esquina Inferior Derecha
        this.vertices[2] = new Coordenada(cx + dx, cy - dy);
        // Vértice 4: Esquina Inferior Izquierda
        this.vertices[3] = new Coordenada(cx - dx, cy - dy);
    }

    // Implementación del método abstracto area() de Figura
    @Override
    public double area() {
        return base * altura;
    }

    // Getters
    public double getBase() {
        return base;
    }

    public double getAltura() {
        return altura;
    }

    // Métodos de compatibilidad con PruebaRectangulo
    public Coordenada superiorIzq() {
        return vertices != null ? vertices[0] : null;
    }

    public Coordenada inferiorDer() {
        return vertices != null ? vertices[2] : null;
    }

    @Override
    public String toString() {
        return "Rectángulo [Base = " + base + ", Altura = " + altura + ", Centro = " + centro + "]\n"
             + "Vértices:\n" + imprimirVertices();
    }
}