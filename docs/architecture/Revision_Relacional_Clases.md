# Modelo relacional y diagrama de clases — Estado actual y proyección

## 1. Estado actual (hoy)

El modelo relacional (7 tablas: ROLES, USUARIOS, CATEGORIAS, MEDICAMENTOS,
UBICACIONES, LOTES, LOG_MOVIMIENTOS) y el DDL están alineados entre sí:
cada tabla del ER tiene su `CREATE TABLE` correspondiente, con las mismas
columnas, tipos y relaciones 1:N.

El diagrama de clases recién compartido **ya no coincide** con ese modelo.
Introduce cambios estructurales que el ER/DDL actuales no contemplan:

| Cambio en el diagrama de clases | Estado en el ER/DDL |
|---|---|
| `Producto` como clase **abstracta**, `Medicamento` hereda de ella | No existe herencia; `MEDICAMENTOS` es una tabla plana |
| `Proveedor` como entidad propia, asociada a `Lote` (0..1) | No existe en el ER ni en el DDL |
| `Alerta` como entidad persistente (vinculada a `Producto`/`Lote`) | No existe; hoy una alerta es solo el *resultado* de una consulta (el `SELECT` de stock crítico), no una fila guardada |
| `LogMovimiento` se renombra a `Movimiento` y gana métodos de dominio (`despacharFEFO()`, `registrarEntrada()`) | `LOG_MOVIMIENTOS` es una tabla pasiva de auditoría, sin lógica de negocio adjunta |

**Punto crítico de motor de base de datos:** el ER original usa tipos
`NUMBER` / `VARCHAR2` / `TIMESTAMP`, que son sintaxis **Oracle**, no
PostgreSQL. Esto ahora se confirma con los requerimientos (R20, R21
mencionan explícitamente "Oracle Database" / "secuencias en Oracle DB").
Sin embargo, el DDL que construimos está en **PostgreSQL** (`SERIAL`,
`ON CONFLICT`, etc.). Esta es una decisión que hay que tomar de forma
explícita y documentada antes de seguir extendiendo el modelo — no es un
detalle menor, cambia sintaxis de PK autoincremental, de upsert, de
full-text search, y de tipos booleanos.

## 2. A futuro (con base en los 44 requerimientos)

El modelo actual cubre bien el *inventario físico* (medicamentos, lotes,
ubicaciones, movimientos), pero los requerimientos describen un sistema
bastante más amplio. Entidades que **no existen todavía en ningún
diagrama** y que se necesitan para cumplir los requerimientos:

- **Orden de despacho** + su detalle (líneas de producto/cantidad) — R01–R10, R21–R28
- **Paciente / Cliente** — receptor de una orden, requerido para el track & trace de R44 y las entregas fraccionadas de R25
- **Receta médica** — R24, con firma médica y vigencia
- **Transferencia entre ubicaciones/bodegas** — R29
- **Conteo de inventario / acta de ajuste** — R30 (hoy `LOG_MOVIMIENTOS` tipo `AJUSTE` cubre el movimiento, pero no el acta ni la justificación formal)
- **Lectura de sensor** (temperatura/humedad, cadena de frío) — R43

Y campos que faltan en entidades ya existentes:

- `MEDICAMENTOS.control_especial` (booleano) — para el reporte normativo INVIMA de R38
- `USUARIOS.otp_secret` — para el MFA de R42
- Un mecanismo de "semáforo de vencimiento" (rojo/amarillo/verde) — R13/R31. No necesita columna nueva, se puede resolver con una vista o función calculada sobre `fecha_vencimiento`, pero hay que decidir dónde vive esa lógica.

**Sobre la herencia `Producto → Medicamento`:** tiene sentido si el
negocio planea vender/inventariar algo más que medicamentos (insumos,
dispositivos médicos) — ningún requerimiento lo pide explícitamente hoy,
pero es una apuesta razonable a futuro. Si se mantiene, hay que decidir
la estrategia de persistencia (tabla única con discriminador, tabla por
subclase, o tabla por clase concreta) porque el modelo relacional no
representa herencia de forma nativa.
