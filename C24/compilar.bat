@echo off
chcp 65001 > nul
echo =================================================================
echo   Compilando micro-SAES RESTful WebServer (Clase 24 - Gson)
echo =================================================================

rem Compilar con soporte para Java 8 y Java 21, incluyendo gson-2.14.0.jar en el classpath
javac -source 8 -target 8 -encoding UTF-8 -cp ".;gson-2.14.0.jar" Alumno.java GsonAlumno.java WebServer.java

if %ERRORLEVEL% EQU 0 (
    echo [OK] Compilación exitosa. Clases generadas:
    echo      - Alumno.class
    echo      - GsonAlumno.class
    echo      - WebServer.class
) else (
    echo [ERROR] Ocurrió un error durante la compilación.
)
pause
