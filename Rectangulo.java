public class Rectangulo{
    private Coordenada superiorIzq, inferiorDer;
    
    public Rectangulo(){
        superiorIzq = new Coordenada(0,0);
        inferiorDer = new Coordenada(0,0);
    }

    public Rectangulo(double xSupIzq,double ySupIzq,double xInfDer,double yInfDer){
        superiorIzq = new Coordenada(xSupIzq, ySupIzq);
        inferiorDer = new Coordenada(xInfDer, yInfDer);
    }

    // getters de las coordenadas
    public Coordenada superiorIzq() { return superiorIzq;}
    public Coordenada inferiorDer() { return inferiorDer;}

    @Override
    public String toString(){
        return "Esquina superior izquierda: " + superiorIzq + "\t Esquina inferior derecha: " + inferiorDer + "\n";
    }

    // constructor que recibe dos coordenadas y valida que supIzq este arriba y a la izquierda
    public Rectangulo(Coordenada supIzq, Coordenada infDer) {
        if (supIzq.abcisa() >= infDer.abcisa() || supIzq.ordenada() <= infDer.ordenada()) {
            throw new IllegalArgumentException("La primer coordenada no se encuentra arriba y a la izquierda de la segunda");
        }
        this.superiorIzq = supIzq;
        this.inferiorDer = infDer;
    }
}