# Flujo actual de una reposición por categoría

Cómo se registra hoy una reposición, es decir, una entrada de stock, en cada categoría; qué movimiento queda y en qué tabla; y qué pasa con la alerta de stock mínimo después. La alerta y la cantidad sugerida están explicadas en la [guía de usuario](../../user_guide/alertas-exportar-y-reponer.md). Verificado sobre el código y los scripts del repositorio en el commit `0b7e935` (2 de noviembre de 2026).

---

## 1. Cómo se registra por categoría

| Categoría | Pantalla | Clases | Movimiento que queda | Tabla | Quién puede |
|---|---|---|---|---|---|
| Medicamentos | Inventario, botón "+ Crear lote" | `GestorLotesDialogo.crear` y `ServicioCrudLotes.crear` | `ENTRADA` con el detalle "Registro inicial del lote" y el número del lote | `log_movimientos`. Además se inserta el lote en `lotes` y se recalcula `medicamentos.stock_total` | `ADMINISTRADOR`, `SUPER_ADMIN` o `FARMACEUTICO` con farmacia asignada |
| Aseo | Movimientos, movimiento `ENTRADA` | `ControladorMovimientos` y `ServicioMovimientosProducto.registrar` | `ENTRADA` con la cantidad, el motivo y el stock anterior y posterior | `movimientos_productos`, columna `id_aseo`. El stock se actualiza en `aseo.stock` | Cualquier usuario activo de la farmacia |
| Maternidad | Movimientos, movimiento `ENTRADA` | `ControladorMovimientos` y `ServicioMovimientosProducto.registrar` | `ENTRADA` con la cantidad, el motivo y el stock anterior y posterior | `movimientos_productos`, columna `id_maternidad`. El stock se actualiza en `maternidad.stock` | Cualquier usuario activo de la farmacia |

Para crear un lote se piden medicamento, número de lote, cantidad, fecha de vencimiento (futura) y ubicación. Para un movimiento se piden categoría, producto, cantidad y motivo, que es obligatorio y de hasta 250 caracteres. El mensaje de error de los lotes dice "Solo administradores con farmacia pueden gestionar lotes", aunque el código también deja pasar al `FARMACEUTICO`.

**La pantalla de Movimientos existe, pero hoy ningún botón la abre.** El botón "Movimientos" de la pantalla de inventario se eliminó el 22 de octubre en el commit `193237d` (HU-119, al migrar la cabecera), y el método `onAbrirMovimientos` quedó sin ningún control que lo use; solo lo invoca la prueba `NavegacionRefactorizadaTest`. Mientras no vuelva el botón, ningún usuario puede llegar desde la aplicación a registrar una entrada de aseo o de maternidad.

---

## 2. Otros caminos que cambian el stock sin registrar una entrada

- **Catálogo.** El formulario de aseo y maternidad tiene un campo de stock, y su `UPDATE` lo guarda con `stock = ?`. Solo el rol `ADMINISTRADOR` puede editar, y no se registra ningún movimiento.
- **Ajustes de inventario.** Un botón de la pantalla de inventario, solo para `ADMINISTRADOR` o `SUPER_ADMIN`. Fija la nueva cantidad de un lote o de un producto con un motivo obligatorio, y deja el rastro en `auditoria_ajustes`, no una `ENTRADA`.

---

## 3. Qué pasa con la alerta después de reponer

La alerta no se cierra ni se guarda: es una consulta a `vw_alertas_inventario`, y desaparece cuando deja de cumplirse que las existencias sean menores o iguales al mínimo. Estos casos se ejecutaron con las sentencias del código en una base con los scripts `01` a `17`.

| Producto, mínimo y reposición | Antes | Después |
|---|---|---|
| Medicamento con 2 y mínimo 5, lote nuevo de 10 que vence en 90 días | Advertencia de stock | La alerta desaparece (stock 12) |
| Medicamento con 0 y mínimo 5, lote nuevo de 3 | Crítica | Sigue como advertencia (stock 3) |
| Medicamento con 2 y mínimo 5, lote nuevo de 10 que vence en 20 días | Advertencia de stock | La de stock desaparece y aparece una advertencia de vencimiento del lote nuevo |
| Aseo con 0 y mínimo 4, entrada de 10 | Crítica | La alerta desaparece |
| Aseo con 0 y mínimo 4, entrada de 2 | Crítica | Sigue como advertencia (stock 2) |
| Maternidad con 0 y mínimo 3, entrada de 5 | Crítica | La alerta desaparece |

Las alertas de vencimiento de los demás lotes no cambian al reponer.

**Cuándo se ve el cambio:**

- **Medicamentos.** Al terminar de crear el lote, `onCrearLote` llama a `onRefrescar()` y se recalculan el contador y la campana del inventario.
- **Aseo y maternidad.** La pantalla de Movimientos no recalcula las alertas; se ven al volver al inventario, que se carga de nuevo.
- **Pantalla de Alertas.** Se actualiza al abrirla, cada 60 segundos y con "Actualizar ahora".
