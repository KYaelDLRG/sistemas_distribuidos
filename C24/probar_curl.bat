@echo off
chcp 65001 > nul
echo =================================================================
echo   Prueba Completa de los 6 Incisos REST con curl (Clase 24)
echo   Servidor RESTful micro-SAES (Puerto 8080)
echo =================================================================
echo.

echo -----------------------------------------------------------------
echo [INCISO 1] Petición a endpoint inexistente o incorrecto (/chetito)
echo Debe devolver HTTP 404 Not Found y JSON de error
echo -----------------------------------------------------------------
curl -i http://localhost:8080/chetito
echo.

echo -----------------------------------------------------------------
echo [INCISO 2] Peticiones con identificadores no numéricos
echo Caso 2.1: GET /alumnos/p (Debe devolver HTTP 400 y {"error": "ID invalido"})
echo -----------------------------------------------------------------
curl -i http://localhost:8080/alumnos/p
echo.

echo Caso 2.2: DELETE /alumnos/f (Debe devolver HTTP 400 y {"error": "ID invalido"})
curl -i -X DELETE http://localhost:8080/alumnos/f
echo.

echo -----------------------------------------------------------------
echo [INCISO 3] POST /alumnos con cuerpo en formato JSON
echo Se envían datos de Carlos Pérez Castro (promedio 8.7)
echo Debe devolver HTTP 201 Created y el JSON del alumno con su nuevo ID
echo -----------------------------------------------------------------
curl -i -X POST -H "Content-Type: application/json" -d "{\"nombre\":\"Carlos\",\"apellido1\":\"Perez\",\"apellido2\":\"Castro\",\"promedio\":8.7}" http://localhost:8080/alumnos
echo.

echo Insertando alumnos adicionales para pruebas posteriores:
echo Insertando alumno 2 (Arturo Carmona Trujillo, 5.9):
curl -i -X POST -H "Content-Type: application/json" -d "{\"nombre\":\"Arturo\",\"apellido1\":\"Carmona\",\"apellido2\":\"Trujillo\",\"promedio\":5.9}" http://localhost:8080/alumnos
echo.
echo Insertando alumno 3 (Ana Carmona Trujillo, 9.2):
curl -i -X POST -H "Content-Type: application/json" -d "{\"nombre\":\"Ana\",\"apellido1\":\"Carmona\",\"apellido2\":\"Trujillo\",\"promedio\":9.2}" http://localhost:8080/alumnos
echo.

echo -----------------------------------------------------------------
echo [INCISO 4] GET /alumnos/{id}
echo Caso 4.1: GET /alumnos/3 - Alumno existente
echo Debe devolver HTTP 200 OK y el JSON con id, nombre, apellidos y promedio
echo -----------------------------------------------------------------
curl -i http://localhost:8080/alumnos/3
echo.

echo Caso 4.2: GET /alumnos/666 - Alumno inexistente
echo Debe devolver HTTP 404 Not Found y {"error": "Alumno no encontrado"}
echo -----------------------------------------------------------------
curl -i http://localhost:8080/alumnos/666
echo.

echo -----------------------------------------------------------------
echo [INCISO 5] PUT /alumnos/{id} con cuerpo en formato JSON
echo Caso 5.1: PUT /alumnos/1 - Actualización de alumno existente a promedio 9.2
echo Debe devolver HTTP 200 OK y {"mensaje": "Alumno actualizado"}
echo -----------------------------------------------------------------
curl -i -X PUT -H "Content-Type: application/json" -d "{\"nombre\":\"Carlos\",\"apellido1\":\"Perez\",\"apellido2\":\"Castro\",\"promedio\":9.2}" http://localhost:8080/alumnos/1
echo.

echo Verificando actualización con GET /alumnos/1:
curl -i http://localhost:8080/alumnos/1
echo.

echo Caso 5.2: PUT /alumnos/999 - Alumno inexistente
echo Debe devolver HTTP 404 Not Found y {"error": "Alumno no encontrado"}
echo -----------------------------------------------------------------
curl -i -X PUT -H "Content-Type: application/json" -d "{\"nombre\":\"Carlos\",\"apellido1\":\"Perez\",\"apellido2\":\"Castro\",\"promedio\":9.2}" http://localhost:8080/alumnos/999
echo.

echo -----------------------------------------------------------------
echo [INCISO 6] DELETE /alumnos/{id}
echo Caso 6.1: DELETE /alumnos/999 - Alumno inexistente para ser borrado
echo Debe devolver HTTP 404 Not Found y {"error": "Alumno no encontrado"}
echo -----------------------------------------------------------------
curl -i -X DELETE http://localhost:8080/alumnos/999
echo.

echo Caso 6.2: DELETE /alumnos/1 - Alumno existente
echo Debe devolver sólo el código HTTP 204 No Content (sin cuerpo)
echo -----------------------------------------------------------------
curl -i -X DELETE http://localhost:8080/alumnos/1
echo.

echo Verificando eliminación con GET /alumnos/1 (Debe dar 404 Not Found):
curl -i http://localhost:8080/alumnos/1
echo.

echo -----------------------------------------------------------------
echo [EXTRA REST] GET /alumnos (Listar todos los alumnos restantes en JSON)
echo -----------------------------------------------------------------
curl -i http://localhost:8080/alumnos
echo.

echo =================================================================
echo   Pruebas finalizadas con éxito.
echo =================================================================
pause
