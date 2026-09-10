public class PruebaPoligonoIrreg{
    public static void main(String[] args){
        // creamos el poligono con 7 vertices aleatorios
        PoligonoIrreg pol = new PoligonoIrreg(7);

        // mostramos el poligono original
        System.out.println("-- Poligono Original --\n" + pol);

        // cambiamos el vertice 3 por [0.0, 0.0]
        System.out.println("Modificando el vértice 3 a [0.0, 0.0]...\n");
        pol.modificaVertice(3, new Coordenada(0.0, 0.0));

        // volvemos a imprimir para verificar el cambio
        System.out.println("-- Poligono modificado --\n" + pol);
    }
}