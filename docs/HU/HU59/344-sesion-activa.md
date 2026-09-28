# Almacenamiento de la sesión activa (UserSession)

**Historia de Usuario:** HU.59
**Tarea:** #344
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar cómo se almacena la sesión activa en CareStock, implementado en `UserSession.java`.

---

## 2. Descripción

La sesión activa se almacena en `UserSession.java`, implementado como **Singleton**. Sus características son:

- Mantiene en memoria únicamente los datos necesarios del usuario autenticado.
- **No almacena** la contraseña ni el `password_hash`.
- Proporciona los métodos:
  - `getInstance()`: devuelve la única instancia de la sesión.
  - `setCurrentUser(Usuario usuario)`: inicia la sesión con los datos del usuario.
  - `getCurrentUser()`: devuelve el usuario autenticado.
  - `isLoggedIn()`: indica si hay una sesión activa.
  - `clearSession()`: invalida completamente la sesión activa.

---

## 3. Datos almacenados en la sesión

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | int | Identificador del usuario |
| nombre | String | Nombre completo del usuario |
| email | String | Correo electrónico |
| rol | String | Rol asignado (ADMINISTRADOR, AUXILIAR_FARMACIA, etc.) |

**Importante:** No se almacena la contraseña ni el `password_hash` por seguridad.

---

## 4. Métodos principales

| Método | Descripción |
|--------|-------------|
| `getInstance()` | Devuelve la única instancia de UserSession |
| `setCurrentUser(Usuario usuario)` | Inicia la sesión con los datos del usuario |
| `getCurrentUser()` | Devuelve el usuario autenticado |
| `isLoggedIn()` | Indica si hay una sesión activa |
| `clearSession()` | Invalida completamente la sesión activa |

---

## 5. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| UserSession | `session/UserSession.java` | Almacenamiento de la sesión activa |
| Usuario | `model/Usuario.java` | Modelo del usuario |

---

**Fin del documento.**
