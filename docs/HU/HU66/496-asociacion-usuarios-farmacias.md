# Asociación de usuarios a farmacias en CareStock

**Historia de Usuario:** HU.66
**Tarea:** #496
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 03 de octubre de 2026

---

## 1. Objetivo

Documentar cómo se asocia un usuario a una farmacia al momento de crearlo en CareStock, explicando la relación entre USUARIOS y FARMACIAS, y cómo se hereda la farmacia del administrador.

---

## 2. Descripción

Cada usuario del sistema debe estar asociado a una farmacia específica para garantizar la segregación de datos. La asociación se realiza durante la creación del usuario, asignando el campo `id_farmacia` en la tabla `USUARIOS`.

---

## 3. Relación entre USUARIOS y FARMACIAS

| Aspecto | Detalle |
|---------|---------|
| **Tipo de relación** | Muchos a uno (N:1) |
| **Significado** | Muchos usuarios pueden pertenecer a una misma farmacia |
| **Clave foránea** | `id_farmacia` en la tabla `USUARIOS` |
| **Restricción** | `FOREIGN KEY (id_farmacia) REFERENCES FARMACIAS(id_farmacia)` |

---

## 4. Tipos de asociación según el rol

| Rol del creador | Rol del nuevo usuario | ¿Cómo se asigna la farmacia? |
|-----------------|----------------------|------------------------------|
| **SUPER_ADMIN** | ADMINISTRADOR | El SUPER_ADMIN selecciona la farmacia explícitamente |
| **ADMINISTRADOR** | FARMACEUTICO | La farmacia se hereda del administrador que crea |

---

## 5. Flujo paso a paso

| Paso | Acción | Componente |
|------|--------|------------|
| 1 | El usuario abre el formulario de creación | `CrearUsuarioFX` |
| 2 | Selecciona el rol del nuevo usuario | `CrearUsuarioFX` |
| 3 | Si es SUPER_ADMIN: selecciona la farmacia | `comboFarmacia` |
| 4 | Si es ADMINISTRADOR: ve su farmacia fija | `lblFarmaciaFija` |
| 5 | Ingresa nombre, correo y contraseña | `CrearUsuarioFX` |
| 6 | El sistema valida los datos | `UsuarioService.validarDatos()` |
| 7 | Se ejecuta la función SQL | `fn_crear_usuario_jerarquico()` |
| 8 | Se inserta el usuario con `id_farmacia` | PostgreSQL |
| 9 | Se muestra mensaje de éxito | `AlertUtil.mostrarExito()` |

---

## 6. Reglas de negocio

| # | Regla | ¿Dónde? |
|---|-------|---------|
| 1 | Un ADMINISTRADOR solo puede crear usuarios en su propia farmacia | `UsuarioService.crearUsuario()` |
| 2 | Un SUPER_ADMIN debe seleccionar la farmacia del nuevo ADMINISTRADOR | `UsuarioService.crearUsuario()` |
| 3 | El FARMACEUTICO hereda la farmacia del ADMINISTRADOR que lo crea | `fn_crear_usuario_jerarquico()` |
| 4 | No se puede asignar una farmacia distinta a la del administrador | `AccesoDenegadoException` |

---

## 7. Función SQL

El registro se realiza mediante `fn_crear_usuario_jerarquico()`, que:

1. Verifica que el usuario actor tenga rol `SUPER_ADMIN` o `ADMINISTRADOR`.
2. Determina el rol destino según el rol del actor.
3. Asigna la farmacia según las reglas de negocio.
4. Inserta el usuario en la tabla `USUARIOS` con `id_farmacia`.
5. Retorna el ID del usuario creado.

---

## 8. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| `CrearUsuarioFX` | `view/CrearUsuarioFX.java` | Interfaz del formulario |
| `UsuarioService` | `service/UsuarioService.java` | Lógica de creación |
| `UsuarioDAO` | `dao/UsuarioDAO.java` | Acceso a la base de datos |
| `FarmaciaDAO` | `dao/FarmaciaDAO.java` | Consulta de farmacias |
| `fn_crear_usuario_jerarquico` | PostgreSQL | Función de creación segura |

---

**Fin del documento.**
