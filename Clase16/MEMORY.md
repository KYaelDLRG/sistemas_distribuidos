# MEMORY: Práctica Clase 16 - Servidor HTTP Concurrente

Este documento preserva la memoria técnica, decisiones de diseño, contexto del entorno y resultados experimentales de la práctica correspondiente a la **Clase 16: Programadores y On-line (Sistemas Distribuidos)**.

---

## 📌 Contexto General del Proyecto

- **Materia:** Sistemas Distribuidos.
- **Tema:** Servidores HTTP Concurrentes, Pool de Hilos (*ThreadPoolExecutor*) y Tiempos de Respuesta.
- **Ruta de trabajo:** `d:\SistemasDistribuidos\16`
- **Objetivo:**
  1. Analizar el código fuente de un servidor HTTP basado en `com.sun.net.httpserver.HttpServer`.
  2. Implementar un retardo simulado de 5 segundos (`Thread.sleep(5000)`).
  3. Comprobar empíricamente el impacto en los tiempos de respuesta al reducir el pool de hilos de 8 hilos a 1 solo hilo cuando dos clientes realizan peticiones simultáneas.

---

## ⚙️ Entorno de Desarrollo y Configuración del Sistema

| Componente | Detalle / Ruta en el Sistema |
| :--- | :--- |
| **Sistema Operativo** | Windows 10/11 |
| **Compilador (`javac`)** | Amazon Corretto JDK 21.0.4 (`C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin\javac.exe`) |
| **Runtime (`java`)** | Java SE 8 (`1.8.0_441`) prioritario en PATH |
| **Herramientas de Medición** | Git Bash (`C:\Program Files\Git\bin\bash.exe`) con comandos `time` y `curl` nativos |

### ⚠️ Nota Crítica de Compilación
Dado que el compilador del sistema es Java 21 pero el runtime predeterminado en PATH es Java 8, la compilación **siempre** debe realizarse con compatibilidad `--release 8` para evitar errores `UnsupportedClassVersionError` (versión 65.0 vs 52.0):
```bash
javac --release 8 ServidorHttp.java
```

---

## 🏛️ Arquitectura del Servidor HTTP

