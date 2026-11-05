# Proyecto 3: Simulación de Asteroides en Red (Sistemas Distribuidos)

**Alumno:** Kaleb Yael De La Rosa Gutiérrez  
**Grupo:** 7CM1  
**Materia:** Sistemas Distribuidos  

---

## Estructura del Proyecto

```
Proyecto 3/
├── lib/
│   └── lanterna-3.1.2.jar       # Librería de interfaz de terminal Lanterna
├── src/
│   ├── Asteroid.java             # Cinemática, rotación con signo, wrap-around toroidal y renderizado
│   ├── AsteroidClientMain.java   # Entrada del cliente interactivo Lanterna
│   ├── AsteroidClientUI.java     # Interfaz gráfica TUI (Lanterna GUI2) con atajos para video
│   ├── AsteroidHttpClient.java   # Cliente HTTP síncrono nativo (Java HttpURLConnection)
│   ├── AsteroidHttpServer.java   # Servidor HTTP nativo (com.sun.net.httpserver.HttpServer)
│   ├── AsteroidPanel.java        # Canvas Swing 1000x1000 px, 60 FPS, fondo negro y HUD
│   ├── AsteroidServerMain.java   # Entrada del servidor gráfico (JFrame 1000x1000 px)
│   ├── AsteroidShape.java        # Geometría y radio máximo del asteroide
│   ├── AsteroidShapeFactory.java # Generador procedural de polígonos cerrados únicos
│   ├── AsteroidSimulation.java   # Gestor de concurrencia y física de asteroides (1000+ entidades)
│   ├── AsteroidSpawnManager.java # Generación y cálculo de aparición fuera de pantalla
│   ├── AsteroidVerifier.java     # Suite de pruebas automatizadas para validar rúbrica al 100%
│   └── SpawnPosition.java        # Modelo de coordenadas de spawn
├── bin/                          # Clases compiladas (.class)
├── compilar.bat                  # Script de compilación rápida para Windows
├── ejecutar_servidor.bat         # Inicia el Servidor Gráfico (Terminal 1)
├── ejecutar_cliente.bat          # Inicia el Cliente Lanterna (Terminal 2)
├── verificar_requisitos.bat      # Ejecuta la prueba formal de unicidad, spawn, wrap y conteo
├── AGENTS.md                     # Reglas y restricciones de desarrollo
└── MEMORY.md                     # Memoria técnica completa y rúbrica
```

---

## Instrucciones de Compilación y Ejecución Manual

### 1. Compilación
Abre una terminal en la raíz del proyecto y ejecuta:
```cmd
compilar.bat
```
*(O de manera manual:)*
```bash
javac -encoding UTF-8 -cp "lib/lanterna-3.1.2.jar;." --release 8 -d bin src/*.java
```

### 2. Ejecutar Servidor (Terminal 1)
```cmd
ejecutar_servidor.bat
```
*(O de manera manual:)*
```bash
java -cp "bin" AsteroidServerMain 8080
```
*Se abrirá una ventana de 1000x1000 píxeles con fondo negro puro y el servidor HTTP escuchará en el puerto 8080.*

### 3. Ejecutar Cliente (Terminal 2)
```cmd
ejecutar_cliente.bat
```
*(O de manera manual:)*
```bash
java -cp "bin;lib/lanterna-3.1.2.jar" AsteroidClientMain http://localhost:8080
```
*Se desplegará la interfaz TUI en la terminal con los controles para agregar asteroides y el contador de asteroides.*

---

## Guía Paso a Paso para la Grabación del Video

El profesor solicita un video breve demostrando exactamente la siguiente secuencia:

1. **Paso 1 - Inicio:**
   - En **Terminal 1**, ejecuta `compilar.bat` y luego `ejecutar_servidor.bat`.
   - Verifica que aparezca el lienzo negro de 1000x1000 px totalmente vacío.
   - En **Terminal 2**, ejecuta `ejecutar_cliente.bat`. Verifica que marque `Servidor HTTP: CONECTADO`.
2. **Paso 2 - Asteroide individual (1):**
   - En el cliente, deja los valores por defecto ($v=3.0$, $dx=1.0$, $dy=1.0$, $\omega=2.5$) y pulsa `[Crear Asteroide Individual]`.
   - Muestra en el servidor cómo el asteroide entra gradualmente desde fuera de la pantalla, avanza en línea recta a rapidez constante, rota en sentido horario y reaparece por el borde opuesto al salir.
3. **Paso 3 - 5 asteroides adicionales (+5):**
   - En el cliente, pulsa el botón `[+5]` (o escribe 5 en $n$ y pulsa `[Crear N Aleatorios]`).
   - Muestra cómo aparecen 5 asteroides de formas completamente distintas y con diferentes giros. El contador marcará 6.
4. **Paso 4 - Asteroide individual adicional (1):**
   - En el cliente, cambia la velocidad angular a un valor negativo (ejemplo: `-3.0`) y pulsa `[Crear Asteroide Individual]`.
   - Muestra cómo este asteroide gira en sentido antihorario (giro inverso). El contador marcará 7.
5. **Paso 5 - 10 asteroides adicionales (+10):**
   - En el cliente, pulsa el botón `[+10]`.
   - Muestra cómo se agregan 10 asteroides aleatorios más. El contador marcará 17.
6. **Paso 6 - 1000 asteroides adicionales (+1000):**
   - En el cliente, pulsa el botón `[+1000]`.
   - Muestra la ventana del servidor con más de 1000 asteroides en pantalla, manteniendo la animación a 60 FPS estables sin congelarse ni trabarse. El contador marcará 1017.
