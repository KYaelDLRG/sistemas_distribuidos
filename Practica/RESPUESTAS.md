# Respuestas a las Preguntas y Ejercicios de la Práctica
**Materia:** Sistemas Distribuidos  
**Tema:** Cliente HTTP Moderno en Java (Java 11+), Patrón Builder y Recorrido de Estructuras

---

## 1. Análisis Teórico y Preguntas Fundamentales

### Pregunta 1: ¿Por qué se utiliza el patrón Builder para crear objetos `HttpClient` y `HttpRequest` en lugar de constructores tradicionales con `new`?
**Respuesta:**
- **Evita constructores telescópicos:** Si se usara un constructor como `new HttpClient(null, null, null, 0, false, null)`, el programador estaría obligado a conocer y pasar valores para todos los parámetros, incluso los que no necesita modificar.
- **Previene errores de posición y tipo:** Cuando existen múltiples parámetros del mismo tipo (por ejemplo, varios `String` o enteros), es muy fácil intercambiar argumentos accidentalmente sin que el compilador detecte el error.
- **Mantenibilidad y evolución de la API:** Si en futuras versiones de Java se añaden nuevas opciones de configuración, un constructor tradicional obligaría a crear sobrecargas infinitas o rompería la compatibilidad hacia atrás. Con el patrón Builder, solo se agrega un nuevo método encadenable sin romper el código existente.
- **Inmutabilidad y validación:** El método final `.build()` valida que todos los parámetros sean consistentes antes de instanciar un objeto inmutable, asegurando integridad en entornos concurrentes.

---

### Pregunta 2: ¿Cómo se configura `HttpClient` y qué implican los parámetros de versión y timeout?
**Respuesta:**
En el código base:
```java
private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(10))
        .build();
```
- **`.version(HttpClient.Version.HTTP_1_1)`:** Fuerza al cliente a negociar la conexión utilizando el protocolo HTTP/1.1 (a diferencia del valor por defecto en Java 11 que intenta HTTP/2 y degrada a HTTP/1.1 si no está soportado).
- **`.connectTimeout(Duration.ofSeconds(10))`:** Establece el tiempo máximo que el cliente esperará para establecer la conexión TCP con el servidor remoto. Si el servidor no responde dentro de los 10 segundos, se dispara una excepción `HttpConnectTimeoutException`.

---

### Pregunta 3: ¿Qué función cumple el servicio `https://httpbin.org/get` y cómo se configura la petición `HttpRequest`?
**Respuesta:**
- **Servicio `httpbin.org`:** Es un servicio de prueba y depuración tipo "eco" (*mirror/echo*). Al recibir una petición GET, responde con un documento JSON que refleja exactamente los datos que le fueron enviados: dirección IP pública de origen (`origin`), encabezados HTTP (`headers`), parámetros de consulta (`args`) y URL consultada (`url`).
- **Configuración de la petición:**
```java
HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create("https://httpbin.org/get"))
        .setHeader("User-Agent", "Java 11 HttpClient Bot")
        .build();
```
  - Define el método HTTP como `GET`.
  - Establece la URI de destino mediante `URI.create(...)`.
  - Agrega el encabezado personalizado `User-Agent: Java 11 HttpClient Bot`, lo que permite identificar el cliente ante el servidor.

---

### Pregunta 4: ¿Cómo opera el método `send()` y cuál es el rol de `HttpResponse.BodyHandlers.ofString()`?
**Respuesta:**
```java
HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
```
- **Operación síncrona:** El método `send(...)` es **bloqueante**. El hilo que lo ejecuta se detiene por completo hasta que el servidor envía la respuesta o se cumple el timeout estipulado.
- **Rol de `BodyHandlers.ofString()`:** Actúa como un convertidor/traductor reactivo. Toma el flujo continuo de bytes binarios recibidos desde la red y los decodifica a una cadena de texto `String` (utilizando por defecto UTF-8 o el charset indicado en la cabecera `Content-Type`).
- **Objeto retornado (`HttpResponse<String>`):** Es un contenedor genérico inmutable que expone los datos de la respuesta:
  - `response.statusCode()`: Código de estado numérico (ej. 200).
  - `response.body()`: El cuerpo del mensaje convertido a `String`.
  - `response.headers()`: Los encabezados de respuesta encapsulados en `HttpHeaders`.

---

