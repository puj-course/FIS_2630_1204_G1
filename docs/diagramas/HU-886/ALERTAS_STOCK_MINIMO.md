# Alertas de stock mínimo por categoría

Dónde vive el stock mínimo de cada categoría, con qué stock se compara y qué falta frente a los requerimientos R14, R16 y R32. Verificado sobre el código y los scripts del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Stock mínimo por categoría

| Categoría | Campo de stock mínimo | Campo de stock | ¿Existe hoy? |
|---|---|---|---|
| Medicamentos | `medicamentos.stock_minimo` (entero, obligatorio, por defecto 0) | `medicamentos.stock_total` | **Sí.** Se edita en el formulario del catálogo y la vista `vw_resumen_farmacias` lo compara |
| Aseo | No existe | `aseo.stock` | **No.** Hay stock, pero no hay stock mínimo |
| Maternidad | No existe | `maternidad.stock` | **No.** Hay stock, pero no hay stock mínimo |

El `stock_total` de un medicamento lo recalcula el trigger `trg_actualizar_stock_total` a partir de sus lotes disponibles. El `stock` de aseo y maternidad se edita directamente en el producto, con `CatalogoDAO.actualizar`.

---

## 2. Cómo se compara hoy

1. **Solo en SQL.** La vista `vw_resumen_farmacias` (`src/sql/vw_resumen_farmacias.sql`) cuenta como `alertas_criticas`, por farmacia, los medicamentos activos con `stock_total <= stock_minimo`.
2. **En Java nunca.** Ninguna clase usa esa vista ni compara el stock con el stock mínimo. Lo único que hace el código con `stockMinimo` es guardarlo, validar que no sea negativo y mostrarlo en el formulario del catálogo de medicamentos.

---

## 3. Lo que falta frente a los requerimientos

- **R14** (alerta de stock mínimo de un medicamento, visible en el Dashboard): ninguna pantalla muestra una alerta; solo existe el conteo de la vista. Además, el requerimiento cita la tabla `PRODUCTOS`, que no existe: el campo está en `medicamentos`.
- **R16** (parametrizar el stock mínimo y los días de anticipación por producto): el stock mínimo solo se edita en medicamentos, desde el formulario del catálogo, que solo permite modificar al rol `ADMINISTRADOR`; los demás roles lo ven en modo consulta. Aseo y maternidad no tienen umbral, y los días de anticipación no se parametrizan: la vista `vw_alertas_vencimiento` los fija en 15 y 30 días.
- **R32** (alerta automática de un producto, con notificación inmediata): aseo y maternidad no pueden alertar porque no tienen stock mínimo, y ninguna pantalla muestra notificaciones.
