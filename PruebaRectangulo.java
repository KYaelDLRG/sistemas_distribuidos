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

        // prueba del try-catch con coordenadas mal ordenadas
        System.out.println("\nEjercicio 2: Probando validación con try-catch:");
        try {
            // coordenadas al reves a proposito para que entre al catch
            Coordenada cInvalida1 = new Coordenada(5, 1);
            Coordenada cInvalida2 = new Coordenada(2, 4);

            System.out.println("Coordenadas proporcionadas:");
            System.out.println("  - Superior Izquierda pretendida: " + cInvalida1);
            System.out.println("  - Inferior Derecha pretendida:   " + cInvalida2);
            System.out.println("Motivo del error:");
            System.out.println("  * En X: " + cInvalida1.abcisa() + " >= " + cInvalida2.abcisa() + " (no está a la izquierda)");
            System.out.println("  * En Y: " + cInvalida1.ordenada() + " <= " + cInvalida2.ordenada() + " (no está arriba)");
            
            System.out.println("Intentando crear rectángulo con coordenadas incorrectas...");
            Rectangulo rectInvalido = new Rectangulo(cInvalida1, cInvalida2);
            
            System.out.println("Rectángulo creado exitosamente.");
        } catch (IllegalArgumentException e) {
            // atrapamos el error del constructor
            System.out.println("-> Se atrapó la excepción: " + e.getMessage());
        }
    }
}