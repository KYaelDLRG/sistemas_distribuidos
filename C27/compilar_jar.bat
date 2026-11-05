@echo off
setlocal
set "PATH=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin;%PATH%"

echo =======================================================================
echo     COMPILACION Y EMPAQUETADO JAR - SERVIDOR WEB (CLOUD SHELL)
echo     Alumno: Kaleb Yael De La Rosa Gutierrez - Grupo: 7CM1
echo =======================================================================

echo [1/3] Creando directorio bin...
if not exist bin mkdir bin

echo [2/3] Compilando WebServer.java con javac...
javac -encoding UTF-8 -d bin WebServer.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] La compilacion fallo.
    pause
    exit /b %ERRORLEVEL%
)

echo [3/3] Empaquetando WebServer.jar con herramienta jar...
jar cfe WebServer.jar WebServer -C bin/ .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] No se pudo crear el archivo JAR.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =======================================================================
echo  [EXITO] WebServer.jar generado correctamente.
echo =======================================================================
