# Práctica Clase 16: Servidor HTTP Concurrente y Análisis de Hilos

Este proyecto contiene la implementación y análisis experimental del servidor HTTP multihilo en Java solicitado en la **Clase 16**.

---

## 📁 Archivos del Proyecto

- [`ServidorHttp.java`](file:///d:/SistemasDistribuidos/16/ServidorHttp.java): Código fuente Java completamente comentado explicando cada componente (Socket, Handlers, ThreadPool, ciclo de petición/respuesta).
- [`reporte_teams.md`](file:///d:/SistemasDistribuidos/16/reporte_teams.md): Plantilla y texto listo para enviar por Teams con la respuesta a la pregunta del Ejercicio 1, justificación técnica y tabla comparativa.
- [`ejecutar_servidor_8.bat`](file:///d:/SistemasDistribuidos/16/ejecutar_servidor_8.bat): Script para iniciar el servidor con 8 hilos.
- [`ejecutar_servidor_1.bat`](file:///d:/SistemasDistribuidos/16/ejecutar_servidor_1.bat): Script para iniciar el servidor con 1 hilo.

---

## 🚀 Guía de Ejecución y Captura para Teams

### Paso 1: Compilar
En la terminal (PowerShell, CMD o Git Bash) dentro de esta carpeta:
```bash
javac --release 8 ServidorHttp.java
```

---

### Paso 2: Experimento A (Pool de 8 Hilos)

1. **Terminal 1 (Servidor):**
   ```bash
   java ServidorHttp 8
   ```
2. **Terminales Cliente (Abrir dos ventanas de Git Bash lado a lado):**
   - En **Terminal A (Cliente 1)** ejecutar:
     ```bash
     time curl -s http://localhost:8000/tarea
     ```
   - Inmediatamente en **Terminal B (Cliente 2)** ejecutar:
     ```bash
     time curl -s http://localhost:8000/tarea
     ```
3. **Resultado:**
   - Ambas terminales terminan en **~5 segundos**.
   - 📸 **Tomar captura de pantalla** de ambas terminales juntas mostrando el `real 0m5.xxx s`.

---

### Paso 3: Experimento B (Pool de 1 Hilo)

1. **Terminal 1 (Servidor):**
   Detener el servidor anterior con `Ctrl + C` e iniciar con 1 hilo:
   ```bash
   java ServidorHttp 1
   ```
2. **Terminales Cliente (Git Bash lado a lado):**
   - En **Terminal A (Cliente 1)** ejecutar:
     ```bash
     time curl -s http://localhost:8000/tarea
     ```
   - Inmediatamente en **Terminal B (Cliente 2)** ejecutar:
     ```bash
     time curl -s http://localhost:8000/tarea
     ```
3. **Resultado:**
   - La primera terminal termina en **~5 segundos**.
   - La segunda terminal termina en **~10 segundos** (5s de espera + 5s de ejecución).
   - 📸 **Tomar captura de pantalla** de ambas terminales juntas mostrando el contraste `5.1 s` vs `10.1 s`.
