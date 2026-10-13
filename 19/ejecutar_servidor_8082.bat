@echo off
setlocal
chcp 65001 >nul
title Servidor HTTP - Puerto 8082

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\java.exe" (
    set "JAVA=%JAVA_HOME_BIN%\java.exe"
) else (
    set "JAVA=java"
)

echo Iniciando Servidor HTTP en el puerto 8082...
"%JAVA%" ServidorHttp 8082
pause
endlocal
