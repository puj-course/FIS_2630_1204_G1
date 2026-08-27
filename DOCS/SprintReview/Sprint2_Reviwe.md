# 📋 Sprint Review — Sprint 2 (CareStock)

**Fecha de la Sesión:** 22 de agosto de 2026 
**Sprint Objetivo:** Definición funcional detallada de módulos core (inventario, alertas, despacho, reportes), diseño del Modelo Relacional y preparación de scripts DDL/DML en Oracle Database  
**Estado General del Sprint:** ÉXITO (100% del compromiso completado)

---

## 1. Revisión de Objetivos y Compromiso (Sprint Backlog vs. Done)

Durante la planificación nos comprometimos con **14 Puntos de Historia** distribuidos en 5 Hitos Unificados (HU.6 a HU.10). Todo el trabajo comprometido se completó al 100% y los entregables pasaron la verificación con el Product Owner.

| Hito / Tarea | Responsables | Est. | Estado Final | Criterio de Aceptación Cumplido |
| :--- | :--- | :---: | :---: | :--- |
| **#6** Documentación de Requerimientos Funcionales - Inventario y Alertas | Laura Ortiz, Valentina Carrillo | 4 pts (L) | **DONE** | Flujos detallados, reglas de negocio para entradas/lotes y sistema de alertas semafóricas (30/60 días) documentados y vinculados a las HU del Sprint 1. |
| **#7** Documentación de Requerimientos Funcionales - Despacho y Reportes | Laura Ortiz, Valentina Carrillo | 4 pts (L) | **DONE** | Especificación funcional del despacho bajo lógica FEFO (sugerencia de lote más próximo a vencer) y formatos de salida para reportes (PDF/Excel). |
| **#8** Definición del Modelo Relacional a partir del MER | Mateo Salazar | 2 pts (M) | **DONE** | Modelo relacional completo (tablas, atributos, PKs, FKs, tipos de datos y restricciones de integridad) derivado del MER del Sprint 1. |
| **#9** Investigación de Implementación en Oracle Database | Alejandro Rodríguez, Santiago Cadena | 2 pts (M) | **DONE** | Configuración de entorno Oracle en Docker/DataGrip, generación y versionamiento de scripts DDL (esquema completo) y DML (población inicial). |
| **#10** Seguimiento de Tareas y Coordinación del Sprint | Laura Ortiz, Santiago Cadena | 2 pts (M) | **DONE** | Facilitación de ceremonias Scrum, gestión de impedimentos en tiempo real y actualización del tablero Kanban en GitHub Projects. |

---

## 2. Demostración del Incremento (Lo que se presentó en vivo)

El equipo expuso los siguientes entregables tangibles ante el Product Owner (Mateo) y los interesados:

* **Demo de Base de Datos y Scripts DDL/DML (Alejandro & Santiago):**
  * Se ejecutó el script **DDL** completo en la instancia de **Oracle Database** corriendo sobre un contenedor Docker, creando exitosamente las tablas de usuarios, roles, productos, lotes, movimientos, alertas, configuraciones y proveedores con sus respectivas restricciones (PK, FK, NOT NULL, CHECK).
  * Se realizó la carga inicial de datos mediante el script **DML** (catálogo base de medicamentos, roles del sistema y usuarios de prueba) verificando la conexión vía JDBC a través de JetBrains DataGrip.

* **Revisión del Modelo Relacional (Mateo):**
  * Se presentó el Diagrama Relacional validado en contraste con los casos de uso del Sprint 1. Se demostró cómo la estructura de tablas soporta de manera nativa la trazabilidad por lotes, el control de vencimientos y las relaciones de cardinalidad necesarias para el inventario.

* **Walkthrough de Especificación Funcional (Laura & Valentina):**
  * Se expuso la documentación detallada de los requerimientos de **Inventario y Alertas** (definición precisa de los rangos de semaforización: rojo $< 30$ días, amarillo $30-60$ días, verde $> 60$ días).
  * Se presentó la especificación del algoritmo de **Despacho FEFO** (First Expired, First Out), mostrando cómo el sistema sugiere automáticamente al auxiliar el lote con vencimiento más cercano, junto con la parametrización de reportes de mermas y productos agotados.

---

## 3. Métricas de Rendimiento del Equipo

* **Velocidad Lograda:** 14 Puntos de Historia completados / 14 Puntos planificados.
* **Tasa de Cumplimiento de Historias:** 5 de 5 Hitos Unificados cerrados exitosamente.
* **Gobernanza del Proyecto:** Tablero de GitHub Projects actualizado al 100%, con trazabilidad completa entre issues, commits y scripts subidos al repositorio.

---

## 4. Feedback Recibido & Acuerdos para el Próximo Sprint

1. **Aprobación de la Capa de Datos:** El Product Owner aprobó el modelo relacional y la ejecución de los scripts DDL/DML. Se dio luz verde para tomar este esquema como la versión oficial sobre la que se construirá el backend.
2. **Priorización para el Sprint 3:** Con los requerimientos funcionales cerrados y el esquema DDL listo, el equipo acordó arrancar en el Sprint 3 con la codificación de la lógica de negocio backend (desarrollo de servicios de entrada/salida de lotes y lógica FEFO) y la creación de los triggers en PL/SQL para automatizar las alertas de stock mínimo.
3. **Flujo de Integración:** Mantener la política de ramas por Hito Unificado y Pull Requests revisados antes de unir cambios al esquema principal de la base de datos.

---

## 5. Estado del Producto al Cierre del Sprint 2

* **¿El Incremento cumple la Definición de Hecho (DoD)?** **SÍ.** Los documentos de requerimientos, la especificación de flujos, el diagrama del modelo relacional y los scripts DDL/DML se encuentran totalmente probados, aprobados y versionados en el repositorio oficial de GitHub.
* **Próximo Paso:** Iniciar la construcción e integración del backend y frontend sobre la base de datos Oracle ya desplegada.
