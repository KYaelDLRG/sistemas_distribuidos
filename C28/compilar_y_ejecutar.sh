#!/bin/bash
# Script para ejecutar en consola UNIX (Google Cloud Shell, WSL o Git Bash)
# Uso: ./compilar_y_ejecutar.sh <nombre_bucket> <nombre_objeto>

BUCKET=${1:-"bucket-ukranio"}
OBJETO=${2:-"cupcake.jpg"}

echo "=== 1. Listado largo de archivos ANTES de la descarga ==="
ls -lh

echo ""
echo "=== 2. Compilando y ejecutando GoogleOAuthJWT ==="
javac -cp .:gson-2.14.0.jar GoogleOAuthJWT.java
java -cp .:gson-2.14.0.jar GoogleOAuthJWT "$BUCKET" "$OBJETO"

echo ""
echo "=== 3. Listado largo de archivos DESPUES de la descarga ==="
ls -lh

