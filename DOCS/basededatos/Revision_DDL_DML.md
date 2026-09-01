# Revisión de scripts DDL y DML — Estado actual y proyección

## 1. Estado actual (hoy)

El DDL (`ddl_carestock.sql`, PostgreSQL) cubre correctamente las 7 tablas
del ER vigente, con:

- Restricciones `UNIQUE` en las 6 columnas que el script de seed
  necesita para que `ON CONFLICT` funcione (`nombre_rol`,
  `nombre_categoria`, `(estante, nivel)`, `email`, `codigo_invima`,
  `(numero_lote, id_medicamento)`).
- `CHECK` para los campos tipo enum (`estado`, `estado_lote`,
  `tipo_movimiento`) y para invariantes de negocio (stock y cantidades
  no negativas).
- `password_hash VARCHAR(60)` — coincide exactamente con la longitud de
  un hash bcrypt real.
- `ON DELETE RESTRICT` en todas las FK, para que el historial de
  auditoría nunca pierda su rastro por un borrado en cascada.
- Índices explícitos en cada columna FK (necesarios en PostgreSQL, que a
  diferencia de MySQL no los crea automáticamente).

El script DML (seed) es idempotente (`ON CONFLICT ... DO NOTHING` /
`DO UPDATE`) y respeta el orden de dependencias al insertar.

## 2. Lo que hay que resolver antes de seguir extendiendo

**Motor de base de datos (bloqueante).** El DDL está en PostgreSQL, pero
el ER original usa tipos de Oracle (`NUMBER`, `VARCHAR2`) y los
requerimientos R20/R21 mencionan explícitamente Oracle Database. Hay que
fijar el motor real del proyecto de forma oficial. Si el motor termina
siendo Oracle, hay que traducir:

| PostgreSQL (actual) | Equivalente Oracle |
|---|---|
| `SERIAL` | `NUMBER` + `GENERATED ALWAYS AS IDENTITY` (Oracle 12c+) |
| `VARCHAR(n)` | `VARCHAR2(n)` |
| `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` | `TIMESTAMP DEFAULT SYSTIMESTAMP` |
| `ON CONFLICT ... DO UPDATE` (en el seed) | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED` |
| `CURRENT_DATE + INTERVAL '15 days'` (en el seed) | `SYSDATE + 15` |
| Índice de texto libre para R20 (`pg_trgm`/`GIN`) | Oracle Text (`CONTEXT`/`CTXCAT`) |

Esto no es un simple *find & replace*: el `MERGE` de Oracle tiene una
sintaxis bastante distinta al `ON CONFLICT` de Postgres, así que el seed
completo tendría que reescribirse, no solo el DDL.

**Desalineación de nombres de rol.** El seed inserta `'ADMINISTRADOR'` y
`'FARMACEUTICO'`, pero los requerimientos (R19, R41) hablan de
"Jefe de Farmacia", "Auxiliar de Farmacia" y "Auditor". Hay que decidir
si son sinónimos o roles distintos, y ajustar el seed en consecuencia.

## 3. A futuro — qué le falta al DDL para cubrir los 44 requerimientos

Tablas nuevas que habría que agregar (ver Bloque 2 para el detalle de qué
requerimiento las pide):

- `PROVEEDORES` + FK desde `LOTES.id_proveedor` (nullable, porque el
  diagrama de clases la marca como 0..1)
- `ALERTAS` (tipo, mensaje, fecha, estado, FK a producto y/o lote)
- `ORDENES_DESPACHO` + `DETALLE_DESPACHO`
- `PACIENTES` (o `CLIENTES`)
- `RECETAS`
- `TRANSFERENCIAS_LOTE` (origen, destino, cantidad, usuario, fecha)
- `CONTEOS_INVENTARIO` (acta de ajuste, con FK a `LOG_MOVIMIENTOS`)
- `LECTURAS_SENSOR` (temperatura, humedad, fecha, FK a ubicación)

Columnas nuevas en tablas existentes:

- `MEDICAMENTOS.control_especial BOOLEAN` (R38)
- `USUARIOS.otp_secret VARCHAR` (R42)
- Posiblemente `MEDICAMENTOS.costo_unitario` o similar, si se quiere que
  el reporte de mermas (R18/R37) calcule valor financiero y no solo
  unidades.

Objetos adicionales de base de datos (no solo tablas):

- Una vista o función para el semáforo de vencimiento (R13/R31), para no
  duplicar esa lógica en cada consulta.
- Un job/procedimiento programado que la ejecute a diario.
- Trigger(s) o mecanismo de auditoría genérico para cubrir R40 (hoy solo
  se audita `LOG_MOVIMIENTOS`, no cambios en `USUARIOS` o
  `MEDICAMENTOS`).

Ninguno de estos cambios rompe lo que ya existe — son extensiones aditivas
sobre el DDL actual, salvo la eventual migración de motor, que si ocurre
sí obliga a reescribir DDL y DML por completo.
