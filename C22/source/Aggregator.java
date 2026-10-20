import networking.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinador / Agregador de Tareas Asíncronas (Aggregator) para la Clase 22.
 * 
 * Modificaciones para el Ejercicio 1:
 * 1. Envía tareas ("hash,letra") a los workers configurados en paralelo.
 * 2. Monitorea mediante sondeo continuo no bloqueante con isDone().
 * 3. En cuanto un worker encuentra la contraseña (respuesta diferente de "NULL"):
 *    - Se activa la bandera passwordFound.
 *    - Se suspende de inmediato la asignación de nuevas tareas a los workers.
 *    - Se espera a que concluyan únicamente las tareas que ya estaban en vuelo.
 * 4. Retorna a la clase Application la lista de todas las tareas ejecutadas
 *    hasta ese momento junto con sus resultados correspondientes.
 */
public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    /**
     * Envía y balancea dinámicamente las tareas entre los servidores trabajadores.
     * 
     * @param workersAddresses Lista de endpoints de los trabajadores (ej. localhost:8081/task, localhost:8082/task)
     * @param tasks Lista de 26 tareas ("hashObjetivo,letra")
     * @return Lista con los resultados de las tareas efectivamente realizadas hasta encontrar la solución
     */
    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        int numWorkers = workersAddresses.size();
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[numWorkers];
        int[] currentTaskIndices = new int[numWorkers];
        String[] results = new String[tasks.size()];

        int nextTaskIndex = 0;
        boolean passwordFound = false;

        // -------------------------------------------------------------------------
        // Asignación inicial: una tarea por cada servidor disponible
        // (Worker 1 recibe tarea 0 ['a'], Worker 2 recibe tarea 1 ['b'])
        // -------------------------------------------------------------------------
        for (int i = 0; i < numWorkers && nextTaskIndex < tasks.size(); i++) {
            String workerAddress = workersAddresses.get(i);
            String task = tasks.get(nextTaskIndex);
            byte[] requestPayload = task.getBytes();
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
            currentTaskIndices[i] = nextTaskIndex;
            nextTaskIndex++;
        }

        // -------------------------------------------------------------------------
        // Bucle de balanceo dinámico y sondeo con isDone()
        // -------------------------------------------------------------------------
        boolean active = true;
        while (active) {
            boolean anyRunning = false;

            for (int j = 0; j < numWorkers; j++) {
                if (futures[j] != null) {
                    if (futures[j].isDone()) {
                        int completedTaskIdx = currentTaskIndices[j];
                        String response = futures[j].join();
                        results[completedTaskIdx] = response;

                        // Verificar si este worker encontró la contraseña
                        if (response != null && !response.trim().equalsIgnoreCase("NULL") && !response.trim().isEmpty()) {
                            passwordFound = true;
                        }

                        // Si NO se ha encontrado la contraseña y aún hay tareas en la cola, despachar la siguiente
                        if (!passwordFound && nextTaskIndex < tasks.size()) {
                            String nextTask = tasks.get(nextTaskIndex);
                            byte[] requestPayload = nextTask.getBytes();
                            futures[j] = webClient.sendTask(workersAddresses.get(j), requestPayload);
                            currentTaskIndices[j] = nextTaskIndex;
                            nextTaskIndex++;
                            anyRunning = true;
                        } else {
                            // Si ya se encontró la clave o no hay más tareas, no reasignar a este worker
                            futures[j] = null;
                        }
                    } else {
                        // El worker j aún sigue procesando su tarea en vuelo
                        anyRunning = true;
                    }
                }
            }

            // Si ya no hay ningún worker con tareas en vuelo, finalizar el bucle
            if (!anyRunning) {
                active = false;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // -------------------------------------------------------------------------
        // Construir la lista de resultados de las tareas realizadas hasta ese momento
        // (Preservando el orden de las tareas e interrumpiendo tras el hallazgo)
        // -------------------------------------------------------------------------
        List<String> resultsList = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (results[i] != null) {
                resultsList.add(results[i]);
                if (!results[i].trim().equalsIgnoreCase("NULL") && !results[i].trim().isEmpty()) {
                    // Se alcanzó la tarea que encontró la contraseña: detener la lista de resultados aquí
                    break;
                }
            }
        }

        return resultsList;
    }
}
