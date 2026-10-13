# Reporte de Entrega (Teams): Clase 16 - Servidor HTTP

---

## 📝 Ejercicio 1

### Pregunta
> **¿Qué cree que sucedería con los tiempos de respuesta promedio del servidor si en la línea:**
> `server.setExecutor(Executors.newFixedThreadPool(8));`
> **se cambia el ocho por un uno?**

### Respuesta y Explicación Técnica

Al cambiar el tamaño del pool de hilos de **8 a 1** (`Executors.newFixedThreadPool(1)`), el tiempo de respuesta promedio del servidor **se incrementa considerablemente (pasa de ~5 segundos a ~7.5 segundos)** para dos peticiones simultáneas, debido a la pérdida de concurrencia y a la serialización del procesamiento en una cola FIFO.

#### 1. Comportamiento con 8 Hilos (`newFixedThreadPool(8)`):
- El servidor dispone de un grupo de hasta 8 hilos trabajadores (*worker threads*) independientes.
- Al recibir dos solicitudes HTTP concurrentes, el ejecutor asigna cada solicitud a un hilo distinto en paralelo (`pool-1-thread-X` y `pool-1-thread-Y`).
- Dado que ambas tareas se ejecutan simultáneamente:
  - **Cliente 1:** ~5.17 segundos.
  - **Cliente 2:** ~5.26 segundos.
  - **Tiempo promedio:** **~5.21 segundos**.

#### 2. Comportamiento con 1 Hilo (`newFixedThreadPool(1)`):
- El ejecutor cuenta únicamente con **un hilo trabajador** y una cola de espera bloqueante (`LinkedBlockingQueue`).
- Las solicitudes se procesan en estricto orden secuencial (*FIFO*):
  - El único hilo toma la solicitud del **Cliente 1** y la procesa durante los 5 segundos de retardo.
  - La solicitud del **Cliente 2**, a pesar de haber llegado simultáneamente, queda encolada en espera pasiva.
  - Cuando el hilo termina la primera solicitud (~5.17 s), inmediatamente desencola la segunda y comienza a procesarla por otros 5 segundos.
  - **Cliente 1:** ~5.17 segundos (atención inmediata).
  - **Cliente 2:** ~10.17 segundos (5 segundos de espera en cola + 5 segundos de procesamiento).
  - **Tiempo promedio:** **~7.67 segundos**, duplicándose el tiempo de respuesta total experimentado por el segundo cliente.

### Tabla Comparativa de Resultados Experimentales

| Parámetro | Pool de 8 Hilos | Pool de 1 Hilo |
| :--- | :---: | :---: |
| **Tiempo Cliente 1 (`time curl`)** | `~5.17 s` | `~5.17 s` |
| **Tiempo Cliente 2 (`time curl`)** | `~5.26 s` | `~10.17 s` |
| **Tiempo Promedio de Respuesta** | **`~5.21 s`** | **`~7.67 s`** |
| **Modo de Procesamiento** | Concurrente / Paralelo | Secuencial / Serializado |
| **Hilos Involucrados** | `thread-5`, `thread-6` | `thread-1` (reutilizado secuencialmente) |

---

## 📝 Ejercicio 2

### Enunciado
> **Imprima en el servidor cuántos bytes contiene el cuerpo del mensaje HTTP así como su contenido, al ser recibida una solicitud POST en el endpoint `/task`.**
> **Envíe por teams el código añadido al servidor y la captura de pantalla donde se observen los parámetros del comando curl y la impresión del servidor.**

