# Documentación de Roles del Sistema CareStock

**Fecha:** 22 de septiembre de 2026

---

## 1. Introducción

El presente documento describe los roles que existen dentro del sistema 
CareStock, sus responsabilidades, permisos y las funcionalidades a las que 
tienen acceso. Esta documentación sirve como referencia para el equipo de 
desarrollo y para los usuarios finales del sistema.

---

## 2. Roles del Sistema

CareStock cuenta con los siguientes roles:

| Rol | Descripción |
|-----|-------------|
| **Administrador** | Usuario con acceso total al sistema. Puede gestionar 
usuarios, consultar auditoría y administrar el catálogo completo. |
| **Encargado de Supervisión General** | Usuario con visión global del 
inventario. Supervisa todas las sedes y toma decisiones estratégicas. |
| **Jefe de Farmacia** | Usuario encargado de la supervisión del 
inventario, aprobación de pedidos y configuración de alertas. |
| **Auxiliar de Farmacia** | Usuario operativo del día a día. Registra 
entradas de lotes, realiza búsquedas y despacha medicamentos. |
| **Auditor** | Usuario encargado de revisar la trazabilidad y el 
historial de movimientos del sistema. |

---

## 3. Detalle de cada Rol

### 3.1. Administrador

**Descripción:** Es el rol con mayor nivel de acceso dentro de CareStock. 
Se encarga de la configuración general del sistema y de la supervisión de 
la operación.

**Responsabilidades:**
- Gestionar usuarios del sistema (crear, editar, desactivar).
- Asignar roles y permisos a cada usuario.
- Consultar el registro de auditoría del sistema.
- Supervisar todas las operaciones realizadas por los demás roles.
- Configurar parámetros generales del sistema.

**Funcionalidades a las que tiene acceso:**
- Dashboard general.
- Gestión de usuarios.
- Registro de auditoría.
- Catálogo de medicamentos.
- Consulta de lotes.
- Reportes del sistema.
- Configuración de alertas.

**Restricciones:**
- No tiene restricciones dentro del sistema.
- Sus acciones quedan registradas en el historial de auditoría.

---

### 3.2. Encargado de Supervisión General

**Descripción:** Es el rol con visión global del inventario en todas las 
sedes o áreas. Se encarga de la supervisión estratégica y la aprobación 
final de pedidos.

**Responsabilidades:**
- Supervisar el inventario global en todas las sedes.
- Aprobar o rechazar los pedidos de compra generados por el Jefe de 
Farmacia.
- Validar presupuesto y condiciones comerciales.
- Tomar decisiones estratégicas sobre el inventario.
- Generar reportes consolidados.

**Funcionalidades a las que tiene acceso:**
- Dashboard general.
- Supervisión de inventario global.
- Aprobación final de pedidos.
- Reportes consolidados.
- Consulta de lotes.

**Restricciones:**
- No puede gestionar usuarios del sistema.
- No puede modificar la configuración general del sistema.

---

### 3.3. Jefe de Farmacia

**Descripción:** Es el rol encargado de la supervisión del inventario y la 
toma de decisiones sobre compras y rotación de productos.

**Responsabilidades:**
- Supervisar el inventario general de la farmacia.
- Aprobar o rechazar pedidos de reabastecimiento.
- Configurar los niveles de stock mínimo por medicamento.
- Parametrizar las alertas de vencimiento (30/60 días).
- Generar reportes de productos agotados, mermas por vencimiento e 
historial de movimientos.
- Gestionar proveedores.

**Funcionalidades a las que tiene acceso:**
- Dashboard general.
- Catálogo de medicamentos.
- Consulta de lotes.
- Registro de entradas de lotes.
- Despacho de medicamentos.
- Panel de alertas semafóricas.
- Configuración de stock mínimo.
- Reportes y auditoría.
- Aprobación de pedidos.

**Restricciones:**
- No puede gestionar usuarios del sistema.
- No puede modificar la configuración general del sistema.

---

### 3.4. Auxiliar de Farmacia

**Descripción:** Es el rol operativo del día a día. Se encarga de las 
tareas directas del mostrador y del registro de inventario.

