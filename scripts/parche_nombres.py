#!/usr/bin/env python3
"""Refactorizacion controlada de nombres para CareStock."""

import argparse
import re
import subprocess
from pathlib import Path

# Nombres actuales -> nombres normalizados
RENOMBRES = {
    "CareStockApp": "AplicacionCareStock",
    "ConexionBD": "ConexionBaseDatos",

    # Controladores MVC
    "CatalogoController": "ControladorCatalogo",
    "ConsultaInventarioController": "ControladorConsultaInventario",
    "InventarioController": "ControladorInventario",
    "LoginController": "ControladorInicioSesion",

    # Acceso a datos
    "AseoDAO": "AseoDao",
    "CatalogoDAO": "CatalogoDao",
    "ConsultaInventarioDAO": "ConsultaInventarioDao",
    "LoteDAO": "LoteDao",
    "LoteDAOImpl": "LoteDaoJdbc",
    "MaternidadDAO": "MaternidadDao",
    "MedicamentoCatalogoDAO": "MedicamentoCatalogoDao",
    "MedicamentoDAO": "MedicamentoDao",
    "MedicamentoDAOImpl": "MedicamentoDaoJdbc",
    "UsuarioDAO": "UsuarioDao",
    "UsuarioDAOImpl": "UsuarioDaoJdbc",

    # Servicios
    "AuthenticationService": "ServicioAutenticacion",
    "ControlVencimientosService": "ServicioControlVencimientos",
    "FarmacovigilanciaService": "ServicioFarmacovigilancia",
    "RegistroLoteService": "ServicioRegistroLote",

    # Clases de demostracion JavaFX
    "HelloApplication": "AplicacionEjemplo",
    "HelloController": "ControladorEjemplo",
    "Launcher": "LanzadorEjemplo",
}

EXTENSIONES = {
    ".java", ".fxml", ".xml", ".md", ".puml",
    ".drawio", ".sh", ".ps1", ".cmd",
    ".properties", ".yaml", ".yml", ".json",
    ".txt", ".jsh",
}

EXCLUIR = {
    ".git", ".idea", ".mvn", "target",
    "build", "out", "node_modules", ".venv",
}


def git(raiz, *args):
    return subprocess.run(
        ["git", *args],
        cwd=raiz,
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )


