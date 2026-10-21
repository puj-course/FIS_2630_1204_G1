# Tablas de movimientos de inventario

Qué tablas y vistas guardan movimientos de inventario, quién escribe en ellas y cómo se determina la farmacia de cada movimiento. Verificado sobre el código y los scripts del repositorio en el commit `e89a0fa` (19 de octubre de 2026). El resumen de la base de datos está en [`COMPONENTE_BASE_DATOS.md`](../COMPONENTE_BASE_DATOS.md).

---

## 1. Resumen

| Objeto | Ubicación | Columnas principales | ¿Quién escribe? | ¿Cómo se determina la farmacia? |
|---|---|---|---|---|
| `log_movimientos` (tabla) | `src/sql/ddl/01_schema_carestock.sql` | `id_log`, `id_usuario`, `id_lote`, `tipo_movimiento`, `cantidad_afectada`, `detalle_cambio`, `fecha_hora` | Java: solo `ControlVencimientosService` (un `AJUSTE` por vencimiento). SQL: seis rutinas que ninguna clase Java invoca | No guarda `id_farmacia`: se deduce a través de `lotes` y `medicamentos` |
| `movimientos_stock` (tabla) | `scripts/migrations/547_movimientos_stock.sql` | `id_movimiento`, `id_lote`, `tipo_movimiento`, `cantidad`, `id_usuario`, `fecha_movimiento` | **Nadie:** ninguna clase Java ni script SQL escribe en ella | No guarda `id_farmacia`: se deduciría a través de `lotes` y `medicamentos` |
| `VW_MOVIMIENTOS_FARMACIA` (vista) | `src/sql/ddl/09_vista_movimientos_farmacia.sql` | `id_log`, `id_farmacia`, `tipo_movimiento`, `numero_lote`, `medicamento`, `cantidad_afectada`, `usuario_responsable`, `fecha_hora` | Nadie: solo lee `log_movimientos`, y ninguna clase Java la consulta | Calcula `id_farmacia` con `lotes` y `medicamentos` |

---

## 2. Rutinas SQL que escriben en `log_movimientos`

| Rutina | Tipo | Script |
|---|---|---|
| `fn_registrar_lote` | `ENTRADA` | `Reegistro_lote.sql` y `Valida_rol.sql` |
| `sp_registrar_nuevo_lote` | `ENTRADA` | `procedures/01_sp_ingreso_lotes.sql` |
| `fn_crear_lote_seguro` | `ENTRADA` | `ddl/06_crud_inventario_multifarmacia.sql` |
| `fn_despachar_lote` | `SALIDA` | `LotesDespacho.sql` y `Valida_rol.sql` |
| `fn_actualizar_lote_seguro` | `AJUSTE` | `ddl/06_crud_inventario_multifarmacia.sql` |
| `fn_cambiar_estado_lote_seguro` | `AJUSTE` | `ddl/06_crud_inventario_multifarmacia.sql` |

Ninguna clase Java invoca estas rutinas. `fn_registrar_lote` y `fn_despachar_lote` están definidas en dos scripts, y las dos versiones no son idénticas.

---

## 3. En qué se solapan `log_movimientos` y `movimientos_stock`

Las dos tablas cumplen el mismo papel de kardex. **Coinciden** en los tres tipos de movimiento (`ENTRADA`, `SALIDA` y `AJUSTE`), en las claves foráneas a `lotes` y a `usuarios`, y en que no guardan `id_farmacia`.

**Se diferencian** en:

- El nombre de dos columnas: `cantidad_afectada` y `fecha_hora` en `log_movimientos`, `cantidad` y `fecha_movimiento` en `movimientos_stock`.
- `detalle_cambio` solo existe en `log_movimientos`; la restricción `cantidad > 0` solo existe en `movimientos_stock`.
- El tipo del identificador (`INT` generado frente a `BIGSERIAL`) y la ubicación: una está en el esquema y la otra en una migración.

---

## 4. Observaciones

Verificadas en el código y en los scripts SQL:

1. **`movimientos_stock` no la usa nadie.** La creó el commit `8d7354e` (2 de octubre de 2026), junto con la clase `MovimientoStockDAO` del código anterior, y esa clase se eliminó en `9034b1e` (7 de octubre) al integrar el nuevo módulo.
2. **Ningún script ni flujo de CI/CD aplica `scripts/migrations/`.** Por eso no se puede asegurar que `movimientos_stock` exista en la base de datos.
3. **Desde la aplicación solo se registra un tipo de movimiento:** el `AJUSTE` por vencimiento. El ingreso de lotes y la dispensación no escriben en ninguna de las dos tablas.
