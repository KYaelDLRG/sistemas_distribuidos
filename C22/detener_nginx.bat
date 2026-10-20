@echo off
setlocal
chcp 65001 >nul
title Detener NGINX

cd nginx
echo Deteniendo procesos de NGINX...
nginx.exe -s stop
cd ..
echo NGINX detenido.
pause
endlocal
