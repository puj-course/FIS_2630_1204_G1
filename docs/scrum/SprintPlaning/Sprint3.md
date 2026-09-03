**Iteración:** Sprint 3  
**Fecha de Cierre:** 29 de agosto de 2026  
---

## 1. Información General del Sprint
* **Sprint Goal:** Construir e implementar la base de datos en Oracle Database ejecutando los scripts DDL y DML para el esquema relacional de CareStock, diseñar el diagrama de clases del dominio del sistema y elaborar los diagramas de secuencia para los flujos operativos principales (despacho FEFO, ingreso de lotes y emisión de alertas), garantizando la integridad de la arquitectura y la coordinación efectiva del equipo.
* **Duración:** 1 semana (Sesión de planificación realizada el 24 de agosto de 2026)
* **Capacidad Compromiso:** 14 Puntos de Historia (5 Hitos Unificados: #11 a #15)
* **Integrantes del Equipo:**
  * Mateo Salazar Bogotá (Product Owner)
  * Laura Sofía Ortiz Gómez (Scrum Master / Diseñadora de Procesos)
  * Valentina Carrillo Peñuela (Diseñadora UI/UX / Modelado Dinámico)
  * Alejandro Rodríguez Molina (Desarrollador Backend / Base de Datos)
  * Johan Santiago Cadena Goyeneche (Ingeniero de Datos / Base de Datos)

---

## 2. Resultado de las Historias de Usuario / Hitos Unificados

| ID | Hito Unificado | Responsables | Est. (Pts) | Estado Final | Criterios de Aceptación Cumplidos |
|---|---|---|---|---|---|
| **#11** | **.11:** Construcción e Implementación Física de la BD en Oracle | Alejandro Rodríguez, Santiago Cadena | 4 (L) | **Done** | Ejecución exitosa de scripts DDL en Oracle, creación de tablas (usuarios, roles, productos, lotes, movimientos, alertas, proveedores), claves primarias/foráneas, secuencias, triggers y restricciones de integridad. |
| **#12** | **.12:** Elaboración del Diagrama de Clases del Sistema CareStock | Mateo Salazar | 2 (M) | **Done** | Diseño del diagrama de clases UML con entidades de dominio, atributos con visibilidad, tipos de datos, métodos clave (`crearLote()`, `despacharFEFO()`, `evaluarVencimientos()`) y multiplicidades validadas. |
| **#13** | **.13:** Modelado de Diagramas de Secuencia para Procesos Clave | Laura Ortiz, Valentina Carrillo | 4 (L) | **Done** | Elaboración de diagramas de secuencia UML para los flujos de Ingreso de Lote, Despacho FEFO y Evaluación/Notificación Semafórica de Alertas, incluyendo actores, objetos y documentación en la Wiki. |
| **#14** | **.14:** Población Inicial de Datos (DML) y Validaciones de Integridad | Santiago Cadena, Alejandro Rodríguez | 2 (M) | **Done** | Desarrollo de scripts DML con datos semilla (roles, catálogo de medicamentos, lotes de prueba y usuarios de prueba), con pruebas exitosas de restricción CHECK y FK en GitHub. |
| **#15** | **.15:** Seguimiento de Tareas y Coordinación del Sprint 3 | Laura Ortiz | 2 (M) | **Done** | Ejecución de Daily Standups, actualización en tiempo real del tablero de GitHub Projects, gestión de bloqueos técnicos y preparación para la Demo. |
