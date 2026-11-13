#!/usr/bin/env bash
set -Eeuo pipefail

trap 'echo "ERROR: Preparacion interrumpida en la linea $LINENO" >&2' ERR

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$RAIZ/src/CareStock"

echo "====================================="
echo "  CareStock - Preparacion para Docker"
echo "====================================="

echo "[1/4] Compilando CareStock..."

bash ./mvnw -B -ntp -DskipTests clean package

if [ ! -s target/CareStock-1.0-SNAPSHOT.jar ]; then
    echo "ERROR: No se genero el JAR."
    exit 1
fi

echo "OK: JAR generado."

echo "[2/4] Descargando dependencias..."

bash ./mvnw -B -ntp \
  org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies \
  -DincludeScope=runtime \
  -DoutputDirectory=target/docker-libs

if [ ! -d target/docker-libs ]; then
    echo "ERROR: No se genero docker-libs."
    exit 1
fi

echo "[3/4] Preparando JavaFX para Linux..."

mkdir -p target/docker-libs-linux

find target/docker-libs -maxdepth 1 \
  -type f -name '*.jar' \
  ! -name 'javafx-*' \
  -exec cp {} target/docker-libs-linux/ \;

for modulo in base controls fxml graphics media swing; do

    archivo="javafx-${modulo}-21-linux.jar"
    destino="target/docker-libs-linux/$archivo"

    echo "Preparando $archivo"

    if [ -f "target/docker-libs/$archivo" ]; then
        cp "target/docker-libs/$archivo" "$destino"
    else
        curl -fLsS --retry 3 \
          -o "$destino" \
          "https://repo.maven.apache.org/maven2/org/openjfx/javafx-${modulo}/21/$archivo"
    fi

    jar tf "$destino" >/dev/null
done

echo "[4/4] Validando archivos..."

CANTIDAD=$(find target/docker-libs-linux \
  -maxdepth 1 -type f \
  -name 'javafx-*-linux.jar' | wc -l)

echo "Librerias JavaFX Linux: $CANTIDAD"

test "$CANTIDAD" -eq 6

test -n "$(find target/docker-libs-linux \
  -maxdepth 1 -type f \
  ! -name 'javafx-*' -print -quit)"

test -s target/CareStock-1.0-SNAPSHOT.jar

echo "====================================="
echo " BUILD DE ARTEFACTOS COMPLETADO"
echo "====================================="
echo "Docker esta listo para construir."
