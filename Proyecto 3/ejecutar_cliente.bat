@echo off
setlocal
title Cliente Lanterna - Terminal 2
echo ===================================================
echo  TERMINAL 2: CLIENTE TUI DE ASTEROIDES (LANTERNA)
echo  Alumno: Kaleb Yael De La Rosa Gutierrez - 7CM1
echo  Conectando a: http://localhost:8080
echo ===================================================
echo.

java -cp "bin;lib/lanterna-3.1.2.jar" AsteroidClientMain http://localhost:8080
pause
