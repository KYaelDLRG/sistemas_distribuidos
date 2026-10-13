# MEMORY - Clase 20: Sistemas Distribuidos

## Resumen y Contexto
- **Materia:** Sistemas Distribuidos
- **Sesión:** Clase 20
- **Fecha:** 2026-10-13

---

## Objetivos de la Sesión
- [x] Crear e inicializar MEMORY.md.
- [x] Analizar requerimientos de `Practica - NGINX.pdf`.
- [x] **Punto 1:** Implementar backend `WebServer.java` con puerto configurable, endpoint `/status` y endpoint `/task`.
- [x] **Punto 2:** Crear archivos frontend (`index.html`, `estilos.css`, `app.js`) para el Ejercicio 1.
- [x] **Punto 3:** Generar archivos de configuración de NGINX (`nginx_reverse_proxy.conf` y `nginx_loadbalancer.conf`).
- [x] **Punto 4:** Pruebas visuales y de red del frontend y backend (validado en navegador headless).
- [x] **Punto 5:** Implementación y validación del Ejercicio 2 (NGINX Load Balancer con 3 servidores en puertos 8080, 8081 y 8082 probados con Round Robin).

---

## Archivos del Proyecto
1. [`WebServer.java`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/WebServer.java) & [`WebServer.class`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/WebServer.class): Servidor backend en Java que atiende peticiones en `/status` y `/task`.
2. [`index.html`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/index.html): Estructura del frontend para el Ejercicio 1.
3. [`estilos.css`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/estilos.css): Estilos visuales acordes a la captura de pantalla de la práctica.
4. [`app.js`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/app.js): Lógica de cliente para comunicarse con `/task` a través de NGINX.
5. [`proxy_server.js`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/proxy_server.js): Reverse Proxy en el puerto 80 que replica NGINX para pruebas del Ejercicio 1.
6. [`loadbalancer_server.js`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/loadbalancer_server.js): Load Balancer en el puerto 80 con Round Robin para pruebas del Ejercicio 2.
7. [`nginx_reverse_proxy.conf`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/nginx_reverse_proxy.conf): Configuración oficial de NGINX como proxy inverso y servidor de estáticos.
8. [`nginx_loadbalancer.conf`](file:///c:/Users/kaleb/SistemasDistribuidos/Clase20/nginx_loadbalancer.conf): Configuración oficial de NGINX con balanceador de carga (`upstream backend_java`).

---

## Resultados de Pruebas - Ejercicio 2 (Balanceo Round Robin)
Prueba ejecutada realizando 10 peticiones consecutivas a `http://localhost/status`:
```text
1. El servidor está vivo en el puerto 8080
2. El servidor está vivo en el puerto 8081
3. El servidor está vivo en el puerto 8082
4. El servidor está vivo en el puerto 8080
5. El servidor está vivo en el puerto 8081
6. El servidor está vivo en el puerto 8082
7. El servidor está vivo en el puerto 8080
8. El servidor está vivo en el puerto 8081
9. El servidor está vivo en el puerto 8082
10. El servidor está vivo en el puerto 8080
```
La distribución alterna cíclicamente entre las 3 instancias de backend como se solicita en el documento.


---

## Guía Rápida de Ejecución en Linux / WSL

### Para el Ejercicio 1 (Reverse Proxy y Formulario Web):
1. Copiar los archivos estáticos a la carpeta de NGINX:
   ```bash
   sudo mkdir -p /var/www/ejemplo
   sudo cp index.html estilos.css app.js /var/www/ejemplo/
   ```
2. Instalar la configuración en NGINX:
   ```bash
   sudo cp nginx_reverse_proxy.conf /etc/nginx/sites-available/default
   sudo ln -sf /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default
   sudo nginx -t
   sudo systemctl restart nginx
   ```
3. Ejecutar el backend en Java en el puerto 8080:
   ```bash
   java WebServer 8080
   ```
4. Abrir en el navegador: `http://localhost`. Probar la multiplicación con `3,4,5,6` y tomar la captura de pantalla solicitada.

---

### Para el Ejercicio 2 (Balanceador de Carga):
1. Iniciar tres instancias del servidor Java en terminales separadas (o en segundo plano):
   ```bash
   java WebServer 8080 &
   java WebServer 8081 &
   java WebServer 8082 &
   ```
2. Instalar la configuración del balanceador:
   ```bash
   sudo rm -f /etc/nginx/sites-enabled/default
   sudo cp nginx_loadbalancer.conf /etc/nginx/sites-available/loadbalancer
   sudo ln -sf /etc/nginx/sites-available/loadbalancer /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```
3. Ejecutar la prueba de balanceo en la terminal:
   ```bash
   for i in {1..10}; do curl http://localhost/status; echo; done
   ```
4. Tomar la captura de la salida en consola mostrando la alternancia entre los puertos 8080, 8081 y 8082.


