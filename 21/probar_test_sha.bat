@echo off
echo ========================================================
echo Probando TestSHA (Verificacion hola, Hola, HOLA)
echo ========================================================
javac --release 8 TestSHA.java
if %errorlevel% neq 0 (
    echo Error al compilar TestSHA.java
    exit /b %errorlevel%
)
java TestSHA
echo ========================================================
