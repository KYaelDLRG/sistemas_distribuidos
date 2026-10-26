@echo off
chcp 65001 > nul
echo ===================================================
echo   Compilando proyecto Practica C25
echo ===================================================
javac -cp ".;lib/gson-2.9.1.jar" Alumno.java WebServer.java CombinacionesArchivos.java ClienteSincrono.java ClienteConsultas.java
if %ERRORLEVEL% equ 0 (
    echo [EXITO] Compilacion completada sin errores.
) else (
    echo [ERROR] Ocurrio un fallo durante la compilacion.
)
pause
