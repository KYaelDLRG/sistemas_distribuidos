@echo off
setlocal
chcp 65001 >nul

set "JAVA_HOME_BIN=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin"
if exist "%JAVA_HOME_BIN%\javac.exe" (
    set "JAVAC=%JAVA_HOME_BIN%\javac.exe"
    set "JAR=%JAVA_HOME_BIN%\jar.exe"
) else (
    set "JAVAC=javac"
    set "JAR=jar"
)

echo [1/2] Compilando networking/WebClient.java, Aggregator.java, Application.java, ServidorHttp.java...
"%JAVAC%" networking/WebClient.java Aggregator.java Application.java ServidorHttp.java
if errorlevel 1 (
    echo Error de compilacion.
    pause
    exit /b 1
)

echo [2/2] Empaquetando Application.jar...
"%JAR%" -cfe Application.jar Application Application.class Aggregator.class networking/WebClient.class
if errorlevel 1 (
    echo Error al crear Application.jar.
    pause
    exit /b 1
)

echo.
echo =======================================================
echo Compilacion exitosa. Application.jar generado.
echo =======================================================
endlocal
