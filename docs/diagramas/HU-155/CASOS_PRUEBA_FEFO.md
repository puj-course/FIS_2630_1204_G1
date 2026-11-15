# Casos de prueba de la regla FEFO

Este documento reúne los casos de prueba de la regla FEFO propuesta en [`DISPENSACION_FEFO.md`](../HU-147/DISPENSACION_FEFO.md). Para cada escenario de lotes indica qué ocurre hoy al dispensar un lote, qué lote que vence antes encuentra la consulta de la regla y cuál sería el resultado con cada opción, advertir o bloquear. **Advertir o bloquear está pendiente de aprobación del equipo y no se implementa nada.** Verificado sobre el código y los scripts del repositorio en el commit `c1c483f` (13 de noviembre de 2026).

Los resultados salen de una base PostgreSQL 16 con los scripts `01` a `18`, el 13 de noviembre de 2026. "Hoy" usa las sentencias SQL del servicio de dispensación, con sus comprobaciones en el mismo orden. La consulta de la regla es la de la propuesta. Cada caso se ejecutó en su propia transacción y se deshizo.

Desde la propuesta, el equipo agregó el botón **Sugerir lote** (HU-152): si el usuario lo pulsa, selecciona el lote apto que vence primero. No avisa ni bloquea al dispensar, y su resultado se ejecutó con la clase `SelectorLoteFefo`.

---

## 1. Escenarios

Cada lote se describe con los días que faltan para que venza, sus unidades y su estado. Todos son `DISPONIBLE` salvo que se indique otro.

| Escenario | Lotes | Casos: lote elegido |
|---|---|---|
| 1. Varios lotes con vencimientos distintos | `FEFO-1A` (10 días, 8 unidades), `FEFO-1B` (40, 20), `FEFO-1C` (90, 30) | 1a `FEFO-1C`, 1b `FEFO-1B`, 1c `FEFO-1A` |
| 2. Un lote retenido que vence antes | `FEFO-2R` (5 días, 10, `RETENIDO`), `FEFO-2D` (30, 15) | 2a `FEFO-2D`, 2b `FEFO-2R` |
| 3. Un lote agotado que vence antes | `FEFO-3G` (5 días, 0, `AGOTADO`), `FEFO-3D` (30, 15) | 3a `FEFO-3D`, 3b `FEFO-3G` |
| 4. Un lote vencido que sigue `DISPONIBLE` | `FEFO-4V` (−3 días, 12), `FEFO-4D` (30, 15) | 4a `FEFO-4D`, 4b `FEFO-4V` |
| 5. Dos lotes con la misma fecha | `FEFO-5P` (40 días, 10), `FEFO-5Q` (40, 10), `FEFO-5R` (90, 10); `FEFO-5P` tiene el menor identificador | 5a `FEFO-5Q`, 5b `FEFO-5R`, 5c `FEFO-5P` |
| 6. Un lote más cercano de otro medicamento | Medicamento A: `FEFO-6D` (60 días, 15). Medicamento B: `FEFO-6Y` (5, 10) | 6a `FEFO-6D`, 6b `FEFO-6Y` |

Cada escenario es de un medicamento distinto, salvo el 6, que usa dos.

---

## 2. Resultado hoy

En cada caso se dispensan 5 unidades del lote elegido.

| Caso | Lote elegido | Qué pasa hoy | Botón Sugerir lote |
|---|---|---|---|
| 1a | `FEFO-1C` | Se dispensa sin aviso: quedan 25 y `FEFO-1A` sigue con 8 | Selecciona `FEFO-1A` |
| 1b | `FEFO-1B` | Se dispensa sin aviso: quedan 15 | Selecciona `FEFO-1A` |
| 1c | `FEFO-1A` | Se dispensa: quedan 3 | Ya es el que vence primero |
| 2a | `FEFO-2D` | Se dispensa sin aviso: quedan 10 y `FEFO-2R` sigue retenido | Ya es el que vence primero |
| 2b | `FEFO-2R` | La pantalla lo rechaza. El servicio diría `Lote no disponible: FEFO-2R` | Selecciona `FEFO-2D` |
| 3a | `FEFO-3D` | Se dispensa: quedan 10 | Ya es el que vence primero |
| 3b | `FEFO-3G` | La pantalla lo rechaza. El servicio diría `Lote no disponible: FEFO-3G` | Selecciona `FEFO-3D` |
| 4a | `FEFO-4D` | Se dispensa: quedan 10 | Ya es el que vence primero |
| 4b | `FEFO-4V` | La pantalla lo rechaza. El servicio diría `Lote vencido: FEFO-4V` | Selecciona `FEFO-4D` |
| 5a | `FEFO-5Q` | Se dispensa sin aviso: quedan 5 y `FEFO-5P` sigue con 10 | Selecciona `FEFO-5P` |
| 5b | `FEFO-5R` | Se dispensa sin aviso: quedan 5 | Selecciona `FEFO-5P` |
| 5c | `FEFO-5P` | Se dispensa: quedan 5 | Ya es el que vence primero |
| 6a | `FEFO-6D` | Se dispensa: quedan 10 | Ya es el que vence primero |
| 6b | `FEFO-6Y` | Se dispensa: quedan 5 | Ya es el que vence primero |

