# Restricción de funcionalidades según el rol

**Historia de Usuario:** HU.59
**Tarea:** #346
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar cómo se restringen las funcionalidades según el rol del usuario en CareStock.

---

## 2. Descripción

La restricción de funcionalidades se implementa mediante dos clases principales:

1. **`AccessControl`**: Verifica si existe una sesión válida antes de permitir el acceso a funcionalidades protegidas.
2. **`ProtectedNavigationGuard`**: Actúa como guardián central para las vistas protegidas, redirigiendo al Login si no hay sesión válida.

---

## 3. AccessControl

Clase que verifica si existe una sesión válida. Sus métodos son:

| Método | Descripción |
|--------|-------------|
| `hasValidSession()` | Verifica que exista una sesión activa y que los datos del usuario sean completos (id, nombre, email y rol no vacíos) |
| `requireAuthenticated()` | Lanza `IllegalStateException` si no hay sesión válida |

**Archivo:** `src/main/java/com/carestock/security/AccessControl.java`

---

## 4. ProtectedNavigationGuard

Clase que actúa como guardián central para las vistas protegidas. Antes de permitir la navegación:

1. Verifica que exista una sesión válida con `AccessControl.hasValidSession()`.
2. Si no existe, detiene el monitoreo de inactividad (`IdleSessionManager`).
3. Limpia cualquier contexto residual con `UserSession.getInstance().clearSession()`.
4. Redirige al Login (`LoginFX`).
5. Muestra una notificación de sesión expirada.

**Archivo:** `src/main/java/com/carestock/view/ProtectedNavigationGuard.java`

---

## 5. Pruebas de control de acceso

`AccessControlTest.java` verifica los siguientes escenarios:

| Escenario | Resultado esperado |
|-----------|-------------------|
| No existe sesión | Deniega el acceso |
| Existe sesión válida | Permite el acceso |
| Sesión incompleta (sin email) | Deniega el acceso |
| Cierre de sesión | Revoca el acceso protegido |
| `requireAuthenticated()` sin sesión | Lanza `IllegalStateException` |

**Archivo:** `src/test/java/com/carestock/security/AccessControlTest.java`

---

## 6. Flujo de restricción

1. El usuario intenta acceder a una vista protegida.
2. `ProtectedNavigationGuard.ensureAuthenticated()` verifica la sesión.
3. Si la sesión es válida, permite el acceso.
4. Si no es válida, redirige al Login y muestra una notificación.

---

## 7. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| AccessControl | `security/AccessControl.java` | Verificación de sesión válida |
| ProtectedNavigationGuard | `view/ProtectedNavigationGuard.java` | Guardián de vistas protegidas |
| UserSession | `session/UserSession.java` | Almacenamiento de la sesión activa |
| AccessControlTest | `test/security/AccessControlTest.java` | Pruebas de control de acceso |

---

**Fin del documento.**
