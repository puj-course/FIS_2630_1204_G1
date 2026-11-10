#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

command -v java >/dev/null 2>&1 || {
  echo 'Falta Java 21 en PATH.'
  exit 1
}

cd "$RAIZ/src/CareStock"

java -version
bash ./mvnw -B -ntp clean test

echo 'Configuracion comprobada.'
