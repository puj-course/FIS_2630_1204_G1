# Diagrama de Análisis (ECB) — HU.51

**Proyecto:** CareStock
**Historia de usuario:** HU.51 — Inicio de sesión con autenticación y sesión activa del usuario (#286)
**Archivo editable:** [`ECB_HU51_Inicio_Sesion.drawio`](./ECB_HU51_Inicio_Sesion.drawio)

## Diagrama

```mermaid
flowchart LR
    A(["👤<br/><b>Auxiliar de<br/>Farmacia</b>"])

    LV(("«boundary»<br/><b>LoginFX</b><br/><small>(+ LoginNotification)</small>"))
    MP(("«boundary»<br/><b>MainDashboardFX</b>"))

    AU(("«control»<br/><b>AuthenticationService</b>"))
    SC(("«control»<br/><b>SessionController</b>"))

    US(("«entity»<br/><b>Usuario</b><br/><small>(USUARIOS)</small>"))
    RO(("«entity»<br/><b>Rol</b><br/><small>(ROLES)</small>"))
    AL(("«entity»<br/><b>AccesoLog</b><br/><small>(log_accesos)</small>"))
    SA(("«entity»<br/><b>UserSession</b><br/><small>(CurrentUser)</small>"))

    A -->|"9. Cerrar sesión<br/>(confirma)"| MP
    MP -->|"10. cerrarSesion()"| SC
    SC -->|"8. redirige al dashboard"| MP
    A -->|"1. ingresa correo<br/>y contraseña"| LV
    LV -->|"2. autenticar(email, password)"| AU
    AU -->|"3. buscarPorEmail()<br/>valida estado ACTIVO<br/>y BCrypt.checkpw()"| US
    AU -->|"4. obtiene nombre del rol"| RO
    AU -->|"5. registrarAcceso()<br/>EXITOSO / FALLIDO"| AL
    LV -->|"6. iniciar sesión(Usuario)"| SC
    SC -->|"7. setCurrentUser()<br/>(ID, nombre, email, rol, farmacia)"| SA
    SC -->|"11. clearSession()"| SA

    classDef actor fill:#ffffff,stroke:#000000,color:#000000
    classDef boundary fill:#dae8fc,stroke:#6c8ebf,color:#000000
    classDef control fill:#fff2cc,stroke:#d6b656,color:#000000
    classDef entity fill:#d5e8d4,stroke:#82b366,color:#000000
    class A actor
    class LV,MP boundary
    class AU,SC control
    class US,RO,AL,SA entity
```

## Explicación

El diagrama modela el flujo de la HU.51 separando la interfaz, la lógica y los datos.

**Límites (Boundary).** Son las pantallas con las que interactúa el Auxiliar de Farmacia. `LoginFX` recibe el correo y la contraseña y, mediante `LoginNotification`, muestra el mensaje genérico *"Usuario o contraseña incorrectos"* sin revelar si el usuario existe. `MainDashboardFX` es la pantalla principal después del ingreso y ofrece la opción de cerrar sesión.

**Control.** `AuthenticationService` contiene la lógica de autenticación: busca el usuario por correo, verifica que su estado sea `ACTIVO`, compara la contraseña con el hash usando BCrypt y registra cada intento como exitoso o fallido. `SessionController` gestiona la sesión: la crea tras un ingreso válido y la destruye al cerrar sesión.

**Entidades (Entity).** `Usuario` y `Rol` corresponden a las tablas `USUARIOS` y `ROLES` de PostgreSQL. `AccesoLog` es la tabla `log_accesos`, que deja la trazabilidad de los intentos de ingreso. `UserSession` guarda en memoria al usuario activo (ID, nombre, email, rol y farmacia) para que todas las operaciones posteriores queden vinculadas a él.

**Flujo.** Los pasos 1 a 8 cubren la autenticación y la captura de la sesión activa. Los pasos 9 a 11 cubren el cierre de sesión. Se respetan las reglas ECB: el actor solo interactúa con límites, los límites se comunican con controles y solo los controles acceden a las entidades.
