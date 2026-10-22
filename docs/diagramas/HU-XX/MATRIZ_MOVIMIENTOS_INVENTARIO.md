# Matriz de movimientos de inventario por categoría

Qué operación cambia las existencias de cada categoría según el tipo de movimiento, qué clase la ejecuta y si deja un registro en una tabla de movimientos. Verificado sobre el código del repositorio en el commit `e89a0fa` (19 de octubre de 2026). La fachada `Inventario` está descrita en [`FACHADA_INVENTARIO.md`](../HU-92/FACHADA_INVENTARIO.md).

---

## 1. Matriz

| Tipo | Categoría | Operación actual | Clase y método | ¿Deja registro? |
|---|---|---|---|---|
| Entrada | Medicamentos | Ingreso de un lote: inserta el lote y suma el stock del medicamento en una transacción | `Inventario.registrarLote` → `RegistroLoteService.registrarMedicamentoLote` | No |
| Salida | Medicamentos | Dispensación: descuenta del lote y del stock del medicamento en una transacción | `Inventario.dispensarMedicamento` → `FarmacovigilanciaService.dispensarMedicamento` | No |
| Ajuste | Medicamentos | Deshabilitación automática de lotes vencidos: marca el lote `VENCIDO` y recalcula el stock | `ControlVencimientosService.sincronizar` | **Sí**: un `AJUSTE` en `log_movimientos` por cada lote con existencias |
| Entrada | Aseo | Stock inicial al registrar el producto; los aumentos posteriores solo se hacen editando el stock | `Inventario.registrarAseo` → `CatalogoDAO.guardar`; `Inventario.actualizarAseo` → `CatalogoDAO.actualizar` | No |
| Salida | Aseo | No existe una operación propia; el stock solo baja editándolo | `Inventario.actualizarAseo` → `CatalogoDAO.actualizar` | No |
| Ajuste | Aseo | Edición del stock desde el formulario del catálogo | `Inventario.actualizarAseo` → `CatalogoDAO.actualizar` | No |
| Entrada | Maternidad | Stock inicial al registrar el producto; los aumentos posteriores solo se hacen editando el stock | `Inventario.registrarMaternidad` → `CatalogoDAO.guardar`; `Inventario.actualizarMaternidad` → `CatalogoDAO.actualizar` | No |
| Salida | Maternidad | No existe una operación propia; el stock solo baja editándolo | `Inventario.actualizarMaternidad` → `CatalogoDAO.actualizar` | No |
| Ajuste | Maternidad | Edición del stock desde el formulario del catálogo | `Inventario.actualizarMaternidad` → `CatalogoDAO.actualizar` | No |

---

## 2. Observaciones

Verificadas en el código y en los scripts SQL:

1. **Solo una de las nueve combinaciones deja registro:** el ajuste de medicamentos por vencimiento.
2. **Seis rutinas SQL sí registran movimientos en `log_movimientos`, pero ninguna clase Java las invoca.** `fn_registrar_lote`, `sp_registrar_nuevo_lote` y `fn_crear_lote_seguro` registran entradas; `fn_despachar_lote`, salidas; `fn_actualizar_lote_seguro` y `fn_cambiar_estado_lote_seguro`, ajustes.
3. **En aseo y maternidad no se distinguen entradas, salidas ni ajustes.** Los tres son un cambio del campo `stock` mediante `CatalogoDAO.actualizar`.
4. **El stock de un medicamento no se edita desde el catálogo.** `MedicamentoCatalogoDAO.actualizar` no lo incluye, y el trigger `trg_actualizar_stock_total` lo recalcula a partir de los lotes disponibles.
5. **Los métodos de escritura de `LoteDAO` no los usa ninguna clase.** `guardar`, `actualizarCantidad` y `actualizarEstado` no tienen llamadas; el ingreso y la dispensación ejecutan su propio SQL.
