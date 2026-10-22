# Diagrama de Secuencia - Login

![Diagrama de secuencia de login](https://raw.githubusercontent.com/puj-course/FIS_2630_1204_G1/Rama_Lau/docs/diagramas/Diagrama_login.png)

## Descripción

El diagrama muestra el flujo de autenticación:

1. El usuario ingresa email y contraseña.
2. `ControladorInicioSesion` llama a `ServicioAutenticacion.autenticar()`.
3. `ServicioAutenticacion` llama a `UsuarioDaoJdbc.buscarPorEmail()`.
4. `UsuarioDaoJdbc` consulta la base de datos.
5. Si el usuario existe y la contraseña es correcta, se crea la sesión con `SesionUsuario.iniciar()`.
6. `AplicacionCareStock.mostrarDashboard()` muestra el dashboard.

## Clases involucradas

| Clase | Rol |
|-------|-----|
| `ControladorInicioSesion` | Controlador de la vista de login |
| `ServicioAutenticacion` | Servicio de autenticación |
| `UsuarioDaoJdbc` | Acceso a datos de usuarios |
| `SesionUsuario` | Gestión de la sesión |
| `AplicacionCareStock` | Navegación entre vistas |
| `Base de Datos` | PostgreSQL/Neon |

## Casos alternativos

- **Campos vacíos:** `ReglaNegocioException` → muestra mensaje de error.
- **Usuario no existe o inactivo:** `ReglaNegocioException` → credenciales incorrectas.
- **Contraseña incorrecta:** `ReglaNegocioException` → credenciales incorrectas.
