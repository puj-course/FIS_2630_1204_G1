# CareStock - Ampliacion de cobertura de controladores

## Objetivo

Incrementar la cobertura de codigo nuevo requerida
por SonarCloud hasta superar el umbral del 80%.

## Intervencion

Se agregan pruebas JUnit 5 con JavaFX para:

- Busqueda y filtros de lotes.
- Filtros por categoria y fecha.
- Lotes vencidos y disponibles.
- Estados sanitarios.
- Carga de inventario y contadores.
- Manejo de fallos simulados de categorias.
- Resumen de alertas.
- Filtros por tipo, categoria y severidad.
- Configuracion de columnas.
- Presentacion de movimientos.
- Rechazo de sesiones invalidas.

## Estrategia de aislamiento

- Fachada Inventario simulada con Mockito.
- Sin operaciones sobre Neon.
- Reutilizacion de plataforma JavaFX existente.
- Pruebas estructuradas mediante AAA.

## Verificacion

La validacion final se realiza mediante:

./mvnw clean verify

El porcentaje definitivo de codigo nuevo debe
confirmarse en SonarCloud despues de ejecutar
el analisis del Pull Request.

## Criterio de aceptacion

- Ninguna prueba fallida.
- Ningun error en la compilacion.
- Sin regresiones del inventario.
- Quality Gate con new_coverage >= 80%.
