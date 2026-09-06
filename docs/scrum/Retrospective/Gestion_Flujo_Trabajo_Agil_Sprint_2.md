# Retrospectiva y Cierre de Sprint 2 - Proyecto CareStock

**Curso:** Fundamentos de Ingeniería de Software  
**Institución:** Pontificia Universidad Javeriana, Bogotá  
**Proyecto:** CareStock  
**Iteración:** Sprint 2  
**Fecha de Cierre:** 23 de agosto de 2026  

---

## 1. Información General del Sprint
* **Sprint Goal:** Avanzar en la definición funcional y técnica detallada del sistema CareStock, documentando requerimientos para los módulos de inventario, alertas, despacho y reportes, definiendo el modelo relacional a partir del MER, e investigando la implementación en Oracle Database.
* **Duración:** 1 semana (18 de agosto - 23 de agosto de 2026)
* **Capacidad Compromiso:** 14 Puntos de Historia (5 Hitos Unificados: HU.6 a HU.10)
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
| **#6** | Documentación de Requerimientos Funcionales - Módulo de Inventario y Alertas | Laura Ortiz, Valentina Carrillo | 4 (L) | **Done** | Documentación completa de flujos de inventario, reglas de cálculo de vencimiento a 30/60 días y semaforización. |
| **#7** | Documentación de Requerimientos Funcionales - Módulo de Despacho y Reportes | Laura Ortiz, Valentina Carrillo | 4 (L) | **Done** | Especificación de lógica FEFO (First Expired, First Out), sugerencia automática de lotes y formatos PDF/Excel. |
| **#8** | Definición del Modelo Relacional a partir del MER | Mateo Salazar Bogotá | 2 (M) | **Done** | Traslado del MER a modelo relacional definitivo con PK, FK, restricciones (CHECK, UNIQUE) y script DDL inicial. |
| **#9** | Investigación de Implementación en Oracle Database | Alejandro Rodríguez, Santiago Cadena | 2 (M) | **Done** | Configuración de entorno Oracle en Docker, estrategia JDBC, scripts DDL/DML iniciales y seguridad. |
| **#10** | Seguimiento de Tareas y Coordinación del Sprint | Santiago Cadena, Laura Ortiz | 2 (M) | **Done** | Facilitación de ceremonias, actualización diaria del tablero en GitHub Projects y control de impedimentos. |

* **Puntos de Historia Completados:** 14 / 14 (100% de cumplimiento)

---

## 3. Cierre del Sprint (Sprint Closure & Review)

### 3.1 Resumen de Entregables e Hitos Alcanzados
1. **Especificación Funcional Completa:** Documentos detallados con las reglas de negocio para entradas de inventario, alertas de stock mínimo, algoritmo FEFO para despacho y reportes de pérdidas/mermas.
2. **Esquema Relacional Definitivo:** Diseño lógico de tablas (*USUARIOS, ROLES, PRODUCTOS, LOTES, MOVIMIENTOS, ALERTAS, PROVEEDORES*) con sus correspondientes llaves primarias, foráneas y restricciones de integridad.
3. **Entorno de BD Validado:** Contenedor de Oracle Database desplegado y probado mediante JetBrains DataGrip, con scripts DDL iniciales versionados en GitHub.
4. **Trazabilidad y Gestión:** Mantenimiento de la métrica de avance sin desviaciones en el tablero de trabajo.

### 3.2 Métricas del Sprint
* **Velocidad del Sprint:** 14 Puntos de Historia.
* **Ratio de Completitud:** 100%.
* **Puntos Acumulados Proyecto:** 28 Puntos de Historia.

---

## 4. Retrospectiva del Sprint (Sprint Retrospective)

### 4.1 ¿Qué funcionó bien? (What went well?)
* **Trabajo en Parejas (Pair Work):** La colaboración en parejas para la documentación de requerimientos (Laura y Valentina) y la investigación técnica (Alejandro y Santiago) aceleró el cumplimiento de entregables complejos.
* **Transición Fluida del MER al Modelo Relacional:** La definición del modelo relacional por parte del PO (Mateo) se alineó perfectamente con las necesidades de las historias de usuario del Sprint 1.
* **Sincronización Técnicos-Funcionales:** Comunicación continua entre la especificación de reglas de negocio (FEFO) y su representación en la base de datos.

### 4.2 ¿Qué se puede mejorar? (What could be improved?)
* **Dependencias de Tareas:** La investigación técnica de Oracle (#9) dependía estrechamente del avance del modelo relacional (#8), lo que generó pequeños tiempos de espera a mitad de semana.
* **Carga de Documentación:** Gran volumen de texto en especificaciones funcionales que requiere simplificación visual para el desarrollo en código.

### 4.3 Acciones de Mejora Acordadas (Action Items para el Sprint 3)
1. **Desacoplamiento de Tareas Técnicas:** Paralelizar la creación de scripts DDL con la elaboración de diagramas UML en la siguiente iteración.
2. **Uso Intensivo de Diagramación Standard (UML):** Convertir las especificaciones narrativas en diagramas de clases y de secuencia para facilitar la interpretación del equipo backend.
3. **Preparación de Datos Semilla (Seed Data):** Diseñar scripts DML con datos de prueba realistas para validar el esquema físico en Oracle.
