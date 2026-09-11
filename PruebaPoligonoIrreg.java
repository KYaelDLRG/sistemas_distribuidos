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

        // Modificamos el vértice principal del polígono (posición 0)
        System.out.println("Modificando el vértice principal (0) del Polígono a [5.0, 5.0]...\n");
        pol.modificaVertice(0, new Coordenada(5.0, 5.0));
        System.out.println("-- Poligono con nuevo vértice principal --\n" + pol);

        // Para el TrianguloEq
        System.out.println("==========================================");
        System.out.println("--- Demostración para TrianguloEq ---");

        // Creamos una instancia de TrianguloEq
        Coordenada t1 = new Coordenada(0.0, 0.0);
        Coordenada t2 = new Coordenada(4.0, 0.0);
        Coordenada t3 = new Coordenada(2.0, 2.0 * Math.sqrt(3));
        TrianguloEq tri = new TrianguloEq(t1, t2, t3);

        System.out.println("-- TrianguloEq Original --");
        System.out.println(tri);
        System.out.println("Área original de TrianguloEq: " + tri.calcularArea());

        // Modificamos el vértice principal (Vértice 1) de TrianguloEq
        System.out.println("\nModificando el vértice principal (Vértice 1) de TrianguloEq a [1.0, 1.0]...\n");
        tri.modificaVerticePrincipal(new Coordenada(1.0, 1.0));

        // Mostramos el TrianguloEq con sus nuevas coordenadas y área
        System.out.println("-- TrianguloEq con nuevo vértice principal --");
        System.out.println(tri);
        System.out.println("Nueva área calculada de TrianguloEq: " + tri.calcularArea());
    }
}