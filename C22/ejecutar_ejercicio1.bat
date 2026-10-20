@echo off
setlocal
chcp 65001 >nul
title Ejecutar Ejercicio 1 - Application

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\java.exe" (
    set "JAVA=%JAVA_HOME_BIN%\java.exe"
) else (
    set "JAVA=java"
)

if not exist "classes\Application.jar" (
    echo Application.jar no encontrado. Compilando primero...
    call compilar_ejercicio1.bat
)

echo ======================================================================
echo Ejecutando: java -cp classes/Application.jar Application
echo ======================================================================
echo.
cd classes
"%JAVA%" -cp Application.jar Application %*
cd ..
echo.
pause
endlocal
