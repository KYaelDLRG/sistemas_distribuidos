# MEMORY: Práctica Clase 22 - Servidor Despachador (Dispatcher), Procesamiento Distribuido, Cliente HTTP Asíncrono y NGINX Reverse Proxy

Este documento preserva la memoria técnica, fundamentación teórica, decisiones de diseño arquitectónico, implementación de código, procedimientos de compilación y ejecución experimental de la **Clase 22: Servidor Despachador (Dispatcher / Master), Procesamiento Distribuido con Workers Asíncronos y NGINX Reverse Proxy** de la materia **Sistemas Distribuidos**.

---

## 📌 Contexto General del Proyecto

- **Materia:** Sistemas Distribuidos
- **Profesor:** Dr. Ukranio Contreras Coronilla (ESCOM - IPN)
- **Tema:** Arquitectura Master-Worker (Coordinador / Despachador y Nodos Trabajadores), Cliente HTTP Asíncrono (`CompletableFuture`), Descomposición de Carga Criptográfica (26 subtareas SHA-256), Balanceo Dinámico con Detección Temprana (*Short-Circuiting*), Servidor HTTP Despachador (`/dispatcher`), NGINX como Reverse Proxy y Frontend Web.
- **Ruta del Proyecto:** [`d:\SistemasDistribuidos\C22`](file:///d:/SistemasDistribuidos/C22)
- **Repositorio:** `KYaelDLRG/sistemas_distribuidos` (rama `main`)
- **Dependencias de Clases Anteriores:**
  - **Clase 21 ([`D:\SistemasDistribuidos\21`](file:///d:/SistemasDistribuidos/21)):** Provee el servidor de cómputo [`ServidorHttp.java`](file:///d:/SistemasDistribuidos/21/ServidorHttp.java) con el endpoint `/task`, encargado de realizar la búsqueda exhaustiva por fuerza bruta de contraseñas de 5 letras para un hash SHA-256 dado un prefijo alfabético (`<hashObjetivo>,<letra>`).
  - **Clase 19 ([`D:\SistemasDistribuidos\19`](file:///d:/SistemasDistribuidos/19)):** Provee la arquitectura del cliente HTTP asíncrono basada en [`WebClient.java`](file:///d:/SistemasDistribuidos/19/networking/WebClient.java) (`java.net.http.HttpClient` con HTTP/1.1 y *Connection Pooling*), [`Aggregator.java`](file:///d:/SistemasDistribuidos/19/Aggregator.java) (`CompletableFuture` e `isDone()`) y [`Application.java`](file:///d:/SistemasDistribuidos/19/Application.java).

---

## ⚙️ Entorno de Desarrollo y Configuración del Sistema

| Parámetro | Detalle en el Sistema |
| :--- | :--- |
| **Sistema Operativo** | Windows 11 (PowerShell / CMD / Git Bash) |
| **Compilador (`javac`)** | Amazon Corretto JDK 21.0.4 (`C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin\javac.exe`) |
| **Runtime prioritario en PATH** | Java SE 8 (`1.8.0_441`) / Amazon Corretto JDK 21 |
| **Requerimiento HTTP Client** | Java 11+ (Satisfecho con Amazon Corretto JDK 21 para `java.net.http.HttpClient`) |
| **Servidor Web / Reverse Proxy** | NGINX para Windows |
| **Herramientas de Diagnóstico** | `curl` (versión nativa 8.13.0 de Windows), Navegador Web (Chrome / Edge / Firefox) |

---

## 🏗️ Arquitectura Global del Sistema Distribuido

El sistema implementa una arquitectura distribuida de tres capas orientada a la resolución paralela de problemas de alta intensidad computacional (ataque de fuerza bruta SHA-256 sobre contraseñas de 5 caracteres en minúsculas).

```mermaid
graph TD
    User["👤 Usuario / Navegador Web"] -->|1. Ingresa Hash SHA-256 (HTTP GET/POST :80)| NGINX["🌐 NGINX (Reverse Proxy)\nPuerto 80 / 8080"]
    
    subgraph Capa de Orquestación ["Capa de Orquestación y Despacho (Master)"]
        NGINX -->|2. Proxy Pass /dispatcher| Dispatcher["🖥️ Servidor Despachador (WebServer)\nPuerto 8080 /dispatcher"]
        Dispatcher -->|3. Crea Tareas y Gestiona| Aggregator["⚙️ Aggregator\n(Cliente Asíncrono)"]
    end

    subgraph Capa de Cómputo ["Capa de Cómputo Distribuido (Workers)"]
        Aggregator -->|4a. POST /task 'hash,a'| Worker1["💻 Worker 1 (ServidorHttp)\nhttp://localhost:8081/task"]
        Aggregator -->|4b. POST /task 'hash,b'| Worker2["💻 Worker 2 (ServidorHttp)\nhttp://localhost:8082/task"]
        Worker1 -.->|5a. 'NULL'| Aggregator
        Worker2 -.->|5b. 'La contraseña es: escom'| Aggregator
    end

    Aggregator -->|6. Detiene envío de tareas restantes y consolida| Dispatcher
    Dispatcher -->|7. Retorna contraseña + X-Debug-Info| NGINX
    NGINX -->|8. Despliega resultado al usuario| User
```

### Roles de los Componentes:

1. **Cliente Usuario (Navegador Web / `curl`):**
   Envía el hash objetivo (ej. `de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f`) sin necesidad de conocer la topología ni el número de servidores trabajadores.
2. **NGINX (Reverse Proxy):**
   Actúa como fachada pública. Sirve el contenido estático de la interfaz web (`index.html`) y redirige de forma transparente las peticiones dinámicas dirigidas a `/dispatcher` hacia el servidor despachador en Java.
3. **Servidor Despachador (Dispatcher Server / Master):**
   Actúa como **servidor** ante el cliente y como **cliente asíncrono** ante los nodos trabajadores.
   - Descompone el problema en **26 tareas independientes** (una por cada letra inicial de `'a'` a `'z'`).
   - Envía las tareas concurrentemente mediante `WebClient` y `CompletableFuture`.
   - Implementa **terminación temprana (*Short-Circuiting*)**: en cuanto un Worker responde con la contraseña encontrada, cancela o evita el envío de las tareas restantes.
   - Calcula el tiempo total de procesamiento con `System.nanoTime()` y retorna la cabecera `X-debug-info`.
4. **Nodos Trabajadores (Workers / Slaves):**
   Dos servidores HTTP independientes (`Worker 1` en el puerto 8081 y `Worker 2` en el puerto 8082), ejecutando el código de la Clase 21 ([`ServidorHttp.java`](file:///d:/SistemasDistribuidos/21/ServidorHttp.java)).
   - Cada Worker expone el endpoint `/task`.
   - Recibe la cadena `"<hashObjetivo>,<letraInicial>"`.
   - Realiza la búsqueda exhaustiva en un espacio de $26^4 = 456,976$ combinaciones (desde `letra + "aaaa"` hasta `letra + "zzzz"`).
   - Responde `"NULL"` si no la encuentra, o `"La contraseña es: <palabra>\n"` si coincide el hash SHA-256.

---

## 💻 Ejercicio 1: Cliente Asíncrono Despachador en Consola

### 1. Requerimiento Oficial
> *"Modifique el código visto en la práctica del cliente HTTP asíncrono para que en la clase `Application` se generen las 26 tareas, donde cada tarea contiene el hash y la letra que se envía a los dos workers con endpoint `/task` que fue programado en la clase anterior. También modifique la clase `Aggregator` para que en el momento que uno de los workers encuentre la contraseña se dejen de enviar más tareas a los workers y se responda a la clase `Application` con el arreglo de todas las tareas realizadas hasta ese momento junto con sus resultados."*

---

### 2. Modificaciones en la Clase `Application.java`

- **Endpoints de los Workers:** Se configuran las URLs hacia los dos servidores trabajadores:
  ```java
  private static final String WORKER_ADDRESS_1 = "http://localhost:8081/task";
  private static final String WORKER_ADDRESS_2 = "http://localhost:8082/task";
  ```
- **Generación de las 26 tareas:** Se itera desde el carácter `'a'` hasta el `'z'`, concatenando el hash objetivo (`de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f`) con cada letra:
  ```java
  String hashObjetivo = "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f";
  List<String> listaTareas = new ArrayList<>();
  for (char c = 'a'; c <= 'z'; c++) {
      listaTareas.add(hashObjetivo + "," + c);
  }
  ```
- **Impresión inicial:** Se imprimen las 26 tareas enumeradas (`Tarea 0 :...`, `Tarea 1 :...`, ..., `Tarea 25 :...`).
- **Invocación del `Aggregator`:** Se llama a `aggregator.sendTasksToWorkers(...)` y se imprime el resultado encontrado junto con las tareas ejecutadas.

---

### 3. Modificaciones en la Clase `Aggregator.java` (Lógica de Detención Temprana)

En la Clase 19, `Aggregator` enviaba incondicionalmente todas las tareas hasta agotar la lista. Para la Clase 22, se implementa la lógica de **parada temprana**:

1. **Bandera de éxito:** Se introduce una variable booleana `boolean encontrada = false;` y una cadena `String resultadoFinal = null;`.
2. **Evaluación de la respuesta:** Cuando `futures[j].isDone()` es verdadero y se extrae la respuesta mediante `futures[j].join()`:
   - Se limpia la respuesta con `.trim()`.
   - Si la respuesta **no es `"NULL"`** (es decir, contiene `"La contraseña es:"`), se activa `encontrada = true;` y se guarda la solución.
3. **Cese de asignación:** En las siguientes iteraciones del bucle de sondeo, si `encontrada == true`, **ya no se despachan nuevas tareas** a los Workers (`nextTaskIndex` no se incrementa y no se invoca `webClient.sendTask(...)`).
4. **Retorno de resultados consolidados:** Se devuelve únicamente la lista de tareas que efectivamente fueron enviadas y procesadas hasta el momento del hallazgo.

---

### 4. Estructura de Directorios y Procedimiento de Compilación

La estructura requerida para la compilación y empaquetado del cliente es:

```text
servidor_despachador/
├── classes/
└── source/
    ├── Aggregator.java
    ├── Application.java
    └── networking/
        └── WebClient.java
```

#### Comandos de Compilación y Ejecución:
1. Posicionarse en la carpeta `source/`:
   ```bash
   cd source
   ```
2. Compilar hacia la carpeta `classes/`:
   ```bash
   javac -d ../classes *.java networking/*.java
   ```
3. Cambiarse a la carpeta `classes/`:
   ```bash
   cd ../classes
   ```
4. Crear el archivo JAR con las clases compiladas:
   ```bash
   jar cf Application.jar Application.class Aggregator.class networking/WebClient.class
   ```
5. Ejecutar la aplicación:
   ```bash
   java -cp Application.jar Application
   ```

---

### 5. Salida Experimental Esperada en Consola

```text
ukranio@HUAWEI-R5:~/SD_2026_ENERO/Procesamiento_Distribuido/servidor_despachador/classes$ java -cp Application.jar Application
Las tareas a resolver son las siguientes:
Tarea 0 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,a
Tarea 1 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,b
Tarea 2 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,c
Tarea 3 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,d
Tarea 4 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e
Tarea 5 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,f
Tarea 6 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,g
...
Tarea 25 :de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,z

el resultado fué: La contraseña es: escom

Para la tarea de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,a NULL
Para la tarea de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,b NULL
Para la tarea de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,c NULL
Para la tarea de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,d NULL
Para la tarea de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e La contraseña es: escom
```

> [!NOTE]
> **Comportamiento Clave de la Salida:**
> Nótese que a pesar de que se generaron 26 tareas en la lista (`a` a `z`), **únicamente se enviaron y reportaron 5 tareas** (de la `a` a la `e`). Al encontrar que la tarea `e` arrojó `"La contraseña es: escom"`, el `Aggregator` suprimió el despacho de las tareas `f` hasta la `z`, ahorrando el procesamiento innecesario de más de 9.5 millones de combinaciones de hashes.

---

## 🌐 Ejercicio 2: Servidor Web HTTP Despachador (`WebServer.java`)

### 1. Requerimiento Oficial
> *"Ahora substituya la clase `Application` por la clase `WebServer` de nuestro servidor web HTTP, de manera que incorpore el endpoint `/dispatcher` en el cual se recibirá el `hashObjetivo` y la letra, posteriormente creará un objeto `Aggregator`, la lista de 26 tareas y su envío a los Workers. Finalmente esperará la solución tal y como estaba hecho en la clase `Application` del ejercicio anterior y devolverá la respuesta final al cliente que acceda al endpoint `/dispatcher`.*
>
> *Adjunte códigos y una captura de pantalla como la siguiente mostrando que funciona el endpoint `/dispatcher` así como el tiempo total de procesamiento. Observe que, aunque teóricamente debería de reducirse a la mitad el tiempo, esto no es así pues no estamos ejecutando los workers en computadoras físicamente independientes."*

---

### 2. Implementación de `WebServer.java` (Endpoint `/dispatcher`)

El servidor HTTP despachador:
1. Escucha en el puerto `8080` con un grupo de hilos (`Executors.newFixedThreadPool(8)`).
2. Registra el contexto `/dispatcher` manejado por `DispatcherHandler`.
3. Al recibir una petición `POST`:
   - Registra la marca de tiempo inicial con `long startTime = System.nanoTime();`.
   - Lee el cuerpo de la petición (que contiene el hash a resolver).
   - Genera las 26 tareas para el abecedario.
   - Invoca a `Aggregator` para coordinar el trabajo entre `http://localhost:8081/task` y `http://localhost:8082/task`.
   - Espera el resultado consolidado.
   - Si el cliente envió la cabecera `X-Debug: true`, calcula los nanosegundos transcurridos y añade la cabecera de respuesta:
     ```text
     X-debug-info: La operacion tomo X nanosegundos = Y segundos con Z milisegundos
     ```
   - Responde con código HTTP 200 y el cuerpo `La contraseña es: <palabra>\n`.

---

### 3. Verificación Experimental con `curl -v`

#### Comando ejecutado:
```bash
curl -v --header 'X-Debug:true' --data 'de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e' localhost:8080/dispatcher
```

#### Salida de referencia (Imagen del Profesor):
```text
ukranio@HUAWEI-R5:~$ curl -v --header 'X-Debug:true' --data 'de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e' localhost:8080/dispatcher
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /dispatcher HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.5.0
> Accept: */*
> X-Debug:true
> Content-Length: 66
> Content-Type: application/x-www-form-urlencoded
> 
< HTTP/1.1 200 OK
< Date: Fri, 20 Mar 2026 17:40:00 GMT
< X-debug-info: La operacion tomo 34362107495 nanosegundos = 34 segundos con 362 milisegundos
< Content-Length: 25
< 
La contraseña es: escom
* Connection #0 to host localhost left intact
```

#### Salida experimental obtenida en el sistema local:
```text
curl.exe -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e" http://localhost:8080/dispatcher
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /dispatcher HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.13.0
> Accept: */*
> X-Debug:true
> Content-Length: 66
> Content-Type: application/x-www-form-urlencoded
> 
< HTTP/1.1 200 OK
< Access-control-allow-headers: Content-Type, X-Debug
< Date: Tue, 20 Oct 2026 04:52:09 GMT
< Access-control-allow-methods: POST, GET, OPTIONS
< Access-control-allow-origin: *
< X-debug-info: La operacion tomo 46564647800 nanosegundos = 46 segundos con 564 milisegundos
< Content-length: 25
< 
La contraseña es: escom
* Connection #0 to host localhost left intact
```


---

### 4. Fundamentación Teórica: ¿Por qué el tiempo NO se reduce exactamente a la mitad al usar 2 Workers locales?

En la formulación matemática ideal de la **Ley de Amdahl**, si se dispone de $P$ procesadores idénticos trabajando en una tarea perfectamente paralelizable sin dependencias, el factor de aceleración (*Speedup*) esperado sería:

$$S(P) = \frac{T_1}{T_P} = P \implies S(2) = 2.0 \quad (T_2 = \frac{T_1}{2})$$

Sin embargo, en el experimento real ejecutado en una única máquina:
1. **Contención de Recursos de CPU y Caché Compartida:**
   Ambos procesos Worker compiten intensivamente por los ciclos de reloj de la misma CPU física y comparten la memoria caché L2/L3. El cálculo masivo de SHA-256 (`MessageDigest`) genera constantes desalojos de líneas de caché (*cache misses*) al alternar contextos.
2. **Sobrecarga de Comunicación de Red Local (Loopback TCP/IP Overhead):**
   El despachador y los dos workers se comunican a través del stack de red virtual (`localhost`). Cada sub-tarea genera serialización HTTP, buffers de sockets TCP, *three-way handshakes* o reutilización de conexiones, y parsing de cabeceras en el despachador y en cada worker.
3. **Planificación de Hilos del Sistema Operativo (*Context Switching*):**
   Al haber múltiples procesos en ejecución concurrente (Despachador Java + Worker 8081 Java + Worker 8082 Java + cliente `curl`), el planificador del kernel (*scheduler*) incurre en sobrecostos significativos por conmutación de contexto e interrupciones.
4. **Comportamiento Térmico y Frecuencia de la CPU (*Thermal Throttling*):**
   Al someter varios núcleos de la laptop a carga máxima continua del 100%, la temperatura del silicio se eleva, provocando que los mecanismos de gestión energética reduzcan la frecuencia sostenida (*boost clock*) del microprocesador.

Por estos factores físicos y arquitectónicos, el Speedup real obtenido se sitúa comúnmente en un rango de $1.3\times$ a $1.7\times$, en lugar del valor teórico ideal de $2.0\times$. La reducción estricta a la mitad solo es alcanzable cuando los workers residen en **computadoras físicamente independientes** interconectadas por una red dedicada.

---

## 🔀 Ejercicio 3: Incorporación de NGINX como Reverse Proxy e Interfaz Web

### 1. Requerimiento Oficial
> *"Incorpore a NGINX como Reverse Proxy, quien va a interactuar con el navegador web que ejecuta el usuario y pueda nuestro sistema de procesamiento distribuido recibir del usuario en una interfaz Web el hash para buscar la contraseña y devolverla junto con el tiempo de procesamiento en el navegador. Adjunte captura de pantalla donde se visualice al mismo tiempo el navegador con una consulta y la terminal donde se ejecuta el servidor despachador.*
>
> *Es importante notar que no sería posible que NGINX haga el papel de un servidor despachador. Lo mas cercano que podría hacer es balancear la carga, pero no divide una tarea en subtareas ni determina si la tarea ya se ha completado ni hace ningún procesamiento adicional con las tareas resueltas para obtener un único resultado. Estas son tareas del programador de un sistema que realiza el procesamiento distribuido."*

---

### 2. Configuración de NGINX (`nginx.conf`)

NGINX se configura para escuchar en el puerto `8088` (para evitar colisión con servicios locales existentes como Apache en el puerto 80), sirviendo los archivos estáticos de la interfaz web en la raíz `/` y redirigiendo de forma transparente las peticiones dirigidas a `/dispatcher` hacia el Servidor Despachador Java en el puerto `8080`:

```nginx
worker_processes  1;

error_log  logs/error.log;
pid        logs/nginx.pid;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # Backend del Servidor Despachador Java (Master / Coordinador)
    upstream dispatcher_backend {
        server 127.0.0.1:8080;
    }

    server {
        listen       8088;
        server_name  localhost;

        # Interfaz Web del Cliente
        location / {
            root   html;
            index  index.html index.htm;
        }

        # Reverse Proxy hacia el Servidor Despachador en Java
        location /dispatcher {
            proxy_pass http://dispatcher_backend/dispatcher;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            
            # Pasar o forzar la cabecera de depuración de tiempo
            proxy_set_header X-Debug "true";
            proxy_pass_header X-debug-info;

            # Tiempos de espera amplios para permitir el cómputo distribuido
            proxy_connect_timeout 300s;
            proxy_send_timeout    300s;
            proxy_read_timeout    300s;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

---

### 3. Interfaz Web de Usuario (`nginx/html/index.html`)

Se implementó una interfaz web moderna, responsiva y con modo oscuro que permite:
1. Ingresar o pegar el hash SHA-256 objetivo (con botón de carga rápida para `"escom"`: `de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f`).
2. Visualizar la arquitectura visual en nodos interactivos (Navegador $\rightarrow$ NGINX :8088 $\rightarrow$ Despachador :8080 $\rightarrow$ Workers 8081 & 8082).
3. Cronómetro en vivo con animación de espera durante el procesamiento distribuido.
4. Despliegue de la solución (`La contraseña es: escom`), tiempo de cómputo del servidor (`X-debug-info`) y tiempo total RTT medido por el cliente web.

---

### 4. Evidencia Experimental de Funcionamiento

#### **Prueba con `curl -v` a través del Reverse Proxy NGINX:**
```text
curl.exe -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e" http://localhost:8088/dispatcher
* Connected to localhost (127.0.0.1) port 8088
> POST /dispatcher HTTP/1.1
> Host: localhost:8088
> User-Agent: curl/8.13.0
> Accept: */*
> X-Debug:true
> Content-Length: 66
> Content-Type: application/x-www-form-urlencoded
> 
< HTTP/1.1 200 OK
< Server: nginx/1.26.2
< Date: Tue, 20 Oct 2026 05:14:09 GMT
< Content-Length: 25
< Connection: keep-alive
< Access-control-allow-headers: Content-Type, X-Debug
< Access-control-allow-methods: POST, GET, OPTIONS
< Access-control-allow-origin: *
< X-debug-info: La operacion tomo 36936218600 nanosegundos = 36 segundos con 936 milisegundos
< 
La contraseña es: escom
* Connection #0 to host localhost left intact
```

#### **Captura de Pantalla en el Navegador Web:**
La interfaz web fue validada y su captura se encuentra almacenada en [`captura_ejercicio3_navegador.png`](file:///d:/SistemasDistribuidos/C22/captura_ejercicio3_navegador.png):
- Muestra el hash de `"escom"` ingresado.
- Despliega el resultado final: `La contraseña es: escom`.
- Despliega el tiempo de cómputo del clúster reportado por el despachador.

---

### 5. Fundamentación Teórica Crucial: ¿Por qué NGINX NO puede sustituir a un Servidor Despachador?

Esta distinción conceptual es medular en la ingeniería de sistemas distribuidos:

| Característica / Función | NGINX (Reverse Proxy / Load Balancer) | Servidor Despachador (Master / Coordinator) |
| :--- | :--- | :--- |
| **Capa de Operación** | Capa de Transporte / Aplicación genérica (L4 TCP / L7 HTTP). | Capa de Lógica de Negocio y Dominio de Aplicación. |
| **Naturaleza del Procesamiento** | **Enrutamiento de flujo:** Recibe 1 petición de un cliente y la reenvía intacta a 1 de los servidores backend. | **Particionamiento de tareas (Map):** Recibe 1 problema y lo descompone algorítmicamente en $N$ subtareas (`a` hasta `z`). |
| **Concurrencia de la Tarea** | No puede disparar múltiples subpeticiones simultáneas a partir de una sola petición entrante. | Despacha en paralelo $N$ subpeticiones asíncronas hacia la granja de Workers usando `CompletableFuture`. |
| **Control de Estado y Semántica** | Agnóstico al contenido semántico del cuerpo de respuesta (solo verifica códigos HTTP 200, 500, etc.). | Inspecciona el significado de cada respuesta parcial (`"NULL"` vs `"La contraseña es:"`). |
| **Cancelación Temprana (*Short-Circuiting*)** | Imposible. No tiene concepto de "solución encontrada". | Detiene inmediatamente el despacho de tareas pendientes tan pronto como un Worker encuentra la solución. |
| **Agregación de Resultados (Reduce)** | No puede mezclar ni reducir múltiples respuestas HTTP en un único resultado consolidado. | Agrega y sintetiza los resultados de todos los trabajadores para responder una única respuesta al cliente. |

> [!IMPORTANT]
> **Conclusión Teórica:**
> NGINX es un excelente componente de infraestructura para **balanceo de carga homogéneo**, **terminación SSL/TLS**, **caching de contenido estático** y **alta disponibilidad**. Sin embargo, **carece de inteligencia algorítmica para orquestación distribuida (MapReduce)**. La descomposición de tareas, coordinación de estados, sincronización asíncrona y reducción de resultados son responsabilidades exclusivas del software de aplicación desarrollado por el ingeniero de sistemas distribuidos.

---

## 📂 Mapeo y Guía de Conexión de Archivos entre Clases

```text
D:\SistemasDistribuidos\
├── 21/                                # CLASE ANTERIOR: Servidores Trabajadores (Workers)
│   ├── ServidorHttp.java              # Expone el endpoint /task y calcula SHA-256 por fuerza bruta
│   ├── Ejercicio1.java                # Algoritmo base de fuerza bruta de 5 letras
│   └── MEMORY.md                      # Memoria técnica de la Clase 21
│
├── 19/                                # CLASE BASE: Cliente HTTP Asíncrono
│   ├── networking/WebClient.java      # Cliente HttpClient con sendAsync() y HTTP/1.1
│   ├── Aggregator.java                # Coordinador con CompletableFuture e isDone()
│   └── Application.java               # Despachador de consola base
│
└── C22/                               # CLASE ACTUAL: Servidor Despachador y NGINX
    ├── MEMORY.md                      # Este documento (Memoria técnica integral de la Clase 22)
    ├── captura_ejercicio3_navegador.png # Captura de la interfaz web en funcionamiento
    ├── source/
    │   ├── Aggregator.java            # Modificado con detención temprana (short-circuiting)
    │   ├── Application.java           # Ejercicio 1: Despacha 26 tareas y muestra tareas a..e
    │   ├── WebServer.java             # Ejercicio 2: Servidor HTTP con endpoint /dispatcher
    │   └── WebClient.java             # Cliente asíncrono con package networking
    ├── classes/                       # Bytecode compilado y Application.jar
    ├── nginx/
    │   ├── conf/nginx.conf            # Configuración de Reverse Proxy (:8088 -> :8080)
    │   └── html/index.html            # Interfaz Web moderna (Ejercicio 3)
    ├── compilar_ejercicio1.bat        # Compila y empaqueta Application.jar
    ├── ejecutar_worker_8081.bat       # Inicia Worker 1 en puerto 8081
    ├── ejecutar_worker_8082.bat       # Inicia Worker 2 en puerto 8082
    ├── ejecutar_ejercicio1.bat        # Ejecuta Ejercicio 1 (Application)
    ├── iniciar_despachador.bat        # Inicia WebServer en puerto 8080
    ├── probar_ejercicio2_curl.bat     # Prueba /dispatcher con curl
    ├── iniciar_nginx.bat              # Inicia NGINX en puerto 8088
    ├── detener_nginx.bat              # Detiene NGINX
    └── abrir_navegador.bat            # Abre http://localhost:8088/ en el navegador
```

---

## 🚀 Guía Rápida de Ejecución de la Práctica Completa

### Paso 1: Iniciar los Servidores Trabajadores (Workers)
- En terminal 1: ejecutar [`ejecutar_worker_8081.bat`](file:///d:/SistemasDistribuidos/C22/ejecutar_worker_8081.bat) (Puerto 8081).
- En terminal 2: ejecutar [`ejecutar_worker_8082.bat`](file:///d:/SistemasDistribuidos/C22/ejecutar_worker_8082.bat) (Puerto 8082).

### Paso 2: Iniciar el Servidor Despachador
- En terminal 3: ejecutar [`iniciar_despachador.bat`](file:///d:/SistemasDistribuidos/C22/iniciar_despachador.bat) (Puerto 8080).

### Paso 3: Iniciar NGINX Reverse Proxy
- Ejecutar [`iniciar_nginx.bat`](file:///d:/SistemasDistribuidos/C22/iniciar_nginx.bat) (Puerto 8088).

### Paso 4: Abrir la Interfaz Web y Probar
- Ejecutar [`abrir_navegador.bat`](file:///d:/SistemasDistribuidos/C22/abrir_navegador.bat) o navegar a `http://localhost:8088/`.
- Presionar **"Iniciar Búsqueda Distribuida"**.
- Observar en vivo cómo el despachador orquesta las 26 tareas y el navegador despliega la solución `La contraseña es: escom` junto con el tiempo de procesamiento.

