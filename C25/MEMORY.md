# MEMORY: Práctica Clase 25 - Servidor REST HTTP, Peticiones GET/POST, Query Parameters y Paginación en Java

Este documento preserva la memoria técnica, análisis de requerimientos, decisiones de diseño arquitectónico, implementación de código, scripts de automatización y evidencias experimentales de la **Clase 25: API REST de Alumnos con Operaciones CRUD, Query Parameters y Paginación** de la materia **Sistemas Distribuidos**.

---

## 📌 1. Información General del Proyecto

- **Materia:** Sistemas Distribuidos
- **Profesor:** Dr. Ukranio Contreras Coronilla (ESCOM - IPN)
- **Directorio de Trabajo:** [`c:\Users\kaleb\SistemasDistribuidos\C25`](file:///c:/Users/kaleb/SistemasDistribuidos/C25)
- **Repositorio Oficial:** [KYaelDLRG/sistemas_distribuidos](https://github.com/KYaelDLRG/sistemas_distribuidos)
- **Tema:** Arquitectura de Servicios Web RESTful con `com.sun.net.httpserver`, Serialización JSON con Google Gson, Cliente HTTP Síncrono (`java.net.http.HttpClient`), Manejo de Query Parameters (`minPromedio`, `nombre`) y Estrategias de Paginación (`page`, `size`) para optimización de ancho de banda y latencia.

---

## ⚙️ 2. Entorno de Desarrollo y Configuración del Sistema

| Componente | Detalle / Versión en el Sistema |
| :--- | :--- |
| **Sistema Operativo** | Windows 11 (PowerShell / CMD / Git Bash) |
| **Java Development Kit (JDK)** | OpenJDK 21.0.12.1 LTS (Microsoft Build) |
| **Compilador (`javac`)** | `javac 21.0.12.1` |
| **Runtime (`java`)** | `java 21.0.12.1` |
| **Librería de Serialización** | Google Gson 2.9.1 ([`lib/gson-2.9.1.jar`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/lib/gson-2.9.1.jar)) |
| **Cliente de Pruebas** | Navegador Web (Chrome / Edge), `curl 8.x` nativo de Windows |
| **Puerto del Servidor** | `8080` (enlace local `http://localhost:8080`) |

---

## 📋 3. Desglose y Cumplimiento de Rúbricas

### Ejercicio 1: Endpoint `GET /alumnos` y Generador de Combinaciones
1. **Petición `GET /alumnos` vacía:**
   - Si la lista de alumnos no contiene registros, devuelve código HTTP **`200 OK`** con cuerpo JSON **`{}`** (objeto vacío explícito para indicar que el recurso existe pero no tiene elementos).
2. **Petición `GET /alumnos` con registros:**
   - Devuelve la lista de alumnos en formato JSON estructurado con código HTTP **`200 OK`**.
3. **Generación Determinista de Registros ([`CombinacionesArchivos.java`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/CombinacionesArchivos.java)):**
   - Lectura de 5 nombres desde [`nombres.txt`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/nombres.txt) y 5 apellidos desde [`apellidos.txt`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/apellidos.txt).
   - Generación de todas las combinaciones $5 \times 5 \times 5 = 125$ registros completos (`nombre + ap1 + ap2`).
   - Semilla fija `Random random = new Random(12345)` para reproducibilidad cruzada entre computadoras.
   - Cálculo del promedio: `numero = Math.round((random.nextDouble() * 100) * 10) / 100.0`.

---

### Ejercicio 2: Cliente HTTP Síncrono e Inserción Masiva por POST
1. **Cliente Síncrono ([`ClienteSincrono.java`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/ClienteSincrono.java)):**
   - Incorpora el cliente HTTP moderno de Java 11+ (`java.net.http.HttpClient`) operando de forma síncrona mediante `.send(request, HttpResponse.BodyHandlers.ofString())`.
2. **Construcción y Envío del Cuerpo JSON:**
   - Formateo mediante `String.format(Locale.US, "{\"nombre\":\"%s\",\"apellido1\":\"%s\",\"apellido2\":\"%s\",\"promedio\":%.2f}", ...)`.
   - Envío a través del publicador de cadenas:
     ```java
     .POST(HttpRequest.BodyPublishers.ofString(json))
     ```
3. **Registro y Visibilidad de Transacciones:**
   - Imprime en consola cada una de las 125 solicitudes POST enviadas (`[001/125]` a `[125/125]`) con el JSON emitido y la respuesta recibida (código `201 Created` y el objeto `Alumno` asignado con su ID autoincremental).
   - Ejecuta una consulta final `GET /alumnos` para mostrar el estado consolidado de la tabla en el servidor.

---

### Ejercicio 3: Query Parameters (`minPromedio`, `nombre`) y Paginación (`page`, `size`)
1. **Filtrado por Query Parameters:**
   - `minPromedio`: Filtra alumnos con calificación mayor o igual al umbral especificado (ej. `minPromedio=8`).
   - `nombre`: Filtra alumnos cuyo nombre inicie con el carácter o prefijo dado (ej. `nombre=C`).
   - Ejemplo de consulta:
     `GET /alumnos?minPromedio=8&nombre=C`
2. **Mecanismo de Paginación:**
   - `page`: Número de página solicitado (1-indexado).
   - `size`: Cantidad de elementos por página (ej. `page=1&size=20`).
3. **Protección contra Descarga Masiva por Defecto:**
   - En caso de que el cliente omita los parámetros (`GET /alumnos`), el servidor asigna automáticamente valores predeterminados (`page=1`, `size=20`). Esto previene la sobrecarga de ancho de banda, reduce latencia y protege los recursos de backend y frontend ante colecciones de miles de registros.

---

## 🏗️ 4. Arquitectura y Archivos del Módulo C25

```
C25/
├── Alumno.java                  # Modelo de datos Alumno (id, nombre, apellido1, apellido2, promedio)
├── WebServer.java               # Servidor HTTP con endpoints /alumnos, /status, query params y paginacion
├── CombinacionesArchivos.java   # Generador determinista de 125 combinaciones con semilla 12345
├── ClienteSincrono.java         # Cliente HTTP sincrono para inserciones POST y consulta GET
├── ClienteConsultas.java        # Cliente de pruebas para Ejercicio 3 (query params y paginacion)
├── nombres.txt                  # Lista de 5 nombres base
├── apellidos.txt                # Lista de 5 apellidos base
├── lib/
│   └── gson-2.9.1.jar           # Libreria Google Gson para serializacion/deserializacion JSON
├── capturas/
│   ├── captura_ejercicio3_paginacion.png      # Captura navegador: /alumnos?page=1&size=20
│   ├── captura_ejercicio3_query_params.png    # Captura navegador: /alumnos?minPromedio=8&nombre=C
│   └── captura_ejercicio3_sin_parametros.png  # Captura navegador: /alumnos (valores por defecto)
├── compilar.bat                 # Script para compilar todas las clases
├── iniciar_servidor.bat         # Script para lanzar WebServer en puerto 8080
├── ejecutar_ejercicio2.bat      # Script para ejecutar ClienteSincrono (125 POSTs)
├── ejecutar_ejercicio3.bat      # Script para ejecutar ClienteConsultas
└── MEMORY.md                    # Memoria tecnica consolidada de la practica
```

---

## 💻 5. Instrucciones de Compilación y Ejecución

### Paso 1: Compilar el proyecto
```bash
javac -cp ".;lib/gson-2.9.1.jar" Alumno.java WebServer.java CombinacionesArchivos.java ClienteSincrono.java ClienteConsultas.java
```
*O bien hacer doble clic en [`compilar.bat`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/compilar.bat).*

### Paso 2: Iniciar el Servidor Web
```bash
java -cp ".;lib/gson-2.9.1.jar" WebServer 8080
```
*O bien ejecutar [`iniciar_servidor.bat`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/iniciar_servidor.bat).*

### Paso 3: Ejecutar el Ejercicio 2 (Cliente Síncrono)
```bash
java -cp . ClienteSincrono
```
*O bien ejecutar [`ejecutar_ejercicio2.bat`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/ejecutar_ejercicio2.bat).*

### Paso 4: Ejecutar el Ejercicio 3 (Consultas y Paginación)
```bash
java -cp . ClienteConsultas
```
*O bien ejecutar [`ejecutar_ejercicio3.bat`](file:///c:/Users/kaleb/SistemasDistribuidos/C25/ejecutar_ejercicio3.bat).*

---

## 🌐 6. Resultados y Capturas de Pantalla

### 1. Paginación en Navegador (`GET /alumnos?page=1&size=20`)
Muestra la primera página de 20 registros ordenados por identificador:
![Paginacion Pagina 1](file:///c:/Users/kaleb/SistemasDistribuidos/C25/capturas/captura_ejercicio3_paginacion.png)

### 2. Filtro con Query Parameters (`GET /alumnos?minPromedio=8&nombre=C`)
Filtra únicamente los alumnos cuyo nombre inicia con "C" y tienen promedio $\ge 8.0$:
![Filtro Query Params](file:///c:/Users/kaleb/SistemasDistribuidos/C25/capturas/captura_ejercicio3_query_params.png)

### 3. Petición sin Parámetros (`GET /alumnos`)
Protección de omisión: el servidor aplica `page=1` y `size=20` por defecto en lugar de enviar los 125 registros completos:
![Omision de Parametros](file:///c:/Users/kaleb/SistemasDistribuidos/C25/capturas/captura_ejercicio3_sin_parametros.png)
