# MEMORY - Proyecto 3: Simulación Distribuida de Asteroides en Red

Este documento almacena la memoria técnica, especificaciones completas, decisiones de diseño, rúbrica de evaluación y plan de ejecución del **Proyecto 3 de Sistemas Distribuidos**.

---

## 1. Descripción General del Proyecto

Desarrollo de una aplicación cliente-servidor en **Java** para la simulación gráfica de asteroides con formas aleatorias únicas que se desplazan en el espacio exterior a velocidad y rotación constantes.

- **Comunicación:** Únicamente a través del protocolo **HTTP** (Cliente HTTP síncrono $\leftrightarrow$ Servidor HTTP).
- **Servidor:** Aplicación gráfica (1000x1000 px, fondo negro) que aloja el servidor HTTP, calcula la física y dibuja la simulación en tiempo real usando exclusivamente `javax.swing` y `java.awt`.
- **Cliente:** Interfaz de usuario interactiva basada en terminal (TUI) desarrollada con la librería **Lanterna** (`.jar`).

---

## 2. Requerimientos Funcionales y de Dominio

### 2.1. Espacio y Canvas (Servidor)
- **Dimensiones:** Ventana/área de dibujo de exactamente **1000x1000 píxeles** con fondo negro.
- **Estado Inicial:** El espacio debe iniciar completamente vacío.
- **Librerías gráficas permitidas:** Únicamente `javax.swing` y `java.awt`.
- **Hilos (Threading):** Uso de hilos para separar la recepción de solicitudes HTTP del ciclo principal de simulación y renderizado (game loop / render loop a 60 FPS aprox.), garantizando fluidez incluso con cientos o miles de asteroides.

### 2.2. Propiedades y Comportamiento de los Asteroides
1. **Formas Únicas y Aleatorias:**
   - Inspiradas en el videojuego de arcade *Asteroids* (polígonos irregulares delineados en blanco).
   - **No deben existir dos asteroides con la misma forma** (generación algorítmica de vértices con perturbaciones radiales y angulares únicas).
2. **Generación Inicial Fuera de Pantalla:**
   - Ningún asteroide debe aparecer espontáneamente dentro del área visible de 1000x1000.
   - Todo asteroide se crea en los bordes exteriores (fuera del rango visible) y se desplaza hacia el interior según su vector de dirección.
3. **Movimiento Continuo y Traslación:**
   - Rapidez constante $v$ en la dirección especificada por el vector director $(dx, dy)$ o ángulo normalizado.
4. **Rotación Constante:**
   - Velocidad angular constante con signo:
     - Signo positivo ($+$): rotación horaria (o antihoraria según convención).
     - Signo negativo ($-$): rotación en sentido opuesto.
5. **Reaparición en Extremo Opuesto (Wrap-around / Pantalla Toroidal):**
   - Un asteroide **nunca deja de existir**.
   - Al salir completamente por un borde del área de 1000x1000 píxeles, debe reaparecer suavemente por el lado opuesto, conservando íntegros todos sus atributos (forma, tamaño, rapidez, vector de dirección y velocidad angular).

### 2.3. Interfaz de Usuario y Cliente (Lanterna)
- Interfaz en terminal construida con **Lanterna**.
- Debe incorporar un **cliente HTTP síncrono**.
- **Entrada Manual de Asteroide:**
  - Valor $v$: Rapidez del asteroide.
  - Coordenada/Vector de dirección: $(dx, dy)$ o dirección hacia donde se desplaza.
  - Velocidad angular con signo: magnitud y sentido de rotación.
- **Entrada Masiva / Aleatoria:**
  - Opción para ingresar un número $n$ de asteroides, asignando a cada uno valores de velocidad, dirección y velocidad angular de forma aleatoria.
- **Contador:**
  - Imprimir/mostrar en la interfaz el número total de asteroides creados hasta el momento.

---

## 3. Restricciones Técnicas y de Entrega

- **Sin Maven ni Gradle:** El proyecto no debe usar gestores de dependencias. Se debe descargar directamente el `.jar` de Lanterna y compilar usando la opción `-cp` de `javac`.
- **Organización del Código:**
  - Cada clase debe residir en su propio archivo `.java` independiente.
- **Encabezado Obligatorio:**
  - Cada archivo `.java` debe incluir al inicio un comentario con:
    ```java
    /*
     * Proyecto: Proyecto 3 - Simulación de Asteroides
     * Nombre: Kaleb Yael De La Rosa Gutiérrez
     * Grupo: 7CM1
     */
    ```
    *(Penalización de 1 punto en la calificación final si falta este comentario).*
