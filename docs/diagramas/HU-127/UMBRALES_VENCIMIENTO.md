# Umbrales de vencimiento de los lotes

Qué umbrales usa cada fuente para decidir si un lote está próximo a vencer, en qué se contradicen y qué se propone. Verificado sobre el código, los scripts y los documentos del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Umbrales por fuente

| Fuente | Umbrales | Observación |
|---|---|---|
| Requerimientos R13 y R31 | Rojo: menos de 30 días. Amarillo: de 30 a 60 días. Verde: más de 60 días | Evaluación diaria de todos los lotes; R13 habla de rangos "parametrizados" |
| Requerimiento R33 | Lote crítico: menos de 15 días | Solo define cuándo notificar por correo y SMS o WhatsApp |
| Vista `vw_alertas_vencimiento` | `VENCIDO` si la fecha es anterior a hoy. `ROJO` hasta 15 días. `AMARILLO` hasta 30 días. `VERDE` por encima | Solo lotes disponibles con cantidad mayor que cero; los rangos están fijos en el script |
| `ControlVencimientosService.sincronizar` | `VENCIDO` si la fecha de vencimiento es hoy o anterior | Cambia el estado del lote en la base de datos |
| Interfaz de lotes (`InventarioController`) | Sin umbrales de días: fila roja y estado `DESHABILITADO` si la fecha es hoy o anterior | El tooltip de la fecha dice "Vence en N días", "Vence hoy" o "Vencido hace N días" |
| Diseño de la interfaz (`DOCUMENTACION_UI.md`, HU-30) | OK: más de 30 días. Próximo a vencer: de 1 a 30 días. Vencido: fecha cumplida | Dos niveles de alerta, no tres |

La documentación de roles de HU59 (`docs/HU/HU59/documentacion-roles.md`) asigna al Jefe de Farmacia "parametrizar las alertas de vencimiento (30/60 días)", pero ninguna pantalla ni parámetro lo permite hoy. Las reglas de negocio de HU.22 (`Reglas_Modelo_Negocio.md`) fijan otros números, 90 y 15 días, pero son reglas de ingreso de un lote, no una alerta.

---

## 2. Un mismo lote según cada fuente

| Días que faltan | Requerimientos | Vista (probada) | Diseño de la interfaz |
|---|---|---|---|
| 10 | Rojo | `ROJO` | Próximo a vencer |
| 20 | Rojo | `AMARILLO` | Próximo a vencer |
| 30 | Amarillo | `AMARILLO` | Próximo a vencer |
| 31 | Amarillo | `VERDE` | OK |
| 45 | Amarillo | `VERDE` | OK |
| 61 | Verde | `VERDE` | OK |

---

## 3. En qué se contradicen

1. **El rojo.** Los requerimientos lo ponen por debajo de 30 días y la vista hasta 15, que coincide con el lote crítico de R33, no con el rojo de R13 y R31.
2. **El amarillo y el verde.** Entre 16 y 29 días la vista dice amarillo y los requerimientos rojo; entre 31 y 60 días la vista dice verde y los requerimientos amarillo.
3. **Tres niveles frente a dos.** El diseño de la interfaz solo distingue "próximo a vencer" (hasta 30 días) y "OK", y no tiene el nivel de 30 a 60 días.
4. **El día del vencimiento.** Un lote que vence hoy es `ROJO` para la vista, pero la sincronización y la interfaz ya lo tratan como vencido.
5. **La pantalla.** El semáforo de R13 no existe en la interfaz, y ninguna clase Java usa la vista.

---

## 4. Propuesta de decisión

**Estado: propuesta pendiente de aprobación del equipo.**

Tomar los requerimientos R13 y R31 como referencia única:

| Estado | Condición |
|---|---|
| Vencido | La fecha de vencimiento es hoy o anterior, como ya hacen la sincronización y la interfaz |
| Rojo | Faltan de 1 a 29 días |
| Amarillo | Faltan de 30 a 60 días |
| Verde | Faltan más de 60 días |
| Crítico (R33) | Faltan menos de 15 días; es un subconjunto del rojo y define cuándo notificar |

Para aplicarla habría que:

- Ajustar `vw_alertas_vencimiento`, o crear una vista nueva, y actualizar `DOCUMENTACION_UI.md` a esos rangos.
- Dejar los rangos en un lugar parametrizable, porque R13 habla de rangos parametrizados y HU59 de parametrizar las alertas; hoy están fijos en el script de la vista.

**Alternativa:** cambiar R13 y R31 a 15 y 30 días para que coincidan con la vista. Implica modificar dos requerimientos de prioridad alta, y deja al lote crítico de R33 igual que el rojo.
