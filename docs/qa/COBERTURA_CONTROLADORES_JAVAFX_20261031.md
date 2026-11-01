# CareStock - Cobertura de controladores JavaFX

**Fecha:** 31 de octubre de 2026
**Rama:** test/coverage-controladores-main

## Resultados locales

| Indicador | Resultado |
|---|---:|
| Pruebas ejecutadas | 358 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Cobertura de instrucciones | 45.77% |
| Cobertura de ramas | 40.91% |
| Cobertura de lineas | 46.12% |

## Cobertura de controladores

| Archivo | Lineas cubiertas | Cobertura |
|---|---:|---:|
| ControladorAlertas.java | 27/137 | 19.71% |
| ControladorCatalogo.java | 157/466 | 33.69% |
| ControladorInventario.java | 32/292 | 10.96% |

## Pruebas implementadas

- Validacion de precios y valores limite.
- Lectura de datos de medicamentos, aseo y maternidad.
- Estados sanitarios de lotes.
- Validacion de sesiones y permisos.
- Comportamiento del formulario JavaFX.
- Limpieza y llenado de campos.
- Acceso al catalogo desde consulta unificada.

## Infraestructura

Los workflows `ci.yml` y `sonar.yml` utilizan
Xvfb para ejecutar pruebas JavaFX en Ubuntu.

## Validacion pendiente

- GitHub Actions del Pull Request.
- Cobertura de codigo nuevo en SonarCloud.
- Quality Gate PASSED.
- Verificacion del workflow de release Windows
  antes de publicar una nueva version.

La cobertura global de JaCoCo no equivale a
Coverage on New Code de SonarCloud.
