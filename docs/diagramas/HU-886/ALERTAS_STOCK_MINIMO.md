# Alertas de stock mínimo por categoría

Dónde vive el stock mínimo de cada categoría, con qué stock se compara, cómo llega la alerta a la pantalla y qué falta frente a los requerimientos R14, R16 y R32. Verificado sobre el código y los scripts del repositorio en el commit `b2b8aac` (6 de noviembre de 2026).

---

## 1. Stock mínimo por categoría

| Categoría | Campo de stock mínimo | Campo de stock | ¿Existe hoy? |
|---|---|---|---|
| Medicamentos | `medicamentos.stock_minimo` (entero, obligatorio, por defecto 0) | `medicamentos.stock_total` | **Sí.** Se edita en el formulario del catálogo, donde es obligatorio |
| Aseo | `aseo.stock_minimo` (entero, obligatorio, por defecto 0 y no negativo; script `15`) | `aseo.stock` | **Sí.** Se edita en el formulario del catálogo; si se deja vacío, se guarda 0 |
| Maternidad | `maternidad.stock_minimo` (igual que en aseo) | `maternidad.stock` | **Sí.** Se edita en el formulario del catálogo; si se deja vacío, se guarda 0 |

El `stock_total` de un medicamento lo recalculan los servicios de lotes y el trigger `trg_actualizar_stock_total` a partir de sus lotes disponibles. El `stock` de aseo y maternidad se edita directamente en el producto. Solo el rol `ADMINISTRADOR` puede modificar el catálogo; los demás roles lo ven en modo consulta.

---

## 2. Cómo se compara hoy

1. **En SQL, con una vista unificada.** `vw_alertas_inventario` (script `16_vista_alertas_inventario.sql`) genera una alerta de stock mínimo por cada producto activo de las tres categorías cuyas existencias son menores o iguales a su mínimo: **crítica** si las existencias son 0 y **advertencia** si son mayores que 0.
2. **En Java, solo se lee.** `AlertasInventarioDao` consulta la vista con `WHERE id_farmacia = ?`, `ServicioAlertasInventario` la ordena y la resume, y `ControladorAlertas` la muestra en la pantalla de Alertas, a la que se llega con el botón "Alertas" del inventario. Ninguna clase compara el stock con el mínimo.
3. **La vista antigua quedó sin uso.** `vw_resumen_farmacias` solo cuenta medicamentos, y ninguna clase la consulta.

Con el valor por defecto 0, un producto sin existencias genera una alerta crítica aunque nadie haya configurado un mínimo; los casos límite están en `docs/diagramas/HU-146/CASOS_LIMITE_REPOSICION.md`.

---

## 3. Frente a los requerimientos

- **R14** (alerta de stock mínimo de un medicamento): la alerta se genera y se muestra en la pantalla de Alertas, no en el Dashboard, como pide su criterio. El contador, la campana y el panel de alertas preventivas se eliminaron del panel principal el 31 de octubre (HU.124), que solo conserva el botón "Alertas". Además, el requerimiento cita la tabla `PRODUCTOS`, que no existe: el campo está en `medicamentos`.
- **R16** (parametrizar el stock mínimo y los días de anticipación por producto): el stock mínimo se parametriza en las tres categorías desde el catálogo, solo con el rol `ADMINISTRADOR`. Los días de anticipación no se parametrizan: la vista los fija en 15 y 30 días.
- **R32** (alerta automática de un producto, con notificación inmediata en el Dashboard): aseo y maternidad ya generan alerta, pero el Dashboard no muestra notificaciones. Hay un aviso por SMS, un proceso aparte (`ProcesoSms`, HU.130) que se activa con `SMS_WORKER_ENABLED=SI` y avisa solo cuando las existencias de un producto llegan a 0.
