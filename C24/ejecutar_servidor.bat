@echo off
chcp 65001 > nul
echo =================================================================
echo   Iniciando Servidor RESTful micro-SAES (Clase 24 - Puerto 8080)
echo =================================================================

rem Verificar si el puerto 8080 ya está ocupado y liberarlo automáticamente
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo [AVISO] Puerto 8080 ocupado por proceso PID: %%a. Liberando...
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 1 >nul
)

java -Dfile.encoding=UTF-8 -cp ".;gson-2.14.0.jar" WebServer 8080
pause
