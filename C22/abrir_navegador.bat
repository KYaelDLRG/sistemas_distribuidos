@echo off
setlocal
chcp 65001 >nul
title Abrir Interfaz Web NGINX

echo Abriendo http://localhost:8088/ en el navegador predeterminado...
start http://localhost:8088/
endlocal
