@echo off
setlocal
set "PATH=C:\Program Files\Amazon Corretto\jdk21.0.4_7\bin;%PATH%"

echo =======================================================================
echo   EJECUCION ITEM 3: GOOGLE CLOUD TRANSLATION API (POST CON API KEY)
echo   Alumno: Kaleb Yael De La Rosa Gutierrez - Grupo: 7CM1
echo =======================================================================

if not exist ClienteTranslationAPI.class (
    echo Compilando ClienteTranslationAPI.java...
    javac -encoding UTF-8 ClienteTranslationAPI.java
)

set "API_KEY=%~1"

if "%API_KEY%"=="" (
    if exist api_key.txt (
        echo Leyendo clave desde api_key.txt...
        java -cp . ClienteTranslationAPI
    ) else (
        echo.
        echo [AVISO] Ingrese su API Key de Google Cloud Translation a continuacion:
        set /p API_KEY="API Key: "
        java -cp . ClienteTranslationAPI "%API_KEY%"
    )
) else (
    java -cp . ClienteTranslationAPI "%API_KEY%"
)

echo.
pause
