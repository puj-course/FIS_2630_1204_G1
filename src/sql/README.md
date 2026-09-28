# SQL de CareStock

Orden recomendado para una instalación nueva:

1. `ddl/01_schema_carestock.sql`
2. `ddl/02_functions_triggers.sql`
3. `dml/01_seed_roles_permisos.sql`
4. `dml/02_seed_catalogo_base.sql`
5. `DatosPrueba.sql` (solo desarrollo/pruebas)
6. `procedures/01_sp_ingreso_lotes.sql`

El flujo Java de ingreso de lotes depende de `sp_registrar_nuevo_lote` y del trigger `trg_actualizar_stock_total`.

## Migración multifarmacia - Issue #457

Para una base de datos existente, ejecutar adicionalmente:

7. `ddl/04_multifarmacia_inventario.sql`

Esta migración crea el catálogo `FARMACIAS`, asigna los registros actuales a
`SEDE-PRINCIPAL` y agrega `MEDICAMENTOS.id_farmacia` con clave foránea e índice.

La selección automática de la farmacia desde la sesión se integra posteriormente
mediante el contexto de sesión y la vista de inventario.
