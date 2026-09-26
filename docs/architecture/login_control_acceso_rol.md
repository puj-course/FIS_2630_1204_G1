# Login y control de acceso por rol — CareStock

> Documenta la pantalla de inicio de sesión y cómo el rol del usuario
> determina qué puede ver y hacer en el sistema, parte de HU.57
> (subissue 57.3).

## Componentes involucrados

- `LoginFX` (`view/LoginFX.java`)
- `MainDashboardFX` (`view/MainDashboardFX.java`)
- `AccessControl.requireRole(String...)` (`security/AccessControl.java`)
- `AccesoDenegadoException` (`exception/AccesoDenegadoException.java`)
- `SessionController` (`controller/SessionController.java`)

## Flujo al abrir la aplicación

`MainDashboardFX` verifica al iniciar si `UserSession.getInstance().isLoggedIn()`
es `true`. Si no hay sesión activa, la aplicación abre `LoginFX` antes de
mostrar cualquier otra pantalla — ninguna funcionalidad de CareStock es
accesible sin haber iniciado sesión primero.

## Restricción de funcionalidades por rol

`MainDashboardFX` consulta el rol del usuario activo
(`UserSession.CurrentUser.getRol()`) y compara contra `"ADMINISTRADOR"`
(con `equalsIgnoreCase`, insensible a mayúsculas) antes de habilitar
opciones reservadas — esta comparación se repite en varios puntos del
dashboard donde se decide qué botones o secciones mostrar según el rol.

Para lógica de negocio más allá de la interfaz (no solo ocultar un botón,
sino bloquear la operación en sí), `AccessControl.requireRole(...)` se usa
como guarda al inicio de las acciones reservadas: valida primero que haya
sesión autenticada, y luego que el rol actual esté entre los roles
permitidos para esa operación. Si no lo está, lanza
`AccesoDenegadoException` con un mensaje que indica el rol actual y que no
está autorizado — la operación nunca se ejecuta.

Esto cubre el criterio de que un Usuario Normal no pueda acceder a
funcionalidades de Administrador **ni siquiera intentando invocar la
acción directamente**, no solo ocultando el botón correspondiente en la
interfaz.

## Cierre de sesión

`SessionController.cerrarSesion()` limpia la sesión activa. Tras cerrar
sesión, cualquier pantalla protegida vuelve a exigir autenticación —
consistente con `AccessControl.requireAuthenticated()`, que depende de que
`UserSession` tenga un usuario válido cargado.
