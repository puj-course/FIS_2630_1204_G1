📋 Sprint Review — Sprint 3 (CareStock)

Fecha de la Sesión: 30 de agosto de 2026 
Sprint Objetivo: Construir e implementar la base de datos en Oracle Database ejecutando los scripts DDL y DML para el esquema relacional de CareStock, diseñar el diagrama de clases del dominio del sistema y elaborar los diagramas de secuencia para los flujos operativos principales (despacho FEFO, ingreso de lotes y emisión de alertas), garantizando la integridad de la arquitectura y la coordinación efectiva del equipo.
Estado General del Sprint: PARCIALMENTE COMPLETADO (80% de las tareas / 71.4% de los puntos completados)

1. Revisión de Objetivos y Compromiso (Sprint Backlog vs. Done)

Durante la planificación nos comprometimos con 14 Puntos de Historia distribuidos en 5 Hitos Unificados (HU.11 a HU.15). Se completaron 4 de los 5 Hitos Unificados (10 de 14 Puntos de Historia). La tarea #13 no fue completada en su totalidad y fue diferida para el siguiente sprint.

Hito / Tarea | Responsables | Est. | Estado Final | Criterio de Aceptación Cumplido
--- | --- | --- | --- | ---
#11 Construcción e Implementación Física de la BD en Oracle | Alejandro Rodríguez, Santiago Cadena | 4 pts (L) | DONE | Ejecución exitosa de scripts DDL en Oracle DB. Creación de tablas principales (USUARIOS, ROLES, PRODUCTOS, LOTES, MOVIMIENTOS, ALERTAS, PROVEEDORES), claves PK/FK, secuencias/triggers y restricciones de integridad.
#12 Elaboración del Diagrama de Clases del Sistema CareStock | Mateo Salazar | 2 pts (M) | DONE | Diagrama de Clases UML elaborado representando las entidades principales, visibilidad de atributos, tipos de datos, multiplicidades y métodos clave (crearLote(), despacharFEFO(), evaluarVencimientos()).
#13 Modelado de Diagramas de Secuencia para Procesos Clave | Laura Ortiz, Valentina Carrillo | 4 pts (L) | INCOMPLETO | Quedó pendiente la formalización de la interacción temporal completa entre vistas, controladores y base de datos para los flujos de Ingreso de Lote, Despacho FEFO y Notificación Semafórica.
#14 Población Inicial de Datos (DML) y Validaciones de Integridad | Santiago Cadena, Alejandro Rodríguez | 2 pts (M) | DONE | Scripts DML desarrollados y versionados con datos maestro (roles, usuarios de prueba, catálogo inicial de medicamentos y lotes de prueba) con validación de restricciones CHECK y FKs.
#15 Seguimiento de Tareas y Coordinación del Sprint 3 | Laura Ortiz | 2 pts (M) | DONE | Facilitación de reuniones Daily Standup, gestión activa de bloqueos técnicos y actualización en tiempo real del tablero en GitHub Projects.

2. Demostración del Incremento (Lo que se presentó en vivo)

El equipo expuso los siguientes entregables tangibles ante el Product Owner (Mateo) y los interesados:
Demo de Construcción e Implementación de la BD en Oracle (#11 & #14 - Alejandro & Santiago):
Se ejecutaron los scripts DDL y DML en la instancia de Oracle Database. Se comprobó la creación correcta de la estructura relacional completa, así como la correcta inserción y consulta de datos semilla (roles, usuarios de prueba y catálogo básico de medicamentos) validando las restricciones de integridad y secuencias.
Presentación del Diagrama de Clases del Dominio (#12 - Mateo):
Se expuso el modelo estático en UML orientado a objetos, detallando las entidades del sistema, sus atributos con tipos y visibilidad, multiplicidades y los métodos del dominio encargados de la lógica del inventario farmacéutico.

3. Métricas de Rendimiento del Equipo

Velocidad Lograda: 10 Puntos de Historia completados / 14 Puntos planificados (71.4% de efectividad en puntos).
Tasa de Cumplimiento de Historias: 4 de 5 Hitos Unificados cerrados exitosamente (80% de cumplimiento en historias).
Gobernanza del Proyecto: Tablero de GitHub Projects actualizado con la trazabilidad de los scripts SQL y el diagrama de clases aprobados.

4. Gestión del Hito Incompleto (#13) & Plan de Acción

Causa del Incumplimiento: La alta complejidad al mapear la interacción detallada entre la interfaz de usuario, los controladores y la capa de persistencia en Oracle, sumado a la dependencia del cierre del modelo de clases (#12), impidió finalizar la documentación formal de los 3 diagramas de secuencia previstos.
¿Qué va a pasar con la tarea #13?
1. Traspaso Inmediato al Sprint 4: El hito #13 (Modelado de Diagramas de Secuencia para Procesos Clave) se re-prioriza y transfiere directamente al Sprint Backlog del Sprint 4 como una tarea de alta prioridad al inicio del sprint.
2. Aprovechamiento del Modelo Existente: Con el Diagrama de Clases (#12) ya aprobado y finalizado en este Sprint 3, la construcción de los diagramas de secuencia para los flujos de Despacho FEFO, Ingreso de Lote y Alertas Semafóricas se completará de forma acelerada durante los primeros días del Sprint 4 antes de avanzar a la codificación plena del backend.

5. Feedback Recibido & Acuerdos para el Próximo Sprint

Aprobación de la Estructura de Datos: El Product Owner aprobó la implementación física de la BD en Oracle Database y el Diagrama de Clases como base para la arquitectura del sistema.
Ajuste de Prioridades para el Sprint 4: Finalizar de manera prioritaria la HU #13 en los primeros días del Sprint 4 e iniciar el desarrollo y codificación de los servicios del backend y las vistas de la aplicación.
Ajuste en la Estimación: Tener en cuenta las dependencias entre el diseño estático (clases) y dinámico (secuencias) para evitar cuellos de botella en futuros sprints.

6. Estado del Producto al Cierre del Sprint 3

¿El Incremento cumple la Definición de Hecho (DoD)? PARCIALMENTE. La base de datos física, sus datos semilla y el diagrama de clases cumplen con la DoD al 100%. Los diagramas de secuencia quedan pendientes para completarse en el Sprint 4.
Próximo Paso: Completar los diagramas de secuencia pendientes (#13) al inicio del Sprint 4 e iniciar la codificación de la lógica de negocio y la interfaz del sistema CareStock.
