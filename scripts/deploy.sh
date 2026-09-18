#!/usr/bin/env bash
set -euo pipefail

# En el estado actual, "deploy" valida y empaqueta el artefacto Java.
# La aplicación es de escritorio JavaFX; no se despliega como servidor web.
mvn -B clean package

echo "Artefacto generado en target/."
