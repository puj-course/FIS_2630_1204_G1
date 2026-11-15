# Disponibilidad: cómo se calculan las existencias hoy

Este documento explica de dónde salen las "Existencias" de la pantalla de consulta de inventario, qué muestra la consulta y en qué se diferencian de un lote apto para dispensar. El defecto que documenta: **un lote vencido que sigue `DISPONIBLE` puede contar como existencia**. La propuesta de corrección está pendiente de aprobación del equipo y no se implementa nada. Verificado sobre el código y los scripts del repositorio en el commit `6863af8` (11 de noviembre de 2026).

---

## 1. Cómo se calcula

| Categoría | Campo que se muestra | Quién lo cambia |
|---|---|---|
| Medicamentos | `medicamentos.stock_total` | Se recalcula a partir de los lotes; no se edita a mano |
| Aseo | `aseo.stock` | El catálogo (solo `ADMINISTRADOR`), los movimientos y los ajustes |
| Maternidad | `maternidad.stock` | Igual que aseo |

Aseo y maternidad no tienen lotes ni vencimientos, así que el defecto solo afecta a los medicamentos. Hay **dos fórmulas** para recalcular `stock_total`:

| Quién | Cuándo | Qué lotes suma |
|---|---|---|
| Trigger `trg_actualizar_stock_total` (función `fn_actualizar_stock_total_medicamento`, script `02`) | Tras insertar un lote o cambiar su `cantidad_actual` o su `estado_lote` | `DISPONIBLE`, **sin mirar la fecha** |
| `FarmacovigilanciaService.dispensarMedicamento`, `ServicioCrudLotes` y `ControlVencimientosService.sincronizar` | Al dispensar, al crear, editar o retirar un lote, y al cargar los lotes en el inventario | `DISPONIBLE` y `fecha_vencimiento > CURRENT_DATE` |
| `ServicioAjustesInventario` | Al ajustar las existencias de un lote: solo actualiza el lote, y el recálculo lo hace el trigger | La del trigger, **sin fecha** |

Cuando un servicio cambia un lote, el trigger corre primero y el servicio después, así que gana la fórmula del servicio. Con un ajuste, o con un cambio directo en SQL, queda la del trigger. Además, **el paso del tiempo no recalcula nada** (el trigger no se activa al cambiar la fecha) y la consulta no sincroniza los vencimientos.

---

## 2. Qué muestra la consulta

Se llega desde el botón de consulta unificada del catálogo o desde "Ver producto / lote" de una alerta de stock, con cualquier rol. `ConsultaInventarioController` llama a `Inventario.listarInventarioUnificado()`, que pasa por la caché (`ConsultaInventarioProxy`, 30 segundos por defecto) y llega a `ConsultaInventarioDAO`, que une las tres tablas con `UNION ALL` filtradas por farmacia.

| Columna | Qué muestra |
|---|---|
| Código | `codigo_invima` del medicamento o `codigo` de aseo y maternidad |
| Producto | El nombre |
| Categoría | Medicamento, Aseo o Maternidad |
| Presentación / unidad | La del medicamento; "No registrada" en aseo y maternidad |
| Existencias | `stock_total` en medicamentos; `stock` en aseo y maternidad |
| Estado | El estado del **producto** (`ACTIVO`, `INACTIVO` o, en medicamentos, `DESCONTINUADO`), no si hay unidades |

| Filtro | Opciones | Cómo funciona |
|---|---|---|
| Categoría | Todas, Medicamento, Aseo y Maternidad | Compara la categoría de la fila |
| Disponibilidad (HU-151) | Todos, Con existencias y Agotados | `existencia > 0` o `existencia == 0` |
| Búsqueda | Texto libre | Lo contenido en el código o en el nombre |

Los filtros se combinan, "Actualizar datos" salta la caché, y los productos inactivos también aparecen. Como el filtro de disponibilidad usa el número de la columna Existencias, hereda el defecto.

---

## 3. Diferencia con un lote apto

| | "Existencias" de la consulta | Lote apto para dispensar |
|---|---|---|
| Qué es | Un número por producto, guardado en `stock_total` | Un lote concreto, evaluado en el momento con `Lote.esAptoParaDispensar()` |
| Condiciones | Según quién lo recalculó: sin fecha (trigger) o con `fecha_vencimiento > CURRENT_DATE` (servicios) | `DISPONIBLE`, `cantidad_actual > 0` y vencimiento posterior a `FechaNegocio.hoy()` |
| Qué fecha es "hoy" | `CURRENT_DATE`, la del servidor de la base | `FechaNegocio.hoy()`, la de Bogotá |
| Dónde se usa | La consulta, las alertas de stock y el SMS | El botón Dispensar y el filtro "Ver aptos" |

