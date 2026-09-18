public class TrianguloEq extends Figura {
    private double lado;

    // Constructor principal: recibe centro y longitud del lado
    public TrianguloEq(Coordenada centro, double lado) {
        super(centro, 3);
        this.lado = lado;
        calcularVertices();
    }

    // Constructor que asume el centro en el origen (0,0)
    public TrianguloEq(double lado) {
        this(new Coordenada(0.0, 0.0), lado);
    }

    // Constructor opcional a partir de 3 coordenadas
    public TrianguloEq(Coordenada v1, Coordenada v2, Coordenada v3) {
        super(new Coordenada((v1.abcisa() + v2.abcisa() + v3.abcisa()) / 3.0,
                             (v1.ordenada() + v2.ordenada() + v3.ordenada()) / 3.0), 3);
        this.vertices[0] = new Coordenada(v1.abcisa(), v1.ordenada());
        this.vertices[1] = new Coordenada(v2.abcisa(), v2.ordenada());
        this.vertices[2] = new Coordenada(v3.abcisa(), v3.ordenada());
        double dx = v2.abcisa() - v1.abcisa();
        double dy = v2.ordenada() - v1.ordenada();
        this.lado = Math.sqrt(dx * dx + dy * dy);
    }

    // Calcula las posiciones de los 3 vértices a partir del centro y lado
    private void calcularVertices() {
        double cx = centro.abcisa();
        double cy = centro.ordenada();
        double h = (Math.sqrt(3.0) / 2.0) * lado;       // Altura total
        double rSup = (2.0 / 3.0) * h;                  // Distancia del centro al vértice superior
        double rInf = (1.0 / 3.0) * h;                  // Distancia del centro a la base

        // Vértice 1: Superior
        this.vertices[0] = new Coordenada(cx, cy + rSup);
        // Vértice 2: Inferior Izquierdo
        this.vertices[1] = new Coordenada(cx - (lado / 2.0), cy - rInf);
        // Vértice 3: Inferior Derecho
        this.vertices[2] = new Coordenada(cx + (lado / 2.0), cy - rInf);
    }

    // Implementación del método abstracto de Figura
    @Override
    public double area() {
        return (Math.sqrt(3.0) / 4.0) * lado * lado;
    }

    // Método de conveniencia para compatibilidad previa
    public double calcularArea() {
        return area();
    }

    public double getLado() {
        return lado;
    }

    // Métodos para modificar vértices específicos
    public void modificaVerticePrincipal(Coordenada nuevaCoordenada) {
        this.vertices[0] = new Coordenada(nuevaCoordenada.abcisa(), nuevaCoordenada.ordenada());
    }

    public void modificaVertice(int n, Coordenada nuevaCoordenada) {
        if (n >= 1 && n <= 3) {
            this.vertices[n - 1] = new Coordenada(nuevaCoordenada.abcisa(), nuevaCoordenada.ordenada());
        } else {
            System.err.println("Índice de vértice no válido para triángulo: " + n);
        }
    }

    @Override
    public String toString() {
        return "Triángulo Equilátero [Lado = " + lado + ", Centro = " + centro + "]\n"
             + "Vértices:\n" + imprimirVertices();
    }
}