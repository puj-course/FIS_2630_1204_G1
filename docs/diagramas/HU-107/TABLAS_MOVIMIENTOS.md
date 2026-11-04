# Tablas de movimientos de inventario

Qué tablas y vistas guardan movimientos de inventario, quién escribe en ellas y cómo se determina la farmacia de cada movimiento. Verificado sobre el código y los scripts del repositorio en el commit `0b7e935` (2 de noviembre de 2026). Qué operación deja qué movimiento está en [`MATRIZ_MOVIMIENTOS_INVENTARIO.md`](../HU-147/MATRIZ_MOVIMIENTOS_INVENTARIO.md), y el resumen de la base de datos en [`COMPONENTE_BASE_DATOS.md`](../COMPONENTE_BASE_DATOS.md).

---

## 1. Resumen

| Objeto | Ubicación | Qué guarda | ¿Quién escribe? | ¿Cómo se determina la farmacia? |
|---|---|---|---|---|
| `log_movimientos` (tabla) | `src/sql/ddl/01_schema_carestock.sql` | Movimientos de lotes de medicamentos: `ENTRADA`, `SALIDA` y `AJUSTE`, con `cantidad_afectada` y `detalle_cambio` | Java: `ServicioCrudLotes`, `RegistroLoteService`, `FarmacovigilanciaService` y `ControlVencimientosService`. SQL: seis rutinas que ninguna clase Java invoca | No guarda `id_farmacia`: se deduce a través de `lotes` y `medicamentos` |
| `movimientos_productos` (tabla) | `src/sql/ddl/13_movimientos_productos.sql` | Entradas y salidas de aseo y maternidad, con motivo y stock anterior y posterior | Java: `ServicioMovimientosProducto` | Guarda `id_farmacia` |
| `auditoria_ajustes` (tabla) | `src/sql/ddl/14_auditoria_ajustes.sql` | Ajustes justificados de un lote, un producto de aseo o uno de maternidad, con la cantidad anterior, la nueva y la diferencia | Java: `AuditoriaAjustesDao`, desde `ServicioAjustesInventario` | Guarda `id_farmacia` |
| `movimientos_stock` (tabla) | `scripts/migrations/547_movimientos_stock.sql` | Kardex alternativo de lotes | **Nadie:** ninguna clase Java ni script SQL escribe en ella | No guarda `id_farmacia`: se deduciría a través de `lotes` y `medicamentos` |
| `movimientos_catalogo` (tabla) | `src/sql/ddl/10_movimientos_catalogo.sql` | Kardex alternativo de aseo y maternidad | **Ninguna clase Java:** solo el script de datos de prueba `src/sql/test_data/movimientos_catalogo_prueba.sql` | Guarda `id_farmacia` |
| `VW_MOVIMIENTOS_FARMACIA` (vista) | `src/sql/ddl/09_vista_movimientos_farmacia.sql` | `log_movimientos` con la farmacia, el lote, el medicamento y el usuario | Nadie: solo lee `log_movimientos`, y ninguna clase Java la consulta | Calcula `id_farmacia` con `lotes` y `medicamentos` |
| `VW_MOVIMIENTOS_CATALOGO_FARMACIA` (vista) | `src/sql/ddl/11_vista_movimientos_catalogo.sql` | `movimientos_catalogo` con la categoría, el producto y el usuario | Nadie: solo lee `movimientos_catalogo`, y ninguna clase Java la consulta | Usa el `id_farmacia` del movimiento y solo muestra el producto si es de esa farmacia |

---

## 2. Cómo se juntan en el historial

La pantalla "Historial de movimientos" usa `HistorialMovimientosDao`, que une con `UNION ALL` tres tablas y no usa ninguna de las vistas. Filtra por la farmacia activa con `WHERE h.id_farmacia = ?`:

- **`log_movimientos`:** la farmacia sale de `medicamentos`.
- **`movimientos_productos`:** usa su `id_farmacia` y comprueba que coincide con la del producto.
- **`auditoria_ajustes`:** usa su `id_farmacia` y comprueba que coincide con la del lote o del producto.

---

## 3. Rutinas SQL que escriben en `log_movimientos`

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

## 4. En qué se solapan

**`log_movimientos` y `movimientos_stock`** cumplen el mismo papel de kardex de lotes. Coinciden en los tres tipos de movimiento, en las claves foráneas a `lotes` y a `usuarios`, y en que no guardan `id_farmacia`. Se diferencian en el nombre de dos columnas (`cantidad_afectada` y `fecha_hora` frente a `cantidad` y `fecha_movimiento`), en que `detalle_cambio` solo existe en `log_movimientos` y la restricción `cantidad > 0` solo en `movimientos_stock`, y en el tipo del identificador (`INT` generado frente a `BIGSERIAL`).

**`movimientos_productos` y `movimientos_catalogo`** cumplen el mismo papel de kardex de aseo y maternidad. Coinciden en guardar `id_farmacia`, en tener exactamente un producto por fila y en la restricción `cantidad > 0`. Se diferencian en:

| Aspecto | `movimientos_productos` | `movimientos_catalogo` |
|---|---|---|
| Tipos permitidos | `ENTRADA` y `SALIDA` | `ENTRADA`, `SALIDA` y `AJUSTE` |
| Motivo | Obligatorio, hasta 250 caracteres | Opcional, hasta 200 caracteres |
| Stock anterior y posterior | Sí, con una restricción de coherencia | No |
| Identificador | `BIGINT` generado | `BIGSERIAL` |
| Creada | 25 de octubre de 2026 | 23 de octubre de 2026 |
| ¿La usa la aplicación? | Sí, `ServicioMovimientosProducto` | No |
| Vista por farmacia | Ninguna | `VW_MOVIMIENTOS_CATALOGO_FARMACIA` |

---

## 5. Observaciones

Verificadas en el código y en los scripts SQL:

1. **`movimientos_stock` y `movimientos_catalogo` no las usa ninguna clase.** El commit `8d7354e` (2 de octubre de 2026) creó `movimientos_stock` junto con la clase `MovimientoStockDAO`, que se eliminó en `9034b1e` (7 de octubre). `movimientos_catalogo` se creó dos días antes que `movimientos_productos`, la tabla que sí usa la aplicación.
2. **Ningún script ni flujo de CI/CD aplica `scripts/migrations/`.** Por eso no se puede asegurar que `movimientos_stock` exista en la base de datos.
3. **Desde la aplicación se registran los tres tipos de movimiento** en `log_movimientos` para medicamentos, entradas y salidas en `movimientos_productos` para aseo y maternidad, y los ajustes justificados en `auditoria_ajustes`.