- **Empaquetado:** No subir archivos comprimidos (`.zip`, `.rar`) ni enlaces a sitios externos; subir los archivos de código individuales según lo requerido.
- **Demostración en Video:** Debe grabarse un video corto mostrando:
  1. Compilación y ejecución del servidor en una terminal y del cliente en otra terminal.
  2. Creación de **1 asteroide** personalizado en la interfaz del cliente.
  3. Creación de **5 asteroides adicionales** (aleatorios).
  4. Creación de **1 asteroide** en la interfaz del cliente.
  5. Creación de **10 asteroides adicionales**.
  6. Creación de **1000 asteroides adicionales** (prueba de estrés y rendimiento).

---

## 4. Rúbrica de Evaluación (10 Puntos)

| Ítem | Criterio | Puntos |
| :--- | :--- | :---: |
| 1 | Asteroides con formas aceptables y todos de forma distinta | 2 |
| 2 | Todos los asteroides aparecen inicialmente fuera de la pantalla | 2 |
| 3 | Se puede introducir los atributos de cada asteroide en Lanterna | 1 |
| 4 | Se pueden introducir correctamente $n$ asteroides aleatorios en Lanterna | 1 |
| 5 | Movimiento continuo con desplazamiento constante | 1 |
| 6 | Movimiento giratorio con velocidad angular constante y en los dos sentidos | 1 |
| 7 | Asteroides aparecen nuevamente en el lado opuesto de la pantalla | 2 |
| **Total** | | **10 pts** |

---

## 5. Arquitectura Propuesta

### 5.1. Componentes del Servidor
- `AsteroidServerMain.java`: Inicializa el servidor HTTP y la ventana gráfica de visualización.
- `AsteroidPanel.java`: `JPanel` de 1000x1000 píxeles con fondo negro; implementa el bucle de simulación a 60 FPS usando `java.awt.Graphics2D` (con antialiasing y transformaciones afines para rotación/traslación).
- `Asteroid.java`: Modelo del asteroide (polígono `java.awt.Polygon` o `Path2D`, centro $(x,y)$, velocidad $(vx, vy)$, ángulo actual $\theta$, velocidad angular $\omega$, radio aproximado para detección de salida/wrap).
- `AsteroidGenerator.java`: Generador algorítmo de formas poligonales cerradas únicas (evitando colisiones de vértices y formas degeneradas) y cálculo del punto de spawn fuera del canvas de 1000x1000.
- `AsteroidHttpHandler.java`: Manejador de peticiones HTTP (e.g. `POST /asteroid` para individual y `POST /asteroids/random?count=n` o payload JSON/Query parameters) utilizando `com.sun.net.httpserver.HttpServer`.

### 5.2. Componentes del Cliente
- `AsteroidClientMain.java`: Punto de entrada del cliente Lanterna.
- `TerminalUI.java`: Construcción de ventanas, formularios y diálogos de Lanterna (modo texto/TUI) para capturar datos individuales o masivos y mostrar el total acumulado.
- `AsteroidHttpClient.java`: Cliente HTTP síncrono (usando `java.net.http.HttpClient` nativo de Java 11+) que envía las solicitudes al servidor.

### 5.3. Directorio de Dependencias
- `lib/`: Directorio local donde se descargará el JAR de Lanterna (por ejemplo `lanterna-3.1.2.jar`).

---

## 6. Checklist de Implementación

- [ ] Descargar `lanterna.jar` en la carpeta `lib/`.
- [ ] Definir protocolo HTTP (rutas `/add` y `/add-random`, métodos y formato de parámetros).
- [ ] Implementar generador de formas aleatorias únicas (`AsteroidGenerator`).
- [ ] Implementar modelo y física de `Asteroid` (traslación, rotación, spawn fuera de pantalla y wrap toroidal).
- [ ] Implementar `AsteroidPanel` y `AsteroidServerMain` con render loop optimizado para soportar 1000+ entidades.
- [ ] Implementar cliente HTTP síncrono y la interfaz gráfica de terminal en `Lanterna`.
- [ ] Configurar scripts o comandos de compilación por lotes (`javac -cp ...`).
- [ ] Probar casos del video: 1 asteroide, 5 aleatorios, 1 manual, 10 aleatorios, 1000 aleatorios.
- [ ] Verificar encabezados con nombre del alumno, número de proyecto y grupo en todos los `.java`.
