public class TrianguloEq {
    public Coordenada vertice1, vertice2, vertice3;
    
    public TrianguloEq(Coordenada v1, Coordenada v2, Coordenada v3){
        this.vertice1 = v1;
        this.vertice2 = v2;
        this.vertice3 = v3;
    }

    public double calcularArea(){
        return Math.abs((vertice1.abcisa()*(vertice2.ordenada()-vertice3.ordenada()) + vertice2.abcisa()*(vertice3.ordenada()-vertice1.ordenada()) + vertice3.abcisa()*(vertice1.ordenada()-vertice2.ordenada()))/2);
    }

    @Override
    public String toString(){
        return "Vértice 1: " + vertice1 + "\t Vértice 2: " + vertice2 + "\t Vértice 3: " + vertice3 + "\n";
    }
}