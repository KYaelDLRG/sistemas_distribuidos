#!/bin/bash
# Script para lanzar dos clientes simultáneamente y medir sus tiempos con 'time'
echo "Lanzando Cliente 1 y Cliente 2 simultáneamente a http://localhost:8000/tarea..."
echo "------------------------------------------------------------------------"

(time curl -s http://localhost:8000/tarea) &
PID1=$!

(time curl -s http://localhost:8000/tarea) &
PID2=$!

wait $PID1
wait $PID2

echo "------------------------------------------------------------------------"
echo "Prueba completada."
