@echo off
echo ========================================================
echo Iniciando Servidor HTTP (Ejercicio 3) en puerto 8080
echo ========================================================
javac --release 8 ServidorHttp.java
if %errorlevel% neq 0 (
    echo Error al compilar ServidorHttp.java
    exit /b %errorlevel%
)
java ServidorHttp 8080
