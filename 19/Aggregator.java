/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import networking.WebClient;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinador / Agregador de Tareas Asíncronas (Aggregator).
 * 
 * Explicación según el video:
 * 1. La clase Aggregator tiene como único atributo un objeto de tipo WebClient.
 * 2. El constructor instancia el objeto WebClient.
 * 3. El método sendTasksToWorkers recibe la lista de direcciones de los trabajadores
 *    (workersAddresses) y la lista de tareas a realizar (tasks).
 * 4. Para el manejo de la comunicación asíncrona se hace uso de CompletableFuture (Java 8+),
 *    lo que permite enviar solicitudes sin bloquear el hilo principal.
 * 5. Se utiliza el método isDone() de CompletableFuture para consultar de forma no bloqueante
 *    si un servidor trabajador ha concluido su tarea, asignándole inmediatamente la siguiente
 *    tarea pendiente para lograr un balanceo de carga continuo.
 */
public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    /**
     * Envía y distribuye dinámicamente las tareas entre los servidores trabajadores.
     * 
     * Implementación del Ejercicio:
     * - Al inicio, el servidor en el puerto 8081 resuelve la primera tarea ("1757600,IPN"),
     *   mientras que el servidor en el puerto 8082 resuelve la segunda tarea ("17576,SAL").
     * - Al primer servidor que termine (el puerto 8082, por ser tarea corta) se le asigna la tercera tarea ("70000,MAS").
     * - Nuevamente, al primer servidor que termine se le asigna la cuarta tarea ("1757600,PEZ"), y así sucesivamente.
     * - Ambos servidores se mantienen trabajando continuamente y se imprimen las asignaciones y terminaciones.
     * 
     * @param workersAddresses Lista de endpoints de los servidores trabajadores
     * @param tasks Lista de tareas ("cantidad,TOKEN")
     * @return Lista de resultados correspondientes a cada tarea en su orden original
     */
    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        int numWorkers = workersAddresses.size();
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[numWorkers];
        int[] currentTaskIndices = new int[numWorkers];
        String[] results = new String[tasks.size()];

        int nextTaskIndex = 0;

        // -------------------------------------------------------------------------
        // Asignación inicial: una tarea para cada servidor disponible
        // (Puerto 8081 recibe tarea 0, Puerto 8082 recibe tarea 1)
        // -------------------------------------------------------------------------
        for (int i = 0; i < numWorkers && nextTaskIndex < tasks.size(); i++) {
            String workerAddress = workersAddresses.get(i);
            String task = tasks.get(nextTaskIndex);

            System.out.println("Al servidor " + workerAddress + " se le asigna la tarea: " + task);
            byte[] requestPayload = task.getBytes();
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
            currentTaskIndices[i] = nextTaskIndex;
            nextTaskIndex++;
        }

        /*
         * =========================================================================
         * FRAGMENTO ILUSTRATIVO INICIAL (Mencionado en la primera parte de la práctica):
         * Demostración de evaluación continua con isDone() para 2 servidores:
         * 
         * boolean bandera = true;
         * while(bandera){
         *     for(int j = 0; j < 2; j++){
         *         System.out.println("futures["+j+"].isDone() = " + futures[j].isDone());
         *         if (true == futures[j].isDone())
         *             bandera = false;
         *     }
         * }
         * =========================================================================
         */

        // -------------------------------------------------------------------------
        // Balanceo dinámico de carga mediante sondeo con isDone()
        // -------------------------------------------------------------------------
        boolean active = true;
        while (active) {
            boolean anyRunning = false;

            for (int j = 0; j < numWorkers; j++) {
                if (futures[j] != null) {
                    // isDone() retorna true cuando el servidor completó la tarea
                    if (futures[j].isDone()) {
                        int completedTaskIdx = currentTaskIndices[j];
                        String response = futures[j].join(); // Recupera el cuerpo devuelto por el servidor
                        results[completedTaskIdx] = response;

                        // Mensaje de tarea completada (con sangría de 2 espacios)
                        System.out.println("  El servidor " + workersAddresses.get(j) + " completó la tarea " + tasks.get(completedTaskIdx));

                        // Si aún hay tareas pendientes por asignar en la cola
                        if (nextTaskIndex < tasks.size()) {
                            String nextTask = tasks.get(nextTaskIndex);
                            System.out.println("Al servidor " + workersAddresses.get(j) + " se le asigna la tarea: " + nextTask);

                            byte[] requestPayload = nextTask.getBytes();
                            futures[j] = webClient.sendTask(workersAddresses.get(j), requestPayload);
                            currentTaskIndices[j] = nextTaskIndex;
                            nextTaskIndex++;
                            anyRunning = true;
                        } else {
                            // No hay más tareas pendientes para este servidor
                            futures[j] = null;
                        }
                    } else {
                        anyRunning = true;
                    }
                }
            }

            // Si ningún servidor está ejecutando y ya se asignaron todas las tareas, finalizamos
            if (!anyRunning && nextTaskIndex >= tasks.size()) {
                active = false;
            } else {
                try {
                    // Breve pausa para no saturar el hilo durante el sondeo
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Construcción de la lista de resultados preservando el orden original de las tareas
        List<String> resultsList = new ArrayList<>();
        for (String result : results) {
            resultsList.add(result);
        }

        return resultsList;
    }
}
