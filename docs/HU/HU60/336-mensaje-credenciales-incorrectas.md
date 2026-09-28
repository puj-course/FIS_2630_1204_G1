# Documentación del mensaje "Usuario o contraseña incorrectos"

**Historia de Usuario:** HU.60
**Tarea:** #336
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar el mensaje de error "Usuario o contraseña incorrectos" del inicio de sesión, su causa y recomendación.

---

## 2. Mensaje

"Usuario y contraseña incorrectos"


---

## 3. Causa

El mensaje se muestra cuando ocurre cualquiera de estos casos:

| # | Causa | ¿Dónde? |
|---|-------|---------|
| 1 | El correo no está registrado en la base de datos | `AuthenticationService.autenticar()` |
| 2 | La contraseña no coincide con el hash almacenado | `AuthenticationService.autenticar()` |
| 3 | El usuario está INACTIVO | `AuthenticationService.autenticar()` |
| 4 | El usuario está BLOQUEADO | `AuthenticationService.autenticar()` |
| 5 | El hash de la contraseña es inválido | `AuthenticationService.autenticar()` |

**Importante:** El sistema usa el mismo mensaje para todos los casos, para no revelar si el correo existe o no.

---

## 4. Recomendación

- Verificar que el correo esté bien escrito.
- Verificar que la contraseña sea correcta.
- Si el problema persiste, contactar al Administrador del sistema.

---

## 5. Archivos involucrados

| Archivo | Función |
|---------|---------|
| `service/AuthenticationService.java` | Genera el mensaje `ERROR_CREDENCIALES` |
| `view/LoginFX.java` | Muestra el mensaje en la interfaz |

---

**Fin del documento.**