### Pregunta 5: ¿Por qué se declaran `IOException` e `InterruptedException` con `throws` en el método `main` en lugar de usar `try-catch`?
**Respuesta:**
- En aplicaciones de prueba o pedagógicas, envolver el código en bloques `try-catch` genera código verboso (*boilerplate*) que dificulta la lectura del flujo principal de red.
- `IOException` es requerida porque cualquier fallo físico en la red, caída del socket o timeout genera esta excepción comprobada (*checked exception*).
- `InterruptedException` es requerida porque el método `send()` es bloqueante y puede ser interrumpido si otro hilo solicita la cancelación de la operación.
- Declararlas en la firma `public static void main(String[] args) throws IOException, InterruptedException` permite delegar la captura a la JVM, manteniendo el código limpio y conciso.

---

## 2. Ejercicio 1: Análisis y Sustitución de Código

### Pregunta 6: Explicación de la línea de código `HttpHeaders headers = response.headers();`
**Respuesta:**
1. **Propósito:** Esta línea recupera el conjunto de todos los encabezados HTTP enviados por el servidor remoto dentro de la respuesta y los almacena en una variable de tipo `java.net.http.HttpHeaders`.
2. **Estructura Interna:** En el protocolo HTTP, un mismo encabezado puede aparecer múltiples veces (por ejemplo, múltiples encabezados `Set-Cookie` o valores repetidos). Por ello, `HttpHeaders` modela los datos internamente como un mapa de listas:
   $$\text{Map}\langle\text{String}, \text{List}\langle\text{String}\rangle\rangle$$
   - **Clave (`String`):** El nombre del encabezado (las consultas no son sensibles a mayúsculas/minúsculas).
   - **Valor (`List<String>`):** Una lista inmutable con todos los valores recibidos para ese encabezado.
3. **Métodos que ofrece:**
   - `headers.map()`: Expone la vista directa del mapa inmutable `Map<String, List<String>>`.
   - `headers.firstValue("Nombre")`: Retorna un `Optional<String>` con el primer valor encontrado.
   - `headers.allValues("Nombre")`: Retorna la lista con todos los valores asociados a la cabecera.

---

### Pregunta 7: Sustitución de `headers.map().forEach(...)` por un `Iterator`

#### Código Original (Enfoque Funcional con Lambda):
```java
headers.map().forEach((k, v) -> System.out.println(k + ":" + v));
```
*Utiliza el método `.forEach()` de la interfaz `Map` introducido en Java 8 junto con un `BiConsumer` implementado mediante una expresión lambda `(k, v) -> ...`.*

#### Código Sustituto (Enfoque Clásico con `Iterator`):
```java
Map<String, List<String>> mapHeaders = headers.map();
Iterator<Map.Entry<String, List<String>>> iterator = mapHeaders.entrySet().iterator();

while (iterator.hasNext()) {
    Map.Entry<String, List<String>> entry = iterator.next();
    String key = entry.getKey();
    List<String> values = entry.getValue();
    System.out.println(key + ":" + values);
}
```

#### Justificación del Cambio:
- Se obtiene la vista de entradas con `mapHeaders.entrySet()`.
- Se solicita un iterador explícito mediante `.iterator()`.
- Se itera elemento por elemento con el bucle `while (iterator.hasNext())`.
- Cada elemento es una entrada individual de tipo `Map.Entry<String, List<String>>`, permitiendo acceder por separado al nombre (`entry.getKey()`) y a la lista de valores asociados (`entry.getValue()`).

---

## 3. Evidencia de Ejecución del Ejercicio 1

### Comando ejecutado:
```powershell
javac Ejercicio1.java
java Ejercicio1
```

### Salida obtenida en consola:
```text
access-control-allow-credentials:[true]
access-control-allow-origin:[*]
connection:[keep-alive]
content-length:[244]
content-type:[application/json]
date:[Tue, 06 Oct 2026 14:44:38 GMT]
server:[gunicorn/19.9.0]

Status Code: 200
Response Body:
{
  "args": {}, 
  "headers": {
    "Host": "httpbin.org", 
    "User-Agent": "Java 11 HttpClient Bot", 
    "X-Amzn-Trace-Id": "Root=1-6ac50956-589187076cdde28129ae1b32"
  }, 
  "origin": "148.204.1.135", 
  "url": "https://httpbin.org/get"
}
```

---

## 4. Ejercicio 2: Copia a Mapa Mutable, Adición de Headers y Estudio de `forEach`

