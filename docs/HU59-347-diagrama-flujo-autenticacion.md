# Diagrama del flujo de autenticación

**Historia de Usuario:** HU.59
**Tarea:** #347
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Diseñar un diagrama que represente el flujo completo de autenticación y control de acceso en CareStock.

---

## 2. Diagrama del flujo

```mermaid
flowchart TD
    A[Usuario abre LoginFX] --> B{¿Sesión activa?}
    B -->|Sí| C[Redirige al Dashboard]
    B -->|No| D[Muestra formulario de login]
    D --> E[Ingresa correo y contraseña]
    E --> F{¿Campos completos?}
    F -->|No| G[Muestra mensaje de error]
    G --> E
    F -->|Sí| H[AuthenticationService.autenticar]
    H --> I[UsuarioDAO.buscarPorEmail]
    I --> J{¿Credenciales válidas?}
    J -->|No| K[Muestra error genérico]
    K --> E
    J -->|Sí| L[UserSession.setCurrentUser]
    L --> M[Redirige al Dashboard]
    M --> N{¿Acceso a vista protegida?}
    N -->|Sí| O[AccessControl.hasValidSession]
    O --> P{¿Sesión válida?}
    P -->|No| Q[ProtectedNavigationGuard redirige a Login]
    P -->|Sí| R[Permite acceso]
    N -->|No| S[Acceso denegado]
