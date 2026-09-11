public abstract class Figura implements Desplazable {
    
    protected Coordenada centro;
    protected Coordenada[] vertices;

    public Figura(Coordenada centro){
        this.centro = centro;
    }

    public Figura(Coordenada centro, int numVertices){
        this.centro = centro;
        this.vertices = new Coordenada[numVertices];
    }

    public abstract double area();

    @Override
    public void desplazar(double dx, double dy) {
        if (this.centro != null) {
            this.centro = new Coordenada(this.centro.abcisa() + dx, this.centro.ordenada() + dy);
        }
        if (this.vertices != null) {
            for (int i = 0; i < this.vertices.length; i++) {
                if (this.vertices[i] != null) {
                    this.vertices[i] = new Coordenada(this.vertices[i].abcisa() + dx, this.vertices[i].ordenada() + dy);
                }
            }
        }
    }

    public Coordenada getCentro() {
        return centro;
    }

    public Coordenada[] getVertices() {
        return vertices;
    }

    public String imprimirVertices() {
        if (vertices == null || vertices.length == 0) return "Sin vértices";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.length; i++) {
            sb.append("  Vértice ").append(i + 1).append(": ").append(vertices[i]).append("\n");
        }
        return sb.toString();
    }
}