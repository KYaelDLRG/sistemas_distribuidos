@echo off
setlocal
chcp 65001 >nul
title Prueba Ejercicio 2 - CURL a /dispatcher

echo ======================================================================
echo Probando endpoint /dispatcher con curl -v (con cabecera X-Debug: true)
echo ======================================================================
echo.

curl.exe -v --header "X-Debug:true" --data "de3925d6e4321394d09e782b1c517c4ead2ea4d1209439ece3c42ac43db6d31f,e" http://localhost:8080/dispatcher

echo.
echo ======================================================================
echo Prueba finalizada.
echo ======================================================================
pause
endlocal
