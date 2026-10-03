# Registro de farmacias en CareStock

**Historia de Usuario:** HU.66
**Tarea:** #495
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 03 de octubre de 2026

---

## 1. Objetivo

Documentar cómo se registra una nueva farmacia en el sistema CareStock, incluyendo los campos obligatorios, las validaciones y el flujo desde la interfaz hasta la base de datos.

---

## 2. Descripción del flujo

El registro de farmacias permite al Administrador crear nuevas sedes en el sistema. El flujo comienza en la interfaz JavaFX, donde el usuario ingresa los datos, luego pasan por validaciones en el servicio y finalmente se almacenan en la base de datos PostgreSQL.

---

## 3. Campos del formulario

| Campo | Tipo | ¿Obligatorio? | Descripción |
|-------|------|---------------|-------------|
| **Código** | TextField | Sí | Código único de la farmacia (ej. SEDE-NORTE) |
| **Nombre** | TextField | Sí | Nombre de la farmacia (ej. Farmacia Norte) |

---

## 4. Validaciones implementadas

| # | Validación | ¿Dónde? |
|---|------------|---------|
| 1 | El código es obligatorio | `FarmaciaService.validarDatos()` |
| 2 | El nombre es obligatorio | `FarmaciaService.validarDatos()` |
| 3 | El código no puede superar 40 caracteres | `FarmaciaService.validarDatos()` |
| 4 | El nombre no puede superar 150 caracteres | `FarmaciaService.validarDatos()` |
| 5 | El código no puede estar duplicado | `FarmaciaDAO.crearSegura()` |

---

## 5. Flujo paso a paso

| Paso | Acción | Componente |
|------|--------|------------|
| 1 | El Administrador expande el panel "+ Crear nueva farmacia" | `CrearUsuarioFX` |
| 2 | Ingresa el código y el nombre | `CrearUsuarioFX` |
| 3 | El botón "Crear farmacia" se habilita | `actualizarEstadoBotonFarmacia()` |
| 4 | Se llama al servicio | `FarmaciaService.crearFarmacia()` |
| 5 | Se validan los datos | `FarmaciaService.validarDatos()` |
| 6 | Se obtiene el ID del usuario actor | `SessionContext.requireAuthenticatedUserId()` |
| 7 | Se ejecuta la función SQL | `fn_crear_farmacia_segura()` |
| 8 | Se recupera la farmacia creada | `FarmaciaDAO.buscarPorId()` |
| 9 | Se muestra mensaje de éxito | `AlertUtil.mostrarExito()` |

---

## 6. Función SQL

El registro se realiza mediante `fn_crear_farmacia_segura()`, que:

1. Verifica que el usuario actor tenga rol `SUPER_ADMIN`.
2. Valida que el código no esté duplicado.
3. Inserta la farmacia en la tabla `FARMACIAS`.
4. Retorna el ID de la farmacia creada.

---

## 7. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| `CrearUsuarioFX` | `view/CrearUsuarioFX.java` | Interfaz del formulario |
| `FarmaciaService` | `service/FarmaciaService.java` | Validaciones y lógica |
| `FarmaciaDAO` | `dao/FarmaciaDAO.java` | Acceso a la base de datos |
| `fn_crear_farmacia_segura` | PostgreSQL | Función de inserción segura |

---

## 8. Estados de la interfaz

| Estado | Comportamiento |
|--------|----------------|
| **Campos vacíos** | Botón "Crear farmacia" deshabilitado |
| **Campos completos** | Botón habilitado |
| **Registro exitoso** | Mensaje verde: "La farmacia 'X' fue creada correctamente." |
| **Código duplicado** | Mensaje rojo: "Ya existe una farmacia con ese código." |
| **Error de conexión** | Mensaje rojo: "No fue posible crear la farmacia." |

---

**Fin del documento.**
