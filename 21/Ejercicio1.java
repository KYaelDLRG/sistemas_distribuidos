import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class Ejercicio1 {

    public static void main(String[] args) throws Exception {

        String passwordSecreta = (args.length > 0) ? args[0] : "zorro";

        String hashObjetivo = sha256(passwordSecreta);

        System.out.println("===============================================================");
        System.out.println(" Hash objetivo: " + hashObjetivo);
        System.out.println(" Buscando contraseña de 5 letras para: '" + passwordSecreta + "'");
        System.out.println("===============================================================");
        System.out.println("Iniciando busqueda exhaustiva por fuerza bruta...");
        System.out.println("(Se mostrara el avance conforme se pruebe cada letra inicial)\n");

        long inicio = System.currentTimeMillis();

        long intentos = 0;

        for (char a = 'a'; a <= 'z'; a++) {
            System.out.println("-> [" + a + "] Evaluando espacio '" + a + "aaaa' hasta '" + a + "zzzz' (intentos acumulados: " + intentos + ")...");

            for (char b = 'a'; b <= 'z'; b++) {
                for (char c = 'a'; c <= 'z'; c++) {
                    for (char d = 'a'; d <= 'z'; d++) {
                        for (char e = 'a'; e <= 'z'; e++) {

                            String intento = "" + a + b + c + d + e;

                            intentos++;

                            String hash = sha256(intento);

                            if (hash.equalsIgnoreCase(hashObjetivo)) {

                                long fin = System.currentTimeMillis();

                                System.out.println("\n===============================================================");
                                System.out.println(" ¡CONTRASEÑA ENCONTRADA!: " + intento);
                                System.out.println(" Intentos totales:       " + intentos);
                                System.out.println(" Tiempo transcurrido:    " + (fin - inicio) + " ms (" + String.format("%.2f", (fin - inicio) / 1000.0) + " segundos)");
                                System.out.println("===============================================================");

                                return;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("\nNo encontrada tras evaluar todo el espacio de combinaciones.");
    }

    public static String sha256(String texto) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
