# CareStock - Diagrama de componentes C4 (nivel 3)

**Sistema:** CareStock  
**Contenedor analizado:** aplicación de escritorio JavaFX 21 sobre Java 21  
**Ubicación del código:** `src/CareStock/src/main/java/org/example/carestock/`  
**Alcance:** arquitectura implementada; no representa una API web ni microservicios.

## Objetivo y límites

Identificar los componentes lógicos dentro de la **misma aplicación de escritorio** y sus dependencias. En C4, un componente es una agrupación de responsabilidades dentro de un contenedor, no necesariamente una clase individual ni un proceso separado. **PostgreSQL/Neon está fuera** del contenedor JavaFX.

## Diagrama de componentes

**Leyenda:** las flechas gruesas son el flujo principal de la aplicación; las punteadas son relaciones transversales con la sesión activa.

```mermaid
%%{init: {
  "theme": "base",
  "flowchart": { "curve": "basis", "nodeSpacing": 45, "rankSpacing": 60, "padding": 12 },
  "themeVariables": {
    "fontFamily": "Segoe UI, Roboto, Helvetica, Arial, sans-serif",
    "fontSize": "14px",
    "lineColor": "#5B6B7C"
  }
}}%%
flowchart TB

    ui["Presentación y navegación<br/>JavaFX / FXML<br/>Login, inventario, catálogo y consulta"]

    subgraph acceso["① Acceso y sesión"]
        direction LR
        auth["Autenticación<br/>AuthenticationService / BCrypt<br/>Verifica credenciales"]
        sesion["Sesión activa<br/>SesionUsuario<br/>Identidad, rol y farmacia"]
    end

    subgraph negocio["② Lógica de negocio"]
        direction LR
        fachada["Fachada de inventario<br/>GoF Facade<br/>Centraliza operaciones"]
        proxy["Consulta de inventario<br/>GoF Proxy<br/>Caché TTL y acceso por farmacia"]
        servicios["Servicios de negocio<br/>Java<br/>Lotes, dispensación y vencimientos"]
    end

    subgraph dominio["③ Dominio"]
        direction LR
        builder["Construcción de productos<br/>AseoBuilder / MaternidadBuilder"]
        modelo["Dominio y DTO<br/>model / DataTransferObject<br/>Entidades y Validable"]
    end

    subgraph datos["④ Acceso a datos"]
        direction LR
        dao["Persistencia<br/>DAO concretos / JDBC"]
        conexion["Conexión a BD<br/>ConexionBD / HikariCP"]
    end

    bd[("Base de datos CareStock<br/>PostgreSQL / Neon")]

    %% Flujo principal
    ui ==> fachada
    ui ==> auth
    ui ==> builder
    fachada ==> proxy
    fachada ==> servicios
    fachada ==> dao
    proxy ==> dao
    servicios ==> conexion
    auth ==> dao
    builder ==> modelo
    dao ==> modelo
    dao ==> conexion
    conexion ==>|"JDBC / TLS"| bd

    %% Relaciones transversales (sesión y autorización)
    ui -.->|"consulta"| sesion
    fachada -.->|"valida"| sesion
    proxy -.->|"autoriza"| sesion
    sesion -.->|"valida usuario"| conexion

    %% Estilos
    classDef presentacion fill:#DCEBFA,stroke:#2F6DB5,stroke-width:2px,color:#0B2A4A
    classDef seguridad fill:#FDE7D3,stroke:#D9822B,stroke-width:2px,color:#5A2E06
    classDef negocioC fill:#D8F0E0,stroke:#2E9E5B,stroke-width:2px,color:#0F3D22
    classDef dominioC fill:#EADCF7,stroke:#8A4FC7,stroke-width:2px,color:#3A1A5C
    classDef datosC fill:#E3E8EE,stroke:#5B6B7C,stroke-width:2px,color:#1F2933
    classDef bdC fill:#1F3A5F,stroke:#10243D,stroke-width:2px,color:#FFFFFF

    class ui presentacion
    class auth,sesion seguridad
    class fachada,proxy,servicios negocioC
    class builder,modelo dominioC
    class dao,conexion datosC
    class bd bdC

    style acceso fill:#FFF8F0,stroke:#D9822B,stroke-dasharray:4 4,color:#5A2E06
    style negocio fill:#F3FBF6,stroke:#2E9E5B,stroke-dasharray:4 4,color:#0F3D22
    style dominio fill:#F9F4FD,stroke:#8A4FC7,stroke-dasharray:4 4,color:#3A1A5C
    style datos fill:#F5F7FA,stroke:#5B6B7C,stroke-dasharray:4 4,color:#1F2933
```

## Componentes y trazabilidad al código

| Componente C4 | Archivos y paquetes reales | Responsabilidad |
|---|---|---|
| Presentación | `CareStockApp.java`, `controller/*`, recursos FXML | Navegación, eventos y formularios JavaFX |
| Autenticación | `service/AuthenticationService.java`, `dao/UsuarioDAOImpl.java` | Validar contraseñas BCrypt |
| Sesión | `session/SesionUsuario.java` | Identidad del usuario, rol y farmacia activa |
| Fachada | `facade/Inventario.java` | Centralizar operaciones y verificar contexto de farmacia |
| Proxy | `proxy/ConsultaInventarioProxy.java`, `proxy/InventarioConsultas.java` | Caché de inventario por farmacia e invalidación |
| Servicios | `service/RegistroLoteService.java`, `service/FarmacovigilanciaService.java`, `service/ControlVencimientosService.java` | Reglas y procesos transaccionales |
| Builder | `model/AseoBuilder.java`, `model/MaternidadBuilder.java` | Construcción de productos desde el catálogo |
| Dominio/DTO | `model/*`, `DataTransferObject/*` | Entidades, validaciones y datos transferidos |
| DAO | `dao/*` (incluye `CatalogoDAO`, `ConsultaInventarioDAO`, `LoteDAOImpl`) | SQL, mapeo de filas y persistencia |
| Conexión | `config/ConexionBD.java` | Pool HikariCP y conexiones JDBC |

## Reglas arquitectónicas que se deben mostrar

1. **Un solo contenedor ejecutable:** JavaFX reúne interfaz, negocio y persistencia; los componentes no son microservicios.
2. **MVC:** las vistas FXML y controladores interactúan con el dominio y la fachada; los DAO encapsulan SQL.
3. **Facade:** `Inventario` coordina servicios y DAO, pero no es una API REST.
4. **Proxy:** `ConsultaInventarioProxy` usa el DAO real al faltar caché, aplica TTL y autorización por farmacia.
5. **Builder:** los builders de aseo y maternidad participan en la creación de productos del catálogo.
6. **Datos:** la aplicación llega a Neon mediante `ConexionBD`, HikariCP, JDBC y PostgreSQL; no existe un backend HTTP intermedio.
7. **Seguridad:** el contexto de farmacia se verifica en la sesión/fachada y en las consultas; no debe interpretarse como autorización universal para todas las operaciones.

**Nota:** El proyecto también contiene `ProductoDirector`, otros builders y tipos de caché. Su existencia no significa que todos participen en los flujos de las pantallas actuales. Esta vista prioriza los componentes integrados.

**Diagrama previo y trazabilidad detallada:** [DIAGRAMA_COMPONENTES_GENERAL.md](DIAGRAMA_COMPONENTES_GENERAL.md).
