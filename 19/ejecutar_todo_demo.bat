@echo off
setlocal
chcp 65001 >nul
title Demo Completo - Sistemas Distribuidos Practica 19

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\java.exe" (
    set "JAVA=%JAVA_HOME_BIN%\java.exe"
    set "JAVAC=%JAVA_HOME_BIN%\javac.exe"
    set "JAR=%JAVA_HOME_BIN%\jar.exe"
) else (
    set "JAVA=java"
    set "JAVAC=javac"
    set "JAR=jar"
)

echo ========================================================
echo  DEMOSTRACION: CLIENTE HTTP ASINCRONO Y SERVIDORES
echo ========================================================
echo.
echo [1/4] Compilando clases Java y creando Application.jar...
"%JAVAC%" networking/WebClient.java Aggregator.java Application.java ServidorHttp.java
"%JAR%" -cfe Application.jar Application Application.class Aggregator.class networking/WebClient.class

echo [2/4] Iniciando Servidor 1 en puerto 8081...
start "Servidor HTTP 8081" /min "%JAVA%" ServidorHttp 8081

echo [3/4] Iniciando Servidor 2 en puerto 8082...
start "Servidor HTTP 8082" /min "%JAVA%" ServidorHttp 8082

echo Esperando 2 segundos para estabilizacion de sockets...
timeout /t 2 /nobreak >nul

echo.
echo [4/4] Ejecutando Cliente HTTP Asincrono:
echo ========================================================
echo java -cp Application.jar Application
echo ========================================================
echo.
"%JAVA%" -cp Application.jar Application
echo.
echo ========================================================
echo Ejecucion completada exitosamente.
echo ========================================================
echo Presione cualquier tecla para cerrar los servidores de prueba...
pause >nul

echo Cerrando procesos de servidores...
taskkill /FI "WINDOWTITLE eq Servidor HTTP 8081*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Servidor HTTP 8082*" /F >nul 2>&1
endlocal
