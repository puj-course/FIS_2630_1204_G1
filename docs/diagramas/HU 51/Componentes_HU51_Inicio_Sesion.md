# Diagrama de Componentes — HU.51

**Proyecto:** CareStock
**Historia de usuario:** HU.51 — Inicio de sesión con autenticación y sesión activa del usuario (#286)
**Archivo editable:** [`Componentes_HU51_Inicio_Sesion.drawio`](./Componentes_HU51_Inicio_Sesion.drawio)

## Diagrama

Notación: el círculo con línea sólida es una interfaz provista y la línea punteada "requiere" es una interfaz requerida (el semicírculo del archivo draw.io). Los elementos grises con borde punteado son externos al sistema.

```mermaid
flowchart LR
    subgraph APP[" "]
        VIS["«componente»<br/><b>Vista</b><br/><small>LoginFX · LoginNotification<br/>MainDashboardFX · ProtectedNavigationGuard</small>"]
        NEG["«componente»<br/><b>Módulos de negocio</b><br/><small>IngresoLoteService · InventarioCrudService<br/>DespachoService · FarmaciaService · UsuarioService</small>"]
        I_AUTH(("<i>Autenticacion</i>"))
        I_ACL(("<i>ControlAcceso</i>"))
        AUTH["«componente»<br/><b>Autenticación</b><br/><small>servicio: AuthenticationService</small>"]
        SEG["«componente»<br/><b>Seguridad</b><br/><small>security: AccessControl</small>"]
        I_USR(("<i>UsuarioDAO</i>"))
        I_ACC(("<i>AccesoDAO</i>"))
        DAO["«componente»<br/><b>Acceso a Datos</b><br/><small>UsuarioDAO · AccesoDAO</small>"]
        I_CON(("<i>Conexion</i>"))
        CFG["«componente»<br/><b>Configuración BD</b>"]
    end
    I_HASH(("<i>Cifrado</i>"))
    BC["«externo»<br/><b>jBCrypt</b>"]
    DB[("«externo»<br/><b>Neon PostgreSQL</b>")]

    VIS -. requiere .- I_AUTH
    I_AUTH --- AUTH
    VIS -. requiere .- I_ACL
    NEG -. requiere .- I_ACL
    I_ACL --- SEG
    AUTH -. requiere .- I_USR
    AUTH -. requiere .- I_ACC
    I_USR --- DAO
    I_ACC --- DAO
    AUTH -. requiere .- I_HASH
    I_HASH --- BC
    DAO -. requiere .- I_CON
    I_CON --- CFG
    CFG -->|JDBC| DB

    classDef comp fill:#dae8fc,stroke:#6c8ebf,color:#000000
    classDef ext fill:#eeeeee,stroke:#666666,stroke-dasharray:5 5,color:#000000
    classDef iface fill:#ffffff,stroke:#000000,color:#000000
    class VIS,AUTH,DAO,SEG,CFG,NEG comp
    class BC,DB ext
    class I_AUTH,I_USR,I_ACC,I_HASH,I_ACL,I_CON iface
    style APP fill:#ffffff,stroke:#000000
```

## Explicación

El diagrama muestra cómo se organiza CareStock en módulos para soportar el inicio de sesión de la HU.51.

**Componentes.**
- **Vista:** pantallas JavaFX del login y del dashboard, junto con la protección de navegación para usuarios sin sesión.
- **Autenticación:** valida las credenciales del usuario.
- **Acceso a Datos:** consulta los usuarios y registra los intentos de acceso.
- **Seguridad:** verifica que exista una sesión válida y que el rol tenga permiso.
- **Configuración BD:** administra la conexión con la base de datos.
- **Módulos de negocio:** servicios de lotes, inventario, despacho, farmacias y usuarios, que dependen del control de acceso para operar.

**Interfaces provistas.** Autenticación ofrece `Autenticacion`; Acceso a Datos ofrece `UsuarioDAO` y `AccesoDAO`; Seguridad ofrece `ControlAcceso`; Configuración BD ofrece `Conexion`.

**Interfaces requeridas.** La Vista necesita `Autenticacion` y `ControlAcceso`. Autenticación necesita `UsuarioDAO`, `AccesoDAO` y `Cifrado`. Los Módulos de negocio necesitan `ControlAcceso`. Acceso a Datos necesita `Conexion`.

**Servicios externos.** **jBCrypt** es la librería que compara la contraseña ingresada con el hash almacenado, de modo que las contraseñas nunca se guardan en texto plano. **Neon PostgreSQL** es la base de datos en la nube con las tablas de usuarios, roles y registro de accesos; Configuración BD se conecta a ella por JDBC.

Cada componente depende de los demás solo a través de interfaces, así que un módulo puede cambiar su implementación sin afectar al resto.
