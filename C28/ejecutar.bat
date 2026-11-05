@echo off
chcp 65001 > nul
if not exist "credentials.json" (
    echo [AVISO] No se encontro credentials.json en esta carpeta.
    echo Descarga primero la clave JSON de tu Cuenta de Servicio en GCP y guardala como credentials.json.
    pause
    exit /b 1
)

set BUCKET=%1
set OBJETO=%2
if "%BUCKET%"=="" set BUCKET=bucket-ukranio
if "%OBJETO%"=="" set OBJETO=cupcake.jpg

echo [INFO] Ejecutando GoogleOAuthJWT para bucket="%BUCKET%" y objeto="%OBJETO%"...
java -cp ".;gson-2.14.0.jar" GoogleOAuthJWT %BUCKET% %OBJETO%
pause

