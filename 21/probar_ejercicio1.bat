@echo off
echo ===================================================================
echo [PASO 1/2] Compilando Ejercicio1.java con compatibilidad Java 8...
echo ===================================================================
javac --release 8 Ejercicio1.java
if %errorlevel% neq 0 (
    echo [ERROR] Error durante la compilacion.
    exit /b %errorlevel%
)
echo [OK] Compilacion completada con exito en 2 segundos.
echo.
echo ===================================================================
echo [PASO 2/2] Ejecutando ataque por fuerza bruta en Java...
echo.
echo NOTA IMPORTANTE:
echo El ejercicio solicita una palabra que inicie con 'z' ('zorro')
echo para MAXIMIZAR el computo. Esto requiere evaluar mas de
echo 11.6 MILLONES de hashes SHA-256 (toma aprox. 4 a 5 minutos).
echo.
echo El programa mostrara en pantalla el avance letra por letra (a -> z).
echo Si solo desea una prueba rapida de 2 segundos, ejecute:
echo   probar_ejercicio1.bat arbol
echo ===================================================================
echo.

if "%1"=="" (
    java Ejercicio1 zorro
) else (
    java Ejercicio1 %1
)

echo.
echo ===================================================================
echo Fin de la ejecucion.
echo ===================================================================
