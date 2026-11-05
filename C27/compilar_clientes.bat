@echo off
setlocal
set "PATH=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin;%PATH%"

echo =======================================================================
echo        COMPILACION DE CLIENTES JAVA - PRACTICA TRANSLATION API
echo        Alumno: Kaleb Yael De La Rosa Gutierrez - Grupo: 7CM1
echo =======================================================================

echo Compilando ClienteJokeAPI.java...
javac -encoding UTF-8 ClienteJokeAPI.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fallo la compilacion de ClienteJokeAPI.java
    pause
    exit /b %ERRORLEVEL%
)

echo Compilando ClientePlaceholder.java...
javac -encoding UTF-8 ClientePlaceholder.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fallo la compilacion de ClientePlaceholder.java
    pause
    exit /b %ERRORLEVEL%
)

echo Compilando ClienteTranslationAPI.java...
javac -encoding UTF-8 ClienteTranslationAPI.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fallo la compilacion de ClienteTranslationAPI.java
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =======================================================================
echo  [EXITO] Todos los clientes Java se compilaron satisfactoriamente.
echo =======================================================================