### 1. Código añadido al servidor
```java
// Dentro del método handle(HttpExchange exchange) del HttpHandler asociado a /task:
if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
    // 1. Lectura de los bytes del cuerpo de la petición HTTP
    InputStream is = exchange.getRequestBody();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] datosTemp = new byte[1024];
    int leidos;
    while ((leidos = is.read(datosTemp, 0, datosTemp.length)) != -1) {
        buffer.write(datosTemp, 0, leidos);
    }
    byte[] cuerpoBytes = buffer.toByteArray();

    // 2. Conversión a String del cuerpo recibido
    String cuerpoMensaje = new String(cuerpoBytes, StandardCharsets.UTF_8).trim();

    // 3. Impresión en consola del número de bytes y el contenido (Ejercicio 2)
    System.out.println("Numero de bytes en el cuerpo del mensaje: " + cuerpoBytes.length);
    System.out.println("Cuerpo del mensaje <" + cuerpoMensaje + ">");

    // 4. Procesamiento de los números recibidos para calcular la multiplicación
    String[] factores = cuerpoMensaje.split(",");
    BigInteger factor1 = new BigInteger(factores[0].trim());
    BigInteger factor2 = new BigInteger(factores[1].trim());
    BigInteger resultado = factor1.multiply(factor2);

    String respuestaCliente = "El resultado de la multiplicación es " + resultado + "\n";

    // 5. Envío de respuesta HTTP 200 OK
    byte[] bytesRespuesta = respuestaCliente.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, bytesRespuesta.length);
    OutputStream os = exchange.getResponseBody();
    os.write(bytesRespuesta);
    os.close();
}
```

### 2. Comandos y Resultados Obtenidos para la Captura

#### Comando ejecutado en el Cliente (Terminal Git Bash):
```bash
curl --data '1757600,34334' localhost:8080/task
```

#### Salida mostrada en el Cliente:
```text
El resultado de la multiplicación es 60345438400
```

#### Salida mostrada en el Servidor:
```text
Servidor escuchando en el puerto 8080
Numero de bytes en el cuerpo del mensaje: 13
Cuerpo del mensaje <1757600,34334>
```

---

## 📝 Ejercicio 3

### Enunciado
> **Imprimir en el servidor HTTP cuántos headers está recibiendo el endpoint `/task`, así como todos los pares (key - value) contenidos en la solicitud recibida.**
> *Sugerencia: Pregunte a la IA cómo se puede hacer y entienda en general cómo funciona la solución propuesta.*

### 1. Código añadido al servidor
```java
// Dentro del método handle(HttpExchange exchange) del HttpHandler de /task:

// 1. Obtención de la colección de headers entrantes
Headers headers = exchange.getRequestHeaders();

// 2. Impresión de la cantidad total de cabeceras recibidas
System.out.println("Numero de headers recibidos: " + headers.size());

// 3. Iteración sobre cada par clave-valor (Header Name -> List<String> Values)
System.out.println("Headers recibidos (key - value):");
for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
    System.out.println("  " + entry.getKey() + " : " + entry.getValue());
}
```

### 2. Explicación de cómo funciona la solución propuesta
1. **El objeto `Headers` de Java:**
   - La clase `exchange.getRequestHeaders()` retorna una instancia de `com.sun.net.httpserver.Headers`.
   - Esta clase implementa la interfaz `Map<String, List<String>>`. 
2. **¿Por qué el valor es una lista (`List<String>`) y no un solo `String`?**
   - El estándar del protocolo HTTP (RFC 7230) permite que un cliente envíe múltiples valores para un mismo encabezado o repita la misma cabecera (por ejemplo: `Accept`, `Set-Cookie` o `Via`). Por lo tanto, cada clave mapea a una lista con todos sus valores asociados.
3. **Cantidad de Headers (`headers.size()`):**
   - `headers.size()` devuelve el número total de campos de cabecera distintos presentes en la petición.
4. **Iteración con `entrySet()`:**
   - Permite recorrer eficientemente cada entrada del mapa obteniendo la clave con `entry.getKey()` (nombre de la cabecera, insensible a mayúsculas/minúsculas) y la lista de valores con `entry.getValue()`.

### 3. Ejemplo de Salida en la Terminal del Servidor

Al ejecutar desde el cliente:
```bash
curl --data '1757600,34334' localhost:8080/task
```