- **Paquete estándar JDK:** `com.sun.net.httpserver`
- **Clase principal:** [`ServidorHttp.java`](file:///d:/SistemasDistribuidos/16/ServidorHttp.java)
- **Componentes clave:**
  - `HttpServer.create(new InetSocketAddress(8000), 0)`: Crea el socket y define el backlog por defecto del SO.
  - `server.setExecutor(Executors.newFixedThreadPool(numHilos))`: Administra la concurrencia delegando conexiones entrantes a un pool fijo de hilos reutilizables.
  - `server.createContext("/tarea", new TareaHandler())`: Enruta las solicitudes hacia el manejador que ejecuta la lógica de negocio y el retardo.
  - `Thread.sleep(5000)`: Simula carga de trabajo / I/O bloqueante.
  - `HttpExchange`: Maneja el ciclo de solicitud/respuesta (código HTTP 200, cabeceras, longitud de bytes y flujo de salida).

---

## 🔬 Resultados y Conclusiones del Ejercicio 1

### Planteamiento
> *¿Qué cree que sucedería con los tiempos de respuesta promedio del servidor si en la línea `server.setExecutor(Executors.newFixedThreadPool(8));` se cambia el ocho por un uno?*

### Datos Experimentales Obtenidos

| Métrica | Pool de 8 Hilos (`newFixedThreadPool(8)`) | Pool de 1 Hilo (`newFixedThreadPool(1)`) |
| :--- | :---: | :---: |
| **Cliente 1 (`time`)** | `5.17 s` | `5.17 s` |
| **Cliente 2 (`time`)** | `5.26 s` | `10.17 s` |
| **Tiempo Promedio** | **`~5.21 s`** | **`~7.67 s`** (+47.2% de incremento promedio) |
| **Hilos Asignados** | `thread-5` y `thread-6` simultáneos | `thread-1` para ambos (reutilización secuencial) |
| **Concurrencia** | Paralelismo real | Serialización estricta por cola FIFO |

### Justificación Teórica
1. **Con 8 hilos:** Ambos clientes son despachados en hilos separados en paralelo. El tiempo de respuesta es independiente para cada uno e igual a la duración de la tarea (~5 s).
2. **Con 1 hilo:** El único hilo atiende al Cliente 1 durante 5 segundos; el Cliente 2 se encola en la `LinkedBlockingQueue` del ejecutor. Solo tras finalizar el primer cliente, el hilo toma la segunda petición y la procesa otros 5 segundos. Por ende, el Cliente 2 experimenta el doble de tiempo (5 s espera + 5 s ejecución = 10 s).

---

## 🔢 Ejercicio 2: Procesamiento de Petición POST en `/task`

### Requerimiento
Imprimir en consola del servidor la cantidad de bytes del cuerpo del mensaje HTTP y su contenido al recibir un POST en `/task`. Procesar la multiplicación de dos factores numéricos (e.g., `1757600,34334`) y retornar el resultado.

### Verificación Experimental
- **Comando Cliente:**
  ```bash
  curl --data '1757600,34334' localhost:8080/task
  ```
- **Salida del Cliente:**
  ```text
  El resultado de la multiplicación es 60345438400
  ```
- **Salida en el Servidor:**
  ```text
  Servidor escuchando en el puerto 8080
  Numero de bytes en el cuerpo del mensaje: 13
  Cuerpo del mensaje <1757600,34334>
  ```

---

## 📋 Ejercicio 3: Inspección de Encabezados HTTP (Headers) en `/task`

### Requerimiento
Imprimir en consola del servidor HTTP cuántos headers está recibiendo el endpoint `/task`, así como todos los pares `(key - value)` de la solicitud entrante.

### Fundamento Técnico
1. **API `exchange.getRequestHeaders()`:** Retorna la colección `Headers` que implementa `Map<String, List<String>>`.
2. **Cardinalidad `headers.size()`:** Indica cuántas claves distintas de encabezado viajan en la petición HTTP.
3. **Pares Clave-Valor (`entrySet()`):** Como HTTP permite valores múltiples para un encabezado (ej. varios tipos MIME en `Accept`), el valor asociado a cada clave es una lista de cadenas (`List<String>`).

---

## ⏱️ Ejercicio 4: Medición de Tiempo y Header Personalizado `X-Debug`

### Requerimiento
Si la petición entrante incluye el header `X-Debug: true`, calcular el tiempo total de procesamiento con `System.nanoTime()`, formatearlo en nanosegundos, segundos y milisegundos según el formato especificado:
`La operacion tomo %d nanosegundos = %d segundos con %d milisegundos.`
Añadirlo como header de respuesta HTTP `X-Debug-Info` e imprimirlo en consola del servidor.

### Verificación Experimental
- **Comando Cliente:**
  ```bash
  curl -v -H "X-Debug: true" --data '1757600,34334' localhost:8080/task
  ```
- **Salida del Cliente (fragmento verbose):**
  ```text
  < X-debug-info: La operacion tomo 1489200 nanosegundos = 0 segundos con 1 milisegundos.
  ```

---

## 🗂️ Índice de Archivos Generados

1. [`ServidorHttp.java`](file:///d:/SistemasDistribuidos/16/ServidorHttp.java): Código fuente Java con Ejercicios 1, 2, 3 y 4 integrados.
2. [`README.md`](file:///d:/SistemasDistribuidos/16/README.md): Guía rápida de uso y ejecución.
3. [`reporte_teams.md`](file:///d:/SistemasDistribuidos/16/reporte_teams.md): Redacción final y formato de entrega para Microsoft Teams.
4. [`MEMORY.md`](file:///d:/SistemasDistribuidos/16/MEMORY.md): Este archivo de memoria y referencia técnica.
5. [`ejecutar_servidor_8.bat`](file:///d:/SistemasDistribuidos/16/ejecutar_servidor_8.bat): Lanzador Windows para 8 hilos.
6. [`ejecutar_servidor_1.bat`](file:///d:/SistemasDistribuidos/16/ejecutar_servidor_1.bat): Lanzador Windows para 1 hilo.
7. [`lanzar_2_clientes.sh`](file:///d:/SistemasDistribuidos/16/lanzar_2_clientes.sh): Script bash para pruebas simultáneas con `time curl`.
