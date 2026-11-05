# AGENTS.md - Reglas, Estándares y Directrices de Proyecto 3

Este archivo define las directivas y restricciones de desarrollo para el **Proyecto 3: Simulación de Asteroides en Red** (Sistemas Distribuidos). Todo agente o desarrollador que opere en este repositorio debe cumplir estrictamente estas reglas.

---

## 1. Restricciones Críticas y Penalizaciones Académicas

1. **PROHIBIDO EL USO DE MAVEN / GRADLE:**
   - No crear `pom.xml` ni `build.gradle`.
   - Las dependencias externas (exclusivamente Lanterna) deben ubicarse en `lib/` en formato `.jar`.
   - Compilación manual con `javac -cp "lib/*;." --release 8 *.java` (o equivalente sin banderas de release si es JDK 8/21).

2. **UNA CLASE POR ARCHIVO:**
   - Cada clase, interfaz o enumeración debe estar en su propio archivo `.java` independiente. No agrupar múltiples clases en un solo archivo fuente.

3. **ENCABEZADO OBLIGATORIO EN CADA ARCHIVO `.java`:**
   - Todo archivo de código fuente debe comenzar obligatoriamente con el siguiente bloque de comentarios:
     ```java
     /*
      * Proyecto: Proyecto 3 - Simulación de Asteroides
      * Nombre: [NOMBRE COMPLETO DEL ALUMNO]
      * Grupo: [GRUPO]
      */
     ```
   - *Nota:* Omitir este encabezado descuenta 1 punto directo de la calificación.

4. **LIBRERÍAS GRÁFICAS PERMITIDAS:**
   - El servidor gráfico sólo puede emplear `javax.swing` y `java.awt`.
   - Prohibido JavaFX, OpenGL, librerías de juegos externas o frameworks de renderizado terceros.

5. **COMUNICACIÓN EXCLUSIVA POR HTTP:**
   - La comunicación entre cliente y servidor debe ser estrictamente mediante el protocolo **HTTP**.
   - El cliente de Lanterna debe usar un cliente HTTP síncrono (`HttpURLConnection` estándar para máxima compatibilidad Java 8 / 21).
   - El servidor debe exponer un `HttpServer` nativo (`com.sun.net.httpserver.HttpServer`).

---

## 2. Rúbrica y Requisitos de Dominio (Objetivo: 10/10)

| Criterio | Puntos | Regla de Implementación |
| :--- | :---: | :--- |
| **Formas de Asteroides Aceptables y Únicas** | 2 pts | Polígonos cerrados irregulares estilo arcade *Asteroids*. Ningún par de asteroides debe tener vértices idénticos. Generación procedural con perturbaciones angulares y radiales aleatorias. |
| **Aparición Inicial Fuera de Pantalla** | 2 pts | El lienzo visible es de exactamente 1000x1000 px. El asteroide NUNCA debe aparecer de golpe dentro de este marco. Debe generarse a una distancia $\ge \text{radio}$ fuera de los bordes y entrar gradualmente con su vector de velocidad. |
| **Entrada de Atributos en Lanterna** | 1 pt | El cliente debe permitir ingresar manualmente rapidez $v$, vector de dirección $(dx, dy)$ y velocidad angular con signo $\omega$ ($+/-$). |
| **Creación de $n$ Asteroides Aleatorios** | 1 pt | El cliente debe tener una opción para generar $n$ asteroides con valores asignados de forma aleatoria. |
| **Movimiento Continuo y Rapidez Constante** | 1 pt | Traslación constante: $x(t) = x_0 + vx \cdot t$, $y(t) = y_0 + vy \cdot t$. |
| **Movimiento Giratorio Constante (+ y -)** | 1 pt | Rotación a velocidad angular constante tanto en sentido horario como antihorario. |
| **Reaparición en el Lado Opuesto (Wrap-around)** | 2 pts | Cuando un asteroide sale completamente de la pantalla por un borde, debe reaparecer inmediatamente en el extremo opuesto manteniendo todos sus atributos intactos. |

---

## 3. Demostración en Video y Protocolo de Verificación

El proyecto debe estar preparado y probado para reproducir la siguiente secuencia en dos terminales independientes:

1. **Terminal 1:** Compilación y ejecución de `AsteroidServerMain`.
2. **Terminal 2:** Compilación y ejecución de `AsteroidClientMain`.
3. Creación de **1 asteroide** con datos manuales desde Lanterna.
4. Creación de **5 asteroides** aleatorios.
5. Creación de **1 asteroide** adicional manual.
6. Creación de **10 asteroides** aleatorios adicionales.
7. Creación de **1000 asteroides** adicionales (verificando que la interfaz no se congele y el servidor mantenga 60 FPS estables con renderizado multihilo / buffer optimizado).

---

## 4. Guía de Compilación y Ejecución

```bash
# Compilar todas las clases
javac -encoding UTF-8 -cp "lib/lanterna-3.1.2.jar;." -d bin src/*.java

# Ejecutar Servidor
java -cp "bin" AsteroidServerMain [puerto opcional, default 8080]

# Ejecutar Cliente Lanterna
java -cp "bin;lib/lanterna-3.1.2.jar" AsteroidClientMain [host/puerto opcional]
```

---

## 5. Parámetros del Estudiante
Antes de la entrega final, solicitar o verificar con el usuario:
- **Nombre Completo:** Kaleb Yael De La Rosa Gutiérrez
- **Grupo:** 7CM1 (confirmar si es este grupo o indicar el correcto)
