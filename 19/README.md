# Práctica 19: Cliente HTTP Asíncrono en Java

Implementación de cliente HTTP asíncrono con **Connection Pooling**, **`CompletableFuture`**, **`isDone()`** y balanceo dinámico de carga entre múltiples servidores distribuidos, correspondiente a la materia de **Sistemas Distribuidos**.

---

## 📋 Contenido de la Solución

1. **Respuestas Teóricas:**
   - **¿Qué diferencia existe entre un cliente HTTP en Java síncrono y un asíncrono?**
     - *Síncrono:* Bloquea el hilo emisor en espera de la respuesta de red. Los recursos del hilo se desperdician en inactividad I/O y las peticiones múltiples se serializan o requieren un hilo por conexión.
     - *Asíncrono:* No bloquea el hilo llamador; retorna de inmediato un `CompletableFuture<T>`. Permite enviar peticiones concurrentes a múltiples servidores, optimizar el uso de hilos y reutilizar conexiones TCP subyacentes (*Connection Pooling*).
   - **¿Cuál es la funcionalidad del método `isDone()` en `CompletableFuture`?**
     - Retorna de manera no bloqueante `true` si la tarea asíncrona ya culminó (con resultado, excepción o cancelación) y `false` si sigue en ejecución. Permite consultar periódicamente el estado de los trabajadores (*polling*) para asignar nuevas tareas dinámicamente al primer servidor que se desocupe.

2. **Código Fuente:**
   - [`WebClient.java`](file:///d:/SistemasDistribuidos/19/WebClient.java): Cliente HTTP asíncrono basado en `java.net.http.HttpClient` con protocolo HTTP/1.1 y método `sendTask(...)`.
   - [`Aggregator.java`](file:///d:/SistemasDistribuidos/19/Aggregator.java): Orquestador con balanceo de carga continuo mediante `isDone()`.
   - [`Application.java`](file:///d:/SistemasDistribuidos/19/Application.java): Clase principal con la lista de 5 tareas solicitadas y formato de salida idéntico a la especificación.
   - [`ServidorHttp.java`](file:///d:/SistemasDistribuidos/19/ServidorHttp.java): Servidor HTTP concurrente con endpoint `/searchtoken` y puerto configurable.

---

## 🚀 Instrucciones de Ejecución

### Opción 1: Ejecución Automatizada Rápida (Recomendada)
Haga doble clic en [`ejecutar_todo_demo.bat`](file:///d:/SistemasDistribuidos/19/ejecutar_todo_demo.bat). Este script:
1. Compila los archivos Java con JDK 21.
2. Empaqueta `Application.jar`.
3. Inicia el Servidor 1 (puerto 8081) y el Servidor 2 (puerto 8082).
4. Ejecuta el cliente asíncrono mostrando la secuencia completa en consola.
5. Permite cerrar los servidores al finalizar.

---

### Opción 2: Ejecución Manual Paso a Paso (3 Terminales)

#### Paso 1: Compilar y empaquetar
```cmd
compilar.bat
```
*(O utilizando `make`: `make compile && make jar`)*

#### Paso 2: Terminal 1 - Servidor 8081
```cmd
ejecutar_servidor_8081.bat
```
*(O directamente: `"C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin\java.exe" ServidorHttp 8081`)*

#### Paso 3: Terminal 2 - Servidor 8082
```cmd
ejecutar_servidor_8082.bat
```
*(O directamente: `"C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin\java.exe" ServidorHttp 8082`)*

#### Paso 4: Terminal 3 - Cliente Application
```cmd
ejecutar_cliente.bat
```
*(O directamente: `"C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin\java.exe" -cp Application.jar Application`)*

---

## 🖥️ Salida de Ejemplo en Consola

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

Para la tarea 1757600,IPN El numero de apariciones es 109

Para la tarea 17576,SAL El numero de apariciones es 0

Para la tarea 70000,MAS El numero de apariciones es 6

Para la tarea 1757600,PEZ El numero de apariciones es 112

Para la tarea 175700,SOL El numero de apariciones es 14
```
