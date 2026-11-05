@echo off
setlocal
set "PATH=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin;%PATH%"

echo =======================================================================
echo          EJECUCION LOCAL DEL SERVIDOR JAR (PUERTO 8080)
echo          Alumno: Kaleb Yael De La Rosa Gutierrez - Grupo: 7CM1
echo =======================================================================

if not exist WebServer.jar (
    echo [AVISO] WebServer.jar no encontrado. Ejecutando compilacion...
    call compilar_jar.bat
)

echo.
echo Iniciando servidor en puerto 8080...
echo Presione Ctrl+C en esta ventana para detener el servidor.
echo Endpoint disponible: http://localhost:8080/status
echo.
java -jar WebServer.jar 8080
