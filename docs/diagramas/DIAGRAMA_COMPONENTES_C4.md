# CareStock - Diagrama de componentes C4 (nivel 3)

**Sistema:** CareStock  
**Contenedor analizado:** aplicación de escritorio JavaFX 21 sobre Java 21  
**Ubicación del código:** `src/CareStock/src/main/java/org/example/carestock/`  
**Alcance:** arquitectura implementada; no representa una API web ni microservicios.

**Nivel 3:** porque hace zoom dentro de un solo contenedor (la aplicación JavaFX) y muestra sus componentes internos y cómo se comunican. La base de datos aparece, pero solo como el elemento externo con el que se conecta.

## Objetivo y límites

Identificar los componentes lógicos dentro de la **misma aplicación de escritorio** y sus dependencias. En C4, un componente es una agrupación de responsabilidades dentro de un contenedor, no necesariamente una clase individual ni un proceso separado. **PostgreSQL/Neon está fuera** del contenedor JavaFX.

## Diagrama de componentes

**Leyenda de conectores:**

- **Gris grueso:** flujo principal de llamadas entre componentes.
- **Naranja punteado:** relaciones transversales con la sesión activa (identidad y farmacia).
- **Azul grueso:** acceso final a la base de datos por JDBC sobre TLS.

```mermaid
%%{init: {
  "theme": "base",
  "flowchart": { "curve": "basis", "nodeSpacing": 30, "rankSpacing": 70, "padding": 20, "useMaxWidth": true },
  "themeVariables": {
    "fontFamily": "Segoe UI, Roboto, Helvetica, Arial, sans-serif",
    "fontSize": "15px",
    "lineColor": "#5B6B7C"
  }
}}%%
flowchart TB

    subgraph presentacion ["PRESENTACION"]
        ui("<b>Presentacion</b><br/>JavaFX / FXML<br/>Controladores y vistas")
        builder("<b>Builders</b><br/>Aseo / Maternidad")
    end

    subgraph acceso ["ACCESO Y SESION"]
        auth("<b>Autenticacion</b><br/>BCrypt")
        sesion("<b>Sesion activa</b><br/>Usuario, rol y farmacia")
    end

    subgraph negocio ["LOGICA DE NEGOCIO"]
        fachada("<b>Fachada</b><br/>GoF Facade")
        proxy("<b>Proxy de consulta</b><br/>GoF Proxy<br/>Cache TTL")
        servicios("<b>Servicios</b><br/>Lotes y vencimientos")
    end

    subgraph datos ["ACCESO A DATOS"]
        dao("<b>Persistencia</b><br/>DAO / JDBC")
        conexion("<b>Conexion</b><br/>HikariCP")
    end

    subgraph dominio ["DOMINIO"]
        modelo("<b>Dominio y DTO</b><br/>Entidades")
    end

    bd[("<b>CareStock DB</b><br/>PostgreSQL / Neon")]

    %% Flujo principal
    ui ==> fachada
    ui ==> auth
    ui ==> builder
    fachada ==> proxy
    fachada ==> servicios
    fachada ==> dao
    proxy ==>|"cache miss"| dao
    servicios ==> conexion
    auth ==> dao
    dao ==> conexion
    dao ==> modelo
    builder ==> modelo

    %% Base de datos
    conexion ==>|"JDBC / TLS"| bd

    %% Sesion
    ui -.->|"consulta"| sesion
    fachada -.->|"valida"| sesion
    proxy -.->|"autoriza"| sesion
    sesion -.->|"usuario"| conexion

    linkStyle 0,1,2,3,4,5,6,7,8,9,10,11 stroke:#4A5B6D,stroke-width:3px
    linkStyle 12 stroke:#2F6DB5,stroke-width:4px
    linkStyle 13,14,15,16 stroke:#D9822B,stroke-width:2px,stroke-dasharray:6 4

    classDef presentacionC fill:#DCEBFA,stroke:#2F6DB5,stroke-width:2.5px,color:#0B2A4A
    classDef seguridad fill:#FDE7D3,stroke:#D9822B,stroke-width:2.5px,color:#5A2E06
    classDef negocioC fill:#D8F0E0,stroke:#2E9E5B,stroke-width:2.5px,color:#0F3D22
    classDef dominioC fill:#EADCF7,stroke:#8A4FC7,stroke-width:2.5px,color:#3A1A5C
    classDef datosC fill:#E3E8EE,stroke:#5B6B7C,stroke-width:2.5px,color:#1F2933
    classDef bdC fill:#1F3A5F,stroke:#10243D,stroke-width:2.5px,color:#FFFFFF

    class ui presentacionC
    class builder,modelo dominioC
    class auth,sesion seguridad
    class fachada,proxy,servicios negocioC
    class dao,conexion datosC
    class bd bdC

    style presentacion fill:#EAF3FC,stroke:#2F6DB5,stroke-width:3px,color:#0B2A4A
    style acceso fill:#FFF1E2,stroke:#D9822B,stroke-width:3px,color:#7A3E05
    style negocio fill:#E6F7EC,stroke:#2E9E5B,stroke-width:3px,color:#0A4A26
    style datos fill:#EDF0F4,stroke:#5B6B7C,stroke-width:3px,color:#1F2933
    style dominio fill:#F3EAFB,stroke:#8A4FC7,stroke-width:3px,color:#4A1F7A
```

### Cómo leer el diagrama

1. La **presentación** (JavaFX) es el único punto de entrada: invoca la fachada, la autenticación y los builders.
2. La **fachada** coordina el proxy de consulta, los servicios de negocio y los DAO.
3. Los **DAO** y los servicios obtienen conexiones del pool `ConexionBD`, que es el único componente que habla con PostgreSQL/Neon.
4. La **sesión activa** se consulta de forma transversal (flechas naranjas) para validar identidad y farmacia, sin formar parte del flujo principal.

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
