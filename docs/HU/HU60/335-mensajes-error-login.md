# Identificación de mensajes de error del inicio de sesión

**Historia de Usuario:** HU.60
**Tarea:** #335
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Identificar todos los mensajes de error que muestra el sistema durante el inicio de sesión en CareStock, indicando qué dice cada mensaje y en qué caso aparece.

---

## 2. Mensajes de error identificados

| # | Mensaje | ¿Qué dice? | Causa |
|---|---------|-----------|-------|
| 1 | "Ingrese su correo y contraseña." | El sistema pide que se ingresen ambos campos | Ambos campos vacíos |
| 2 | "Ingrese su correo." | El sistema pide que se ingrese el correo | Campo correo vacío |
| 3 | "Ingrese su contraseña." | El sistema pide que se ingrese la contraseña | Campo contraseña vacío |
| 4 | "Usuario o contraseña incorrectos" | El sistema indica que las credenciales no son válidas | Credenciales incorrectas, usuario inexistente, INACTIVO o BLOQUEADO |
| 5 | "No fue posible iniciar sesión. Intente nuevamente." | El sistema indica que hubo un error de conexión | Error de conexión SQL |
| 6 | "No fue posible iniciar sesión. Intente nuevamente." | El sistema indica que hubo un error inesperado | Error inesperado (RuntimeException) |

---

## 3. Descripción de cada mensaje

### Mensaje 1: "Ingrese su correo y contraseña."

- **¿Qué dice?** El sistema solicita que se ingresen ambos campos.
- **¿Cuándo aparece?** Cuando el usuario intenta iniciar sesión sin haber ingresado ni correo ni contraseña.
- **¿Dónde?** `LoginFX.java`, método `iniciarSesion()`.

### Mensaje 2: "Ingrese su correo."

- **¿Qué dice?** El sistema solicita que se ingrese el correo.
- **¿Cuándo aparece?** Cuando el usuario intenta iniciar sesión solo con la contraseña, sin correo.
- **¿Dónde?** `LoginFX.java`, método `iniciarSesion()`.

### Mensaje 3: "Ingrese su contraseña."

- **¿Qué dice?** El sistema solicita que se ingrese la contraseña.
- **¿Cuándo aparece?** Cuando el usuario intenta iniciar sesión solo con el correo, sin contraseña.
- **¿Dónde?** `LoginFX.java`, método `iniciarSesion()`.

### Mensaje 4: "Usuario o contraseña incorrectos"

- **¿Qué dice?** El sistema indica que las credenciales no son válidas.
- **¿Cuándo aparece?** Cuando el correo no está registrado, la contraseña no coincide, el usuario está INACTIVO o está BLOQUEADO.
- **¿Dónde?** `AuthenticationService.java`, constante `ERROR_CREDENCIALES`.

### Mensaje 5: "No fue posible iniciar sesión. Intente nuevamente."

- **¿Qué dice?** El sistema indica que hubo un error de conexión con la base de datos.
- **¿Cuándo aparece?** Cuando ocurre una `SQLException` al conectar con PostgreSQL.
- **¿Dónde?** `LoginFX.java`, bloque `catch (SQLException e)`.

### Mensaje 6: "No fue posible iniciar sesión. Intente nuevamente."

- **¿Qué dice?** El sistema indica que hubo un error inesperado.
- **¿Cuándo aparece?** Cuando ocurre una `RuntimeException` no controlada.
- **¿Dónde?** `LoginFX.java`, bloque `catch (RuntimeException e)`.

---

## 4. Archivos involucrados

| Archivo | Función |
|---------|---------|
| `view/LoginFX.java` | Interfaz de inicio de sesión y validaciones del formulario |
| `service/AuthenticationService.java` | Lógica de autenticación y mensaje de credenciales incorrectas |

---

**Fin del documento.**
