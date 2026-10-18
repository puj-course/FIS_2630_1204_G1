# CareStock - Diagrama de despliegue C4

**Tipo:** diagrama de despliegue (vista complementaria al C4 estático)  
**Sistema:** CareStock, aplicación JavaFX 21 / Java 21  
**Escenarios:** ejecución local habitual y ejecución Docker opcional.

## Alcance

Se muestran **instancias ejecutadas** y los nodos que las alojan. No se presenta GitHub, Maven ni GitHub Actions como servidores de producción: pertenecen al proceso de construcción y entrega. Neon aloja la base PostgreSQL y **no** ejecuta una API de CareStock.

**Leyenda de los diagramas:**

- **Cajas grandes con borde punteado:** nodos de despliegue (equipo, JVM, Docker, nube). Los nodos anidados se ejecutan dentro del que los contiene.
- **Recuadros redondeados:** software que se ejecuta (la aplicación CareStock).
- **Cilindro:** base de datos.
- **Flecha azul gruesa:** conexión de datos por JDBC sobre TLS.
- **Flecha naranja punteada:** lectura de la configuración local.

## Escenario A - Ejecución local del sistema

```mermaid
%%{init: {
  "theme": "base",
  "flowchart": { "curve": "basis", "nodeSpacing": 40, "rankSpacing": 70, "padding": 15, "useMaxWidth": false },
  "themeVariables": {
    "fontFamily": "Segoe UI, Roboto, Helvetica, Arial, sans-serif",
    "fontSize": "16px",
    "lineColor": "#5B6B7C"
  }
}}%%
flowchart TB

    subgraph equipo["EQUIPO DEL USUARIO · Windows / escritorio compatible"]
        cfg("<b>conexion-local.properties</b><br/>db.url, db.user, db.password<br/>db.pool.min / db.pool.max")

        subgraph jvm["ENTORNO JAVA · JDK 21 + JavaFX 21"]
            app("<b>CareStock</b><br/>Java 21 / JavaFX 21<br/>MVC, Facade, Proxy, DAO<br/>HikariCP y caché en memoria")
        end
    end

    subgraph nube["NUBE NEON · PostgreSQL administrado"]
        bd[("<b>Base de datos CareStock</b><br/>PostgreSQL<br/>Usuarios, farmacias, productos,<br/>lotes y movimientos")]
    end

    cfg -.->|"lee al iniciar"| app
    app ==>|"JDBC / PostgreSQL<br/>TLS · puerto 5432"| bd

    linkStyle 0 stroke:#D9822B,stroke-width:2px,stroke-dasharray:6 4
    linkStyle 1 stroke:#2F6DB5,stroke-width:4px

    classDef appC fill:#DCEBFA,stroke:#2F6DB5,stroke-width:2.5px,color:#0B2A4A
    classDef cfgC fill:#FDE7D3,stroke:#D9822B,stroke-width:2.5px,color:#5A2E06
    classDef bdC fill:#1F3A5F,stroke:#10243D,stroke-width:2.5px,color:#FFFFFF

    class app appC
    class cfg cfgC
    class bd bdC

    style equipo fill:#F5F7FA,stroke:#5B6B7C,stroke-width:3px,stroke-dasharray:6 4,color:#1F2933
    style jvm fill:#EAF3FC,stroke:#2F6DB5,stroke-width:3px,stroke-dasharray:6 4,color:#0B2A4A
    style nube fill:#E6F7EC,stroke:#2E9E5B,stroke-width:3px,stroke-dasharray:6 4,color:#0A4A26
```

**Recorrido:** el usuario interactúa con ventanas JavaFX en su equipo. La JVM ejecuta el código de CareStock, lee la configuración local y abre conexiones con Neon a través de HikariCP/JDBC. No hay navegador, SPA ni API HTTP intermedia en la arquitectura actual.

## Escenario B - Ejecución Docker opcional

