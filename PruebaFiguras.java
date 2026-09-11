public class PruebaFiguras {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   FIGURAS Y DESPLAZAMIENTO");
        System.out.println("==================================================\n");

        // -------------------------------------------------------------
        // 1. Creación de las figuras iniciales
        // -------------------------------------------------------------
        // Triángulo equilátero con centro en (0, 0) y lado de 6
        TrianguloEq triangulo = new TrianguloEq(new Coordenada(0.0, 0.0), 6.0);

        // Rectángulo con centro en (2, 3), base de 4 y altura de 2
        Rectangulo rectangulo = new Rectangulo(new Coordenada(2.0, 3.0), 4.0, 2.0);

        // -------------------------------------------------------------
        // 2. Mostrar estado inicial (Áreas y Coordenadas de Vértices)
        // -------------------------------------------------------------
        System.out.println(">>> POSICIONES INICIALES <<<");
        System.out.println("--------------------------------------------------");
        System.out.println(triangulo);
        System.out.println("Área del Triángulo Equilátero: " + triangulo.area());
        System.out.println("--------------------------------------------------");
        System.out.println(rectangulo);
        System.out.println("Área del Rectángulo: " + rectangulo.area());
        System.out.println("--------------------------------------------------\n");

        // -------------------------------------------------------------
        // 3. Desplazamiento de las figuras en el plano
        // -------------------------------------------------------------
        double dxTri = 4.0, dyTri = 5.0;
        double dxRect = 3.0, dyRect = -2.0;

        System.out.println(">>> DESPLAZANDO FIGURAS <<<");
        System.out.println("Desplazando Triángulo: dx = " + dxTri + ", dy = " + dyTri);
        triangulo.desplazar(dxTri, dyTri);

        System.out.println("Desplazando Rectángulo: dx = " + dxRect + ", dy = " + dyRect);
        rectangulo.desplazar(dxRect, dyRect);
        System.out.println("--------------------------------------------------\n");

        // -------------------------------------------------------------
        // 4. Mostrar estado final (Nuevas Coordenadas y Áreas)
        // -------------------------------------------------------------
        System.out.println(">>> POSICIONES DESPLAZADAS <<<");
        System.out.println("--------------------------------------------------");
        System.out.println(triangulo);
        System.out.println("Área tras desplazamiento: " + triangulo.area());
        System.out.println("--------------------------------------------------");
        System.out.println(rectangulo);
        System.out.println("Área tras desplazamiento: " + rectangulo.area());
        System.out.println("==================================================");
    }
}
