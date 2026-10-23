# Consistencia de las existencias

Qué restricciones protegen las existencias en cada tabla, qué comprueba el script de verificación y cómo ejecutarlo en Neon. Las restricciones se verificaron aplicando en orden los scripts de `src/sql/ddl` (01 a 09 y 12) sobre una base nueva.

---

## 1. Restricciones de existencias

| Tabla | Restricción | Qué impide | Script |
|---|---|---|---|
| `lotes` | `ck_lotes_cantidad` | Una `cantidad_actual` negativa | `ddl/01` |
| `lotes` | `id_medicamento` obligatorio y `fk_lotes_medicamento` | Un lote sin medicamento o de un medicamento que no existe | `ddl/01` |
| `lotes` | `uq_lotes_numero` | Repetir un `numero_lote` para el mismo medicamento | `ddl/01` |
| `medicamentos` | `ck_medicamentos_stock` | Un `stock_total` negativo | `ddl/01` |
| `medicamentos` | `id_farmacia` obligatorio y `fk_medicamentos_farmacia` | Un medicamento sin farmacia o de una farmacia que no existe | `ddl/04` |
| `medicamentos` | Índice único `uq_medicamentos_farmacia_invima` | Repetir un `codigo_invima` dentro de una farmacia | `ddl/04` |
| `aseo` y `maternidad` | `ck_aseo_stock` y `ck_maternidad_stock` | Un `stock` negativo | `ddl/12` |
| `aseo` y `maternidad` | `ck_aseo_precio` y `ck_maternidad_precio` | Un `precio` negativo | `ddl/12` |
| `aseo` y `maternidad` | `id_farmacia` obligatorio, `fk_aseo_farmacia` y `fk_maternidad_farmacia` | Un producto sin farmacia o de una farmacia que no existe | `ddl/12` |
| `aseo` y `maternidad` | `uq_aseo_farmacia_codigo` y `uq_maternidad_farmacia_codigo` | Repetir un `codigo` dentro de una farmacia | `ddl/12` |

Además, el trigger `trg_actualizar_stock_total` recalcula el `stock_total` de un medicamento a partir de sus lotes disponibles cada vez que se inserta un lote o cambia su cantidad o su estado.

`aseo` y `maternidad` ya existían en Neon antes del script `ddl/12`, así que sus restricciones reales pueden diferir de las listadas.

---

## 2. Qué verifica cada consulta

`src/sql/verificacion/consistencia_inventario.sql` es una sola sentencia `SELECT` con nueve comprobaciones unidas por `UNION ALL`. Cada fila del resultado es un problema encontrado.

| `problema` | Tabla | Qué verifica | Restricción que debería impedirlo |
|---|---|---|---|
| `STOCK_NEGATIVO` | `aseo`, `maternidad` | `stock` menor que 0 | `ck_aseo_stock`, `ck_maternidad_stock` |
| `CANTIDAD_NEGATIVA` | `lotes` | `cantidad_actual` menor que 0 | `ck_lotes_cantidad` |
| `PRODUCTO_SIN_FARMACIA` | `aseo`, `maternidad`, `medicamentos` | `id_farmacia` vacío o de una farmacia que no existe | `id_farmacia` obligatorio y las claves foráneas a `farmacias` |
| `LOTE_SIN_MEDICAMENTO` | `lotes` | `id_medicamento` vacío o de un medicamento que no existe | `fk_lotes_medicamento` |
| `CODIGO_REPETIDO` | `aseo`, `maternidad` | El mismo código, sin contar espacios en los extremos, en una misma farmacia | `uq_aseo_farmacia_codigo`, `uq_maternidad_farmacia_codigo` |

---

## 3. Cómo ejecutarlo en Neon

1. Abre el editor SQL de Neon desde la consola de tu proyecto y elige la base de datos que usa CareStock.
2. Copia todo el contenido de `src/sql/verificacion/consistencia_inventario.sql` y pégalo en el editor.
3. Ejecútalo. Es una sola sentencia `SELECT`, así que no modifica ningún dato.
4. Lee el resultado como se explica en la sección siguiente.

---

## 4. Cómo interpretar el resultado

**Cero filas** significa que no se encontró ningún problema.

**Cada fila es un problema.** Sus columnas son `problema`, `tabla`, `id_registro`, `id_farmacia` y `detalle`. En `CODIGO_REPETIDO`, `id_registro` lista los ids que comparten el código.

| `problema` | Qué hacer |
|---|---|
| `STOCK_NEGATIVO` | Corregir el stock del producto con su valor real |
| `CANTIDAD_NEGATIVA` | Revisar el historial del lote y corregir su cantidad |
| `PRODUCTO_SIN_FARMACIA` | Asignar la farmacia correcta al producto |
| `LOTE_SIN_MEDICAMENTO` | Recuperar el medicamento o retirar el lote |
| `CODIGO_REPETIDO` | Conservar un registro y cambiar el código de los demás |

El script solo informa: no corrige nada.

Si aparece un problema que una restricción de la sección 1 debería impedir, la base de datos real no tiene esa restricción. Para comprobarlo, usa las dos consultas de estructura de la sección 3 de `docs/architecture/design/SEGREGACION_FARMACIA_BASE_DATOS.md`.

---

## 5. Observaciones

1. **El script no revisa `medicamentos`.** No comprueba un `stock_total` negativo ni un `codigo_invima` repetido, que impiden `ck_medicamentos_stock` y `uq_medicamentos_farmacia_invima`. Sí revisa que cada medicamento tenga farmacia.
2. **El resultado depende de la base de datos real,** no de los scripts del repositorio: en una base creada solo con ellos, varias de las comprobaciones no pueden encontrar nada.
