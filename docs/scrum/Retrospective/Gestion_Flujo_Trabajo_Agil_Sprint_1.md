# Retrospectiva y Cierre de Sprint 1 - Proyecto CareStock

**Curso:** Fundamentos de Ingeniería de Software  
**Institución:** Pontificia Universidad Javeriana, Bogotá  
**Proyecto:** CareStock  
**Iteración:** Sprint 1  
**Fecha de Cierre:** 16 de agosto de 2026  
---

## 1. Información General del Sprint
* **Sprint Goal:** Sentar las bases conceptuales, técnicas y visuales del sistema CareStock, delimitando el alcance para farmacias independientes e IPS pequeñas, evaluando la arquitectura de datos y definiendo la identidad visual y los casos de uso principales.
* **Duración:** 1 semana
* **Capacidad Compromiso:** 14 Puntos de Historia (5 Hitos Unificados: #.1 a #.5)
* **Integrantes del Equipo:**
  * Mateo Salazar Bogotá (Product Owner)
  * Laura Sofía Ortiz Gómez (Scrum Master)
  * Valentina Carrillo Peñuela (Diseñadora UI/UX)
  * Alejandro Rodríguez Molina (Desarrollador Backend)
  * Johan Santiago Cadena Goyeneche (Ingeniero de Datos)

---







## 2. Resultado de las Historias de Usuario / Hitos Unificados

| ID | Hito Unificado  | Responsables | Est. (Pts) | Estado Final | Criterios de Aceptación Cumplidos |
|---|---|---|---|---|---|
| **#1** | **#.1:** Casos de Uso y Asignación de Roles | Laura Ortiz | 4 (L) | **Done** | Definición detallada de 13 casos de uso (CU-01 a CU-13) para Jefe de Farmacia, Auxiliar y Administrador. |
| **#2** | **#.2:** Inicio de Planteamiento de Interfaz y Paleta de Colores | Valentina Carrillo | 2 (M) | **Done** | Selección de paleta "Pastel Farmacéutico" (regla 60-30-10) y diseño conceptual del Dashboard. |
| **#3** | **#.3:** Evaluación Arquitectónica de Datos | Alejandro Rodríguez, Santiago Cadena | 2 (M) | **Done** | Matriz comparativa entre BD propia y API externa; decisión fundamentada por BD propia para cumplir Res. 1403/2007. |
| **#4** | **#.4:** Investigación de Integración e Inspección de Código | Alejandro Rodríguez, Santiago Cadena | 2 (M) | **Done** | Definición del stack técnico: Oracle Database + PL/SQL + JetBrains DataGrip + Docker. |
| **#5** | **#.5:** Análisis de Requerimientos y Delimitación de Alcance | Mateo Salazar Bogotá | 4 (L) | **Done** | Delimitación del alcance a droguerías/IPS pequeñas, roles de mostrador (Jefe/Auxiliar) y matriz de funcionalidades. |

* **Puntos de Historia Completados:** 14 / 14 (100% de cumplimiento)

---

## 3. Cierre del Sprint (Sprint Closure & Review)

### 3.1 Resumen de Entregables e Hitos Alcanzados
1. **Documentación de Alcance y Casos de Uso:** Formulación explícita de los flujos de interacción, precondiciones y postcondiciones para los roles de *Jefe de Farmacia*, *Auxiliar de Farmacia* y *Administrador*.
2. **Sistema de Diseño y Mockup Inicial:** Selección de la paleta oficial pastel (#FDFBF7, #A7DBD8, #B7A6E0, #F6CEBE, #F0A8A8) para reducir la fatiga visual y jerarquizar alertas semafóricas.
3. **Decisión Arquitectónica Formal:** Selección de Oracle Database propia sobre contenedores Docker para garantizar soberanía de datos, auditoría estricta y trazabilidad de lotes FEFO.
4. **Gobierno del Proyecto en GitHub Projects:** Configuración del tablero Kanban (`FIS_2630_1204_G1`), hitos (*Milestone v1.0.0*), etiquetado (*Labels*) y trazabilidad visual (*Burn-up*, *Priority Breakdown* y *Status Overview*).

### 3.2 Métricas del Sprint
* **Velocidad del Sprint:** 14 Puntos de Historia.
* **Ratio de Completitud:** 100%.
* **Desviación de Tiempo:** 0 días (Entregas finalizadas entre el 13 y el 16 de agosto de 2026).

---

## 4. Retrospectiva del Sprint (Sprint Retrospective)

### 4.1 ¿Qué funcionó bien? (What went well?)
* **Especialización por Roles:** La clara separación de responsabilidades (PO, SM, UI/UX, Backend, Datos) permitió avanzar en paralelo en requerimientos, diseño y arquitectura sin pisarse tareas.
* **Adopción de GitHub Projects:** La vinculación entre Issues, Pull Requests y el tablero Kanban facilitated un seguimiento transparente del avance diario.
* **Justificación de Decisiones Técnicas:** Evaluar la normativa colombiana (Resolución 1403 de 2007) antes de seleccionar el motor de BD evitó reestructuraciones futuras.

### 4.2 ¿Qué se puede mejorar? (What could be improved?)
* **Curva de Aprendizaje en Git/GitHub:** Disparidad inicial en el nivel de manejo de políticas de commits y gestión de ramas dentro del equipo.
* **Sincronización Inicial:** Necesidad de reuniones más cortas y concisas para evitar que las discusiones de diseño retrasen las definiciones de arquitectura.




### 4.3 Acciones de Mejora Acordadas (Action Items para el Sprint 2)
1. **Estandarización de Commits:** Adoptar una convención estándar de mensajes de commit vinculados explícitamente al número del Issue (`#ID`).
2. **Sesiones de Alineación Rápida:** Implementar “Daily” semanales de máximo 15 minutos para destrabar bloqueos en tiempo real.
3. **Mayor Nivel de Detalle Funcional:** Pasar de los casos de uso macro a la documentación detallada de reglas de negocio para los módulos de inventario, alertas y despacho.
4. **Solicitar acceso al repositorio:** Por parte de los integrantes que hacen falta para que puedan trabajar en el proyecto.
