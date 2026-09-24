#!/usr/bin/env bash
set -euo pipefail

command -v java >/dev/null 2>&1 || { echo "Java no está instalado."; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Maven no está instalado."; exit 1; }

java -version
mvn -version
mvn -B clean test

echo "CareStock listo. Configure DB_URL, DB_USER, DB_PASSWORD y CARESTOCK_USER_EMAIL antes de ejecutar."