### Requerimiento del Ejercicio 2:
Retomar el código del ejercicio anterior y copiar el mapa `headers` recibido en un nuevo mapa, y posteriormente agregar los siguientes headers al nuevo mapa:
- `Set-Cookie: Max-Age=0`
- `Set-Cookie: id=123`
- `Set-Cookie: theme=dark`

Imprimir el contenido del nuevo mapa (manteniendo también la impresión del ejercicio 1) usando el método `forEach`. *(Pregunte a la IA sobre los casos en que puede usarse y cómo se usa)*.

---

### Análisis Técnico y Respuestas Teóricas

#### 1. ¿Por qué es necesario copiar `headers.map()` a un nuevo mapa?
- El mapa devuelto por `headers.map()` es una colección **inmutable/no modificable** (`Collections.unmodifiableMap`).
- Si intentamos ejecutar operaciones de mutación directa como `headers.map().put(...)`, la máquina virtual de Java lanzará de forma inmediata una excepción de tipo **`java.lang.UnsupportedOperationException`**.
- Por ello, es imperativo instanciar una nueva colección mutable (como `HashMap`) y transferir las entradas originales antes de insertar nuevas cabeceras o modificar valores.

#### 2. ¿En qué casos puede usarse el método `forEach` y cómo se usa?

`forEach` fue introducido en **Java 8** como parte de la integración del paradigma de programación funcional y la API de Streams. Su propósito es realizar una iteración interna (*internal iteration*) ejecutando una acción sobre cada elemento.

Existen dos variantes principales de `forEach` en las colecciones de Java:

##### A. `Map.forEach(BiConsumer<? super K, ? super V> action)`
- **Dónde se usa:** Directamente sobre instancias de `Map` (`HashMap`, `TreeMap`, etc.).
- **Cómo se usa:** Recibe una interfaz funcional `BiConsumer<T, U>`, es decir, una expresión lambda que acepta dos parámetros (la clave y el valor):
  ```java
  mapa.forEach((clave, valor) -> {
      System.out.println(clave + " -> " + valor);
  });
  ```
- **Ventaja:** Elimina la necesidad de escribir `map.entrySet()` o bucles `for (Map.Entry<...> entry : map.entrySet())`, haciendo el código más conciso, limpio y declarativo.

##### B. `Iterable.forEach(Consumer<? super T> action)`
- **Dónde se usa:** En cualquier colección que implemente la interfaz `Iterable` (como `List`, `Set`, `Queue`).
- **Cómo se usa:** Recibe un `Consumer<T>`, es decir, una expresión lambda con un único parámetro (el elemento individual):
  ```java
  lista.forEach(elemento -> System.out.println(elemento));
  // O con referencia a método:
  lista.forEach(System.out::println);
  ```

##### C. `Stream.forEach(Consumer<? super T> action)`
- **Dónde se usa:** Como operación terminal en un flujo (`Stream`).
- **Cómo se usa:** Procesa cada elemento tras aplicar filtros, mapeos o transformaciones previas:
  ```java
  mapa.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .forEach(entry -> System.out.println(entry.getKey() + ":" + entry.getValue()));
  ```

---

### Casos de uso recomendados para `forEach`:
1. **Operaciones de sólo lectura y efectos secundarios (*side effects*) limpios:** Impresión en consola, envío de métricas, serialización o notificación a observers.
2. **Código conciso y declarativo:** Reduce la sobrecarga de sintaxis tradicional de bucles imperativos.

### Casos donde NO debe usarse `forEach` (o donde es preferible un bucle tradicional):
1. **Modificación de la colección durante la iteración:** Modificar o eliminar elementos directamente dentro de un `forEach` provoca `ConcurrentModificationException`. Para eliminar elementos se debe usar `Iterator.remove()` o `collection.removeIf(...)`.
2. **Control de flujo con `break` o `continue`:** En una lambda dentro de `forEach`, un `return` solo equivale a un `continue` (pasa al siguiente elemento), y no es posible romper (`break`) la iteración prematuramente. Para detener la ejecución anticipada es preferible un bucle `for` clásico o un `Stream` con `anyMatch`/`takeWhile`.
3. **Manejo de excepciones comprobadas (*checked exceptions*):** Si la acción lanza excepciones comprobadas (como `IOException`), el bloque lambda obliga a capturarlas con `try-catch` interno, volviéndose engorroso.

---

## 5. Ejercicio 3: Ordenamiento Alfabético de Headers con Stream API (`entrySet`, `sorted`, `forEach`)