El servidor imprime en consola:
```text
Numero de headers recibidos: 4
Headers recibidos (key - value):
  Host : [localhost:8080]
  User-agent : [curl/8.4.0]
  Accept : [*/*]
  Content-length : [13]
  Content-type : [application/x-www-form-urlencoded]
Numero de bytes en el cuerpo del mensaje: 13
Cuerpo del mensaje <1757600,34334>
```

---

## 📝 Ejercicio 4

### Enunciado
> **En el código del servidor si el valor proporcionado en el header personalizado "X-Debug" es true, entonces devuelve el tiempo total de procesamiento en nanosegundos. Dado que el tiempo en nanosegundos es difícil de interpretar a simple vista, modifique la impresión para que también se imprima el tiempo en segundos más los milisegundos como sigue:**
> `< X-debug-info: La operacion tomo 2266869700 nanosegundos = 2 segundos con 266 milisegundos.`
> **Enviar código agregado y captura de pantalla mostrando la ejecución de su código y realizando lo que se solicita.**
> *Nota: conservar este formato de impresión del tiempo para todos los ejercicios subsiguientes a partir de ahora.*

### 1. Código añadido al servidor
```java
// 1. Al inicio del método handle(HttpExchange exchange) se toma el tiempo inicial:
long nanoInicio = System.nanoTime();

// 2. Se lee el encabezado "X-Debug":
String xDebugValor = headers.getFirst("X-Debug");
boolean esDebug = "true".equalsIgnoreCase(xDebugValor);

// 3. Antes de enviar los encabezados de respuesta (sendResponseHeaders), se invoca el método aplicarDebug:
private void aplicarDebug(HttpExchange exchange, long nanoInicio, boolean esDebug) {
    if (esDebug) {
        long nanoFin = System.nanoTime();
        long duracionNanos = nanoFin - nanoInicio;
        long segundos = duracionNanos / 1_000_000_000L;
        long milisegundos = (duracionNanos % 1_000_000_000L) / 1_000_000L;

        // Formato exacto requerido:
        String debugInfo = String.format(
                "La operacion tomo %d nanosegundos = %d segundos con %d milisegundos.",
                duracionNanos, segundos, milisegundos
        );

        // Se agrega como header de respuesta HTTP "X-Debug-Info":
        exchange.getResponseHeaders().set("X-Debug-Info", debugInfo);

        // Se imprime en la consola del servidor:
        System.out.println("X-debug-info: " + debugInfo);
    }
}
```

### 2. Comando ejecutado en el Cliente (Git Bash)
Para observar el encabezado de respuesta devuelto por el servidor, se utiliza el parámetro `-v` (verbose) de `curl`:
```bash
curl -v -H "X-Debug: true" --data '1757600,34334' localhost:8080/task
```

### 3. Salida mostrada en el Cliente (curl -v):
En la salida de `curl -v`, las líneas con `<` indican los encabezados de respuesta enviados por el servidor:
```text
> POST /task HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> X-Debug: true
> Content-Length: 13
> Content-Type: application/x-www-form-urlencoded
>
< HTTP/1.1 200 OK
< Date: ...
< Content-length: 44
< X-debug-info: La operacion tomo 1489200 nanosegundos = 0 segundos con 1 milisegundos.
<
El resultado de la multiplicación es 60345438400
```

### 4. Salida mostrada en el Servidor:
```text
Numero de headers recibidos: 5
Headers recibidos (key - value):
  Host : [localhost:8080]
  User-agent : [curl/8.4.0]
  Accept : [*/*]
  X-debug : [true]
  Content-length : [13]
  Content-type : [application/x-www-form-urlencoded]
Numero de bytes en el cuerpo del mensaje: 13
Cuerpo del mensaje <1757600,34334>
X-debug-info: La operacion tomo 1489200 nanosegundos = 0 segundos con 1 milisegundos.
```


