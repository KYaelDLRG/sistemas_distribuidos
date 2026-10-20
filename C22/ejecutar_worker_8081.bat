@echo off
setlocal
chcp 65001 >nul
title Servidor Worker 1 (Puerto 8081)

set "DIR_21=D:\SistemasDistribuidos\21"
if not exist "%DIR_21%\ServidorHttp.class" (
    echo Compilando ServidorHttp en %DIR_21%...
    javac --release 8 "%DIR_21%\ServidorHttp.java"
)

echo Iniciando Worker 1 en http://localhost:8081/task ...
java -cp "%DIR_21%" ServidorHttp 8081
pause
endlocal