Ningún caso avisa de que otro lote vence antes. La pantalla rechaza los lotes que no son aptos con **Lote deshabilitado**: "No se puede dispensar un lote vencido, retenido o agotado.". Los casos que sí se dispensan dejan intacto el lote que vencía antes, y esa es la pérdida que la regla quiere evitar.

---

## 3. Resultado con advertir y con bloquear

La consulta de la regla busca, entre los lotes del mismo medicamento, el `DISPONIBLE`, con unidades, que vence después de hoy y **estrictamente antes** que el elegido.

| Caso | Lote elegido | La consulta encuentra | Con advertir | Con bloquear |
|---|---|---|---|---|
| 1a | `FEFO-1C` | `FEFO-1A` | La confirmación indica `FEFO-1A` y deja continuar | Rechaza e indica `FEFO-1A`; no se descuenta stock |
| 1b | `FEFO-1B` | `FEFO-1A` | La confirmación indica `FEFO-1A` y deja continuar | Rechaza e indica `FEFO-1A` |
| 1c | `FEFO-1A` | Ninguno | Sin aviso | Se dispensa |
| 2a | `FEFO-2D` | Ninguno: `FEFO-2R` está retenido | Sin aviso | Se dispensa |
| 2b | `FEFO-2R` | Ninguno | No llega: la pantalla lo rechaza | No llega: el servicio lo rechaza antes |
| 3a | `FEFO-3D` | Ninguno: `FEFO-3G` está agotado | Sin aviso | Se dispensa |
| 3b | `FEFO-3G` | Ninguno | No llega: la pantalla lo rechaza | No llega: el servicio lo rechaza antes |
| 4a | `FEFO-4D` | Ninguno: `FEFO-4V` ya venció | Sin aviso | Se dispensa |
| 4b | `FEFO-4V` | Ninguno | No llega: la pantalla lo rechaza | No llega: el servicio lo rechaza antes |
| 5a | `FEFO-5Q` | Ninguno: `FEFO-5P` vence el mismo día | Sin aviso | Se dispensa |
| 5b | `FEFO-5R` | `FEFO-5P` | La confirmación indica `FEFO-5P` y deja continuar | Rechaza e indica `FEFO-5P` |
| 5c | `FEFO-5P` | Ninguno | Sin aviso | Se dispensa |
| 6a | `FEFO-6D` | Ninguno: `FEFO-6Y` es de otro medicamento | Sin aviso | Se dispensa |
| 6b | `FEFO-6Y` | Ninguno | Sin aviso | Se dispensa |

Con la regla, el aviso o el rechazo solo aparece cuando hay un lote del mismo medicamento que se puede dispensar y vence antes (1a, 1b y 5b). Los lotes retenidos, agotados y vencidos no cuentan (escenarios 2, 3 y 4), y los de otros medicamentos tampoco (escenario 6).

---

## 4. Casos pendientes de decisión

Además de elegir entre advertir y bloquear, que está pendiente de aprobación, la ejecución dejó estos casos sin resolver:

| Caso | Qué se vio | Qué hay que decidir |
|---|---|---|
| Misma fecha (5a) | La consulta no encuentra nada para `FEFO-5Q`, pero Sugerir lote selecciona `FEFO-5P`, el de menor identificador | Si el empate cuenta como "vence antes" o si se mantiene el criterio estricto de la propuesta |
| Cantidad mayor que el lote que vence antes | Para `FEFO-1C` la consulta encuentra `FEFO-1A`, que solo tiene 8 unidades; un pedido de 25 no cabe en él | Qué hacer con un pedido que el lote anterior no cubre: repartirlo, avisar sin bloquear o permitirlo |
| Lote que se agota después de cargar la tabla | Si otra persona agota `FEFO-1A`, la consulta en el servicio señala `FEFO-1B`, mientras un aviso calculado con la tabla ya cargada seguiría señalando `FEFO-1A` | Dónde se calcula el aviso: en la pantalla o dentro de la transacción |
| Excepciones para bloquear | Con bloquear no hay forma de saltarse el orden, por ejemplo con un lote dañado o devuelto | Quién puede autorizar la excepción y cómo se registra |
| Lotes retenidos, agotados y vencidos | La regla los ignora: un lote retenido que vence antes no genera aviso (escenarios 2, 3 y 4) | Confirmar que es lo que se quiere |
