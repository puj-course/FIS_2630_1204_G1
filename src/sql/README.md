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


## Contexto de farmacia de usuarios - Issue #456

Después de la migración multifarmacia de inventario, ejecutar:

8. `ddl/05_usuario_farmacia_issue_456.sql`

Esta migración:

- incorpora `id_farmacia` en `USUARIOS`;
- asocia los usuarios existentes con la Farmacia Principal;
- incorpora la relación entre usuario y farmacia;
- actualiza `fn_crear_usuario` para recibir la farmacia obtenida desde la sesión;
- evita que `id_farmacia` tenga que ser ingresado manualmente desde JavaFX.

La columna se conserva nullable para permitir posteriormente roles globales
como `SUPER_ADMIN`, cuya definición de permisos se implementará por separado.

## Contexto de farmacia y administración jerárquica - Issue #456

Después de la migración multifarmacia #457 ejecutar:

`ddl/05_superadmin_jerarquia_usuarios_issue_456.sql`

Jerarquía implementada:

- `SUPER_ADMIN`: usuario global de CareStock, sin farmacia obligatoria.
- `SUPER_ADMIN` puede crear únicamente `ADMINISTRADOR`.
- `SUPER_ADMIN` selecciona la farmacia del nuevo administrador.
- `ADMINISTRADOR` pertenece obligatoriamente a una farmacia.
- `ADMINISTRADOR` puede crear únicamente `FARMACEUTICO`.
- El farmacéutico hereda automáticamente la farmacia del administrador.
- `FARMACEUTICO` no puede crear usuarios.
- La asignación de roles se valida tanto en Java como en PostgreSQL.
- Se registra `id_usuario_creacion` para trazabilidad.

El rol del nuevo usuario no se recibe libremente desde JavaFX.
