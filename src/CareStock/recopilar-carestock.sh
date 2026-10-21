#!/usr/bin/env bash
set -euo pipefail

SALIDA="$HOME/Desktop/CareStock_fuentes_Proxypatch.txt"

ARCHIVOS=(
    "src/main/java/org/example/carestock/controller/ControladorCatalogo.java"
    "src/main/java/org/example/carestock/controller/ControladorInventario.java"
    "src/main/java/org/example/carestock/facade/Inventario.java"
    "src/main/java/org/example/carestock/dao/LoteDaoJdbc.java"
    "src/main/java/org/example/carestock/session/SesionUsuario.java"
)

# Validar archivos antes de generar el documento.
for archivo in "${ARCHIVOS[@]}"; do
    if [[ ! -f "$archivo" ]]; then
        echo "ERROR: No existe $archivo"
        exit 1
    fi
done

{
    echo "CARESTOCK - FUENTES ACTUALES"
    echo "RAMA: $(git branch --show-current)"
    echo

    for archivo in "${ARCHIVOS[@]}"; do
        echo "========================================"
        echo "ARCHIVO: $archivo"
        echo "========================================"

        cat "$archivo"

        echo
        echo "FIN: $archivo"
        echo
    done
} > "$SALIDA"

echo "Archivo generado correctamente:"
echo "$SALIDA"
