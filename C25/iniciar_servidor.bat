@echo off
chcp 65001 > nul
echo Iniciando WebServer en puerto 8080...
start "WebServer - Puerto 8080" cmd /k "java -cp .;lib/gson-2.9.1.jar WebServer 8080"
