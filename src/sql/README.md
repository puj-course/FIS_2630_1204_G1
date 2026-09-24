# SQL de CareStock

Orden recomendado para una instalación nueva:

1. `ddl/01_schema_carestock.sql`
2. `ddl/02_functions_triggers.sql`
3. `dml/01_seed_roles_permisos.sql`
4. `dml/02_seed_catalogo_base.sql`
5. `DatosPrueba.sql` (solo desarrollo/pruebas)
6. `procedures/01_sp_ingreso_lotes.sql`

El flujo Java de ingreso de lotes depende de `sp_registrar_nuevo_lote` y del trigger `trg_actualizar_stock_total`.
