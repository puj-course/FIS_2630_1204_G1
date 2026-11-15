# Criterios de atención del inventario

Este documento reúne cuándo considera el sistema que algo requiere atención en cada pantalla o proceso, muestra con un ejemplo ejecutado en qué difieren y propone qué criterio unificar. **La propuesta está pendiente de aprobación del equipo y no se implementa nada.** Verificado sobre el código y los scripts del repositorio en el commit `d1d8db3` (13 de noviembre de 2026).

---

## 1. Criterios por fuente

| Fuente | Dónde se ve y quién | Cuándo requiere atención | Clase o consulta |
|---|---|---|---|
| Vista `vw_alertas_inventario` | Pantalla de **Alertas**, cualquier rol con farmacia | **Stock:** un producto `ACTIVO` con existencias menores o iguales al mínimo; `CRITICA` si las existencias son 0 y, si no, `ADVERTENCIA`. **Vencimiento:** un lote `DISPONIBLE` con unidades que vence en 30 días o menos, incluidos los que ya vencieron; `CRITICA` a 15 días o menos y `ADVERTENCIA` entre 16 y 30 | Script `16_vista_alertas_inventario.sql`, leído por `ServicioAlertasInventario` |
| Columna **Reponer** | Pantalla de **Alertas**, cualquier rol con farmacia | En las alertas de stock mínimo, la cantidad sugerida `max(0, 2 × mínimo − existencias)`. En las de vencimiento, "—" | `SugerenciaReposicion` (`FACTOR_STOCK_OBJETIVO = 2`) |
| Pantalla **Reposición** | Botón **Reposición** del inventario, solo `ADMINISTRADOR` con farmacia | Las alertas de stock mínimo de la vista, con el **Faltante** `max(0, mínimo − existencias)`. Crítica si las existencias son 0 | `ServicioPlanificacionReposicion` |
| Proceso de **SMS** | Mensajes a los destinatarios autorizados; no tiene pantalla | **Stock:** existencias igual a 0. **Vencimiento:** un lote `DISPONIBLE` o `VENCIDO` con unidades que vence en 15 días o menos, incluidos los que ya vencieron | `ServicioAlertasSms`, con su propio SQL |

Las existencias de un medicamento son su `stock_total`; las de aseo y maternidad, su `stock`. El mínimo es `stock_minimo`, que vale 0 por defecto.

---

## 2. Ejemplo ejecutado

Lo ejecuté el 13 de noviembre de 2026 en una base PostgreSQL 16 con los scripts `01` a `18`. Usé la vista y la consulta del SMS con sus sentencias reales, y las clases `SugerenciaReposicion` y `ServicioPlanificacionReposicion`, que recibieron las filas reales de la vista. Reemplacé la sesión, la conexión y la caché compartida por piezas mínimas, porque la prueba no las usa. Los datos se crearon en una transacción que se deshizo.

**Stock.** Productos de aseo con distintas existencias y mínimos:

| Producto | Existencias | Mínimo | Vista | Alertas: Reponer | Reposición: Faltante | SMS |
|---|---|---|---|---|---|---|
| `CRIT-A` | 2 | 5 | Advertencia | **8** | **3** | No |
| `CRIT-B` | 0 | 5 | Crítica | 10 | 5 | Sí |
| `CRIT-C` | 5 | 5 | Advertencia | 5 | 0 | No |
| `CRIT-D` | 20 | 5 | Sin alerta | No aparece | No aparece | No |
| `CRIT-E` | 0 | 0 | Crítica | 0 | 0 | Sí |
| `CRIT-F` | 3 | 0 | Sin alerta | No aparece | No aparece | No |

**Con 2 existencias y mínimo 5, Alertas sugiere 8 y Reposición muestra 3.**

**Vencimiento.** Un medicamento con lotes de 7 unidades a distintos días de vencer:

| Días para vencer | Estado del lote | Vista | SMS |
|---|---|---|---|
| 31 | `DISPONIBLE` | Sin alerta | No |
| 30 | `DISPONIBLE` | Advertencia | No |
| 16 | `DISPONIBLE` | Advertencia | No |
| 15 | `DISPONIBLE` | Crítica | Sí |
| 0 (vence hoy) | `DISPONIBLE` | Crítica | Sí |
| −3 (ya venció) | `DISPONIBLE` | Crítica | Sí |
| −3 (ya venció) | `VENCIDO` | Sin alerta | Sí |

