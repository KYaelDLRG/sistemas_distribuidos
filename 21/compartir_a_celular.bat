@echo off
echo ===================================================================
echo   SERVIDOR PARA TRANSFERIR Ejercicio1.java A TU CELULAR (TERMUX)
echo ===================================================================
echo Tu Laptop tiene la IP local: 192.168.2.111
echo.
echo Pasos en tu celular Android (conectado al mismo Wi-Fi):
echo   1. Abre Termux.
echo   2. Ejecuta el siguiente comando para descargar Ejercicio1.java:
echo.
echo      curl -O http://192.168.2.111:8000/Ejercicio1.java
echo.
echo Presiona Ctrl+C en esta ventana cuando ya lo hayas descargado.
echo ===================================================================
python -m http.server 8000
