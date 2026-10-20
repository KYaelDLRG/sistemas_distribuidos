import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class TestSHA {

    public static void main(String[] args) throws Exception {

        // Si se pasa un argumento por línea de comandos, procesa esa palabra
        if (args.length > 0) {
            String texto = args[0];
            System.out.println("Texto: " + texto);
            System.out.println("Hash:  " + calcularSHA256(texto));
            return;
        }

        // Prueba comparativa requerida para "hola", "Hola" y "HOLA"
        String[] pruebas = {"hola", "Hola", "HOLA"};

        System.out.println("=== Verificación de Funciones Hash (SHA-256) ===");
        System.out.println("Demostración: cadenas 'hola', 'Hola' y 'HOLA' no producen el mismo hash\n");

        for (String texto : pruebas) {
            String hash = calcularSHA256(texto);
            System.out.println("Cadena: \"" + texto + "\"");
            System.out.println("SHA-256: " + hash);
            System.out.println();
        }

        String hash1 = calcularSHA256(pruebas[0]);
        String hash2 = calcularSHA256(pruebas[1]);
        String hash3 = calcularSHA256(pruebas[2]);

        boolean distintos = !hash1.equals(hash2) && !hash1.equals(hash3) && !hash2.equals(hash3);
        System.out.println("¿Todos los hashes son completamente distintos?: " + distintos);
        System.out.println("Conclusión: SHA-256 es sensible a mayúsculas/minúsculas y presenta efecto avalancha.");
    }

    public static String calcularSHA256(String texto) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}