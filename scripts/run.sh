#!/usr/bin/env bash

set -e

cd "$(dirname "$0")/.."

if [ ! -f ".env" ]; then
    echo "ERROR: No existe el archivo .env"
    exit 1
fi

set -a
source .env
set +a

echo "=== CareStock ==="
echo "Compilando y ejecutando pruebas..."

./mvnw clean verify

echo "Compilacion correcta."
echo "Iniciando CareStock..."

./mvnw javafx:run