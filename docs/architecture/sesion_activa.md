# Sesión activa — CareStock

> Documenta cómo el sistema mantiene identificado al usuario autenticado
> durante toda la ejecución de la aplicación, parte de HU.57 (subissue 57.2).

## Componentes involucrados

- `UserSession` (`session/UserSession.java`) — Singleton
- `UserSession.CurrentUser` (clase interna)
- `AccessControl.hasValidSession()` / `requireAuthenticated()` (`security/AccessControl.java`)
- `IdleSessionManager` (`session/IdleSessionManager.java`)

## Patrón usado: Singleton

`UserSession` se implementa como Singleton (`UserSession.getInstance()`),
con constructor privado, para garantizar que exista **una sola instancia**
de la sesión durante toda la ejecución de la app — coherente con que
CareStock es una aplicación de escritorio de un solo puesto de trabajo.

## Qué guarda la sesión

Al autenticarse, `UserSession.setCurrentUser(Usuario usuario)` construye un
objeto interno `CurrentUser` con:
- `id` (idUsuario)
- `nombreCompleto`
- `email`
- `rol` (nombreRol)

**Deliberadamente no guarda el `passwordHash`** — una vez validada la
contraseña, no hay razón para mantenerla en memoria durante la sesión.

## Verificación de sesión válida

`AccessControl.hasValidSession()` no solo comprueba que exista un
`CurrentUser`, sino que sus campos clave (`id > 0`, `email` no vacío,
`rol` no vacío) sean válidos — evita que una sesión "a medias" o corrupta
se trate como autenticada.

`AccessControl.requireAuthenticated()` lanza `IllegalStateException` si no
hay sesión válida, para que cualquier acción protegida falle explícitamente
en vez de continuar silenciosamente sin usuario identificado.

## Cierre de sesión automático por inactividad

`IdleSessionManager` añade una capa adicional no exigida explícitamente por
los criterios de aceptación originales: cierra la sesión automáticamente
si el usuario permanece inactivo un tiempo determinado, reduciendo el
riesgo de que una sesión quede abierta sin vigilancia en un puesto
compartido de farmacia.
