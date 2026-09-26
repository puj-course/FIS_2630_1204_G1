# Identificación del rol del usuario (UsuarioSesion)

**Historia de Usuario:** HU.59
**Tarea:** #345
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar cómo se identifica el rol del usuario en CareStock, a través de las clases `Usuario.java` y `UserSession.java`.

---

## 2. Descripción

El rol del usuario se identifica a través de la clase `Usuario.java` y se almacena en `UserSession.CurrentUser`. Los datos del rol son:

- `idRol`: identificador numérico del rol.
- `nombreRol`: nombre del rol (ADMINISTRADOR, AUXILIAR_FARMACIA, etc.).

---

## 3. Atributos del rol en la clase Usuario

| Campo | Tipo | Descripción |
|-------|------|-------------|
| idRol | int | Identificador numérico del rol |
| nombreRol | String | Nombre del rol (ADMINISTRADOR, AUXILIAR_FARMACIA, etc.) |
| estado | String | Estado del usuario (ACTIVO, INACTIVO, BLOQUEADO) |

El método `estaActivo()` de `Usuario` verifica que el estado del usuario sea `ACTIVO`.

---

## 4. Acceso al rol desde la sesión

`UserSession.CurrentUser` expone el rol a través del método `getRol()`. Esto permite que las vistas y los servicios consulten el rol del usuario autenticado sin acceder directamente a la base de datos.

| Método | Descripción |
|--------|-------------|
| `getRol()` | Devuelve el nombre del rol del usuario autenticado |
| `getId()` | Devuelve el identificador del usuario |
| `getNombre()` | Devuelve el nombre completo del usuario |
| `getEmail()` | Devuelve el correo electrónico del usuario |

---

## 5. Roles del sistema

| Rol | Descripción |
|-----|-------------|
| ADMINISTRADOR | Acceso total al sistema |
| AUXILIAR_FARMACIA | Gestión de inventario, consulta de lotes e ingresos/salidas |

---

## 6. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| Usuario | `model/Usuario.java` | Modelo del usuario con idRol y nombreRol |
| UserSession | `session/UserSession.java` | Almacena el rol en CurrentUser |

---

**Fin del documento.**