**Responsabilidades:**
- Registrar la entrada de nuevos lotes de medicamentos.
- Registrar la salida y despacho de medicamentos.
- Consultar la disponibilidad de medicamentos en tiempo real.
- Visualizar el panel de alertas semafóricas.
- Atender las solicitudes de los clientes en el mostrador.

**Funcionalidades a las que tiene acceso:**
- Dashboard de inventario.
- Búsqueda de medicamentos por nombre o principio activo.
- Registro de entradas de lotes.
- Despacho de medicamentos (con regla FEFO).
- Consulta de lotes registrados.
- Panel de alertas semafóricas.

**Restricciones:**
- No puede aprobar pedidos de compra.
- No puede gestionar usuarios.
- No puede modificar la configuración de stock mínimo.
- No puede acceder al registro de auditoría.
- No puede generar reportes de gestión.

---

### 3.5. Auditor

**Descripción:** Es el rol encargado de revisar la trazabilidad y el 
historial de movimientos del sistema. Su función principal es verificar 
que las operaciones se hayan realizado correctamente.

**Responsabilidades:**
- Consultar el historial de movimientos del inventario.
- Revisar la trazabilidad de entradas y salidas de lotes.
- Verificar que cada operación tenga registrado el usuario que la realizó.
- Consultar el registro de auditoría del sistema.
- Generar reportes de auditoría.

**Funcionalidades a las que tiene acceso:**
- Dashboard general (solo lectura).
- Registro de auditoría.
- Historial de movimientos.
- Consulta de lotes.
- Reportes de trazabilidad.

**Restricciones:**
- No puede registrar entradas de lotes.
- No puede despachar medicamentos.
- No puede gestionar usuarios.
- No puede modificar la configuración del sistema.
- Solo tiene acceso de lectura a la información.

---

## 4. Matriz de Permisos por Rol

| Funcionalidad | Administrador | Encargado Supervisión | Jefe de Farmacia 
| Auxiliar de Farmacia | Auditor |
|---------------|:-------------:|:---------------------:|:----------------:|:--------------------:|:-------:|
| Iniciar sesión | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver dashboard | ✅ | ✅ | ✅ | ✅ | ✅ |
| Buscar medicamentos | ✅ | ✅ | ✅ | ✅ | ✅ |
| Registrar entrada de lotes | ✅ | ✅ | ✅ | ✅ | ❌ |
| Despachar medicamentos | ✅ | ✅ | ✅ | ✅ | ❌ |
| Consultar lotes | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver panel de alertas | ✅ | ✅ | ✅ | ✅ | ✅ |
| Configurar stock mínimo | ✅ | ✅ | ✅ | ❌ | ❌ |
| Aprobar pedidos | ✅ | ✅ | ✅ | ❌ | ❌ |
| Generar reportes | ✅ | ✅ | ✅ | ❌ | ✅ |
| Consultar auditoría | ✅ | ❌ | ❌ | ❌ | ✅ |
| Gestionar usuarios | ✅ | ❌ | ❌ | ❌ | ❌ |
| Configurar sistema | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## 5. Restricción de Funcionalidades según Rol

El sistema CareStock implementa las siguientes restricciones según el rol 
del usuario:

- **Administrador:** Acceso total a todas las funcionalidades del sistema.
- **Encargado de Supervisión General:** Acceso a funcionalidades de 
supervisión global y aprobación final de pedidos.
- **Jefe de Farmacia:** Acceso a funcionalidades de supervisión, gestión 
de inventario y aprobación de pedidos.
- **Auxiliar de Farmacia:** Acceso únicamente a las funcionalidades 
operativas del mostrador (registro, búsqueda y despacho).
- **Auditor:** Acceso de solo lectura a la información de trazabilidad y 
auditoría.

---

## 6. Registro de Identidad del Usuario Activo

Cada operación realizada en el sistema queda registrada con la identidad 
del usuario activo. Esto permite:

- Auditar quién realizó cada operación.
- Rastrear cambios en el inventario.
- Garantizar la trazabilidad de las acciones.

El registro incluye:
- Nombre del usuario.
- Rol del usuario.
- Fecha y hora de la operación.
- Tipo de operación realizada.

---

**Fin del documento.**
