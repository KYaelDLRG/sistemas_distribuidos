public class PoligonoIrreg {
    private Coordenada[] vertices;

    // constructor que crea el arreglo y genera los vertices aleatorios
    public PoligonoIrreg(int numVertices) {
        this.vertices = new Coordenada[numVertices];
        for (int i = 0; i < vertices.length; i++) {
            // valores entre -10 y 10 para abarcar los 4 cuadrantes
            double x = (Math.random() * 20.0) - 10.0;
            double y = (Math.random() * 20.0) - 10.0;
            this.vertices[i] = new Coordenada(x, y);
        }
    }

    // modifica el vertice de la posicion n
    public void modificaVertice(int n, Coordenada nuevaCoordenada) {
        if (n >= 0 && n < vertices.length) {
            vertices[n] = nuevaCoordenada;
        } else {
            System.err.println("Indice de vertice fuera de rango: " + n);
        }
    }

    public void modificarVertice(int n, Coordenada nuevaCoordenada) {
        modificaVertice(n, nuevaCoordenada);
    }

    // muestra todos los vertices del poligono
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Poligono Irregular con ").append(vertices.length).append(" vertices:\n");
        for (int i = 0; i < vertices.length; i++) {
            sb.append("  Vertice ").append(i).append(": ").append(vertices[i]).append("\n");
        }
        return sb.toString();
    }
}
