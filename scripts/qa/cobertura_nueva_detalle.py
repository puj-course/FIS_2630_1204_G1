#!/usr/bin/env python3
"""Diagnostico del mismo indicador que calcula pre-pr-coverage.yml.

Ejecutar desde la raiz: python scripts/qa/cobertura_nueva_detalle.py
Necesita informe Maven en src/CareStock/target/site/jacoco/jacoco.xml.
"""
from collections import defaultdict
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

RAIZ = 'src/CareStock/src/main/java/'
INFORME = Path('src/CareStock/target/site/jacoco/jacoco.xml')

if not INFORME.is_file():
    sys.exit('Falta el JaCoCo XML. Ejecuta primero: cd src/CareStock && mvn clean verify && cd ../..')

diff = subprocess.check_output(
    ['git', 'diff', '--no-ext-diff', '--no-renames', '--unified=0',
     'origin/main...HEAD', '--', RAIZ], text=True)
modificadas = defaultdict(set)
archivo = None
for linea in diff.splitlines():
    if linea.startswith('+++ b/'):
        ruta = linea[6:]
        archivo = ruta[len(RAIZ):] if ruta.startswith(RAIZ) and ruta.endswith('.java') else None
    elif linea.startswith('@@ ') and archivo:
        match = re.match(r'@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@', linea)
        if match:
            inicial = int(match.group(1))
            cantidad = int(match.group(2) or 1)
            modificadas[archivo].update(range(inicial, inicial + cantidad))

jacoco = {}
for paquete in ET.parse(INFORME).getroot().findall('package'):
    for fuente in paquete.findall('sourcefile'):
        ruta = f"{paquete.get('name', '')}/{fuente.get('name')}".lstrip('/')
        jacoco[ruta] = {
            int(linea.get('nr')): (
                int(linea.get('ci', 0)),
                int(linea.get('mb', 0)),
                int(linea.get('cb', 0)))
            for linea in fuente.findall('line')
        }

por_archivo = []
for ruta, numeros in sorted(modificadas.items()):
    if ruta in ('module-info.java', 'package-info.java'):
        continue
    if ruta not in jacoco:
        sys.exit(f'JaCoCo no reporto el archivo: {ruta}')
    ejecutables = [(n, jacoco[ruta][n]) for n in sorted(numeros) if n in jacoco[ruta]]
    tl = len(ejecutables)
    cl = sum(ci > 0 for _, (ci, _, _) in ejecutables)
    tb = sum(mb + cb for _, (_, mb, cb) in ejecutables)
    cb = sum(cb for _, (_, _, cb) in ejecutables)
    pendientes = [n for n, (ci, mb, _) in ejecutables if ci == 0 or mb > 0]
    if tl + tb:
        por_archivo.append((ruta, cl, tl, cb, tb, pendientes))

por_archivo.sort(key=lambda x: (x[2] - x[1]) + (x[4] - x[3]), reverse=True)
print('\nCOBERTURA NUEVA POR ARCHIVO (ordenado por deficit)\n')
for ruta, cl, tl, cb, tb, pendientes in por_archivo:
    deficit = tl - cl + tb - cb
    print(f'{ruta}\n  Lineas: {cl}/{tl} | Ramas: {cb}/{tb} | Pendientes: {deficit}')
    if pendientes:
        print('  Lineas por revisar:', ', '.join(map(str, pendientes)))

total = sum(v[2] + v[4] for v in por_archivo)
cubierto = sum(v[1] + v[3] for v in por_archivo)
if total:
    print(f'\nTOTAL: {100*cubierto/total:.2f}% ({cubierto}/{total})')
    print('Elementos adicionales requeridos:', max(0, (4 * total + 4) // 5 - cubierto))
else:
    print('\nNo hay lineas nuevas ejecutables frente a origin/main.')
