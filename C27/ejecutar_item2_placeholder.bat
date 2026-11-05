@echo off
setlocal
set "PATH=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin;%PATH%"

echo =======================================================================
echo   EJECUCION ITEM 2: CREACION DE RECURSO REST (JSONPlaceholder - POST)
echo   Alumno: Kaleb Yael De La Rosa Gutierrez - Grupo: 7CM1
echo =======================================================================

if not exist ClientePlaceholder.class (
    echo Compilando ClientePlaceholder.java...
    javac -encoding UTF-8 ClientePlaceholder.java
)

java -cp . ClientePlaceholder "7CM1" "Kaleb Yael De La Rosa Gutierrez"
echo.
pause
