import java.util.Random;

public class PromedioEnteros {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Debe proporcionar un numero entero n en la linea de comandos.");
            System.out.println("Uso: java PromedioEnteros <n>");
            return;
        }

        int n;
        try {
            n = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("El argumento debe ser un numero entero valido.");
            return;
        }

        Random rand = new Random();
        int divisionesPorCero = 0;
        int divisionesValidas = 0;

        for (int i = 0; i < n; i++) {
            int num1 = rand.nextInt(10);
            int num2 = rand.nextInt(10);

            try {
                // En Java la division entera lanza ArithmeticException si num2 == 0
                int divisionEntera = num1 / num2;
                // Calculo con parte fraccionaria
                double resultado = (double) num1 / num2;
                System.out.println(num1 + " / " + num2 + " = " + resultado);
                divisionesValidas++;
            } catch (ArithmeticException e) {
                System.out.println(num1 + " / " + num2 + " = Algo salio mal (division por cero)");
                divisionesPorCero++;
            }
        }

        System.out.println("\nResumen:");
        System.out.println("Veces que se intento dividir por cero: " + divisionesPorCero);
        System.out.println("Veces que no se dividio por cero: " + divisionesValidas);
    }
}
