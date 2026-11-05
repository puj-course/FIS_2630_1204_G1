# Auditoría de SQL y scripts - CareStock

Fecha: 05/11/2026

## Objetivo

Mejorar la mantenibilidad y confiabilidad del repositorio,
sin modificar las funcionalidades de CareStock.

## Inventario inicial

Se identificaron 47 archivos SQL en el ZIP analizado:

- 40 en src/sql.
- 5 migraciones en scripts/migrations.
- 2 archivos de pruebas en scripts/tests.

## Hallazgos SQL

1. Se conserva el historial de migraciones.
2. Los archivos de consistencia de inventario ubicados en
   verificacion/ y verificaciones/ contienen comprobaciones
   diferentes. No deben eliminarse por similitud de nombres.
3. Se detectó una referencia histórica obsoleta en el README SQL.
4. La funcionalidad SMS contiene 12 comprobaciones de integración
   reutilizables de la Issue #926.

## Hallazgos Shell y PowerShell

Los scripts principales utilizaban rutas Maven incompatibles
con la estructura actual del repositorio.

Se ajustan para utilizar src/CareStock como directorio Maven,
conservando Maven Wrapper y las configuraciones existentes.

## Automatización

Se agrega el workflow qa-sql-scripts.yml con:

- Validación de sintaxis Shell.
- Análisis ShellCheck.
- Verificación de sintaxis PowerShell.
- Validación de sintaxis Python.
- PostgreSQL 16 temporal para pruebas SQL de SMS.

## Seguridad

No se ejecutan migraciones en Neon.

No se modifican los scripts SQL históricos.

No se modifican las funcionalidades Java ni los workflows
existentes de CI, CD, SonarQube y Telegram.

## Validación pendiente

Confirmar que ambos jobs del nuevo workflow finalicen
correctamente en GitHub Actions.

## Trabajo futuro

Revisar la secuencia completa de migraciones restantes,
incluidos los archivos con prefijos repetidos, y ampliar las
pruebas SQL a movimientos, lotes e integridad de inventario.

No eliminar ni consolidar migraciones sin analizar sus
dependencias y su historial de aplicación.