def mover_con_git(raiz, origen, destino):
    anterior = origen.relative_to(raiz).as_posix()
    nuevo = destino.relative_to(raiz).as_posix()

    # Evita conflictos con core.ignorecase en Windows.
    temporal = anterior + ".nombres_tmp"

    if (raiz / temporal).exists():
        raise RuntimeError(
            f"Ya existe el archivo temporal: {temporal}"
        )

    git(raiz, "mv", "-f", "--", anterior, temporal)
    git(raiz, "mv", "-f", "--", temporal, nuevo)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Mostrar cambios sin modificar archivos",
    )
    args = parser.parse_args()

    raiz = Path(
        git(
            Path.cwd(),
            "rev-parse",
            "--show-toplevel",
        ).stdout.strip()
    ).resolve()

    modulo = raiz / "src" / "CareStock"
    fuentes = modulo / "src"

    if not (modulo / "pom.xml").is_file():
        raise SystemExit(
            "ERROR: ejecuta desde FIS_2630_1204_G1"
        )

    # Identificar archivos Java reales.
    javas = [
        p for p in raiz.rglob("*.java")
        if not any(
            x in EXCLUIR
            for x in p.relative_to(raiz).parts
        )
    ]

    presentes = {p.stem for p in javas}

    activos = {
        anterior: nuevo
        for anterior, nuevo in RENOMBRES.items()
        if anterior in presentes
    }

    no_encontrados = sorted(
        set(RENOMBRES) - presentes
    )

    # Normalizar paquete DataTransferObject -> dto.
    carpetas = [
        fuentes / sub / "org" / "example"
        / "carestock" / "DataTransferObject"
        for sub in ("main/java", "test/java")
    ]

    carpetas = [
        p for p in carpetas if p.is_dir()
    ]

    sustituciones = dict(activos)

    if carpetas:
        sustituciones["DataTransferObject"] = "dto"

    if not sustituciones:
        raise SystemExit(
            "No hay nombres pendientes de normalizacion."
        )

    # Sustituye identificadores completos, nunca
    # fragmentos de nombres.
    patron = re.compile(
        r"\b(?:"
        + "|".join(
            map(
                re.escape,
                sorted(
                    sustituciones,
                    key=len,
                    reverse=True,
                ),
            )
        )
        + r")\b"
    )

    textos = []

    for p in raiz.rglob("*"):
        if not p.is_file():
            continue

        if p.suffix.lower() not in EXTENSIONES:
            continue

        if any(
            parte in EXCLUIR
            for parte in p.relative_to(raiz).parts
        ):
            continue

        bruto = p.read_bytes()

        try:
            contenido = bruto.decode("utf-8-sig")
        except UnicodeDecodeError:
            continue

        actualizado = patron.sub(
            lambda m: sustituciones[m.group()],
            contenido,
        )

        if actualizado != contenido:
            bom = bruto.startswith(
                b"\xef\xbb\xbf"
            )

            codificacion = (
                "utf-8-sig" if bom else "utf-8"
            )

            textos.append(
                (
                    p,
                    actualizado.encode(codificacion),
                )
            )

    # Planificar cambios de nombres de archivos.
    cambios_archivos = []

    for p in javas:
        if p.stem not in activos:
            continue

        destino = p.with_name(
            activos[p.stem] + ".java"
        )

        if destino == p:
            continue

        if (
            destino.exists()
            and destino.name.lower() != p.name.lower()
        ):
            raise SystemExit(
                f"ERROR: colision de archivo: {destino}"
            )

        cambios_archivos.append(
            (p, destino)
        )

    print("\n=== PARCHE CARESTOCK ===")
    print(
        "Clases e interfaces a renombrar:",
        len(activos),
    )
    print(
        "Archivos Java a mover:",
        len(cambios_archivos),
    )
    print(
        "Archivos de texto a actualizar:",
        len(textos),
    )
    print(
        "Directorios DTO a mover:",
        len(carpetas),
    )

    if no_encontrados:
        print(
            "Nombres ausentes, omitidos:",
            ", ".join(no_encontrados),
        )

    print("\nRenombramientos:")

    for anterior, nuevo in activos.items():
        print(f"  {anterior} -> {nuevo}")

    if args.dry_run:
        print(
            "\nSimulacion finalizada sin modificar archivos."
        )
        return

    # Aplicar cambios a referencias.
    for p, datos in textos:
        p.write_bytes(datos)

    # Renombrar clases mediante Git.
    for origen, destino in cambios_archivos:
        mover_con_git(raiz, origen, destino)

    # Cambiar directorios y paquetes.
    for origen in carpetas:
        mover_con_git(
            raiz,
            origen,
            origen.with_name("dto"),
        )

    # Documentar el resultado del parche.
    informe = (
        raiz / "docs" / "architecture"
        / "NOMENCLATURA_CLASES.md"
    )

    informe.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    filas = "\n".join(
        f"| `{anterior}` | `{nuevo}` |"
        for anterior, nuevo in activos.items()
    )

    informe.write_text(
        "# Normalizacion de nombres - CareStock\n\n"
        "Refactorizacion de nomenclatura sin modificar "
        "las reglas de negocio.\n\n"
        "| Nombre anterior | Nombre nuevo |\n"
        "|---|---|\n"
        + filas
        + "\n\n"
        "Paquete: "
        "`org.example.carestock.DataTransferObject`"
        " -> `org.example.carestock.dto`.\n\n"
        "Verificar con `./mvnw clean verify` "
        "desde `src/CareStock`.\n",
        encoding="utf-8",
    )

    print("\nPARCHE APLICADO.")
    print(
        "Revisa git status y ejecuta las pruebas Maven."
    )


if __name__ == "__main__":
    main()
