# Autenticación de credenciales — CareStock

> Documenta el flujo de validación de correo y contraseña contra la base de
> datos, parte de HU.57 (subissue 57.1).

## Componentes involucrados

- `AuthenticationService` (`service/AuthenticationService.java`)
- `UsuarioDAO.buscarPorEmail(String email)` (`dao/UsuarioDAO.java`)
- `AccesoDAO.registrarAcceso(int idUsuario, String resultado)` (`dao/AccesoDAO.java`)
- `Usuario` (`model/Usuario.java`)

## Flujo de autenticación

1. `AuthenticationService.autenticar(email, password)` recibe el correo y la
   contraseña en texto plano ingresados en el login.
2. Valida que ninguno de los dos campos venga vacío o nulo; si falta alguno,
   lanza `IllegalArgumentException` con el mensaje genérico
   `"Usuario o contraseña incorrectos"`.
3. Busca el usuario por correo con `UsuarioDAO.buscarPorEmail()`. Si no
   existe, lanza la misma excepción genérica — **no revela si el correo
   está registrado o no**.
4. Si el usuario existe pero su estado no es `ACTIVO` (`INACTIVO` o
   `BLOQUEADO`), registra el intento como fallido en `AccesoDAO` y lanza
   **el mismo mensaje genérico**, sin distinguir el motivo del rechazo.
5. Verifica la contraseña con `BCrypt.checkpw(password, usuario.getPasswordHash())`.
   Si la contraseña es incorrecta, o si el hash almacenado no tiene un
   formato BCrypt válido, también se registra como fallido y se lanza el
   mismo mensaje genérico.
6. Si todo es correcto, registra el acceso como `EXITOSO` en `AccesoDAO` y
   retorna el objeto `Usuario` completo (incluyendo su rol).

## Decisión de diseño: un solo mensaje de error para todo

A diferencia de distinguir "credenciales inválidas" de "cuenta inactiva",
este flujo usa **el mismo mensaje** (`ERROR_CREDENCIALES`) en los tres
casos de fallo (usuario inexistente, cuenta no activa, contraseña
incorrecta). Esto es una decisión de seguridad más estricta: ningún caso
revela información adicional sobre el estado real de la cuenta a quien
intenta iniciar sesión.

## Trazabilidad

Cada intento de login, exitoso o fallido, queda registrado en la tabla de
accesos vía `AccesoDAO.registrarAcceso()`, asociado al `id_usuario`
correspondiente (cuando el usuario existe) y al resultado (`EXITOSO` /
`FALLIDO`). Esto cubre el requisito de trazabilidad de operaciones de
autenticación.
