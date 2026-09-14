# Retrospectiva y Cierre de Sprint 4 - Proyecto CareStock

**Curso:** Fundamentos de Ingeniería de Software
**Institución:** Pontificia Universidad Javeriana, Bogotá
**Proyecto:** CareStock
**Iteración:** Sprint 4
**Fecha de Cierre:** 05 de septiembre de 2026

---

## 1. Información General del Sprint
* **Sprint Goal:** Completar la Historia de Usuario HU.22 (registro de entrada de nuevos lotes y sincronización del inventario físico-digital), junto con las tareas de infraestructura y diseño necesarias para madurar el prototipo de interfaz, definir el modelo de datos de ubicaciones y lotes, establecer reglas de negocio con el equipo farmacéutico y refinar el modelo entidad-relación y las clases del dominio.
* **Duración:** 6 días (31 de agosto - 05 de septiembre de 2026)
* **Capacidad Compromiso:** 16 Puntos de Historia (6 Hitos/Tareas: #22, #70, #105, #106, #109, #119)
* **Integrantes del Equipo:**
  * Mateo Salazar Bogotá (Product Owner)
  * Laura Sofía Ortiz Gómez (Scrum Master)
  * Valentina Carrillo Peñuela (Diseñadora UI/UX)
  * Alejandro Rodríguez Molina (Desarrollador Backend)
  * Johan Santiago Cadena Goyeneche (Ingeniero de Datos)

---

## 2. Resultado de las Historias de Usuario / Hitos

| ID | Hito / Historia | Responsables | Est. (Pts) | Estado Final | Criterios de Aceptación Cumplidos |
|---|---|---|---|---|---|
| **#22** | **HU.22:** Registro de entrada de nuevos lotes e inventario físico-digital | Laura Ortiz, Valentina Carrillo, Alejandro Rodríguez | 5 (L) | **No completado (trasladado a Sprint 5)** | Interfaz de registro diseñada; validación de caducidad y actualización de stock pendientes de cierre. |
| **#70** | Maduración del Prototipo de Interfaz y Experiencia de Usuario | Valentina Carrillo | 3 (M) | **Done** | Prototipo de alta fidelidad refinado, validado con el equipo y guía de estilos documentada. |
| **#105** | Diseño e implementación del modelo de datos para Ubicaciones, Lotes, Stock y Auditoría | Santiago Cadena, Alejandro Rodríguez | 3 (M) | **Done** | Tablas Ubicaciones, Lotes, Stock y Auditoría definidas; scripts DDL generados para Oracle. |
| **#106** | Reglas de negocio y parametrización operativa | Laura Ortiz, Mateo Salazar | 2 (M) | **Done** | Políticas de inventario, alertas y criterios de priorización FEFO documentados. |
| **#109** | Diagrama entidad-relación | Mateo Salazar | 2 (M) | **Done** | Diagrama ER completo con relaciones y cardinalidades, validado con el equipo. |
| **#119** | Refactor de clases Medicamento y Producto | Alejandro Rodríguez | 1 (P) | **Done** | Clases revisadas, reorganizadas en su carpeta correspondiente y diagrama de clases actualizado. |

* **Puntos de Historia Completados:** 11 / 16 (69% de cumplimiento)
* **Hitos Cerrados:** 5 / 6

---

## 3. Cierre del Sprint (Sprint Closure & Review)

### 3.1 Resumen de Entregables e Hitos Alcanzados
1. **Prototipo de Interfaz Maduro:** Versión de alta fidelidad validada por el equipo, con guía de estilos documentada.
2. **Modelo de Datos Completo:** Tablas de Ubicaciones, Lotes, Stock y Auditoría diseñadas e implementadas en Oracle vía scripts DDL.
3. **Reglas de Negocio Formalizadas:** Políticas de stock mínimo/máximo, puntos de reorden y priorización FEFO acordadas con el equipo farmacéutico.
4. **Diagrama ER y Clases Actualizados:** Base técnica consolidada para el desarrollo de HU.22 y las siguientes historias del módulo de inventario.
5. **Pendiente:** HU.22 no se cerró en su totalidad dentro del sprint; el registro de entrada de lotes queda como prioridad para el inicio del Sprint 5.

### 3.2 Métricas del Sprint
* **Velocidad del Sprint:** 11 Puntos de Historia completados / 16 planificados.
* **Ratio de Completitud:** 69% (5 de 6 hitos).
* **Puntos Acumulados Proyecto:** 53 Puntos de Historia (42 previos + 11 de este sprint).

---

## 4. Retrospectiva del Sprint (Sprint Retrospective)

### 4.1 ¿Qué funcionó bien? (What went well?)
* **Avance sólido en la base técnica:** El modelo de datos, las reglas de negocio y el diagrama ER se cerraron sin retrasos, dejando una base clara para construir HU.22.
* **Maduración real del prototipo:** El trabajo de UI/UX llegó a una versión validada con el equipo.

### 4.2 ¿Qué se puede mejorar? (What could be improved?)
* **Subestimación de HU.22:** Se estimaron 5 puntos para una historia que dependía de varias piezas (interfaz, validaciones, actualización de stock y auditoría) que requerían más tiempo del esperado.
* **Dependencias entre frontend y backend:** El registro de entrada de lotes necesitaba que el modelo de datos (#105) y las reglas de negocio (#106) estuvieran cerrados antes de poder completarse.

### 4.3 Acciones de Mejora Acordadas (Action Items para el Sprint 5)
1. **Cerrar HU.22 como prioridad #1** del Sprint 5, ahora que sus dependencias técnicas están resueltas.
2. **Dividir historias grandes en subissues desde el planning** para detectar mejor si una estimación de puntos es realista.
3. **Mantener el ritmo de documentación en `docs/scrum/`** actualizando Retrospective y Review el mismo día de cierre del sprint.