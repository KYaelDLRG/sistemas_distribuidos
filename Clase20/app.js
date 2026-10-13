// Lógica de cliente para interactuar con el endpoint /task a través de NGINX Reverse Proxy
document.addEventListener('DOMContentLoaded', () => {
    const numerosInput = document.getElementById('numerosInput');
    const btnEnviar = document.getElementById('btnEnviar');
    const resultadoBox = document.getElementById('resultadoBox');

    // Función para enviar los números al servidor
    async function enviarMultiplicacion() {
        const valor = numerosInput.value.trim();

        if (!valor) {
            resultadoBox.className = 'resultado-box error';
            resultadoBox.textContent = 'Por favor ingresa al menos un número.';
            return;
        }

        resultadoBox.className = 'resultado-box cargando';
        resultadoBox.textContent = 'Calculando...';

        try {
            // Cuando NGINX actúa como reverse proxy en http://localhost,
            // la petición a /task se redirige internamente a http://localhost:8080/task.
            let response;
            try {
                response = await fetch('/task', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain; charset=UTF-8'
                    },
                    body: valor
                });
            } catch (errRed) {
                // Respaldo de desarrollo: Si se abre directamente el archivo HTML sin NGINX
                response = await fetch('http://localhost:8080/task', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain; charset=UTF-8'
                    },
                    body: valor
                });
            }

            if (response.ok) {
                const textoRespuesta = await response.text();
                resultadoBox.className = 'resultado-box';
                resultadoBox.textContent = textoRespuesta.trim();
            } else {
                const errorTexto = await response.text();
                resultadoBox.className = 'resultado-box error';
                resultadoBox.textContent = errorTexto || `Error HTTP: ${response.status}`;
            }
        } catch (error) {
            resultadoBox.className = 'resultado-box error';
            resultadoBox.textContent = 'Error al conectar con el servidor: ' + error.message;
        }
    }

    // Event listener para el botón
    btnEnviar.addEventListener('click', enviarMultiplicacion);

    // Permitir enviar con Enter (y Shift+Enter para salto de línea si se desea)
    numerosInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            enviarMultiplicacion();
        }
    });
});
