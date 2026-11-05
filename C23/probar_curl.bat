@echo off
chcp 65001 > nul
echo =================================================================
echo   Prueba de los 4 Metodos REST con curl (micro-SAES)
echo =================================================================
echo.

echo 1. Insertando Alumno 1 (POST):
curl --data "Alejandro,Pérez,Castro,8.3" localhost:8080/alumnos
echo.

echo 2. Insertando Alumno 2 (POST):
curl --data "Arturo,Carmona,Trujillo,5.9" localhost:8080/alumnos
echo.

echo 3. Insertando Alumno 3 (POST):
curl --data "Mariana,Cuellar,Paz,7.6" localhost:8080/alumnos
echo.

echo 4. Consultando Alumno con ID=1 (GET):
curl localhost:8080/alumnos/1
echo.

echo 5. Consultando Alumno con ID=2 (GET):
curl localhost:8080/alumnos/2
echo.

echo 6. Actualizando Alumno con ID=2 con promedio 8.2 (PUT):
curl -X PUT --data "Arturo,Carmona,Trujillo,8.2" localhost:8080/alumnos/2
echo.

echo 7. Verificando actualizacion de Alumno con ID=2 (GET):
curl localhost:8080/alumnos/2
echo.

echo 8. Borrando Alumno con ID=1 (DELETE):
curl -X DELETE localhost:8080/alumnos/1
echo.

echo 9. Consultando Alumno con ID=1 eliminado (GET - espera Empty reply from server):
curl localhost:8080/alumnos/1
echo.
echo =================================================================
echo   Pruebas finalizadas.
echo =================================================================
pause
