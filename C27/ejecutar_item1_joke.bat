@echo off
setlocal
set "PATH=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin;%PATH%"

echo =======================================================================
echo     EJECUCION ITEM 1: CLIENTE REST PUBLICO (JokeAPI - GET)
echo     Alumno: Kaleb Yael De La Rosa Gutierrez - Grupo: 7CM1
echo =======================================================================

if not exist ClienteJokeAPI.class (
    echo Compilando ClienteJokeAPI.java...
    javac -encoding UTF-8 ClienteJokeAPI.java
)

java -cp . ClienteJokeAPI
echo.
pause
