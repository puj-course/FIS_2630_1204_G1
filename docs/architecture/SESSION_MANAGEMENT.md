# HU-53 - Gestión de Sesiones y Cierre Seguro

## Resumen

Esta documentación consolida la implementación de la Historia de Usuario HU-53 relacionada con la gestión segura de sesiones en CareStock.

## Funcionalidades implementadas

- Integración del botón Cerrar sesión en la Topbar.
- Confirmación previa al cierre de sesión.
- Redirección automática al Login después del logout.
- Notificación de cierre seguro.
- Limpieza completa del contexto mediante `UserSession.clearSession()`.
- Protección de vistas internas mediante `AccessControl` y `ProtectedNavigationGuard`.
- Bloqueo de acceso a vistas protegidas sin sesión válida.
- Temporizador global de inactividad mediante `IdleSessionManager`.
- Detección de actividad por mouse, clics, arrastre y teclado.
- Cierre automático de sesión por inactividad.
- Mensaje: "Tu sesión ha caducado por inactividad".
- Configuración individual del timeout por usuario.
- Activación y desactivación del timeout.
- Tiempo configurable entre 1 y 120 minutos.
- Persistencia de preferencias en PostgreSQL mediante `PREFERENCIAS_USUARIO`.
- Pruebas unitarias e integración del ciclo completo de sesión.

## Flujo general

```text
Login
  |
  v
UserSession
  |
  v
Dashboard
  |
  +--> Logout manual
  |       |
  |       v
  |   SessionController
  |       |
  |       v
  |   clearSession()
  |       |
  |       v
  |     Login
  |
  +--> Inactividad
          |
          v
    IdleSessionManager
          |
          v
    SessionController
          |
          v
      clearSession()
          |
          v
        Login
```

## Subissues relacionadas

- #369 - Diseño e integración del botón Cerrar Sesión.
- #370 - Redirección automática y notificación de cierre seguro.
- #371 - Limpieza del contexto de sesión.
- #372 - Control de accesos y vistas protegidas.
- #373 - Temporizador configurable de inactividad.
- #374 - Pruebas unitarias y de integración.
- #375 - Gestión de rama, PR y Definition of Done.

## Pruebas

- `./mvnw.cmd clean test`
- `./mvnw.cmd -Pintegration -Dit.test=SessionLifecycleIT verify`

Se validó el flujo:

Login -> Uso autorizado -> Logout -> Intento de reingreso rechazado.

## Definition of Done

- [x] Cierre manual implementado.
- [x] Confirmación previa al logout.
- [x] Limpieza del contexto de sesión.
- [x] Redirección al Login.
- [x] Protección de vistas.
- [x] Timeout por inactividad.
- [x] Timeout configurable por usuario.
- [x] Persistencia de preferencias.
- [x] Pruebas unitarias.
- [x] Prueba de integración.
- [x] Documentación técnica.
- [x] Rama `feature/HU-53-cierre-sesion`.
- [ ] Pull Request aprobado.
- [ ] Code Review resuelto.
- [ ] Merge a `main`.
