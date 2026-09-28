# HU.41 - Criterios para identificar cuándo una tarea está en riesgo o 
bloqueada

**Proyecto:** CareStock
**Sprint:** 6
**Historia de Usuario:** HU.41
**Tarea:** #243
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 17 de septiembre de 2026

---

## 1. Objetivo

Establecer criterios claros y objetivos que permitan al Scrum Master 
identificar oportunamente cuándo una tarea del Sprint 6 está en riesgo de 
no completarse o se encuentra bloqueada, para tomar acciones correctivas a 
tiempo.

---

## 2. Criterios para identificar una tarea EN RIESGO

Una tarea está en riesgo cuando aún no está bloqueada, pero existen 
señales de que podría no completarse dentro del tiempo del sprint.

| # | Criterio | Descripción |
|---|----------|-------------|
| 1 | Avance menor al esperado | La tarea lleva más de 2 días sin cambios 
en su estado (sigue en "In Progress" sin commits ni PR). |
| 2 | Dependencia sin resolver | La tarea depende de otra que aún no está 
terminada y esa dependencia no tiene fecha estimada de cierre. |
| 3 | Falta de claridad | El responsable no tiene claro qué debe hacer, 
cómo hacerlo o cuál es el criterio de aceptación. |
| 4 | Responsable sobrecargado | La persona asignada tiene 3 o más tareas 
activas simultáneamente. |
| 5 | Comunicación interrumpida | El responsable no ha reportado avance en 
las Daily Standups por 2 días seguidos. |
| 6 | Riesgo técnico identificado | Se detectó un problema técnico que 
podría retrasar la tarea (ej. incompatibilidad, falta de herramienta, 
error de integración). |
| 7 | Tiempo restante insuficiente | Quedan menos de 2 días de sprint y la 
tarea está por debajo del 50% de avance. |

---

## 3. Criterios para identificar una tarea BLOQUEADA

Una tarea está bloqueada cuando no puede avanzar por una causa externa o 
interna que impide su desarrollo.

| # | Criterio | Descripción |
|---|----------|-------------|
| 1 | Dependencia crítica sin resolver | La tarea depende de otra que no 
se ha completado y no hay forma de avanzar sin ella. |
| 2 | Falta de acceso o permisos | El responsable no puede acceder a la 
base de datos, repositorio, herramienta o entorno necesario. |
| 3 | Error técnico no resuelto | Existe un error que impide continuar 
(ej. fallo de conexión, error de compilación, conflicto de ramas). |
| 4 | Falta de información o decisión | Se necesita una decisión del 
Product Owner o del equipo que aún no se ha tomado. |
| 5 | Espera de revisión externa | La tarea está pendiente de aprobación o 
revisión por parte de alguien fuera del equipo. |
| 6 | Recurso no disponible | La persona responsable no está disponible 
(ausencia, enfermedad, otra prioridad urgente). |
| 7 | Conflicto de prioridades | El responsable fue reasignado a otra 
tarea de mayor prioridad y no puede continuar. |

---

## 4. Acciones según el estado

| Estado | Acción del Scrum Master |
|--------|-------------------------|
| En riesgo | Notificar al responsable, revisar el avance en la Daily, 
ofrecer apoyo y replanificar si es necesario. |
| Bloqueada | Registrar el bloqueo en GitHub Projects, escalarlo al equipo 
o al Product Owner, y buscar una solución inmediata. |

---

## 5. Registro de bloqueos y riesgos

Cada vez que se identifique un riesgo o bloqueo, se debe registrar en el 
tablero de GitHub Projects con:

- Descripción del riesgo o bloqueo
- Tarea afectada
- Responsable
- Fecha de identificación
- Acción tomada
- Estado actual (En riesgo / Bloqueada / Resuelta)

---

## 6. Ejemplo de aplicación en el Sprint 6

| Tarea | Estado | Criterio aplicado | Acción tomada |
|-------|--------|-------------------|---------------|
| Crear script DDL para tabla USUARIOS | En riesgo | Lleva 2 días sin 
commits | Se habló con Santiago para revisar avance |
| Conectar frontend con backend | Bloqueada | Falta endpoint del backend | 
Se escaló a Alejandro para priorizar el endpoint |

---

## 7. Referencias

- HU.41 - Criterios de seguimiento del Sprint 6
- Tarea #243 - Definir criterios de riesgo y bloqueo
- GitHub Projects del repositorio FIS_2630_1204_G1

---

**Fin del documento.**
