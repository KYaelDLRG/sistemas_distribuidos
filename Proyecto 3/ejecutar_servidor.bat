@echo off
setlocal
title Servidor de Asteroides - Terminal 1
echo ===================================================
echo  TERMINAL 1: SERVIDOR GRAFICO Y HTTP DE ASTEROIDES
echo  Alumno: Kaleb Yael De La Rosa Gutierrez - 7CM1
echo  Lienzo: 1000x1000 px - Puerto: 8080
echo ===================================================
echo.

java -cp "bin" AsteroidServerMain 8080
pause