En las alertas de vencimiento, la columna Reponer muestra "—".

---

## 3. Contradicciones

| Qué pasa | Detalle |
|---|---|
| Dos cantidades para reponer el mismo producto | Alertas sugiere `2 × mínimo − existencias` y Reposición muestra `mínimo − existencias`. Con 2 y 5 son 8 y 3; con 5 y 5, son 5 y 0. Ninguna pantalla dice qué mide su número |
| Un producto agotado sin mínimo | `CRIT-E` es crítico en Alertas, en Reposición y en el SMS, pero sugiere reponer 0 unidades. El mínimo vale 0 por defecto |
| El SMS avisa menos que las pantallas | Solo avisa del stock en 0. `CRIT-A`, con 2 y mínimo 5, aparece en Alertas y en Reposición, pero no genera SMS |
| Un lote `VENCIDO` con unidades | La vista no lo muestra, así que desaparece de Alertas, pero el SMS sigue avisando mientras tenga unidades |
| Quién ve qué | La columna Reponer la ve cualquier rol con farmacia, pero la pantalla de Reposición solo el `ADMINISTRADOR`: un `FARMACEUTICO` ve la sugerencia y no puede abrir la planificación |
| Las existencias de un medicamento pueden contar lotes vencidos | La alerta de stock usa `stock_total`, que en ciertos casos incluye lotes vencidos que siguen `DISPONIBLE`, así que la alerta puede no aparecer. Está explicado en [`DISPONIBILIDAD_ESTADO_ACTUAL.md`](../HU-156/DISPONIBILIDAD_ESTADO_ACTUAL.md) |
| Los documentos no coinciden | R13 pide rojo a menos de 30 días, amarillo de 30 a 60 y verde a más de 60, y el sistema usa 15 y 30 días, sin verde. R33 habla de lotes a menos de 15 días, y el SMS avisa a 15 o menos. R16 pide parametrizar los días de anticipación, y están fijos en la vista. `conf/config.yaml` tiene `expiration_warning_days: 30`, pero ningún código lo lee |
| Otras pantallas pintan "crítico" con reglas propias | El inventario marca como crítica la fila de un lote que vence hoy o antes, y la consulta unificada la de un producto con 0 existencias |

---

## 4. Propuesta

**Estado: pendiente de aprobación del equipo.** Hoy cuatro fuentes deciden por su cuenta cuándo algo requiere atención. Se propone unificar estos tres criterios:

| Criterio | Hoy | Propuesta |
|---|---|---|
| **Cantidad a reponer** | 8 en Alertas y 3 en Reposición, sin que ninguna pantalla diga qué mide | Definirla una sola vez, en una clase que usen las dos pantallas y el CSV, con un nombre que diga qué es. Las opciones están en la tabla siguiente |
| **Qué es crítico** | La vista y el SMS repiten por separado "existencias en 0" y "lote a 15 días o menos" | Que el SMS lea la vista y se quede con la severidad `CRITICA`, así un umbral se cambia en un solo lugar |
| **Qué lotes cuentan** | La vista solo cuenta los `DISPONIBLE`; el SMS también los `VENCIDO` | Una misma condición para los dos: solo `DISPONIBLE`, o `DISPONIBLE` y `VENCIDO` mientras tengan unidades |

Opciones para la cantidad a reponer:

| Opción | Qué cambia | Qué resuelve |
|---|---|---|
| **A. Dos cantidades con nombre (propuesta)** | Mostrar en las dos pantallas el **Faltante para el mínimo** (`mínimo − existencias`) y el **Sugerido a pedir** (`2 × mínimo − existencias`), calculados en una sola clase | Ya no hay dos números sin nombre: con 2 existencias y mínimo 5, las dos pantallas dirían faltante 3 y sugerido 8 |
| **B. Solo el doble del mínimo** | Reposición usa `SugerenciaReposicion` y deja de mostrar el faltante | Un solo número, pero se pierde cuánto falta para llegar al mínimo |
| **C. Solo el faltante** | Alertas usa `mínimo − existencias` | Un solo número, pero se pierde el colchón hasta el doble del mínimo |
