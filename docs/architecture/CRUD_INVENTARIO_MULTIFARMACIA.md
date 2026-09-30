# CRUD de inventario multifarmacia

## Objetivo

Implementar gestión segura de medicamentos y lotes para un entorno
multifarmacia, aplicando segregación de datos por farmacia y autorización
basada en el rol del usuario autenticado.

## Roles

| Rol | Alcance |
|---|---|
| SUPER_ADMIN | Puede seleccionar y administrar cualquier farmacia activa |
| ADMINISTRADOR | Solo puede administrar su farmacia asignada |
| FARMACEUTICO | No posee permisos de CRUD administrativo |

## Eliminación lógica

No se realiza DELETE físico sobre medicamentos ni lotes.

- Medicamento: ACTIVO -> INACTIVO.
- Lote: DISPONIBLE -> RETENIDO.

Esto conserva la trazabilidad y las relaciones históricas.

## Diseño estructural

```text
GestionInventarioFX
        |
        v
InventarioCrudService
        |
        +---- SessionContext
        |
        v
InventarioCrudDAO
        |
        v
PostgreSQL / Neon
        |
        +---- fn_validar_gestion_farmacia
        +---- fn_crear_medicamento_seguro
        +---- fn_actualizar_medicamento_seguro
        +---- fn_cambiar_estado_medicamento_seguro
        +---- fn_crear_lote_seguro
        +---- fn_actualizar_lote_seguro
        +---- fn_cambiar_estado_lote_seguro

## Creación de farmacias

La creación de farmacias se encuentra restringida exclusivamente a
`SUPER_ADMIN`.

El formulario se integra como un panel desplegable dentro de la vista de
creación de administradores.

Flujo:

```text
SUPER_ADMIN
    |
    v
Crear usuario
    |
    v
+ Crear nueva farmacia
    |
    v
FarmaciaService
    |
    +--> AccessControl.requireRole("SUPER_ADMIN")
    |
    v
FarmaciaDAO
    |
    v
fn_crear_farmacia_segura
    |
    +--> valida nuevamente SUPER_ADMIN
    |
    v
FARMACIAS
```

## Consulta de farmacias

La consulta de farmacias registradas está disponible únicamente
para `ADMINISTRADOR` y `SUPER_ADMIN`, con búsqueda por nombre y
visualización de estado (ACTIVA / INACTIVA).

Nota sobre el campo identificador: la tabla `FARMACIAS` utiliza
`codigo` como identificador legible de cada sede, no un NIT. La
Historia de Usuario original hacía referencia a "NIT", pero el
esquema real implementado usa `codigo`, por lo que la pantalla de
consulta y el modelo `Farmacia` reflejan ese campo.

Flujo:

```text
ADMINISTRADOR / SUPER_ADMIN
    |
    v
FarmaciaConsultaFX
    |
    v
FarmaciaDAO.buscarPorNombre(filtro)
    |
    v
FARMACIAS
```
