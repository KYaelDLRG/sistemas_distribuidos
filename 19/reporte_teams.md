# Reporte de Práctica: Cliente HTTP Asíncrono en Java

**Alumno:** Kevin Yael De La Rosa Galindo  
**Materia:** Sistemas Distribuidos  
**Profesor:** Dr. Ukranio Contreras Coronilla  
**Directorio:** `d:\SistemasDistribuidos\19`  

---

## 1. Preguntas de Consulta con la IA

### ¿Qué diferencia existe entre un cliente HTTP en Java síncrono y un asíncrono?
- **Cliente Síncrono (Bloqueante):** El hilo que emite la solicitud HTTP queda suspendido en espera de que el servidor remoto procese la petición y devuelva la respuesta por la red. Los recursos del hilo se desperdician en inactividad I/O y las peticiones consecutivas se serializan, requiriendo un hilo dedicado por conexión para lograr paralelismo.
- **Cliente Asíncrono (No Bloqueante):** El hilo emisor despacha la solicitud y retorna de inmediato, devolviendo una promesa o futuro (`CompletableFuture<T>`). La espera de red y transferencia de datos ocurre en segundo plano mediante `java.net.http.HttpClient` reutilizando conexiones TCP mediante **Connection Pooling** (HTTP/1.1). Permite despachar tareas simultáneamente a múltiples servidores y coordinar la concurrencia de forma no bloqueante.

### ¿Cuál es la funcionalidad del método `isDone()` perteneciente a la clase `CompletableFuture`?
El método `isDone()` evalúa el estado del objeto `CompletableFuture` y retorna un valor booleano:
- Devuelve `true` si la operación asíncrona ya culminó (con resultado exitoso, error/excepción o cancelación).
- Devuelve `false` si la operación continúa ejecutándose en el servidor.
A diferencia de `join()` o `get()`, `isDone()` **no bloquea** el hilo actual, lo que permite implementar un ciclo de monitoreo (*polling*) para saber en tiempo real cuándo se desocupa un servidor y asignarle de inmediato la siguiente tarea pendiente.

---

## 2. Código Fuente Implementado

### `networking/WebClient.java`
```java
/*
 *  MIT License
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 */
package networking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class WebClient {
    private HttpClient client;

    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public CompletableFuture<String> sendTask(String url, byte[] requestPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
                .uri(URI.create(url))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
```

### `Aggregator.java`
```java
/*
 *  MIT License
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 */
import networking.WebClient;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        int numWorkers = workersAddresses.size();
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[numWorkers];
        int[] currentTaskIndices = new int[numWorkers];
        String[] results = new String[tasks.size()];

        int nextTaskIndex = 0;

        // Asignación inicial: una tarea por trabajador activo
        for (int i = 0; i < numWorkers && nextTaskIndex < tasks.size(); i++) {
            String workerAddress = workersAddresses.get(i);
            String task = tasks.get(nextTaskIndex);

            System.out.println("Al servidor " + workerAddress + " se le asigna la tarea: " + task);
            byte[] requestPayload = task.getBytes();
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
            currentTaskIndices[i] = nextTaskIndex;
            nextTaskIndex++;
        }

        // Monitoreo y balanceo continuo con isDone()
        boolean active = true;
        while (active) {
            boolean anyRunning = false;

            for (int j = 0; j < numWorkers; j++) {
                if (futures[j] != null) {
                    if (futures[j].isDone()) {
                        int completedTaskIdx = currentTaskIndices[j];
                        String response = futures[j].join();
                        results[completedTaskIdx] = response;

                        System.out.println("  El servidor " + workersAddresses.get(j) + " completó la tarea " + tasks.get(completedTaskIdx));

                        if (nextTaskIndex < tasks.size()) {
                            String nextTask = tasks.get(nextTaskIndex);
                            System.out.println("Al servidor " + workersAddresses.get(j) + " se le asigna la tarea: " + nextTask);

                            byte[] requestPayload = nextTask.getBytes();
                            futures[j] = webClient.sendTask(workersAddresses.get(j), requestPayload);
                            currentTaskIndices[j] = nextTaskIndex;
                            nextTaskIndex++;
                            anyRunning = true;
                        } else {
                            futures[j] = null;
                        }
                    } else {
                        anyRunning = true;
                    }
                }
            }

            if (!anyRunning && nextTaskIndex >= tasks.size()) {
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

        List<String> resultsList = new ArrayList<>();
        for (String result : results) {
            resultsList.add(result);
        }

        return resultsList;
    }
}
```

### `Application.java`
```java
/*
 *  MIT License
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application {
    private static final String WORKER_ADDRESS_1 = "http://localhost:8081/searchtoken";
    private static final String WORKER_ADDRESS_2 = "http://localhost:8082/searchtoken";

    public static void main(String[] args) {
        Aggregator aggregator = new Aggregator();

        List<String> listaTareas = new ArrayList<String>();
        listaTareas.add("1757600,IPN");
        listaTareas.add("17576,SAL");
        listaTareas.add("70000,MAS");
        listaTareas.add("1757600,PEZ");
        listaTareas.add("175700,SOL");

        System.out.println("Las tareas a resolver son las siguientes:");
        for (int i = 0; i < listaTareas.size(); i++) {
            System.out.println("Tarea " + i + " :" + listaTareas.get(i));
        }
        System.out.println();

        List<String> results = aggregator.sendTasksToWorkers(
                Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2),
                listaTareas);

        System.out.println();
        for (int i = 0; i < listaTareas.size(); i++) {
            System.out.println("Para la tarea " + listaTareas.get(i) + " El numero de apariciones es " + results.get(i));
            System.out.println();
        }
    }
}
```

---

## 3. Evidencia de Ejecución en Consola

```text
Las tareas a resolver son las siguientes:
Tarea 0 :1757600,IPN
Tarea 1 :17576,SAL
Tarea 2 :70000,MAS
Tarea 3 :1757600,PEZ
Tarea 4 :175700,SOL

Al servidor http://localhost:8081/searchtoken se le asigna la tarea: 1757600,IPN
Al servidor http://localhost:8082/searchtoken se le asigna la tarea: 17576,SAL
  El servidor http://localhost:8082/searchtoken completó la tarea 17576,SAL
Al servidor http://localhost:8082/searchtoken se le asigna la tarea: 70000,MAS
  El servidor http://localhost:8082/searchtoken completó la tarea 70000,MAS
Al servidor http://localhost:8082/searchtoken se le asigna la tarea: 1757600,PEZ
  El servidor http://localhost:8081/searchtoken completó la tarea 1757600,IPN
Al servidor http://localhost:8081/searchtoken se le asigna la tarea: 175700,SOL
  El servidor http://localhost:8081/searchtoken completó la tarea 175700,SOL
  El servidor http://localhost:8082/searchtoken completó la tarea 1757600,PEZ

Para la tarea 1757600,IPN El numero de apariciones es 100

Para la tarea 17576,SAL El numero de apariciones es 0

Para la tarea 70000,MAS El numero de apariciones es 3

Para la tarea 1757600,PEZ El numero de apariciones es 84

Para la tarea 175700,SOL El numero de apariciones es 11
```