```mermaid
%%{init: {
  "theme": "base",
  "flowchart": { "curve": "basis", "nodeSpacing": 40, "rankSpacing": 70, "padding": 15, "useMaxWidth": false },
  "themeVariables": {
    "fontFamily": "Segoe UI, Roboto, Helvetica, Arial, sans-serif",
    "fontSize": "16px",
    "lineColor": "#5B6B7C"
  }
}}%%
flowchart TB

    subgraph host["EQUIPO ANFITRIÓN · Windows / Linux"]
        cfg("<b>conexion-local.properties</b><br/>Archivo local de configuración")

        subgraph engine["DOCKER ENGINE · Entorno de contenedores"]
            subgraph instancia["CONTENEDOR CARESTOCK · Eclipse Temurin 21 + Xvfb"]
                app("<b>CareStock JavaFX</b><br/>Java 21 / Maven / JavaFX<br/>Display virtual Xvfb<br/>HikariCP y caché en memoria")
            end
        end
    end

    subgraph neon["NUBE NEON · PostgreSQL administrado"]
        bd[("<b>Base de datos CareStock</b><br/>PostgreSQL<br/>Datos compartidos por la aplicación")]
    end

    cfg -.->|"montado solo lectura<br/>/app/config"| app
    app ==>|"JDBC / PostgreSQL<br/>TLS · puerto 5432"| bd

    linkStyle 0 stroke:#D9822B,stroke-width:2px,stroke-dasharray:6 4
    linkStyle 1 stroke:#2F6DB5,stroke-width:4px

    classDef appC fill:#DCEBFA,stroke:#2F6DB5,stroke-width:2.5px,color:#0B2A4A
    classDef cfgC fill:#FDE7D3,stroke:#D9822B,stroke-width:2.5px,color:#5A2E06
    classDef bdC fill:#1F3A5F,stroke:#10243D,stroke-width:2.5px,color:#FFFFFF

    class app appC
    class cfg cfgC
    class bd bdC

    style host fill:#F5F7FA,stroke:#5B6B7C,stroke-width:3px,stroke-dasharray:6 4,color:#1F2933
    style engine fill:#EDF0F4,stroke:#0DB7ED,stroke-width:3px,stroke-dasharray:6 4,color:#0B4F6C
    style instancia fill:#E1F5FD,stroke:#0DB7ED,stroke-width:3px,stroke-dasharray:6 4,color:#0B4F6C
    style neon fill:#E6F7EC,stroke:#2E9E5B,stroke-width:3px,stroke-dasharray:6 4,color:#0A4A26
```

**Importante:** el `Dockerfile` ejecuta JavaFX mediante `xvfb-run`; esto **no publica una interfaz web** ni implica que se pueda ver la GUI automáticamente desde un navegador. Docker es una opción de empaquetado y ejecución, no el contenedor lógico C4 del nivel 2.

## Nodos, tecnología y artefactos

| Nodo | Tecnología | Qué contiene o ejecuta |
|---|---|---|
| Equipo del usuario | Sistema operativo con Java 21 y JavaFX 21 | Aplicación de escritorio CareStock |
| JVM | Java 21 | Controladores, servicios, modelos, DAO, HikariCP y caché en memoria |
| Nube Neon | PostgreSQL administrado | Base de datos persistente |
| Docker Engine (opcional) | Docker / Compose | Contenedor construido desde `src/CareStock/Dockerfile` |
| Contenedor Docker (opcional) | Eclipse Temurin 21, Xvfb | Ejecución JavaFX sobre display virtual |

## Conexiones y configuración

- **Aplicación a Neon:** JDBC con protocolo PostgreSQL, puerto típico **5432**, con TLS exigido mediante `sslmode=require` en la URL de ejemplo.
- **Credenciales y parámetros:** archivo local `src/CareStock/config/conexion-local.properties`, leído por `ConexionBD.java`. Las claves son `db.url`, `db.user`, `db.password`, `db.pool.max` y `db.pool.min`.
- **Configuración de referencia:** `src/CareStock/config/conexion-local.properties.example`. Nunca versionar credenciales reales.
- **Docker Compose:** `compose.yaml` monta el archivo `conexion-local.properties` en `/app/config/conexion-local.properties` en modo solo lectura.
- **Rendimiento:** HikariCP administra el pool de conexiones; el Proxy mantiene su caché en memoria dentro del proceso Java.

## Alcances que NO deben aparecer como hechos implementados

- No existe servidor HTTP, API REST, Spring Boot, microservicios, balanceador, Kubernetes o Redis operando en la arquitectura actual.
- No se ejecuta una aplicación web o móvil para los usuarios finales.
- GitHub Actions ayuda a construir y comprobar software; no es un nodo que hospeda permanentemente la aplicación.
- Docker no es obligatorio para la instalación local ni equivale al nivel de contenedores C4.

## Evidencia en el repositorio

- `src/CareStock/pom.xml`: Java 21, dependencias JavaFX y Maven.
- `src/CareStock/src/main/java/org/example/carestock/CareStockApp.java`: entrada JavaFX.
- `src/CareStock/src/main/java/org/example/carestock/config/ConexionBD.java`: JDBC y HikariCP con configuración local.
- `src/CareStock/config/conexion-local.properties.example`: configuración de base de datos.
- `src/CareStock/Dockerfile` y `compose.yaml`: ejecución Docker opcional.

**Diagrama histórico:** [DIAGRAMA_DESPLIEGUE.md](DIAGRAMA_DESPLIEGUE.md).
