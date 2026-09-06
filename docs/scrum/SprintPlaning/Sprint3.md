# Sprint Planning – Sprint 3

**Fecha:** 24/08/2026  
**Duración del Sprint:** 1 semana (24/08/2026 a 29/08/2026)  

---

## Sprint Goal

Completar las Historias de Usuario #17, #18, #19 y #20, relacionadas con la búsqueda y disponibilidad de medicamentos en tiempo real, la aprobación de pedidos de compra, la generación de reportes descargables para auditoría y la optimización de la atención al cliente en el mostrador, avanzando en la funcionalidad operativa del sistema CareStock.

---

## Sprint Backlog

Las historias de usuario del sprint llevaron a la definición de las siguientes tareas en el sprint backlog:

| Historia de Usuario | Tareas Asociadas | Responsable(s) | Estimación (Pts) |
|----------------------|------------------|----------------|------------------|
| **#17 – HU.17:** Como Auxiliar de Farmacia, quiero buscar medicamentos por nombre o principio activo y ver su disponibilidad, lote y ubicación en tiempo real, para poder atender rápidamente las solicitudes de los clientes en el mostrador. | - Diseñar interfaz de búsqueda con filtros por nombre y principio activo<br>- Implementar consulta en tiempo real a la base de datos Oracle<br>- Mostrar resultados con disponibilidad, lote y ubicación física<br>- Optimizar tiempos de respuesta de búsqueda | Laura Ortiz, Valentina Carrillo | 4 (L) |
| **#18 – HU.18:** Como Encargado de Supervisión General, quiero revisar y aprobar o rechazar los pedidos de compra generados por el Jefe de Farmacia, para poder validar el presupuesto y las condiciones comerciales antes de confirmar la compra. | - Diseñar vista de pedidos pendientes de aprobación<br>- Implementar flujo de aprobación/rechazo con justificación<br>- Definir reglas de validación presupuestal<br>- Registrar auditoría de decisiones tomadas | Mateo Salazar, Alejandro Rodríguez | 4 (L) |
| **#19 – HU.19:** Como Jefe de Farmacia, quiero generar reportes descargables de productos agotados, pérdidas por vencimiento e historial de movimientos, para poder usarlos como soporte en auditorías y en la toma de decisiones de compra. | - Definir estructura de reportes (PDF/Excel)<br>- Implementar consultas para productos agotados y pérdidas por vencimiento<br>- Generar historial de movimientos filtrado por fechas<br>- Agregar opción de descarga con formato definido | Alejandro Rodríguez, Santiago Cadena | 2 (M) |
| **#20 – HU.20:** Como Auxiliar de Farmacia, quiero buscar medicamentos por nombre o principio activo y ver su disponibilidad, lote y ubicación en tiempo real, para poder atender rápidamente las solicitudes de los clientes en el mostrador. | *(Duplicada de HU.17 - se integra en las mismas tareas)* | Laura Ortiz, Valentina Carrillo | - (M) |

---

## Incremento Esperado

Una vez finalizado el sprint, el Auxiliar de Farmacia podrá realizar búsquedas rápidas de medicamentos por nombre o principio activo, visualizando en tiempo real la disponibilidad, número de lote y ubicación exacta en el almacén, lo que agilizará significativamente la atención al cliente en el mostrador. El Encargado de Supervisión General contará con un panel para revisar, aprobar o rechazar pedidos de compra con validación presupuestal, mientras que el Jefe de Farmacia podrá generar y descargar reportes de productos agotados, pérdidas por vencimiento e historial de movimientos en formatos PDF/Excel, proporcionando herramientas clave para auditorías y decisiones de compra informadas. Esto completará el módulo de gestión de inventario y atención al cliente del sistema CareStock.
