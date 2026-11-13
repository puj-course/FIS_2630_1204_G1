#!/usr/bin/env bash
set -Eeuo pipefail

IMAGEN="${1:?Debe indicar la imagen Docker}"
NOMBRE="carestock-smoke-${GITHUB_RUN_ID:-local}-$$"
PUERTO=16089

limpiar() {
  docker rm -f "$NOMBRE" >/dev/null 2>&1 || true
}

trap limpiar EXIT

echo "[1/4] Descargando imagen publicada..."
docker pull "$IMAGEN"

echo "[2/4] Creando contenedor de prueba..."
docker run -d \
  --name "$NOMBRE" \
  -p "127.0.0.1:${PUERTO}:6080" \
  "$IMAGEN"

echo "[3/4] Verificando noVNC..."

EXITO=0

for intento in $(seq 1 40); do
  if curl -fsS --max-time 3 \
    -o /dev/null \
    "http://127.0.0.1:${PUERTO}/vnc.html"; then
    EXITO=1
    break
  fi
  sleep 2
done

if [ "$EXITO" -ne 1 ]; then
  echo "ERROR: noVNC no respondio."
  docker logs "$NOMBRE" 2>&1 || true
  exit 1
fi

sleep 3

test "$(docker inspect \
  --format '{{.State.Running}}' "$NOMBRE")" = "true"

docker logs "$NOMBRE" 2>&1 \
  | grep -q '\[CareStock\] Iniciando JavaFX'

echo "[4/4] Validacion completada."
echo "OK: Imagen descargada."
echo "OK: Contenedor funcionando."
echo "OK: JavaFX iniciado."
echo "OK: noVNC responde."
