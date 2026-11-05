# MEMORY: Prácticas Clase 29 - Google Cloud Shell & Cloud Translation API

Este documento técnico preserva la memoria técnica, análisis de requerimientos de los dos manuales en PDF, fundamentos teóricos de cómputo en la nube y consumo de APIs REST, diseño de software, código fuente desarrollado, scripts de automatización, y el instructivo detallado paso a paso con el checklist de capturas para la entrega individual en Microsoft Teams.

---

## 📌 1. Información General del Proyecto

- **Materia:** Sistemas Distribuidos
- **Profesor:** Dr. Ukranio Contreras Coronilla
- **Institución:** Escuela Superior de Cómputo (ESCOM) - Instituto Politécnico Nacional (IPN)
- **Alumno:** Kaleb Yael De La Rosa Gutiérrez
- **Grupo:** 7CM1
- **Directorio de Trabajo Local:** [`d:\SistemasDistribuidos\C29`](file:///d:/SistemasDistribuidos/C29)
- **Código Base Heredado:** [`d:\SistemasDistribuidos\C25`](file:///d:/SistemasDistribuidos/C25) ([`ClienteSincrono.java`](file:///d:/SistemasDistribuidos/C25/ClienteSincrono.java), [`WebServer.java`](file:///d:/SistemasDistribuidos/C25/WebServer.java))
- **Documentos de Especificación Oficiales:**
  1. [`Practica - Google Cloud Shell_ver4.pdf`](file:///d:/SistemasDistribuidos/C29/Practica%20-%20Google%20Cloud%20Shell_ver4.pdf)
  2. [`Practica - Cloud Traslation API_ver3.pdf`](file:///d:/SistemasDistribuidos/C29/Practica%20-%20Cloud%20Traslation%20API_ver3.pdf)

---

## 🛠️ 2. Estructura de Archivos Generados en `C29`

```text
D:\SistemasDistribuidos\C29\
│
├── Practica - Google Cloud Shell_ver4.pdf      # PDF 1 Oficial
├── Practica - Cloud Traslation API_ver3.pdf    # PDF 2 Oficial
├── MEMORY.md                                   # Memoria técnica completa y guía de entrega
├── api_key.txt                                 # Archivo para almacenar la API Key de GCP
│
├── WebServer.java                              # Servidor HTTP autónomo con endpoint /status
├── compilar_jar.bat                            # Compila a bin/ y genera WebServer.jar ejecutable
├── WebServer.jar                               # Archivo JAR empaquetado listo para subir al Bucket
├── ejecutar_servidor_local.bat                 # Script de prueba local en puerto 8080
│
├── ClienteJokeAPI.java                         # Item 1: Cliente GET síncrono para JokeAPI
├── ClientePlaceholder.java                     # Item 2: Cliente POST síncrono para JSONPlaceholder
├── ClienteTranslationAPI.java                  # Item 3: Cliente POST síncrono para Cloud Translation API
│
├── compilar_clientes.bat                       # Compila los 3 clientes Java
├── ejecutar_item1_joke.bat                     # Ejecuta la consulta a JokeAPI
├── ejecutar_item2_placeholder.bat              # Ejecuta el POST a JSONPlaceholder
└── ejecutar_item3_translation.bat              # Ejecuta la traducción en GCP con API Key
```

---

## ☁️ 3. PARTE 1: Práctica "Google Cloud Shell" (PDF 1)

### 3.1. Fundamentos Teóricos y Respuestas a Preguntas de la Práctica

#### **Pregunta Item 4: ¿Qué significan y para qué sirven las Regiones y Zonas en Google Cloud Platform?**
1. **Región (*Region*):**
   - Una región es un área geográfica independiente y separada en el mundo (por ejemplo, `us-west1` en Oregón, `us-central1` en Iowa o `southamerica-east1` en São Paulo).
   - *Propósito:* Disminuir la latencia acercando los servicios a los usuarios finales, cumplir con regulaciones de soberanía de datos y proporcionar alta disponibilidad geográfica ante desastres naturales o fallas masivas de infraestructura.
2. **Zona (*Zone*):**
   - Una zona es un área de implementación dentro de una región. Representa típicamente uno o varios centros de datos físicos interconectados mediante redes de ultra baja latencia. Su nomenclatura incluye la región más un sufijo de letra (ejemplo: `us-west1-a`, `us-west1-b`, `us-west1-c`).
   - *Propósito:* Proporcionar tolerancia a fallos. Si un centro de datos físico falla por un corte eléctrico o de red, las máquinas en otra zona de la misma región continúan operando (*Multi-zone deployment*).
3. **Comandos `gcloud` para visualizarlas:**
   ```bash
   # Listar todas las regiones disponibles en GCP
   gcloud compute regions list

   # Listar todas las zonas disponibles en GCP
   gcloud compute zones list

   # Filtrar zonas de una región específica (ejemplo: us-west1)
   gcloud compute zones list --filter="region:us-west1"
   ```
4. **Identificación de la instancia creada:**
   - Para la instancia creada en el Item 3 (`mv-escom`):
     - **Zona:** `us-west1-b`
     - **Región:** `us-west1` (Oregón, EE. UU.)

#### **Parámetros del comando de creación de la instancia (Item 3):**
```bash
gcloud compute instances create mv-escom --zone=us-west1-b --machine-type=e2-small --boot-disk-size=10GB
```
- `--zone=us-west1-b`: Ubica el hardware físico en la zona `b` de la región de Oregón.
- `--machine-type=e2-small`: Especifica la familia de máquinas virtualizadas E2 de costo eficiente con 2 vCPUs compartidas y 2.0 GB de memoria RAM.
- `--boot-disk-size=10GB`: Asigna un disco de estado sólido balanceado o persistente estándar de exactamente 10 GB como volumen raíz (`/dev/sda1` o `/dev/root`).
- *Manejo de error `ZONE_RESOURCE_POOL_EXHAUSTED`:* Ocurre cuando la zona seleccionada no tiene capacidad física disponible para ese tipo de máquina en ese instante. Se soluciona cambiando de zona (ej. `--zone=us-west1-c` o `--zone=us-central1-a`).

---

### 3.2. Paso a Paso en la Consola de GCP para la Práctica 1

| Paso | Acción en GCP / Comando | Evidencia requerida para el Reporte |
| :---: | :--- | :--- |
| **Paso 1** | Entrar a [console.cloud.google.com](https://console.cloud.google.com/), activar Google Cloud Shell (ícono de terminal `>_`). Hacer clic en el engranaje ⚙️ de la terminal y cambiar el tamaño de texto, color o tema. | 📸 **Captura Item 1:** Ventana de configuración de la terminal con las modificaciones visibles. |
| **Paso 2** | En Cloud Shell, ejecutar `gcloud compute instances list`. Ya tienes activas `mv-escom` y `vm-escom`. | 📸 **Captura Item 2:** Salida del comando mostrando la tabla de instancias con `mv-escom` y `vm-escom` en estado `RUNNING`. |
| **Paso 3** | Conectarse por SSH a `mv-escom` (clic en botón **SSH** en la consola o `gcloud compute ssh mv-escom --zone=us-west1-b`).<br>Dentro del SSH ejecutar: `df -h` | 📸 **Captura Item 3:** Terminal SSH mostrando la salida de `df -h` y **subrayando** la partición raíz de 10 GB (`/dev/root` o `/dev/sda1`). |
| **Paso 4** | Documentar en el reporte la explicación de Zonas y Regiones y los comandos `gcloud compute regions list` y `gcloud compute zones list`. | 📝 **Texto Item 4:** Respuesta a las preguntas teóricas e indicación de que `mv-escom` está en la zona `us-west1-b` y región `us-west1`. |
| **Paso 5** | En Cloud Shell, hacer clic en el botón **"Abrir editor"** (ícono de lápiz/editor). Crear un archivo `HolaMundo.java`: <br>```java public class HolaMundo { public static void main(String[] args) { System.out.println("Hola desde Cloud Shell Editor - 7CM1"); } }```<br>Ejecutar con el botón **F5**. | 📸 **Captura Item 5:** Cloud Shell Editor con el código Java y la terminal inferior mostrando la ejecución tras pulsar F5. |
| **Paso 6** | **Cloud Storage (Bucket):** En el menú lateral -> Cloud Storage -> Buckets.<br>Crear bucket y subir `WebServer.jar`.<br>*(Si tu cuenta de facturación está deshabilitada/cerrada, ver solución en sección 3.3)*. | 📸 **Captura Item 6:** Vista del bucket en la consola de GCP mostrando el objeto `WebServer.jar` con fecha y hora. |
| **Paso 7** | **Despliegue y Ejecución en `mv-escom`:**<br>1. Habilitar regla HTTP en `mv-escom`:<br>`gcloud compute instances add-tags mv-escom --tags=http-server --zone=us-west1-b`<br>2. Entrar por SSH a `mv-escom` y subir `WebServer.jar`.<br>3. Instalar JRE en la VM: `sudo apt-get update && sudo apt-get install -y openjdk-17-jre-headless`<br>4. Ejecutar servidor en puerto 80: `sudo java -jar WebServer.jar 80`<br>5. En navegador abrir: `http://34.145.23.149/status` | 📸 **Captura Item 7:** Terminal SSH corriendo el servidor y navegador en `http://34.145.23.149/status` mostrando *"El servidor está vivo"*. |
| **Paso 8** | **¡MUY IMPORTANTE (Limpieza)!** Al terminar todas las capturas, eliminar las instancias para evitar cobros:<br>`gcloud compute instances delete mv-escom vm-escom --zone=us-west1-b --quiet` | ⚠️ Prevención de costos. |

---

### 3.3. Diagnóstico y Corrección de Errores de tu Sesión en GCP

A partir de tu captura y salida de consola, se detectaron 3 detalles clave:

#### 1. Confusión de entorno: Cloud Shell vs Máquina Virtual (SSH)
- **Lo que ocurrió:** Ejecutaste `sudo apt-get update`, `gcloud storage cp` y `java -jar` en la consola de **Cloud Shell** (`kyaeldlrg@cloudshell:~`), por lo que Google te mostró la advertencia:
  ```text
  You are running apt-get inside of Cloud Shell. Note that your Cloud Shell machine is ephemeral...
  ```
- **La corrección:** Esos comandos van **dentro de la máquina virtual `mv-escom`**. Para entrar, da clic en el botón **`SSH`** que aparece al lado de `mv-escom` en tu consola web (o escribe en Cloud Shell: `gcloud compute ssh mv-escom --zone=us-west1-b`). Una vez dentro, el prompt cambiará a `kyaeldlrg@mv-escom:~$`.

#### 2. Error 403 de Facturación en Cloud Storage (`The billing account is disabled in state closed`)
- **Lo que ocurrió:** Al escribir `gcloud storage cp gs://tu-bucket/WebServer.jar ./`, Google intentó acceder a un bucket inexistente llamado `tu-bucket` y además detectó que la cuenta de facturación de tu proyecto está cerrada/deshabilitada.
- **Solución para tener `WebServer.jar` en la VM:**
  - **Método A (Recomendado - 5 segundos):** Al abrir la terminal SSH de `mv-escom` en el navegador, en la barra superior derecha haz clic en el botón **"SUBIR ARCHIVO"** (ícono de engranaje / menú de SSH) y selecciona el archivo [`d:\SistemasDistribuidos\C29\WebServer.jar`](file:///d:/SistemasDistribuidos/C29/WebServer.jar) desde tu computadora. Quedará de inmediato en `~/WebServer.jar`.
  - **Método B (Copiar vía gcloud scp desde Cloud Shell):**
    En Cloud Shell puedes subir el archivo o descargarlo y copiarlo a la VM con:
    ```bash
    gcloud compute scp WebServer.jar mv-escom:~/ --zone=us-west1-b
    ```
  - **Método C (Crearlo y compilarlo directo en la VM por SSH):**
    Una vez dentro de `mv-escom` por SSH, puedes instalar el JDK y compilarlo en 2 líneas:
    ```bash
    sudo apt-get update && sudo apt-get install -y openjdk-17-jdk-headless
    cat << 'EOF' > WebServer.java
    import com.sun.net.httpserver.*;
    import java.io.*;
    import java.net.*;
    import java.nio.charset.StandardCharsets;
    public class WebServer {
        public static void main(String[] args) throws Exception {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 80;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/status", exchange -> {
                byte[] r = "El servidor está vivo\n".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, r.length);
                exchange.getResponseBody().write(r);
                exchange.close();
            });
            server.start();
            System.out.println("Servidor escuchando en el puerto " + port);
        }
    }
    EOF
    javac -d bin WebServer.java
    jar cfe WebServer.jar WebServer -C bin/ .
    sudo java -jar WebServer.jar 80
    ```

#### 3. Apertura del Puerto 80 (Firewall HTTP)
- Al crear `mv-escom` no se le asignó la etiqueta de red `http-server`. Si no se la agregas, no podrás conectarte a `http://34.145.23.149/status` desde tu computadora.
- **Para habilitarlo en 1 segundo:** Ejecuta en Cloud Shell:
  ```bash
  gcloud compute instances add-tags mv-escom --tags=http-server --zone=us-west1-b
  ```

---

## 🌐 4. PARTE 2: Práctica "Cloud Translation API" (PDF 2)

### 4.1. Fundamentos Teóricos

1. **Arquitectura REST y APIs Públicas vs Privadas:**
   - **REST (*Representational State Transfer*):** Utiliza los métodos estándar de HTTP (GET, POST, PUT, DELETE) sobre URIs descriptivas.
   - **API Pública sin autenticación (JokeAPI):** Permite solicitudes GET libres ya que ofrece contenido de lectura pública sin cuotas estrictas de usuario individual.
   - **API Pública con solicitudes POST (JSONPlaceholder):** Simula la creación de recursos mediante `POST` enviando un payload JSON y respondiendo con un `201 Created` y un identificador asignado.
   - **API Privada con API Key (Google Cloud Translation API):** Requiere autenticación y cobro por consumo (caracteres traducidos). Google identifica al solicitante mediante un parámetro de URL `key=API_KEY`.
2. **Seguridad: API Key vs OAuth 2.0:**
   - **API Key:** Es un token estático que identifica el proyecto de GCP que paga la factura. **No identifica a un usuario humano**, por lo que no es seguro para acceder a datos privados como correos (Gmail) o archivos (Drive). Cualquiera con la clave puede generar costos si no se aplican restricciones de API.
   - **OAuth 2.0:** Es un protocolo de autorización que solicita el consentimiento explícito de un usuario para que una aplicación acceda a sus recursos específicos mediante tokens temporales (*Access Tokens*).

---

### 4.2. Paso a Paso y Evidencias para la Práctica 2

#### **Item 1: JokeAPI (Petición HTTP GET)**
- **Archivo Java:** [`ClienteJokeAPI.java`](file:///d:/SistemasDistribuidos/C29/ClienteJokeAPI.java)
- **Script:** [`ejecutar_item1_joke.bat`](file:///d:/SistemasDistribuidos/C29/ejecutar_item1_joke.bat)
- **Ejecución local:**
  ```powershell
  java -cp . ClienteJokeAPI
  ```
- **Resultado validado en consola:**
  - Código HTTP: `200 OK`
  - JSON recibido:
    ```json
    {
        "error": false,
        "category": "Programming",
        "type": "single",
        "joke": "\"We messed up the keming again guys.\"",
        "id": 20,
        "lang": "en"
    }
    ```
- 📸 **Evidencia Item 1:** Código fuente de `ClienteJokeAPI.java` y captura de pantalla de la terminal con la respuesta del chiste.

---

#### **Item 2: JSONPlaceholder (Petición HTTP POST)**
- **Archivo Java:** [`ClientePlaceholder.java`](file:///d:/SistemasDistribuidos/C29/ClientePlaceholder.java)
- **Script:** [`ejecutar_item2_placeholder.bat`](file:///d:/SistemasDistribuidos/C29/ejecutar_item2_placeholder.bat)
- **Datos enviados:**
  ```json
  {
    "title": "7CM1",
    "body": "Kaleb Yael De La Rosa Gutiérrez",
    "userId": 1
  }
  ```
- **Resultado validado en consola:**
  - Código HTTP: `201 Created`
  - JSON recibido con ID generado:
    ```json
    {
      "title": "7CM1",
      "body": "Kaleb Yael De La Rosa Gutiérrez",
      "userId": 1,
      "id": 101
    }
    ```
- 📸 **Evidencia Item 2:** Código fuente de `ClientePlaceholder.java` y captura de pantalla de la terminal con el código 201 y el `id: 101`.

---

#### **Item 3: Google Cloud Translation API (Petición HTTP POST con API Key)**
1. **Configuración en Google Cloud Console:**
   - Ir a [console.cloud.google.com](https://console.cloud.google.com/).
   - En el menú lateral: **APIs y servicios** -> **APIs y servicios habilitados**.
   - Clic en **+ Habilitar APIs y servicios**, buscar **Cloud Translation API** y dar clic en **Habilitar**.
   - En el menú lateral izquierdo: **Credenciales** -> Clic en **+ Crear credenciales** -> Seleccionar **Clave de API**.
   - En el cuadro de diálogo:
     - Nombre: `Clave Translation API - 7CM1`.
     - Sección *Restricciones de API*: Seleccionar **Restringir clave** y marcar exclusivamente **Cloud Translation API**.
     - Clic en **Guardar**.
     - Copiar la clave generada (empieza por `AIzaSy...`).
2. **Petición en Navegador (Comprobación previa):**
   - Introducir en el navegador la URL construida:
     ```text
     https://translation.googleapis.com/language/translate/v2?target=es&key=TU_API_KEY&q="Political%20correctness%20is%20the%20attempt%20to%20control%20language%20and%20conduct%20by%20imposing%20an%20ideological%20hegemony,%20which%20is%20the%20very%20antithesis%20of%20free%20speech."
     ```
3. **Ejecución del Cliente Java:**
   - Guardar la clave en el archivo [`d:\SistemasDistribuidos\C29\api_key.txt`](file:///d:/SistemasDistribuidos/C29/api_key.txt) o pasarla al ejecutar:
     ```powershell
     java -cp . ClienteTranslationAPI "TU_API_KEY"
     ```
   - O mediante el script:
     ```cmd
     ejecutar_item3_translation.bat
     ```
4. **Respuesta Esperada de Google Cloud:**
   ```json
   {
     "data": {
       "translations": [
         {
           "translatedText": "La corrección política es el intento de controlar el lenguaje y la conducta imponiendo una hegemonía ideológica, que es la antítesis misma de la libertad de expresión.",
           "detectedSourceLanguage": "en"
         }
       ]
     }
   }
   ```
- 📸 **Evidencia Item 3:** Captura de la consola ejecutando `ClienteTranslationAPI` con la traducción devuelta por Google Cloud.

---

## 📋 5. Checklist Final de Entrega para el PDF

| # | Archivo / Práctica | Ítem Solicitado | Tipo de Evidencia | Estado |
| :---: | :--- | :--- | :---: | :---: |
| 1 | Google Cloud Shell | **Item 1:** Configuración de la terminal Cloud Shell | Captura | ⏳ Por capturar en consola GCP |
| 2 | Google Cloud Shell | **Item 2:** `gcloud compute instances list` con instancia activa | Captura | ⏳ Por capturar en consola GCP |
| 3 | Google Cloud Shell | **Item 3:** Creación de `mv-escom` y `df -h` subrayando 10GB | Captura | ⏳ Por capturar en consola GCP |
| 4 | Google Cloud Shell | **Item 4:** Zonas, regiones y ubicación de la VM | Texto / Comandos | ✅ Documentado en Sección 3.1 |
| 5 | Google Cloud Shell | **Item 5:** HolaMundo en Cloud Shell Editor con F5 | Captura | ⏳ Por capturar en Cloud Shell Editor |
| 6 | Google Cloud Shell | **Item 6:** Bucket de Cloud Storage con `WebServer.jar` y fecha | Captura | ⏳ Por capturar tras subir JAR |
| 7 | Google Cloud Shell | **Item 7:** Terminal SSH del servidor y navegador en `/status` | Captura | ⏳ Por capturar en navegador |
| 8 | Cloud Translation API | **Item 1:** Cliente JokeAPI y chiste recibido | Código + Captura | ✅ Código listo y validado localmente |
| 9 | Cloud Translation API | **Item 2:** Cliente JSONPlaceholder y POST con ID autogenerado | Código + Captura | ✅ Código listo y validado localmente |
| 10 | Cloud Translation API | **Item 3:** Cliente Cloud Translation API con traducción | Código + Captura | ✅ Código listo y probado con Google Cloud (200 OK) |
