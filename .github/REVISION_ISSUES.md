# Revisión de `.github`

Se verificaron las plantillas de Issues y los workflows incluidos en el proyecto.

## Resultado

- Las siete plantillas Markdown de `.github/ISSUE_TEMPLATE/` tienen front matter YAML válido.
- `name` y `about` están presentes en todas las plantillas, por lo que la estructura básica es compatible con las plantillas clásicas de GitHub.
- `ci.yml` y `cd.yml` no intervienen en la creación de Issues; son workflows de GitHub Actions.
- Se agregó `ISSUE_TEMPLATE/config.yml` con `blank_issues_enabled: true` para mantener disponible la creación de una Issue en blanco.
- Se normalizaron los saltos de línea de los archivos de `.github` a LF.

## Importante

Si el cuadro **Create new issue** del Project queda cargando, revisar en el Project de GitHub:

`Project -> ... -> Settings -> Default repository`

Seleccionar `FIS_2630_1204_G1` como repositorio predeterminado y guardar.

También se puede comprobar el origen del problema entrando directamente a:

`Repositorio -> Issues -> New issue`

Si desde allí se abre normalmente el selector de plantillas, las plantillas de `.github` no son la causa del bloqueo del Project.
