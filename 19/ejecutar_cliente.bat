@echo off
setlocal
chcp 65001 >nul
title Cliente HTTP Asincrono - Application

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\java.exe" (
    set "JAVA=%JAVA_HOME_BIN%\java.exe"
) else (
    set "JAVA=java"
)

if not exist "Application.jar" (
    echo Application.jar no encontrado. Ejecutando compilar.bat primero...
    call compilar.bat
)

echo Ejecutando: java -cp Application.jar Application
echo.
"%JAVA%" -cp Application.jar Application
echo.
pause
endlocal
