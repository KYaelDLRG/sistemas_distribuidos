/**
 * Simulador de NGINX Reverse Proxy para Windows / Entornos de prueba.
 * Replica el comportamiento exacto de /etc/nginx/sites-available/default:
 * - Escucha en el puerto 80 (http://localhost)
 * - Sirve los archivos estáticos (index.html, estilos.css, app.js)
 * - Redirige /task y /status por proxy inverso al backend Java en http://localhost:8080
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 80;
const BACKEND_URL = 'http://127.0.0.1:8080';

const MIME_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.png': 'image/png',
    '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
    const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = url.pathname;

    // --- REVERSE PROXY: /task y /status hacia backend Java (8080) ---
    if (pathname.startsWith('/task') || pathname.startsWith('/status')) {
        console.log(`[NGINX Reverse Proxy :80] ${req.method} ${pathname} -> Redirigiendo a ${BACKEND_URL}${pathname}`);

        const backendReq = http.request({
            hostname: '127.0.0.1',
            port: 8080,
            path: req.url,
            method: req.method,
            headers: {
                ...req.headers,
                'host': req.headers.host || 'localhost',
                'x-real-ip': req.socket.remoteAddress || '127.0.0.1',
                'x-forwarded-for': req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1'
            }
        }, (backendRes) => {
            res.writeHead(backendRes.statusCode, backendRes.headers);
            backendRes.pipe(res);
        });

        backendReq.on('error', (err) => {
            console.error(`[NGINX Error 502] No se pudo conectar con el backend en ${BACKEND_URL}:`, err.message);
            res.writeHead(502, { 'Content-Type': 'text/plain; charset=utf-8' });
            res.end('502 Bad Gateway: El servidor backend en Java (puerto 8080) no está disponible.');
        });

        req.pipe(backendReq);
        return;
    }

    // --- SERVIDOR WEB ESTÁTICO: index.html, estilos.css, app.js ---
    let relativePath = pathname === '/' ? 'index.html' : pathname.replace(/^\//, '');
    let filePath = path.join(__dirname, relativePath);

    fs.readFile(filePath, (err, content) => {
        if (err) {
            console.log(`[NGINX :80] 404 Not Found: ${pathname}`);
            res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
            res.end('404 Not Found');
            return;
        }

        const ext = path.extname(filePath).toLowerCase();
        const contentType = MIME_TYPES[ext] || 'application/octet-stream';

        console.log(`[NGINX :80] 200 OK Servido: ${relativePath} (${contentType})`);
        res.writeHead(200, { 'Content-Type': contentType });
        res.end(content);
    });
});

server.listen(PORT, () => {
    console.log(`================================================================`);
    console.log(`   NGINX Reverse Proxy Activo en: http://localhost (Puerto ${PORT})`);
    console.log(`   Backend Java esperado en:     ${BACKEND_URL}`);
    console.log(`================================================================`);
});

server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
        console.error(`Error: El puerto ${PORT} ya está ocupado por otro proceso.`);
    } else {
        console.error('Error al iniciar el servidor proxy:', err);
    }
});
