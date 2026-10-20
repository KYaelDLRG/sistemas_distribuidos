@echo off
setlocal
chcp 65001 >nul
title Compilador Ejercicio 1 - Cliente Asincrono Despachador

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\javac.exe" (
    set "JAVAC=%JAVA_HOME_BIN%\javac.exe"
    set "JAR=%JAVA_HOME_BIN%\jar.exe"
) else (
    set "JAVAC=javac"
    set "JAR=jar"
)

if not exist "classes" mkdir "classes"

echo [1/2] Compilando WebClient.java, Aggregator.java, Application.java desde source/...
cd source
"%JAVAC%" -d ../classes *.java
if errorlevel 1 (
    echo Error de compilacion.
    cd ..
    pause
    exit /b 1
)
cd ..

echo [2/2] Empaquetando Application.jar en classes/...
cd classes
"%JAR%" cf Application.jar Application.class Aggregator.class networking/WebClient.class
if errorlevel 1 (
    echo Error al crear Application.jar.
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo =======================================================
echo Compilacion exitosa. Application.jar generado en classes/
echo =======================================================
endlocal
