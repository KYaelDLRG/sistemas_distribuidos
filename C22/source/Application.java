import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase Application para el Ejercicio 1 (Cliente HTTP Asíncrono Despachador).
 * 
 * Funcionalidades:
 * 1. Define los endpoints de los 2 workers en /task (puertos 8081 y 8082).
 * 2. Genera las 26 tareas con el hash objetivo ("de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f")
 *    y cada letra de la 'a' a la 'z'.
 * 3. Imprime la lista completa de las 26 tareas a resolver.
 * 4. Envía las tareas de manera concurrente al Aggregator.
 * 5. Muestra el resultado final encontrado y el desglose de las tareas
 *    efectivamente realizadas hasta el momento del hallazgo.
 */
public class Application {
    private static final String WORKER_ADDRESS_1 = "http://localhost:8081/task";
    private static final String WORKER_ADDRESS_2 = "http://localhost:8082/task";

    public static void main(String[] args) {
        String hashObjetivo = "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f";
        if (args.length > 0) {
            hashObjetivo = args[0].trim();
        }

        Aggregator aggregator = new Aggregator();

        // Generar las 26 tareas correspondientes al abecedario de 'a' a 'z'
        List<String> listaTareas = new ArrayList<String>();
        for (char c = 'a'; c <= 'z'; c++) {
            listaTareas.add(hashObjetivo + "," + c);
        }

        // Impresión inicial de las tareas a resolver
        System.out.println("Las tareas a resolver son las siguientes:");
        for (int i = 0; i < listaTareas.size(); i++) {
            System.out.println("Tarea " + i + " :" + listaTareas.get(i));
        }
        System.out.println();

        // Envío distribuido de tareas hacia los dos servidores trabajadores
        List<String> results = aggregator.sendTasksToWorkers(
                Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2),
                listaTareas);

        // Identificar la solución encontrada entre los resultados devueltos
        String resultadoFinal = "NULL";
        for (String r : results) {
            if (r != null && !r.trim().equalsIgnoreCase("NULL") && !r.trim().isEmpty()) {
                resultadoFinal = r.trim();
                break;
            }
        }

        // Impresión del resultado final y del detalle de tareas realizadas
        System.out.println("el resultado fué: " + resultadoFinal);
        System.out.println();
        for (int i = 0; i < results.size(); i++) {
            System.out.println("Para la tarea " + listaTareas.get(i) + " " + results.get(i).trim());
        }
    }
}
