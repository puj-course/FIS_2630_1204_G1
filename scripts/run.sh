#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Compatibilidad con configuraciones locales existentes.
if [[ -f "$RAIZ/.env" ]]; then
  set -a
  source "$RAIZ/.env"
  set +a
fi

cd "$RAIZ/src/CareStock"

echo '=== CareStock: compilacion y pruebas ==='
bash ./mvnw -B -ntp clean verify

echo '=== Iniciando JavaFX ==='
bash ./mvnw -B -ntp javafx:run
