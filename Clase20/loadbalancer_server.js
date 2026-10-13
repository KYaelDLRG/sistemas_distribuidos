/**
 * Simulador de NGINX Load Balancer para Windows / Entornos locales.
 * Replica fielmente la configuración de: /etc/nginx/sites-available/loadbalancer
 *
 * upstream backend_java {
 *     server localhost:8080;
 *     server localhost:8081;
 *     server localhost:8082;
 * }
 *
 * server {
 *     listen 80;
 *     server_name localhost;
 *     location / {
 *         proxy_pass http://backend_java;
 *     }
 * }
 */

const http = require('http');

const PORT = 80;
const SERVERS = [
    { host: '127.0.0.1', port: 8080 },
    { host: '127.0.0.1', port: 8081 },
    { host: '127.0.0.1', port: 8082 }
];

let currentIndex = 0;

const server = http.createServer((req, res) => {
    // Algoritmo Round Robin (por defecto en NGINX)
    const target = SERVERS[currentIndex];
    const serverIndex = currentIndex;
    currentIndex = (currentIndex + 1) % SERVERS.length;

    console.log(`[NGINX LoadBalancer :80] Request ${req.method} ${req.url} -> Backend [${serverIndex + 1}/3]: ${target.host}:${target.port}`);

    const proxyReq = http.request({
        hostname: target.host,
        port: target.port,
        path: req.url,
        method: req.method,
        headers: {
            ...req.headers,
            'host': req.headers.host || 'localhost',
            'x-real-ip': req.socket.remoteAddress || '127.0.0.1',
            'x-forwarded-for': req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1'
        }
    }, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
        console.error(`[Error de conexión a backend en puerto ${target.port}]:`, err.message);
        res.writeHead(502, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end(`502 Bad Gateway: Backend en puerto ${target.port} no disponible.\n`);
    });

    req.pipe(proxyReq);
});

server.listen(PORT, () => {
    console.log(`================================================================`);
    console.log(`   NGINX Load Balancer Activo en: http://localhost (Puerto ${PORT})`);
    console.log(`   Algoritmo: Round Robin`);
    console.log(`   Upstream backend_java:`);
    SERVERS.forEach((s, idx) => console.log(`     [${idx + 1}] http://${s.host}:${s.port}`));
    console.log(`================================================================`);
});

server.on('error', (err) => {
    console.error('Error en el servidor loadbalancer:', err);
});
