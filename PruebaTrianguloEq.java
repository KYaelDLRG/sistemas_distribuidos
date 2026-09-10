public class PruebaTrianguloEq {
    public static void main(String[] args) {
        // Creamos 3 coordenadas para los vértices
        // Ejemplo de triángulo equilátero con lado = 4
        Coordenada v1 = new Coordenada(0.0, 0.0);
        Coordenada v2 = new Coordenada(4.0, 0.0);
        Coordenada v3 = new Coordenada(2.0, 2.0 * Math.sqrt(3)); // Altura h = sqrt(3)/2 * lado = 2 * sqrt(3)

        // Instanciamos el triángulo equilátero
        TrianguloEq tri = new TrianguloEq(v1, v2, v3);

        System.out.println("--- Prueba de TrianguloEq ---");
        System.out.println(tri);

        double area = tri.calcularArea();
        System.out.println("El área calculada es: " + area);
    }
}
