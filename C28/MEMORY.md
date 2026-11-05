# MEMORY: Práctica Clase 28 - Google Cloud Storage JSON API (OAuth 2.0 + JWT)

Este documento técnico contiene el análisis de requerimientos, fundamentos de autenticación OAuth 2.0 mediante Cuentas de Servicio y JSON Web Tokens (JWT), el código fuente completo de [`GoogleOAuthJWT.java`](file:///d:/SistemasDistribuidos/C28/GoogleOAuthJWT.java) y la guía paso a paso para realizar la configuración en Google Cloud Platform (GCP) y obtener la captura de consola UNIX solicitada en el entregable.

---

## 📌 1. Información General

- **Materia:** Sistemas Distribuidos
- **Profesor:** M. en C. Ukranio Contreras Coronilla
- **Institución:** Escuela Superior de Cómputo (ESCOM) - Instituto Politécnico Nacional (IPN)
- **Directorio de Trabajo:** [`d:\SistemasDistribuidos\C28`](file:///d:/SistemasDistribuidos/C28)
- **Documento de Especificación:** [`Practica - Google Cloud Storage JSON API_ver3.pdf`](file:///d:/SistemasDistribuidos/C28/Practica%20-%20Google%20Cloud%20Storage%20JSON%20API_ver3.pdf)

---

## 🛠️ 2. Estructura de Archivos en `C28`

```text
D:\SistemasDistribuidos\C28\
│
├── Practica - Google Cloud Storage JSON API_ver3.pdf  # Manual oficial de la práctica
├── MEMORY.md                                          # Memoria técnica e instructivo paso a paso
├── GoogleOAuthJWT.java                                # Código fuente completo (OAuth 2.0 JWT + Descarga GCS)
├── GoogleOAuthJWT.class                               # Bytecode compilado
├── gson-2.14.0.jar                                    # Librería Gson de Google para parseo de JSON
├── WebServer.java                                     # Archivo de código de ejemplo para subir al bucket
├── compilar_y_ejecutar.sh                             # Script para consola UNIX (Cloud Shell / WSL / Git Bash)
├── compilar.bat                                       # Script de compilación en Windows
└── ejecutar.bat                                       # Script de ejecución en Windows
```

---

## 🔐 3. Funcionamiento del Flujo OAuth 2.0 con JWT y Google Cloud Storage JSON API

1. **Lectura de Credenciales (`credentials.json`):**
   Se extraen con `Gson` tres atributos clave de la Cuenta de Servicio:
   - `client_email`: Identificador de la cuenta de servicio (`iss` en el JWT).
   - `private_key`: Llave privada RSA codificada en formato PEM (PKCS#8).
   - `token_uri`: Endpoint de autenticación de Google (`https://oauth2.googleapis.com/token`).
2. **Construcción y Firma del JWT (`RS256`):**
   - **Header:** `{"alg":"RS256","typ":"JWT"}` codificado en Base64URL sin padding.
   - **Payload:** Incluye el emisor (`iss`), el alcance de solo lectura sobre Cloud Storage (`scope`: `https://www.googleapis.com/auth/devstorage.read_only`), la audiencia (`aud`), el tiempo de emisión (`iat`) y expiración de 1 hora (`exp = now + 3600`).
   - **Firma Digital:** Se firma la cadena `<header_b64>.<payload_b64>` con el algoritmo `SHA256withRSA` y la clave privada `PrivateKey`.
3. **Intercambio de JWT por Access Token (Paso 6-8):**
   - Se envía una petición `POST` a `token_uri` con header `Content-Type: application/x-www-form-urlencoded` y cuerpo:
     `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=<jwt>`
   - Google valida la firma RSA y responde con un JSON que contiene `"access_token"`.
4. **Descarga del Objeto desde el Bucket (Paso 9-10 - Ejercicio de la práctica):**
   - Se construye la URL del endpoint REST de **Google Cloud Storage JSON API**:
     ```text
     https://storage.googleapis.com/storage/v1/b/<NOMBRE_BUCKET>/o/<NOMBRE_OBJETO>?alt=media
     ```
     *(El parámetro `alt=media` indica a la API que devuelva el contenido binario del archivo en lugar de sus metadatos JSON).*
   - Se realiza una petición `GET` incluyendo el header `Authorization: Bearer <access_token>`.
   - Se reciben los bytes mediante `HttpResponse.BodyHandlers.ofByteArray()` y se guardan en disco con `Files.write(...)`.

---

## ☁️ 4. Guía Paso a Paso en Google Cloud Platform (GCP)

### Paso 1: Subir archivos al Bucket y habilitar la API
1. Entra a [Google Cloud Console - Cloud Storage Buckets](https://console.cloud.google.com/storage/browser).
2. Abre tu bucket (o crea uno nuevo, por ejemplo `bucket-kaleb`) y sube dos archivos de prueba dando clic en **Subir -> Subir archivos**:
   - Un archivo de código (puedes subir [`WebServer.java`](file:///d:/SistemasDistribuidos/C28/WebServer.java)).
   - Una imagen (por ejemplo `cupcake.jpg`).
3. En el buscador superior de GCP, busca **Google Cloud Storage JSON API** y verifica que esté **Habilitada**.

### Paso 2: Crear la Cuenta de Servicio (IAM)
1. Menú lateral ☰ -> **IAM y administración** -> **Cuentas de servicio**.
2. Clic en **+ Crear cuenta de servicio**.
3. En nombre escribe `mi-app-bucket` y da clic en **Crear y cerrar**.
4. En la lista de cuentas de servicio, copia el correo electrónico generado (ej. `mi-app-bucket@tu-proyecto.iam.gserviceaccount.com`).

### Paso 3: Asignar permiso de lectura en el Bucket
1. Menú lateral ☰ -> **Cloud Storage** -> **Buckets** -> haz clic sobre tu bucket.
2. Ve a la pestaña **Permisos** y da clic en **Otorgar acceso**.
3. En **Entidades nuevas**, pega el correo de tu cuenta de servicio (`mi-app-bucket@...`).
4. En **Seleccionar un rol**, elige **Cloud Storage** -> **Visualizador de objetos de Storage** (*Storage Object Viewer*).
5. Da clic en **Guardar**.

### Paso 4: Generar y descargar `credentials.json`
1. Regresa a **IAM y administración** -> **Cuentas de servicio** y haz clic sobre el correo `mi-app-bucket@...`.
2. Abre la pestaña **Claves** -> **Agregar clave** -> **Crear clave nueva**.
3. Selecciona **JSON** y haz clic en **Crear**. Se descargará un archivo `.json` a tu computadora.
4. Renombra ese archivo descargado a **`credentials.json`** y colócalo en la carpeta donde ejecutarás el programa.

---

## 🖥️ 5. Ejecución en Consola UNIX y Captura para el PDF Entregable

El ejercicio de la página 7 pide:
> *"Complete el código anterior para realizar la descarga de uno de los archivos que hay en el bucket. Envíe en un archivo pdf el código y la captura de pantalla de la consola UNIX donde se hace un listado largo de archivos, se ejecuta el programa y se vuelve a hacer el listado largo para mostrar que se ha descargado el archivo de manera exitosa."*

Puedes hacerlo directamente en **Google Cloud Shell** (o en **WSL / Git Bash** en tu laptop):

### Opción A: En WSL o Git Bash (Local en tu Laptop)
1. Asegúrate de haber copiado tu archivo `credentials.json` dentro de `D:\SistemasDistribuidos\C28\`.
2. Abre Git Bash o WSL en la carpeta `C28` y ejecuta exactamente esta secuencia de comandos para tomar tu captura de pantalla:

```bash
# 1. Listado largo ANTES de ejecutar el programa (muestra que el archivo aún no está)
ls -l

# 2. Compilar y ejecutar indicando el nombre de tu bucket y el archivo a descargar
javac -cp .:gson-2.14.0.jar GoogleOAuthJWT.java
java -cp .:gson-2.14.0.jar GoogleOAuthJWT nombre-de-tu-bucket cupcake.jpg

# 3. Listado largo DESPUÉS de ejecutar el programa (muestra el archivo descargado con su tamaño)
ls -l
```
*(Nota: Si usas Git Bash en Windows y `java` requiere `;` como separador de classpath, usa `java -cp ".;gson-2.14.0.jar" GoogleOAuthJWT nombre-de-tu-bucket cupcake.jpg`).*

### Opción B: En Google Cloud Shell
1. Abre Google Cloud Shell y sube a tu directorio los archivos:
   - `GoogleOAuthJWT.java`
   - `gson-2.14.0.jar`
   - `credentials.json`
2. Ejecuta en la terminal de Cloud Shell:
```bash
ls -l
javac -cp .:gson-2.14.0.jar GoogleOAuthJWT.java
java -cp .:gson-2.14.0.jar GoogleOAuthJWT nombre-de-tu-bucket cupcake.jpg
ls -l
```
3. Toma la captura de pantalla donde se aprecien los tres pasos (`ls -l` inicial, salida de `GoogleOAuthJWT` con el token y el mensaje de descarga exitosa, y el `ls -l` final mostrando el archivo descargado) e inclúyela junto con el código de [`GoogleOAuthJWT.java`](file:///d:/SistemasDistribuidos/C28/GoogleOAuthJWT.java) en tu PDF de entrega.

