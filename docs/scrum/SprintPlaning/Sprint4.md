# Sprint Planning – Sprint 4

**Fecha:** 31/08/2026  
**Duración del Sprint:** 6 días (31/08/2026 a 05/09/2026)  

---

## Sprint Goal

Completar la Historia de Usuario #22 relacionada con el registro de entrada de nuevos lotes y la sincronización del inventario físico-digital, junto con las tareas de infraestructura y diseño necesarias para madurar el prototipo de interfaz, definir el modelo de datos de ubicaciones y lotes, establecer reglas de negocio con el equipo farmacéutico y refinar el modelo entidad-relación y las clases del dominio, sentando las bases técnicas para la gestión completa de inventario en CareStock.

---

## Sprint Backlog

Las historias de usuario del sprint llevaron a la definición de las siguientes tareas en el sprint backlog:

| Historia de Usuario | Tareas Asociadas | Responsable(s) | Estimación (Pts) |
|----------------------|------------------|----------------|------------------|
| **#22 – HU.22:** Registro de entrada de nuevos lotes e inventario físico-digital: Como Auxiliar de Farmacia, quiero registrar la entrada de nuevos lotes ingresando producto, cantidad, número de lote, fecha de vencimiento y ubicación física, para mantener el stock físico del almacén perfectamente sincronizado con el inventario digital del sistema. | - Diseñar interfaz de registro de entrada de lotes con campos obligatorios (producto, cantidad, lote, vencimiento, ubicación) | Laura Ortiz, Valentina Carrillo | 5 (L) |
| | - Implementar validación de caducidad (bloquear si fecha ≤ fecha actual) | Laura Ortiz, Valentina Carrillo | |
| | - Desarrollar lógica de actualización de stock global y asignación a ubicación física | Alejandro Rodríguez | |
| | - Implementar registro de auditoría (fecha, hora, usuario) automático | Alejandro Rodríguez, Santiago Cadena | |
| **#70 – Maduración del Prototipo de Interfaz y Experiencia de Usuario** | - Refinar prototipo de alta fidelidad para registro de lotes | Valentina Carrillo | 3 (M) |
| | - Validar flujo de usuario con equipo farmacéutico | Laura Ortiz, Valentina Carrillo | |
| | - Incorporar feedback de pruebas de usabilidad | Valentina Carrillo | |
| | - Documentar guía de estilos y componentes UI | Valentina Carrillo | |
| **#105 – Diseñar e implementar el modelo de datos para Ubicaciones, Lotes, Stock y Auditoría** | - Definir estructura de tablas: Ubicaciones (id, nombre, descripción, tipo) | Santiago Cadena, Alejandro Rodríguez | 3 (M) |
| | - Definir estructura de tablas: Lotes (id, producto_id, numero_lote, cantidad, fecha_vencimiento, ubicacion_id) | Santiago Cadena, Alejandro Rodríguez | |
| | - Definir estructura de tablas: Stock (id, producto_id, cantidad_total) | Santiago Cadena, Alejandro Rodríguez | |
| | - Definir estructura de tablas: Auditoría (id, tabla, operacion, usuario_id, fecha_hora, datos_previos, datos_nuevos) | Santiago Cadena, Alejandro Rodríguez | |
| | - Generar scripts DDL y migraciones para Oracle | Santiago Cadena, Alejandro Rodríguez | |
| **#106 – Definir reglas de negocio y parametrización operativa con el equipo farmacéutico** | - Sesiones de trabajo con el equipo farmacéutico para validar reglas de negocio | Laura Ortiz, Mateo Salazar | 2 (M) |
| | - Documentar políticas de inventario (stock mínimo, máximo, puntos de reorden) | Laura Ortiz | |
| | - Definir parametrización de alertas y notificaciones | Mateo Salazar, Laura Ortiz | |
| | - Establecer criterios de priorización para despacho (FEFO, ubicación) | Laura Ortiz | |
| **#109 – Creación del diagrama entidad-relación** | - Elaborar diagrama ER completo con todas las entidades del dominio | Mateo Salazar | 2 (M) |
| | - Definir relaciones y cardinalidades entre entidades | Mateo Salazar | |
| | - Validar diagrama con el equipo de desarrollo y base de datos | Mateo Salazar, Santiago Cadena | |
| | - Actualizar documentación técnica con el diagrama ER | Mateo Salazar | |
| **#119 – Modificar las clases Medicamento y Producto y asignarlas a su carpeta correspondiente** | - Revisar y refactorizar clases Medicamento y Producto | Alejandro Rodríguez | 1 (P) |
| | - Definir herencia o composición entre Medicamento y Producto según necesidad | Alejandro Rodríguez | |
| | - Asignar clases a carpeta correspondiente (ej. domain/models) | Alejandro Rodríguez | |
| | - Actualizar diagrama de clases con cambios realizados | Alejandro Rodríguez, Mateo Salazar | |

---

## Incremento Esperado

Una vez finalizado el sprint, el Auxiliar de Farmacia podrá registrar la entrada de nuevos lotes de medicamentos ingresando producto, cantidad, número de lote, fecha de vencimiento y ubicación física, con validaciones automáticas que bloquean lotes vencidos y actualizan el stock global en tiempo real, manteniendo el inventario físico perfectamente sincronizado con el sistema digital. Además, el equipo contará con un prototipo maduro de interfaz validado, un modelo de datos completo para ubicaciones, lotes, stock y auditoría implementado en Oracle, reglas de negocio documentadas y validadas con el equipo farmacéutico, el diagrama entidad-relación actualizado y las clases del dominio refactorizadas y organizadas correctamente. Esto constituirá la base sólida para la gestión integral de inventario y trazabilidad en CareStock.