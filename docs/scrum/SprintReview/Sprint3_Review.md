# 📋 Sprint Review — Sprint 3 (CareStock)

* **Fecha de la Sesión:** 30 de agosto de 2026
* **Sprint Objetivo:** Construir e implementar la base de datos en Oracle Database ejecutando los scripts DDL y DML para el esquema relacional de CareStock, diseñar el diagrama de clases del dominio del sistema y elaborar los diagramas de secuencia para los flujos operativos principales (despacho FEFO, ingreso de lotes y emisión de alertas), garantizando la integridad de la arquitectura y la coordinación efectiva del equipo.
* **Estado General del Sprint:** COMPLETADO (100% de las tareas / 100% de los puntos completados)

---

## 🎯 Revisión de Objetivos y Compromiso (Sprint Backlog vs. Done)

Durante la planificación nos comprometimos con **14 Puntos de Historia** distribuidos en **5 Hitos Unificados (HU.11 a HU.15)**. Se completaron exitosamente la totalidad de los 5 Hitos Unificados (14 de 14 Puntos de Historia).

| Hito / Tarea | Responsables | Est. | Estado Final | Criterio de Aceptación Cumplido |
| :--- | :--- | :---: | :---: | :--- |
| **#11** Construcción e Implementación Física de la BD en Oracle | Alejandro Rodríguez, Santiago Cadena | 4 pts (L) | **DONE** | Ejecución exitosa de scripts DDL en Oracle DB. Creación de tablas principales (`USUARIOS`, `ROLES`, `PRODUCTOS`, `LOTES`, `MOVIMIENTOS`, `ALERTAS`, `PROVEEDORES`), claves PK/FK, secuencias/triggers y restricciones de integridad. |
| **#12** Elaboración del Diagrama de Clases del Sistema CareStock | Mateo Salazar | 2 pts (M) | **DONE** | Diagrama de Clases UML elaborado representando las entidades principales, visibilidad de atributos, tipos de datos, multiplicidades y métodos clave (`crearLote()`, `despacharFEFO()`, `evaluarVencimientos()`). |
| **#13** Modelado de Diagramas de Secuencia para Procesos Clave | Laura Ortiz, Valentina Carrillo | 4 pts (L) | **DONE** | Formalización completa de la interacción temporal y de mensajes entre vistas, controladores y base de datos para los flujos principales: *Ingreso de Lote*, *Despacho FEFO* y *Notificación Semafórica de Alertas*. |
| **#14** Población Inicial de Datos (DML) y Validaciones de Integridad | Santiago Cadena, Alejandro Rodríguez | 2 pts (M) | **DONE** | Scripts DML desarrollados y versionados con datos maestro (roles, usuarios de prueba, catálogo inicial de medicamentos y lotes de prueba) con validación de restricciones `CHECK` y FKs. |
| **#15** Seguimiento de Tareas y Coordinación del Sprint 3 | Laura Ortiz | 2 pts (M) | **DONE** | Facilitación de reuniones Daily Standup, gestión activa de bloqueos técnicos y actualización en tiempo real del tablero en GitHub Projects. |

---

## 🖥️ Demostración del Incremento (Lo que se presentó en vivo)

El equipo expuso los siguientes entregables tangibles ante el Product Owner (Mateo) y los interesados:

1. **Demo de Construcción e Implementación de la BD en Oracle (#11 & #14 - Alejandro & Santiago):** Se ejecutaron los scripts DDL y DML en la instancia de Oracle Database. Se comprobó la creación correcta de la estructura relacional completa, así como la correcta inserción y consulta de datos semilla (roles, usuarios de prueba y catálogo básico de medicamentos) validando las restricciones de integridad y secuencias.
2. **Presentación del Diagrama de Clases del Dominio (#12 - Mateo):** Se expuso el modelo estático en UML orientado a objetos, detallando las entidades del sistema, sus atributos con tipos y visibilidad, multiplicidades y los métodos del dominio encargados de la lógica del inventario farmacéutico.
3. **Presentación de los Diagramas de Secuencia (#13 - Laura & Valentina):** Se expuso el modelado dinámico UML para los flujos operativos fundamentales (*Ingreso de Lote*, *Despacho FEFO* y *Emisión de Alertas Semafóricas*), definiendo con precisión el intercambio de mensajes entre la interfaz de usuario, los controladores de negocio y la capa de persistencia en Oracle Database.

---

## 📊 Métricas de Rendimiento del Equipo

* **Velocidad Lograda:** 14 Puntos de Historia completados / 14 Puntos planificados (**100% de efectividad en puntos**).
* **Tasa de Cumplimiento de Historias:** 5 de 5 Hitos Unificados cerrados exitosamente (**100% de cumplimiento en historias**).
* **Gobernanza del Proyecto:** Tablero de GitHub Projects actualizado al 100% con la trazabilidad de los scripts SQL, el diagrama de clases y los diagramas de secuencia aprobados.

---

## 💬 Feedback Recibido & Acuerdos para el Próximo Sprint

* **Aprobación de la Arquitectura Completa:** El Product Owner aprobó la implementación física de la BD en Oracle Database, el Diagrama de Clases y los Diagramas de Secuencia como la base arquitectónica oficial del proyecto.
* **Priorización para el Sprint 4:** Dar inicio directo al desarrollo y codificación de las interfaces del sistema (frontend) y la lógica de servicios/controladores en el backend, tomando los modelos UML y la base de datos construidos en el Sprint 3 como especificación técnica directa.
* **Continuidad de Gobernanza:** Mantener la dinámica de trabajo y sincronización en GitHub Projects que permitió el cierre al 100% de los compromisos del sprint.

---

## 🚀 Estado del Producto al Cierre del Sprint 3

* **¿El Incremento cumple la Definición de Hecho (DoD)?** **SÍ (100%)**. La base de datos física en Oracle DB, los scripts de datos iniciales, el diagrama de clases y los diagramas de secuencia están formalizados, validados y aprobados bajo los criterios de la DoD.
* **Próximo Paso:** Iniciar el Sprint 4 enfocado en el desarrollo de la lógica de negocio backend, endpoints/servicios y las vistas GUI para los módulos de inventario y despacho de CareStock.
