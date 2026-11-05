---
name: asteroids-distributed
description: >-
  Instrucciones y procedimientos para compilar, ejecutar, probar y verificar la simulación distribuida de asteroides con Java, HTTP, Lanterna y Swing/AWT.
---

# Procedimientos de Trabajo para Proyecto 3: Asteroides Distribuidos

Esta skill define los flujos de trabajo específicos para la compilación, ejecución y verificación de los componentes del servidor y cliente.

## 1. Verificación del Entorno Java
Asegurarse de que el compilador `javac` y el ejecutable `java` sean accesibles. Debido a configuraciones mixtas de PATH en Windows (JDK 21 vs Java 8), compilar siempre con compatibilidad `--release 8` o verificar que ambas terminales ejecuten la misma versión.

## 2. Flujo de Compilación
```powershell
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" }
javac -encoding UTF-8 -cp "lib/lanterna-3.1.2.jar;." -d bin src/*.java
```

## 3. Pruebas de API HTTP
El servidor expone endpoints REST/HTTP simples:
- `POST /api/asteroid`: Envía un objeto con `{ "speed": double, "dirX": double, "dirY": double, "angularVelocity": double }`.
- `POST /api/asteroids/random`: Envía `{ "count": int }` para crear $n$ asteroides aleatorios.
- `GET /api/asteroids/count`: Devuelve `{ "count": int }` con el total actual.

Para probar el servidor sin cliente terminal (headless/curl):
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/asteroid" -Method Post -Body '{"speed":3.0,"dirX":1.0,"dirY":0.5,"angularVelocity":2.0}' -ContentType "application/json"
Invoke-RestMethod -Uri "http://localhost:8080/api/asteroids/random" -Method Post -Body '{"count":5}' -ContentType "application/json"
Invoke-RestMethod -Uri "http://localhost:8080/api/asteroids/count" -Method Get
```

## 4. Verificación de Rendimiento (1000+ Asteroides)
- Render loop a 60 FPS con doble buffer en Swing (`AsteroidPanel`).
- Colección de asteroides concurrente optimizada (usar array snapshot o buffer sincronizado durante el render para evitar contención de bloqueos).
- Métodos de dibujo con `Graphics2D` y `AffineTransform` reutilizables para minimizar la presión sobre el Garbage Collector.
