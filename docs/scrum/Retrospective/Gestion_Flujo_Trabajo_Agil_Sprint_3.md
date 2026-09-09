# Retrospectiva y Cierre de Sprint 3 - Proyecto CareStock

**Curso:** Fundamentos de Ingeniería de Software  
**Institución:** Pontificia Universidad Javeriana, Bogotá  
**Proyecto:** CareStock  
**Iteración:** Sprint 3  
**Fecha de Cierre:** 30 de agosto de 2026  

---

## 1. Información General del Sprint
* **Sprint Goal:** Construir e implementar la base de datos en Oracle Database ejecutando los scripts DDL y DML para el esquema relacional de CareStock, diseñar el diagrama de clases del dominio del sistema y elaborar los diagramas de secuencia para los flujos operativos principales.
* **Duración:** 1 semana (24 de agosto - 30 de agosto de 2026)
* **Capacidad Compromiso:** 14 Puntos de Historia (5 Hitos Unificados: HU.11 a HU.15)
* **Integrantes del Equipo:**
  * Mateo Salazar Bogotá (Product Owner)
  * Laura Sofía Ortiz Gómez (Scrum Master)
  * Valentina Carrillo Peñuela (Diseñadora UI/UX)
  * Alejandro Rodríguez Molina (Desarrollador Backend)
  * Johan Santiago Cadena Goyeneche (Ingeniero de Datos)

---

## 2. Resultado de las Historias de Usuario / Hitos Unificados

| ID | Hito Unificado | Responsables | Est. (Pts) | Estado Final | Criterios de Aceptación Cumplidos |
|---|---|---|---|---|---|
| **#11** | Construcción e Implementación Física de la BD en Oracle | Alejandro Rodríguez, Santiago Cadena | 4 (L) | **Done** | Ejecución exitosa de scripts DDL en Oracle DB; creación de tablas, PK/FK, secuencias y triggers. |
| **#12** | Elaboración del Diagrama de Clases del Sistema CareStock | Mateo Salazar Bogotá | 2 (M) | **Done** | Diseñado diagrama de clases en UML con visibilidad, atributos, métodos clave y multiplicidades. |
| **#13** | Modelado de Diagramas de Secuencia para Procesos Clave | Laura Ortiz, Valentina Carrillo | 4 (L) | **Done** | Diagramas de secuencia para Carga de Lotes, Despacho FEFO y Notificación Semafórica de Alertas. |
| **#14** | Población Inicial de Datos (DML) y Validaciones de Integridad | Santiago Cadena, Alejandro Rodríguez | 2 (M) | **Done** | Scripts DML con datos semilla (roles, usuarios de prueba, catálogo de medicamentos y lotes). |
| **#15** | Seguimiento de Tareas y Coordinación del Sprint 3 | Laura Ortiz | 2 (M) | **Done** | Gestión de Dailies, tablero en GitHub Projects actualizado y preparación de entregables para demo. |

* **Puntos de Historia Completados:** 14 / 14 (100% de cumplimiento)

---

## 3. Cierre del Sprint (Sprint Closure & Review)

### 3.1 Resumen de Entregables e Hitos Alcanzados
1. **Base de Datos Física Operativa:** Instancia de Oracle Database creada e implementada con la estructura física completa y datos semilla (DML) cargados sin errores de integridad.
2. **Diagrama de Clases de Dominio (UML):** Representación estática del software incluyendo entidades (`Producto`, `Lote`, `Movimiento`, `Alerta`, `Usuario`, `Rol`, `Proveedor`) y sus comportamientos principales (`despacharFEFO()`, `evaluarVencimientos()`).
3. **Diagramas de Secuencia (UML):** Modelado dinámico de las interacciones temporales para los tres procesos críticos del negocio (FEFO, Recepción y Alertas).
4. **Preparación para la Fase de Carga/Codificación:** Artefactos técnicos listos para dar paso al desarrollo de software y construcción de la capa de aplicación.

### 3.2 Métricas del Sprint
* **Velocidad del Sprint:** 14 Puntos de Historia.
* **Ratio de Completitud:** 100%.
* **Puntos Acumulados Proyecto:** 42 Puntos de Historia.

---

## 4. Retrospectiva del Sprint (Sprint Retrospective)

### 4.1 ¿Qué funcionó bien? (What went well?)
* **Coherencia Arquitectónica:** El diagrama de clases y los diagramas de secuencia coincidieron exactamente con la estructura de la base de datos física implementada en Oracle.
* **Calidad de los Scripts DML:** Los datos semilla permitieron realizar consultas complejas de prueba verificando el correcto funcionamiento de las restricciones `CHECK` y `FOREIGN KEY`.
* **Rendimiento Acelerado:** El equipo completó la suite completa de artefactos de diseño técnico UML a tiempo para el inicio del desarrollo.

### 4.2 ¿Qué se puede mejorar? (What could be improved?)
* **Manejo de Excepciones en Diagramas:** Incluir más caminos alternativos y flujos de error dentro de los diagramas de secuencia (p. ej., intento de despacho sin stock suficiente).
* **Documentación en Repositorio:** Mantener actualizada la Wiki del repositorio a la par con la adición de archivos .sql.

### 4.3 Acciones de Mejora Acordadas (Action Items para el Sprint 4)
1. **Entrar a Fase de Codificación/Desarrollo:** Iniciar la construcción de componentes de software frontend/backend apoyándose directamente en las clases y diagramas de secuencia definidos.
2. **Verificación Continuada de Scripts:** Mantener los archivos DDL y DML modularizados dentro de la estructura de carpetas del repositorio.
3. **Reforzamiento del Control de Calidad:** Preparar casos de prueba unitarios e integrales basados en los criterios de aceptación ya consolidados.
