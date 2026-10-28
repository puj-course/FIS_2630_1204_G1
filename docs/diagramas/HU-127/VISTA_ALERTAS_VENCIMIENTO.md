# Vista `vw_alertas_vencimiento`

Lista los lotes disponibles de los medicamentos con su farmacia, los días que faltan para vencer y un nivel de alerta. Está definida en `src/sql/vw_alertas_vencimiento.sql`. Verificado sobre el código y los scripts del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Columnas

| Columna | Qué contiene |
|---|---|
| `id_lote` | Identificador del lote |
| `id_medicamento` | Identificador del medicamento del lote |
| `id_farmacia` | Farmacia del medicamento; sale de `medicamentos`, no de `lotes` |
| `nombre_comercial` | Nombre comercial del medicamento |
| `numero_lote` | Número del lote |
| `cantidad_actual` | Cantidad actual del lote |
| `fecha_vencimiento` | Fecha de vencimiento del lote |
| `dias_para_vencer` | Días entre hoy y la fecha de vencimiento (`fecha_vencimiento - CURRENT_DATE`); negativo si ya pasó |
| `nivel_alerta` | `VENCIDO`, `ROJO`, `AMARILLO` o `VERDE` |

---

## 2. Qué lotes incluye

Une `lotes` con `medicamentos` y solo incluye los lotes con `estado_lote = 'DISPONIBLE'` y `cantidad_actual > 0`. No aparecen los lotes `AGOTADO`, los que ya están marcados `VENCIDO` ni los de cantidad 0.

---

## 3. Niveles de alerta

| Nivel | Condición |
|---|---|
| `VENCIDO` | La fecha de vencimiento es anterior a hoy |
| `ROJO` | Faltan de 0 a 15 días |
| `AMARILLO` | Faltan de 16 a 30 días |
| `VERDE` | Faltan más de 30 días |

Un lote que vence hoy es `ROJO` (0 días). El nivel `VENCIDO` solo aparece mientras `ControlVencimientosService.sincronizar` no haya pasado el lote a estado `VENCIDO`; después el lote sale de la vista.

---

## 4. Consulta de ejemplo por farmacia

```sql
SELECT id_lote, nombre_comercial, numero_lote, cantidad_actual, fecha_vencimiento, dias_para_vencer, nivel_alerta
FROM vw_alertas_vencimiento
WHERE id_farmacia = 1
ORDER BY dias_para_vencer;
```

Reemplaza `1` por el identificador de la farmacia. Para ver solo las alertas, agrega `AND nivel_alerta <> 'VERDE'`.

---

## 5. Límites

- **También devuelve los lotes en verde.** No es una lista de alertas, sino de todos los lotes disponibles; hay que filtrar `nivel_alerta` para quedarse con las alertas.
- **Ninguna clase Java la usa.** La aplicación no consulta la vista.
- **Vive en `src/sql/`, fuera de `ddl/`,** y el `README` de esa carpeta no la menciona. Solo la nombra `docs/diagramas/COMPONENTE_BASE_DATOS.md`.
- **Nada garantiza que exista en Neon.** Ningún flujo de CI/CD, script ni compose aplica los archivos de `src/sql/`. Se comprueba con `SELECT viewname FROM pg_views WHERE viewname = 'vw_alertas_vencimiento';`.
