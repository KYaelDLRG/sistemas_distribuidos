@echo off
setlocal
chcp 65001 >nul
title NGINX Reverse Proxy :8088

cd nginx
echo ======================================================================
echo Iniciando NGINX Reverse Proxy en http://localhost:8088/
echo Enrutando /dispatcher hacia http://localhost:8080/dispatcher
echo ======================================================================
echo.

start nginx.exe
cd ..
echo NGINX iniciado exitosamente en segundo plano.
echo Puede abrir en su navegador: http://localhost:8088/
echo.
pause
endlocal
