# Sprint Planning – Sprint 2

**Fecha:** 18/08/2026  
**Duración del Sprint:** 1 semana (18/08/2026 a 22/08/2026)  

---

## Sprint Goal

Completar las Historias de Usuario #6, #7, #8 y #9, relacionadas con la trazabilidad de inventario en tiempo real, mejora de la interfaz para el registro de medicamentos, gestión de lotes y sugerencias automáticas de reabastecimiento, sentando las bases funcionales del sistema CareStock.

---

## Sprint Backlog

Las historias de usuario del sprint llevaron a la definición de las siguientes tareas en el sprint backlog:

| Historia de Usuario | Tareas Asociadas | Responsable(s) | Estimación (Pts) |
|----------------------|------------------|----------------|------------------|
| **#6 – HU.6:** Como jefe de farmacia quiero ver en tiempo real las modificaciones que realizan los auxiliares de farmacia en el sistema para poder mantener la trazabilidad, supervisar el inventario de manera efectiva y tomar decisiones oportunas. | - Definir eventos de auditoría a capturar (creación, edición, eliminación, ajustes de stock)<br>- Diseñar vista de trazabilidad con filtros por usuario, fecha y tipo de modificación<br>- Especificar actualización en tiempo real (WebSockets o polling) | Laura Ortiz, Valentina Carrillo | 4 (L) |
| **#7 – HU.7:** Como auxiliar de farmacia quiero contar con una interfaz fácil e intuitiva al momento de registrar medicamentos para poder agilizar el proceso de ingreso y ahorrar tiempo en las tareas operativas. | - Diseñar flujo de registro de medicamentos (paso a paso)<br>- Definir campos mínimos y validaciones en frontend<br>- Especificar autocompletado y búsqueda de medicamentos existentes | Laura Ortiz, Valentina Carrillo | 4 (L) |
| **#8 – HU.8:** Como auxiliar de farmacia, quiero registrar el ingreso de nuevos lotes de medicamentos indicando cantidad, número de lote, fecha de vencimiento y ubicación, para poder mantener el inventario actualizado. | - Definir estructura de datos para lotes (cantidad, lote, vencimiento, ubicación)<br>- Especificar reglas de validación (fechas, cantidades positivas, ubicaciones válidas)<br>- Diseñar flujo de ingreso de lote asociado a un medicamento existente | Mateo Salazar | 2 (M) |
| **#9 – HU.9:** Como jefe de farmacia, quiero recibir sugerencias automáticas de pedidos de reabastecimiento cuando el stock esté por debajo del mínimo configurado, para poder aprobar o ajustar las compras a proveedores. | - Definir lógica de cálculo de stock mínimo vs disponible<br>- Especificar reglas de generación de sugerencias (umbral, periodicidad)<br>- Diseñar vista de sugerencias con opción de aprobar/rechazar/ajustar | Alejandro Rodríguez, Santiago Cadena | 2 (M) |

---

## Incremento Esperado

Una vez finalizado el sprint, el jefe de farmacia podrá visualizar en tiempo real la trazabilidad de todas las modificaciones realizadas por los auxiliares sobre el inventario, los auxiliares contarán con una interfaz ágil e intuitiva para registrar medicamentos y gestionar lotes con sus respectivas fechas de vencimiento y ubicación, y el sistema generará automáticamente sugerencias de reabastecimiento cuando el stock caiga por debajo del mínimo configurado, permitiendo al jefe de farmacia aprobar o ajustar pedidos a proveedores. Esto completará el núcleo funcional de gestión de inventario del sistema CareStock.
