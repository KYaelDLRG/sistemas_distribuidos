@echo off
chcp 65001 > nul
echo =================================================================
echo   Compilando micro-SAES RESTful WebServer (Java 8 / Java 21)
echo =================================================================

javac -source 8 -target 8 -encoding UTF-8 Alumno.java WebServer.java

if %ERRORLEVEL% EQU 0 (
    echo [OK] Compilación exitosa. Clases generadas: Alumno.class, WebServer.class
) else (
    echo [ERROR] Ocurrió un error durante la compilación.
)
pause
