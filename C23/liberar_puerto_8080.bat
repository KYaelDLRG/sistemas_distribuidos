@echo off
chcp 65001 > nul
echo =================================================================
echo   Liberando puerto 8080
echo =================================================================

set ENCONTRADO=0
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Deteniendo proceso con PID: %%a en puerto 8080...
    taskkill /F /PID %%a
    set ENCONTRADO=1
)

if %ENCONTRADO% EQU 0 (
    echo El puerto 8080 ya se encuentra completamente libre.
) else (
    echo [OK] Puerto 8080 liberado exitosamente.
)

pause
