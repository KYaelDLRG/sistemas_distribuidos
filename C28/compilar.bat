@echo off
chcp 65001 > nul
echo [INFO] Compilando GoogleOAuthJWT.java con gson-2.14.0.jar...
javac -encoding UTF-8 -cp ".;gson-2.14.0.jar" GoogleOAuthJWT.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Compilacion exitosa: GoogleOAuthJWT.class generado.
) else (
    echo [ERROR] Fallo en la compilacion.
)
pause

