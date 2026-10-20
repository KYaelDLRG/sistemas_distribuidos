@echo off
setlocal
chcp 65001 >nul
title Servidor Despachador HTTP - WebServer :8080

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\java.exe" (
    set "JAVA=%JAVA_HOME_BIN%\java.exe"
) else (
    set "JAVA=java"
)

if not exist "classes\WebServer.class" (
    echo Compilando WebServer primero...
    cd source
    "%JAVA_HOME_BIN%\javac.exe" -d ../classes *.java
    cd ..
)

echo ======================================================================
echo Iniciando Servidor Despachador en http://localhost:8080/dispatcher
echo Workers requeridos:
echo   - Worker 1: http://localhost:8081/task
echo   - Worker 2: http://localhost:8082/task
echo ======================================================================
echo.
"%JAVA%" -cp classes WebServer 8080
pause
endlocal
