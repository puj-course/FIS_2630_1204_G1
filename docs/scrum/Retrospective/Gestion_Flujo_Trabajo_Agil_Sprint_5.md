# Estado de Avance del Sprint 5 - Proyecto CareStock

**Curso:** Fundamentos de Ingeniería de Software
**Institución:** Pontificia Universidad Javeriana, Bogotá
**Proyecto:** CareStock
**Iteración:** Sprint 5
**Corte de este reporte:** 09 de septiembre de 2026
**Fecha de Cierre real del sprint:** 

---

## 1. Información General del Sprint
* **Sprint Goal:** Cerrar HU.22 (arrastrada del Sprint 4) y avanzar en HU.23, HU.24 y HU.25 — ajuste manual de inventario, catálogo de medicamentos y gestión de usuarios con roles — junto con la infraestructura de base de datos (migraciones e índices) que las soporta.
* **Inicio del Sprint:** 08/09/2026
* **Fecha de Cierre:** Pendiente de definir por el equipo
* **Integrantes del Equipo:**
  * Mateo Salazar Bogotá (Product Owner)
  * Laura Sofía Ortiz Gómez (Scrum Master)
  * Valentina Carrillo Peñuela (Diseñadora UI/UX)
  * Alejandro Rodríguez Molina (Desarrollador Backend)
  * Johan Santiago Cadena Goyeneche (Ingeniero de Datos)

---

## 2. Resultado de las Historias de Usuario / Hitos

| ID | Hito / Historia | Responsable(s) asignado(s) en GitHub | Subtareas completadas | Estado Final |
|---|---|---|---|---|
| **#103** | HU.22: Registro de entrada de nuevos lotes (arrastrada del Sprint 4) | Alejandro Rodríguez | 5 / 5 | **Casi Done** — subtareas cerradas, falta cerrar formalmente el issue padre |
| **#104** | HU.23: Ajuste manual de inventario por merma o rotura | Sin responsable asignado | 5 / 5 (subtareas ya completas) | **Casi Done** — falta asignar responsable y cerrar |
| **#116** | HU.24: Administración y gestión de medicamentos en el catálogo | Alejandro Rodríguez | 6 / 6 | **Casi Done** — falta cerrar el issue padre |
| **#132** | HU.25: Registro y consulta de usuarios con rol asignado | Johan Santiago Cadena | 1 / 4 | **En progreso** — es la historia con más trabajo pendiente |
| **#133** | Script de migración DDL para tabla usuarios | Johan Santiago Cadena | — (tarea única) | **En progreso** |
| **#137** | Script de índices de búsqueda para USUARIOS | Alejandro Rodríguez | — (tarea única) | **En progreso** |
| **#138** | Acompañamiento y verificación de la HU.25 | Laura Ortiz | — | **En progreso** (rol de seguimiento del SM) |

* **Puntos de Historia:** pendiente asignación de Pts por el PO para cálculo de velocity.
* **Dato clave:** 3 de 4 historias de usuario (HU.22, HU.23, HU.24) ya tienen todas sus subtareas cerradas — falta cerrar el issue padre. HU.25 continúa en desarrollo activo.

---

## 3. Cierre del Sprint (Sprint Closure & Review)

### 3.1 Resumen de Entregables e Hitos Alcanzados
1. Ajuste manual de inventario (HU.23), catálogo de medicamentos (HU.24) y registro de lotes (HU.22): subtareas técnicas completadas, pendiente cierre formal.
2. Migración DDL e índices de búsqueda para la tabla USUARIOS, como base técnica para HU.25.
3. HU.25 (gestión de usuarios y roles) en desarrollo activo.

### 3.2 Métricas del Sprint
* **Historias con subtareas 100% completas:** 3 de 4 (HU.22, HU.23, HU.24).
* **Historias en desarrollo activo:** 1 (HU.25).
* **Puntos Acumulados Proyecto:** 53 Puntos de Historia (hasta Sprint 4).

---

## 4. Retrospectiva del Sprint

### 4.1 ¿Qué está funcionando bien?
* **Desglose en subissues:** Las historias con subtareas bien divididas (HU.22, HU.23, HU.24) llegaron rápido al 100% de sus tareas técnicas, facilitando el avance.
* **Base técnica lista antes de la funcionalidad:** Los scripts de migración e índices (#133, #137) se hicieron en paralelo a HU.25, sin bloquear su desarrollo.

### 4.2 ¿Qué hay que mejorar?
* **Asignación en GitHub:** HU.23 no tiene responsable asignado en GitHub aunque sus subtareas están completas.
* **Carga de trabajo concentrada:** HU.25 concentra el trabajo pendiente y depende de un único responsable (Johan), lo que puede generar un cuello de botella.
* **Estimaciones:** Faltan los puntos de historia (Pts) asignados en el backlog para poder calcular la velocidad del sprint.
* **Asignación de roles:** Valentina (UI/UX) no aparece asignada a historias en este sprint en GitHub.

### 4.3 Acciones de Mejora Acordadas
1. Asignar formalmente un responsable a HU.23 (#104) en GitHub.
2. Cerrar los issues padre de HU.22, HU.23 y HU.24.
3. Agregar puntos de historia (Pts) a los issues del sprint para medir velocity.
4. Crear y asignar a Valentina Carrillo subissues de diseño dentro de HU.25 (mockup de pantallas de gestión de usuarios/roles).
5. Apoyar en el desarrollo de HU.25 para asegurar el cierre a tiempo.