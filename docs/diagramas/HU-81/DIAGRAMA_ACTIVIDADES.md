# HU-81 — Puntos de entrada y flujo de arranque e inicio de sesión

**Historia de Usuario:** HU-81  
**Código analizado:** `src/CareStock`, commit `0d85f9c` (merge del PR #624)  
**Fecha:** 10 de octubre de 2026

## 1. Puntos de entrada de la aplicación

El módulo tiene tres clases con método `main`. Las clases se citan con su nombre simple; todas están en el paquete `org.example.carestock`.

| Clase | Cómo se ejecuta | Qué hace | ¿Pasa por el inicio de sesión? |
|---|---|---|---|
| `CareStockApp` | Es la clase principal configurada en el `javafx-maven-plugin` del `pom.xml` (`./mvnw javafx:run`), y la que usa el `Dockerfile` del módulo | Abre la ventana, muestra primero `login-view.fxml` y solo después de autenticarse pasa a `inventario-view.fxml` | Sí. Es el único recorrido que muestra el inventario, y solo lo hace tras el inicio de sesión |
| `Launcher` | Tiene su propio `main`; ni el `pom.xml` ni el `Dockerfile` lo usan | Lanza `HelloApplication`, que muestra la ventana de ejemplo `hello-view.fxml` con un botón y un texto de bienvenida | No aplica. No muestra el inicio de sesión ni datos del inventario |
| `PruebaConexion` | Tiene su propio `main` y se ejecuta por consola | Consulta `LoteDAOImpl.listarTodos()` y escribe en la consola los lotes registrados en la base de datos | No. No pide ni valida credenciales de usuario |

---

## 2. Flujo de arranque e inicio de sesión

Diagrama de actividades del recorrido por `CareStockApp`, desde que se ejecuta hasta que se ve el inventario o un mensaje de error.

```mermaid
flowchart TD
    A(["Se ejecuta CareStockApp"]) --> B["start: configura la ventana principal"]
    B --> C["mostrarLogin: carga login-view.fxml"]
    C --> D["El usuario escribe correo y contraseña y pulsa Iniciar sesión o Enter"]
    D --> E["LoginController.ejecutarLogin: deshabilita los controles y muestra el indicador de carga"]
    E --> F["AuthenticationService.autenticar"]
    F --> G{"¿Correo y contraseña completos?"}
    G -->|No| X1["ReglaNegocioException: Debe ingresar tanto el correo como la contraseña"]
    G -->|Sí| H["UsuarioDAOImpl.buscarPorEmail consulta la tabla usuarios"]
    H --> I{"¿Falla la conexión con la base de datos?"}
    I -->|Sí| X4["Mensaje: Error al conectar con la base de datos"]
    I -->|No| J{"¿Existe el usuario?"}
    J -->|No| X2["ReglaNegocioException: Credenciales incorrectas o usuario no registrado"]
    J -->|Sí| K{"¿La contraseña es igual al valor guardado o este empieza por $2a$?"}
    K -->|No| X3["ReglaNegocioException: Contraseña incorrecta"]
    K -->|Sí| L["Mensaje: Acceso concedido"]
    L --> M["CareStockApp.mostrarDashboard: carga inventario-view.fxml"]
    M --> N(["El usuario ve el inventario"])
    X1 --> Z["LoginController muestra el mensaje, limpia la contraseña y habilita los controles"]
    X2 --> Z
    X3 --> Z
    X4 --> Y["LoginController muestra el mensaje y habilita los controles"]
    Z --> D
    Y --> D
```

Notas sobre el diagrama:

- La decisión sobre la contraseña refleja el código actual: la condición acepta el acceso cuando el valor guardado empieza por `$2a$`, el prefijo de bcrypt. El detalle de seguridad está en `HU-80-arquitectura-por-capas.md`, sección 5.2.
- El estado del usuario (`ACTIVO`, `INACTIVO` o `BLOQUEADO`) no se evalúa en este recorrido.
- Si la autenticación falla, el usuario permanece en la pantalla de inicio de sesión y puede volver a intentarlo.
