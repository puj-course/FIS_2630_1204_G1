# Proceso de inicio de sesión (LoginFX)

**Historia de Usuario:** HU.59
**Tarea:** #343
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar el proceso de inicio de sesión en CareStock, implementado en `LoginFX.java`.

---

## 2. Descripción del proceso

El inicio de sesión se implementa en `LoginFX.java`. El flujo es el siguiente:

1. El usuario ingresa su correo y contraseña.
2. El sistema valida que ambos campos no estén vacíos.
3. Si algún campo está vacío, muestra un mensaje de error y detiene el proceso.
4. Si ambos campos están completos, llama a `AuthenticationService.autenticar(email, password)`.
5. El servicio consulta la base de datos a través de `UsuarioDAO.buscarPorEmail(email)`.
6. Si las credenciales son correctas, retorna un objeto `Usuario` con los datos del usuario.
7. Si las credenciales son incorrectas, lanza `IllegalArgumentException` y muestra un mensaje genérico ("Correo o contraseña incorrectos").
8. Si ocurre un error de conexión, muestra un mensaje genérico sin exponer detalles internos.
9. Si la autenticación es exitosa, se crea la sesión con `UserSession.getInstance().setCurrentUser(usuario)`.
10. Finalmente, redirige al Dashboard (`MainDashboardFX`).

---

## 3. Validaciones del formulario

| Validación | Acción |
|------------|--------|
| Correo vacío | Muestra "Ingrese su correo" |
| Contraseña vacía | Muestra "Ingrese su contraseña" |
| Ambos vacíos | Muestra "Ingrese su correo y contraseña" |
| Credenciales incorrectas | Muestra "Correo o contraseña incorrectos" |
| Error de conexión | Muestra "No fue posible iniciar sesión. Intente nuevamente." |

---

## 4. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| LoginFX | `view/LoginFX.java` | Interfaz de inicio de sesión |
| AuthenticationService | `service/AuthenticationService.java` | Lógica de autenticación |
| UsuarioDAO | `dao/UsuarioDAO.java` | Consulta a la base de datos |
| UserSession | `session/UserSession.java` | Almacenamiento de la sesión |

---

**Fin del documento.**