### Ejemplo ejecutado

Lo ejecuté en una base PostgreSQL 16 con los scripts `01` a `18`, cargada con `src/sql/test_data/alertas_prueba.sql`, usando las sentencias reales de la consulta, de los servicios y de la sincronización. Cada fila parte de los datos de prueba, que dejan el medicamento `ALERTAS-A-MED-03` con un lote vencido de 20 unidades y uno vigente de 50.

| Caso | Existencias | Aptas |
|---|---|---|
| 1. Los datos de prueba tal como quedan | 70 | 50 |
| 2. Se ajusta el lote vigente de 50 a 48 | 68 | 48 |
| 3. `ALERTAS-A-MED-04` tiene un lote vigente de 30 unidades; pasan 11 días sin que nadie recalcule | 30 | 0 |
| 4. Se cargan los lotes en el inventario y corre la sincronización: el lote vencido pasa a `VENCIDO` | 50 | 50 |

Las existencias del caso 1, la consulta y sus unidades aptas:

```sql
SELECT m.codigo_invima, COALESCE(m.stock_total, 0) AS existencias
FROM medicamentos m
WHERE m.codigo_invima = 'ALERTAS-A-MED-03';
```

```sql
SELECT COALESCE(SUM(l.cantidad_actual), 0) AS unidades_aptas
FROM lotes l
JOIN medicamentos m ON m.id_medicamento = l.id_medicamento
WHERE m.codigo_invima = 'ALERTAS-A-MED-03'
  AND l.estado_lote = 'DISPONIBLE'
  AND l.cantidad_actual > 0
  AND l.fecha_vencimiento > CURRENT_DATE;
```

**Conclusión:** las 20 unidades vencidas cuentan como existencias en el caso 1 y tras un ajuste (caso 2), y un lote que vence sigue contando mientras nadie recalcule (caso 3). Solo la sincronización lo corrige.

---

## 4. Propuesta

**Estado: pendiente de aprobación del equipo.** El objetivo es que la disponibilidad que ve el usuario no cuente los lotes vencidos.

| Opción | Qué cambia | Qué corrige | Qué no corrige |
|---|---|---|---|
| **A. Calcular las existencias aptas al consultar** | En `ConsultaInventarioDAO`, reemplazar `COALESCE(m.stock_total, 0)` por la suma de los lotes `DISPONIBLE` con `fecha_vencimiento > CURRENT_DATE` | Todos los casos del ejemplo, incluido el paso del tiempo, y los filtros de disponibilidad | Los demás lectores de `stock_total`. Agrega una subconsulta por medicamento, que el índice `ix_lotes_medicamento_estado_vencimiento` cubre |
| **B. Alinear el trigger con los servicios** | Agregar `AND fecha_vencimiento > CURRENT_DATE` en `fn_actualizar_stock_total_medicamento`, con un script nuevo | Los ajustes y los cambios por SQL directo: el ajuste de 50 a 48 deja 48 y no 68 | El paso del tiempo, y lo ya guardado, que necesita un `UPDATE` único que recalcule `stock_total` |
| **C. Sincronizar los vencimientos al consultar o de forma periódica** | Ejecutar `ControlVencimientosService.sincronizar` desde la consulta o desde un proceso programado | El paso del tiempo: los lotes pasan a `VENCIDO` | Escribe en la base desde una lectura, o exige un planificador que no existe hoy |

**Recomendación:** A y B juntas. A hace que la pantalla siempre diga la verdad, y B deja coherente el valor guardado. Las probé en la misma base, sin cambiar el repositorio: con A, la existencia coincide con las unidades aptas en los cuatro casos; con B, el ajuste de 50 a 48 deja 48, y los datos ya guardados siguen en 70 hasta recalcularlos.

También leen `stock_total` la vista `vw_alertas_inventario`, `ServicioAlertasSms`, `MedicamentoCatalogoDAO.desactivar` y `MedicamentoDAOImpl`. Con A seguirían contando los lotes vencidos, y con B quedarían corregidos salvo por el paso del tiempo.
