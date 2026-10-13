public class PruebaRectangulo{
    public static void main(String[] args){
        Rectangulo rect1 = new Rectangulo(2,3,4,1);
        double ancho, alto;

        System.out.println("Calculando el área de un rectángulo dadas las coordenadas en un plano cartesiano:");
        System.out.println(rect1);
        alto = rect1.superiorIzq().ordenada() - rect1.inferiorDer().ordenada();
        ancho = rect1.inferiorDer().abcisa() - rect1.superiorIzq().abcisa();
        System.out.println("El área del rectángulo es: " + ancho*alto);

        // probamos el nuevo constructor pasandole dos objetos Coordenada
        Coordenada c1 = new Coordenada(2, 3);
        Coordenada c2 = new Coordenada(4, 1);
        Rectangulo rect2 = new Rectangulo(c1, c2);

        System.out.println("\nEjercicio 1:");
        System.out.println(rect2);
        alto = rect2.superiorIzq().ordenada() - rect2.inferiorDer().ordenada();
        ancho = rect2.inferiorDer().abcisa() - rect2.superiorIzq().abcisa();
        System.out.println("Ejercicio 1 el area es: " + ancho*alto);

        // =========================================================================
        // Ejercicio 2: Validación de coordenadas con try-catch
        // =========================================================================
        System.out.println("\n==================================================");
        System.out.println("Ejercicio 2: Validación con try-catch");
        System.out.println("==================================================");

        // Caso 1: Coordenadas válidas (NO erróneas)
        System.out.println("\n--- Caso 1: Cuando las coordenadas NO son erróneas ---");
        try {
            Coordenada si = new Coordenada(2, 3);
            Coordenada id = new Coordenada(4, 1);
            System.out.println("Coordenadas enviadas: Superior Izquierda (2, 3), Inferior Derecha (4, 1)");
            Rectangulo rectValido = new Rectangulo(si, id);
            System.out.println("Rectángulo creado exitosamente:");
            System.out.println(rectValido);
            alto = rectValido.superiorIzq().ordenada() - rectValido.inferiorDer().ordenada();
            ancho = rectValido.inferiorDer().abcisa() - rectValido.superiorIzq().abcisa();
            System.out.println("Área calculada: " + (ancho * alto));
        } catch (IllegalArgumentException e) {
            System.out.println("Error al crear el objeto Rectangulo: " + e.getMessage());
        }

        // Caso 2: Coordenadas inválidas (SÍ erróneas)
        System.out.println("\n--- Caso 2: Cuando se introduce una coordenada errónea ---");
        try {
            Coordenada si = new Coordenada(5, 1);
            Coordenada id = new Coordenada(2, 4);
            System.out.println("Coordenadas enviadas: Superior Izquierda (5, 1), Inferior Derecha (2, 4)");
            System.out.println("Intentando instanciar objeto Rectangulo...");
            Rectangulo rectInvalido = new Rectangulo(si, id);
            System.out.println("Rectángulo creado exitosamente.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error al crear el objeto Rectangulo: " + e.getMessage());
        }
    }
}