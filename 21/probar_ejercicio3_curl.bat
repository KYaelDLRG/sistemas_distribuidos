@echo off
echo ========================================================
echo Probando Ejercicio 3 con curl segun especificaciones
echo Servidor: localhost:8080/task
echo ========================================================

echo.
echo [TEST 1] Caso donde NO se encuentra la contrasena (letra 'n') -> Esperado: NULL
echo curl -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,n" localhost:8080/task
echo --------------------------------------------------------
curl -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,n" localhost:8080/task

echo.
echo.
echo --------------------------------------------------------
echo [TEST 2] Caso donde SI se encuentra la contrasena (letra 'e') -> Esperado: "La contrasena es: escom"
echo curl -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e" localhost:8080/task
echo --------------------------------------------------------
curl -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e" localhost:8080/task

echo.
echo ========================================================
echo Pruebas finalizadas.
echo ========================================================
