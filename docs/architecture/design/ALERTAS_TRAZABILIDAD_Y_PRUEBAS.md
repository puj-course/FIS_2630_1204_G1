# Alertas de inventario: trazabilidad, pruebas y guía de uso

Este documento explica cómo una alerta permite rastrear qué pasó con el producto, qué pruebas respaldan el módulo y cómo usarlo.

## 1. Trazabilidad: el último movimiento

Cada alerta trae tres datos que dicen **quién tocó el producto por última vez y cuándo**: `ultimo_movimiento_fecha`, `ultimo_movimiento_tipo` y `ultimo_movimiento_usuario`. La pantalla los muestra en la columna "Ultimo movimiento" (`ControladorAlertas.textoUltimoMovimiento`, L247), con el formato `SALIDA · Farma A · 26/10/2026 14:41`, o "Sin movimientos registrados" si no hay ninguno.

| Alerta de | Movimiento que se considera | Fuentes |
|---|---|---|
| Medicamento (stock mínimo) | El más reciente de **cualquiera de sus lotes** | `log_movimientos` y `auditoria_ajustes` |
| Lote (vencimiento) | El más reciente **de ese lote** | `log_movimientos` y `auditoria_ajustes` |
| Aseo o maternidad | El más reciente del producto | `movimientos_productos` y `auditoria_ajustes` |

En la vista, la búsqueda se hace con `LEFT JOIN LATERAL` y `ORDER BY fecha DESC LIMIT 1` (por ejemplo, L38 en el bloque de medicamentos). Es un `LEFT JOIN` a propósito: un producto que nunca tuvo movimientos igual debe alertar.

### Para qué sirve

Ante una alerta de stock bajo, el usuario puede saber de inmediato si el último cambio fue una venta, una baja o un ajuste, y quién lo hizo. Para ver el detalle completo se usa el historial de movimientos, que reúne las mismas tres fuentes.

| Si el último movimiento es | Lectura probable |
|---|---|
| `SALIDA` | El stock bajó por una dispensación o una venta |
| `ENTRADA` | Hubo reposición, pero todavía no alcanza el mínimo |
| `AJUSTE` | El conteo físico corrigió las existencias |
| Sin movimientos | El producto se creó con ese stock o nunca se movió |

### Limitación

Si el stock de aseo o maternidad se modifica **editando el producto en el catálogo**, ese cambio no genera un movimiento y no aparece como "último movimiento". El script `src/sql/verificaciones/consistencia_inventario.sql` detecta ese caso.

## 2. Pruebas

### Pruebas unitarias nuevas (29)

No necesitan base de datos y corren con `mvnw clean verify`.

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `AlertaInventarioTest` | 13 | Los siete textos de `mensaje()`, la referencia (lote o código) y las validaciones del constructor |
| `ServicioAlertasInventarioTest` | 10 | Resumen por severidad y tipo, los tres filtros, su combinación y la lista sin filtros, farmacia inválida, sesión inactiva y DAO nulo |
| `StockMinimoCatalogoTest` | 6 | El stock mínimo de aseo y maternidad: valor por defecto, valores válidos y rechazo de negativos |

### Pruebas de integración con PostgreSQL (49 comprobaciones)

Se ejecutaron sobre una base creada con los scripts del repositorio.

| Bloque | Qué comprueba |
|---|---|
| Vista | Las 11 alertas esperadas, las 9 exclusiones, el orden, el último movimiento, el mensaje de un lote vencido y la separación entre farmacias |
| Servicio y fachada | Lista, resumen (5 críticas, 6 advertencias, 6 de stock, 5 de vencimiento) y rechazo de otra farmacia |
| Información actualizada | Una alerta aparece tras una salida, desaparece tras una entrada, vuelve tras un ajuste y pasa a crítica al llegar a 0 |
| Stock mínimo en el catálogo | Guardar y editar el mínimo de aseo y maternidad, rechazo de negativos en el modelo y en la base |

### Pruebas de interfaz (25 comprobaciones)

Se ejecutaron en un entorno gráfico virtual con la aplicación contra PostgreSQL.

| Pantalla | Qué comprueba |
|---|---|
| Alertas | Carga, resumen, orden, filtros por tipo, categoría y severidad (y combinados), último movimiento, "Actualizar ahora" y el **refresco automático de 60 segundos** |
| Panel principal | El botón muestra el total, se pinta de rojo si hay críticas, y cada farmacia ve su propio conteo |
| Catálogo | Los campos de stock mínimo aparecen según la categoría y las filas bajo su mínimo se resaltan también en aseo y maternidad |

### Resultado de la suite completa

82 pruebas JUnit, todas correctas (las 29 nuevas y las 53 anteriores).

## 3. Guía de uso

### Ver el estado de las alertas

1. Inicie sesión con un usuario con farmacia asignada.
2. En el panel principal, el botón **Alertas** indica cuántas hay. Su color resume la gravedad:

| Color | Significado |
|---|---|
| Rojo | Hay al menos una alerta crítica |
| Ámbar | Hay solo advertencias |
| Verde azulado | No hay alertas (el botón dice solo "Alertas") |

3. Pulse el botón para abrir la pantalla de alertas.

### Leer la pantalla de alertas

- Arriba están los totales de **críticas**, **advertencias** y el total, y la hora de la última actualización.
- Las filas rojas son críticas y las amarillas, advertencias.
- La columna **Detalle** dice qué ocurre ("Existencias 3 (mínimo 5)", "Vence en 12 días").
- La columna **Ultimo movimiento** dice quién tocó el producto por última vez.

### Filtrar

Use los tres selectores **Tipo**, **Categoria** y **Severidad**; se pueden combinar. "Todos" quita el filtro.

### Mantenerla al día

La pantalla se actualiza sola cada 60 segundos. Para verla de inmediato, pulse **Actualizar ahora**.

### Definir el stock mínimo de aseo y maternidad

1. Abra el **Catálogo** y elija la categoría.
2. Al registrar o editar un producto, complete **Stock mínimo (genera alerta)**.
3. Si lo deja vacío, se guarda 0 y el producto solo alertará cuando se agote.

### Resolver una alerta

| Tipo | Qué hacer |
|---|---|
| Stock mínimo | Registrar una entrada de existencias; la alerta desaparece sola |
| Vencimiento | Dispensar el lote antes de que venza, o retirarlo; un lote ya vencido se deshabilita al abrir el panel |

## 4. Limitaciones conocidas

- Los umbrales de 15 y 30 días para el vencimiento no se pueden configurar desde la aplicación.
- Las alertas no se marcan como leídas ni se atienden por separado: desaparecen cuando la condición deja de cumplirse.
- El contador del panel principal se actualiza al cargar el panel, no cada 60 segundos como la pantalla de alertas.
- Los usuarios sin farmacia asignada no ven alertas.
