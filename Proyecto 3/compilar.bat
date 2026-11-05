@echo off
setlocal
echo ===================================================
echo  COMPILANDO PROYECTO 3: SIMULACION DE ASTEROIDES
echo  Alumno: Kaleb Yael De La Rosa Gutierrez - 7CM1
echo ===================================================

if not exist bin mkdir bin

echo [1/2] Compilando clases Java hacia bin...
javac -encoding UTF-8 -cp "lib/lanterna-3.1.2.jar;." --release 8 -d bin src/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ADVERTENCIA] Reintentando compilacion estandar...
    javac -encoding UTF-8 -cp "lib/lanterna-3.1.2.jar;." -d bin src/*.java
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [EXITO] Compilacion completada sin errores.
    echo Clases generadas en el directorio bin\
) else (
    echo.
    echo [ERROR] Ocurrio un error durante la compilacion.
)
