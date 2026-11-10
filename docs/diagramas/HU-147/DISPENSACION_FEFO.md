# Dispensación y FEFO: cómo se elige el lote y una regla propuesta

FEFO (First Expired, First Out) significa dispensar primero el lote que vence antes. Este documento describe cómo se elige hoy el lote a dispensar y propone una regla FEFO. **Es una propuesta pendiente de aprobación del equipo y no se implementa nada.** Verificado sobre el código y los scripts del repositorio en el commit `0b7e935` (2 de noviembre de 2026).

---

## 1. Estado actual

1. **La tabla trae todos los lotes de la farmacia.** `Inventario.listarLotes` usa `LoteDAOImpl.listarPorFarmacia`, que ordena por `fecha_vencimiento ASC, id_lote ASC`, con todos los medicamentos juntos y en cualquier estado.
2. **La tabla muestra** ID Lote, Número de Lote, ID Medicamento, Cantidad, Fecha Vencimiento y Estado Sanitario, pero no el nombre del medicamento. Se busca por número de lote, id de medicamento o estado, y se reordena haciendo clic en cualquier columna.
3. **El usuario elige la fila.** No se preselecciona ningún lote: la tabla limpia la selección al recargarse y la pantalla muestra "Ninguno (Haga clic en una fila de la tabla)".
4. **`onDispensar`** pide un lote seleccionado y apto (`Lote.esAptoParaDispensar`: `DISPONIBLE`, con unidades y que vence después de hoy), una cantidad válida y una confirmación que muestra el lote, la cantidad, el stock actual y el stock que quedará.
5. **`FarmacovigilanciaService.dispensarMedicamento`** bloquea ese lote y valida sobre él las seis reglas de la dispensación. No lo compara con los demás lotes del medicamento.

**No existe ninguna regla FEFO.** Ordenar la tabla no basta: `DocBase.md` dice que ordenarlos "no constituye por sí solo una garantía de dispensación FEFO". Un ejemplo ejecutado en una base de pruebas, con las sentencias del código:

| Posición en la tabla | Lote | Medicamento | Vence en | Estado |
|---|---|---|---|---|
| 1 | X6 | Med X | -3 días | `VENCIDO` |
| 2 | X5 | Med X | 2 días | `AGOTADO` |
| 3 | X4 | Med X | 3 días | `RETENIDO` |
| 4 | Y1 | Med Y | 5 días | `DISPONIBLE` |
| 5 | X1 | Med X | 10 días | `DISPONIBLE` |
| 6 | X2 | Med X | 40 días | `DISPONIBLE` |
| 7 | X7 | Med X | 40 días | `DISPONIBLE` |
| 8 | X3 | Med X | 90 días | `DISPONIBLE` |

Las tres primeras filas no se pueden dispensar y la cuarta es de otro medicamento. Dispensar 5 unidades del lote X3 se permite sin ningún aviso y deja intacto X1, con 8 unidades; cuando X1 vence, la sincronización registra un `AJUSTE` de 8 unidades por vencimiento, que es la pérdida.

**Lo que los documentos prometen y el código no cumple:**

- **R11 y R26**, de prioridad Alta, piden que el sistema aplique FEFO "sugiriendo" el lote más próximo a vencer, y su criterio exige que en el 100 % de los despachos se seleccione por defecto ese lote. Hoy no se selecciona ninguno.
- **`FlujoDespachoAsistido.md`** dice que el usuario selecciona el lote "por defecto, el primero de la lista". En el ejemplo, el primero es un lote vencido.
- **La documentación de roles (HU59)** lista para el Auxiliar de Farmacia el "Despacho de medicamentos (con regla FEFO)".
- **`conf/settings.json`** tiene `"fefoDispatch": false` y **`conf/config.yaml`** tiene `lot_strategy: FEFO`, pero ningún código lee esos archivos.

---

## 2. Propuesta de regla FEFO

**Regla:** antes de dispensar un lote, comprobar si existe otro lote del mismo medicamento que vence antes, y en ese caso avisar o bloquear.

**Qué es "vence antes":** un lote del mismo medicamento que está `DISPONIBLE`, tiene unidades, vence después de hoy y vence **estrictamente antes** que el elegido. No cuentan los lotes vencidos, retenidos ni agotados, ni los de otros medicamentos, y con la misma fecha no hay aviso:

```sql
SELECT l.id_lote, l.numero_lote, l.fecha_vencimiento
FROM lotes l
WHERE l.id_medicamento = ?
  AND l.id_lote <> ?
  AND l.estado_lote = 'DISPONIBLE'
  AND l.cantidad_actual > 0
  AND l.fecha_vencimiento > CURRENT_DATE
  AND l.fecha_vencimiento < ?
ORDER BY l.fecha_vencimiento, l.id_lote
LIMIT 1
```