### Requerimiento del Ejercicio 3:
Retomar el código del ejercicio anterior y utilizar, además de `forEach`, los métodos `entrySet` y `sorted` de la interfaz `Stream` para imprimir los headers ordenados alfabéticamente por su nombre (llave).

---

### Análisis Técnico y Fundamentos de Streams

#### 1. ¿Cómo se encadenan los métodos solicitados?
```java
nuevoMapaHeaders.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> System.out.println(entry.getKey() + ":" + entry.getValue()));
```

1. **`nuevoMapaHeaders.entrySet()`**:
   - Obtiene una vista de tipo `Set<Map.Entry<String, List<String>>>`.
   - Cada elemento del conjunto encapsula la tupla compuesta por la clave (nombre del encabezado HTTP) y el valor (la lista de cadenas asociadas).

2. **`.stream()`**:
   - Convierte el `Set` en una secuencia de elementos computables (`Stream<Map.Entry<String, List<String>>>`).
   - Los Streams no almacenan datos, sino que transmiten elementos a través de un canal de operaciones intermedias y terminales.

3. **`.sorted(Map.Entry.comparingByKey())`**:
   - Es una **operación intermedia con estado (*stateful intermediate operation*)**.
   - **¿Por qué se necesita un comparador?** A diferencia de `String` o `Integer`, la interfaz `Map.Entry` **no implementa `Comparable`**. Si llamáramos a `.sorted()` sin argumentos, Java arrojaría un error en tiempo de compilación o un `ClassCastException` en tiempo de ejecución.
   - `Map.Entry.comparingByKey()` compara las entradas según su clave (`String`). Al utilizar `String.CASE_INSENSITIVE_ORDER`, garantiza un orden alfabético estricto de la 'A' a la 'Z' independientemente de si la cabecera comienza con mayúscula (`Set-Cookie`) o minúscula (`access-control...`).

4. **`.forEach(...)`**:
   - Es una **operación terminal (*terminal operation*)**.
   - Consume cada elemento del Stream resultante ya ordenado y ejecuta la acción indicada (imprimir en consola `entry.getKey() + ":" + entry.getValue()`).

---

### Comparación: ¿Por qué ordenar con Stream vs ordenar el Mapa directamente?
- Si quisiéramos mantener el mapa permanentemente ordenado en memoria, tendríamos que haber instanciado un `TreeMap` en lugar de un `HashMap`.
- Sin embargo, la ventaja de usar **Streams (`.stream().sorted()`)** es que no altera ni reconstruye la estructura original del `HashMap`. Permite obtener una visualización ordenada "al vuelo" de forma declarativa, funcional e inmutable.

---

### Evidencia de Ejecución del Ejercicio 3

#### Comandos:
```powershell
cd src
javac Ejercicio3.java
java Ejercicio3
```

#### Salida real obtenida en consola:
```text
--- Headers originales (usando Iterator) ---
access-control-allow-credentials:[true]
access-control-allow-origin:[*]
connection:[keep-alive]
content-length:[244]
content-type:[application/json]
date:[Tue, 06 Oct 2026 15:14:40 GMT]
server:[gunicorn/19.9.0]

--- Mapa modificado con Set-Cookie (sin ordenar, usando forEach) ---
Set-Cookie:[Max-Age=0, id=123, theme=dark]
access-control-allow-credentials:[true]
access-control-allow-origin:[*]
connection:[keep-alive]
content-length:[244]
content-type:[application/json]
date:[Tue, 06 Oct 2026 15:14:40 GMT]
server:[gunicorn/19.9.0]

--- Headers ordenados alfabéticamente por llave (Stream sorted y forEach) ---
Set-Cookie:[Max-Age=0, id=123, theme=dark]
access-control-allow-credentials:[true]
access-control-allow-origin:[*]
connection:[keep-alive]
content-length:[244]
content-type:[application/json]
date:[Tue, 06 Oct 2026 15:14:40 GMT]
server:[gunicorn/19.9.0]

Status Code: 200
Response Body:
{
  "args": {}, 
  "headers": {
    "Host": "httpbin.org", 
    "User-Agent": "Java 11 HttpClient Bot", 
    "X-Amzn-Trace-Id": "Root=1-6ac51060-3178b9a31902b8b41c60bda6"
  }, 
  "origin": "148.204.1.135", 
  "url": "https://httpbin.org/get"
}
```

