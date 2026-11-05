#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$RAIZ/src/CareStock"

# CareStock es una aplicacion JavaFX de escritorio.
bash ./mvnw -B -ntp clean package

echo "Artefacto generado en $RAIZ/src/CareStock/target/"
