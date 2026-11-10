#!/bin/sh
set -eu

export DISPLAY=:99

echo "[CareStock] Iniciando pantalla virtual..."
Xvfb :99 -screen 0 1280x800x24 -nolisten tcp \
  > /tmp/xvfb.log 2>&1 &

# Esperar a que Xvfb este disponible
i=0
until xdpyinfo -display "$DISPLAY" >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -ge 30 ]; then
    cat /tmp/xvfb.log >&2
    exit 1
  fi
  sleep 1
done

echo "[CareStock] Iniciando VNC..."
x11vnc -display "$DISPLAY" \
  -rfbport 5900 -localhost \
  -forever -shared -nopw -quiet \
  > /tmp/vnc.log 2>&1 &

echo "[CareStock] Iniciando noVNC..."
websockify --web=/usr/share/novnc \
  0.0.0.0:6080 127.0.0.1:5900 \
  > /tmp/novnc.log 2>&1 &

echo "[CareStock] Iniciando JavaFX..."
exec java \
  --module-path /app/carestock.jar:/app/libs \
  -m org.example.carestock/org.example.carestock.CareStockApp