Con el ejemplo anterior, para X3, X2 y X7 devuelve X1; para X1 y para Y1 no devuelve nada. Si X1 se agota, X2 y X7, que vencen el mismo día, tampoco se avisan entre sí. La consulta usa las columnas del índice `ix_lotes_medicamento_estado_vencimiento`.

| Aspecto | Advertir | Bloquear |
|---|---|---|
| Qué hace | La confirmación indica el lote que vence antes y deja continuar | Rechaza la dispensación con una `ReglaNegocioException` |
| Dónde se aplica | En la pantalla, con los lotes ya cargados | En `FarmacovigilanciaService`, dentro de la transacción |
| Garantía | Ninguna: el usuario puede ignorar el aviso | Total: no se dispensa un lote antes que otro que vence antes |
| Casos legítimos | Los permite | Exigirían una excepción autorizada |
| Requerimientos | R11 y R26 piden "sugerir" | Va más allá de lo pedido |
| Costo | Bajo: no cambia el servicio | Mayor: consulta, mensaje, excepción y pruebas |

**Recomendación:** empezar por advertir, que es lo que piden R11 y R26, y evaluar bloquear más adelante. Para cumplir el criterio de R11 también habría que preseleccionar el lote apto que vence antes del medicamento buscado, y no el primero de la tabla.

---

## 3. A qué clases afectaría

| Clase | Qué cambiaría | Opción |
|---|---|---|
| `ValidadorDispensacion` | Un método puro que busque el lote que vence antes entre los lotes cargados, y el aviso en `mensajeConfirmacion` | Advertir |
| `ControladorInventario` | `onDispensar` y `confirmarDispensacion` mostrarían el aviso y, si se aprueba, preseleccionarían el lote sugerido | Advertir |
| `FarmacovigilanciaService` | `dispensarMedicamento` haría la consulta después de leer el lote y antes de descontar, con un mensaje nuevo | Bloquear |
| `Inventario` | `dispensarMedicamento` recibiría una excepción autorizada, solo si se bloquea con excepción | Bloquear |
| `ServicioFarmacovigilanciaTest` y `ValidadorDispensacionTest` | Pruebas de la regla | Ambas |
| `docs/user_guide/dispensacion.md`, `conf/settings.json` y `conf/config.yaml` | Explicar la regla y alinear los dos interruptores que ningún código lee | Ambas |

---

## 4. Riesgos

1. **Un lote con menos unidades que las pedidas.** Se dispensa de un solo lote: si el que vence antes tiene 2 unidades y hacen falta 5, habría que dispensar en dos operaciones o permitir repartir.
2. **Casos legítimos para saltarse el orden,** como un lote dañado o devuelto. Bloquear sin excepción frena la atención en el mostrador.
3. **Avisos ignorados.** Si siempre se aceptan, advertir no cambia nada. El detalle del movimiento es fijo ("Dispensación del lote" más el número), así que habría que ampliarlo para registrar cuándo se ignoró.
4. **Datos desactualizados.** La pantalla usa los lotes de la última carga, y otro usuario puede haber agotado el lote que vence antes. Bloquear en el servicio no tiene este problema, porque consulta dentro de la transacción.
5. **El primero de la tabla no sirve para sugerir:** el orden es global y mezcla lotes no dispensables y de otros medicamentos.
6. **Solo aplica a medicamentos,** porque aseo y maternidad no tienen lotes ni vencimiento.
7. **Costo de calidad.** El código nuevo necesita pruebas para cumplir el 80 % de cobertura que exige el quality gate de Sonar.

---

## 5. Referencias

- [`ARQUITECTURA_FUTURA.md`](../../architecture/ARQUITECTURA_FUTURA.md) ya marca FEFO como propuesto: lo lista entre las responsabilidades del "Servicio de inventario", que figura como "Propuesto" (sección 2.1), y fija como criterio de diseño que la selección por fecha de vencimiento es una regla de ese servicio, con controles de disponibilidad del lote (sección 4).
- Otros documentos que tratan FEFO: [`Requerimientos_CareStock.md`](../../architecture/Requerimientos_CareStock.md) (R11 y R26), [`DocBase.md`](../../architecture/DocBase.md), [`FlujoDespachoAsistido.md`](../../architecture/FlujoDespachoAsistido.md), [`documentacion-roles.md`](../../HU/HU59/documentacion-roles.md) y [`Sprint2_Review.md`](../../scrum/SprintReview/Sprint2_Review.md), donde el equipo presentó el despacho FEFO como una sugerencia automática al auxiliar.
