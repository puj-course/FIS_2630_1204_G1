# CareStock

**Gestión y trazabilidad de inventario hospitalario por farmacia**

CareStock es una aplicación académica de escritorio desarrollada en **Java 21 y JavaFX 21** para administrar productos e inventario en farmacias. Utiliza **PostgreSQL (Neon)** mediante **JDBC**, con un pool de conexiones **HikariCP**, autenticación de usuarios, contexto de sesión y consultas de inventario segregadas por farmacia.

El catálogo contempla **medicamentos, productos de aseo y productos de maternidad**. Su arquitectura se documenta mediante los **niveles 1, 2 y 3 del modelo C4**, un diagrama UML de clases como referencia del nivel 4 y una vista complementaria de despliegue. En el código se emplean los patrones **Factory Method, Facade, Proxy y Template Method**, además de mecanismos de caché para consultas.

> **Estado del proyecto:** prototipo académico en evolución. Cuenta con pruebas automatizadas y pipelines de calidad, pero **no está certificado para uso hospitalario productivo**. El JAR de Maven no equivale a un instalador autónomo; la distribución Windows se construye mediante un workflow de Release, sujeto a que finalice satisfactoriamente.

## Contenido

- [Funcionalidades](#funcionalidades)
- [Tecnologías](#tecnologías)
- [Arquitectura C4 y diagramas](#arquitectura-c4-y-diagramas)
- [Patrones de diseño y componentes](#patrones-de-diseño-y-componentes)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Instalación y configuración](#instalación-y-configuración)
- [Compilación y ejecución](#compilación-y-ejecución)
- [Ejecución con Docker](#ejecución-con-docker)
- [Base de datos y scripts SQL](#base-de-datos-y-scripts-sql)
- [Pruebas y estado de calidad](#pruebas-y-estado-de-calidad)
- [Integración y entrega continuas](#integración-y-entrega-continuas)
- [Seguridad y buenas prácticas](#seguridad-y-buenas-prácticas)
- [Equipo y documentación](#equipo-y-documentación)
- [Versiones y Releases](#versiones-y-releases)
- [Distribución para Windows](#distribución-para-windows)

## Funcionalidades

### Gestión de usuarios y sesiones

- Inicio de sesión mediante credenciales y autenticación respaldada por la base de datos.
- Contraseñas verificadas mediante **BCrypt**.
- Contexto de usuario autenticado y cierre de sesión.
- Uso de la farmacia asignada al usuario como contexto de las consultas de inventario.
- Restricciones de acceso y validaciones de negocio según el flujo implementado.

### Catálogo de productos

- Consulta y registro de **medicamentos**, **aseo** y **maternidad** mediante formularios JavaFX.
- Campos y validaciones específicos según el tipo de producto.
- Organización del catálogo por categorías y presentación.
- Creación de productos mediante fábricas especializadas con Factory Method.

### Inventario y lotes

- Visualización de lotes y sus datos de identificación, cantidades, vencimientos y estado.
- Consulta unificada de existencias de medicamentos, aseo y maternidad, asociadas a la farmacia activa.
- Filtros de búsqueda y categoría en la consulta de inventario.
- Componentes de lógica de dominio y persistencia para registro y seguimiento de lotes y movimientos.
- Infraestructura SQL para inventario y segmentación por farmacia.

> **Alcance:** la presencia de una clase, procedimiento SQL o historia de usuario no garantiza, por sí sola, que todos los escenarios de negocio estén conectados y validados desde la interfaz. Cada flujo debe comprobarse con sus criterios de aceptación.

### Alertas, movimientos y dispensación

- Consulta de alertas relacionadas con inventario, stock mínimo y vencimientos, según los flujos implementados.
- Exportación de alertas y sugerencias de reposición descritas en la [guía funcional](docs/user_guide/alertas-exportar-y-reponer.md).
- Registro de movimientos y dispensación de lotes con controles de cantidad y confirmación, según la [guía de dispensación](docs/user_guide/dispensacion.md).
- Las propuestas FEFO y demás mejoras de sprint deben verificarse contra la versión integrada y sus pruebas antes de presentarse como completadas.

### Rendimiento y acceso a datos

- **HikariCP:** reutilización de conexiones JDBC con PostgreSQL/Neon.
- **Caching Proxy:** consultas unificadas mediante `ConsultaInventarioProxy` e `InventarioConsultas`, con caché por farmacia e invalidación.
- **Infraestructura de caché adicional:** `CacheFarmacia` y `VistasCache`, con políticas de expiración para datos de catálogo y lotes. Su aplicación efectiva depende de que las vistas y servicios correspondientes la utilicen.
- Separación entre vistas, lógica de aplicación y acceso a datos para facilitar el mantenimiento.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Interfaz | JavaFX 21, FXML |
| Construcción | Apache Maven y Maven Wrapper |
| Base de datos | PostgreSQL / Neon |
| Persistencia | JDBC y DAOs concretos |
| Pool de conexiones | HikariCP |
| Autenticación | BCrypt |
| Registro de eventos | SLF4J |
| Patrones utilizados en el código | Factory Method, Facade, Proxy, Template Method |
| Pruebas (dependencias) | JUnit 5 / Maven Surefire |
| CI/CD | GitHub Actions |
| Contenerización opcional | Docker / Docker Compose |

**CareStock no requiere Spring Boot, Oracle ni Python para ejecutar su aplicación JavaFX.** Docker es opcional para el uso local; sí se necesita una base PostgreSQL accesible para los flujos que consultan datos.

## Arquitectura C4 y diagramas

CareStock es una **aplicación de escritorio JavaFX**: UI, controladores, servicios, modelos de dominio y DAO se ejecutan dentro del **mismo proceso Java**. PostgreSQL es el almacén de datos; no existe una API REST de CareStock ni un backend HTTP desplegado por separado.

La documentación usa el **modelo C4** para presentar la arquitectura de mayor a menor nivel de detalle. Los diagramas fueron elaborados en Visual Paradigm Online y se almacenan en `docs/diagramas/imagenes/`.

| Vista | Qué representa | Documentación |
|---|---|---|
| **Nivel 1 — Contexto** | Actores Administrador, Auxiliar y Farmacéutico, CareStock y proveedor externo de SMS | [Diagrama de contexto](docs/diagramas/imagenes/c4_nivel_1_contexto.jpg) |
| **Nivel 2 — Contenedores** | Aplicación JavaFX y PostgreSQL dentro del límite de CareStock; proveedor SMS externo | [Diagrama de contenedores](docs/diagramas/imagenes/c4_nivel_2_contenedores.jpg) |
| **Nivel 3 — Componentes** | Presentación, autenticación, Facade, Proxy, servicios, Factory Method, dominio/DTO, DAO y conexiones | [Diagrama y explicación técnica](docs/diagramas/DIAGRAMA_COMPONENTES_C4.md) |
| **Nivel 4 — Código** | Clases, interfaces y relaciones del código Java | [Documentación UML de clases](docs/diagramas/DIAGRAMA_CLASES_ACTUAL.md) |
| **Vista complementaria — Despliegue** | Equipo del usuario, ejecución Docker opcional y PostgreSQL/Neon | [Diagrama de despliegue C4](docs/diagramas/DIAGRAMA_DESPLIEGUE_C4.md) |

### Nivel 1 — Contexto del sistema

![C4 nivel 1: contexto del sistema CareStock](docs/diagramas/imagenes/c4_nivel_1_contexto.jpg)

Muestra quién utiliza CareStock y su relación con el proveedor de SMS. La entrega de mensajes al administrador representa el **escenario de notificaciones configurado**, no garantiza que todos los modos de envío estén habilitados por defecto.

### Nivel 2 — Contenedores

![C4 nivel 2: contenedores CareStock](docs/diagramas/imagenes/c4_nivel_2_contenedores.jpg)

El contenedor principal es **Aplicación CareStock** (Java 21 y JavaFX 21). Se comunica con **PostgreSQL 16 / Neon** mediante JDBC, usando HikariCP para gestionar conexiones. Neon describe un **modo de alojamiento** de PostgreSQL, no una API o servidor Java independiente.

### Nivel 3 — Componentes internos

![C4 nivel 3: componentes de la aplicación CareStock](docs/diagramas/imagenes/c4_nivel_3_componentes.jpg)

El nivel 3 hace zoom **exclusivamente sobre el contenedor de escritorio JavaFX**. PostgreSQL y el servicio de SMS permanecen fuera de ese límite. Los componentes representan **responsabilidades lógicas**, no microservicios. Para clases concretas, flujo de llamadas, reglas por farmacia y patrones GoF, consulta [la documentación detallada de componentes](docs/diagramas/DIAGRAMA_COMPONENTES_C4.md).

### Nivel 4 — Código y despliegue

No se elaboró un segundo diagrama de código: se reutiliza el **diagrama UML de clases** creado en Visual Paradigm como referencia del nivel 4, evitando duplicar documentación. También se documenta una vista complementaria de [despliegue C4](docs/diagramas/DIAGRAMA_DESPLIEGUE_C4.md) para ejecución local y Docker.

> **Nota:** la documentación C4 describe la arquitectura del sistema; **Docker no es sinónimo de «contenedor C4»**. Para que las imágenes anteriores se visualicen en GitHub, deben estar versionadas en las rutas relativas indicadas.

## Patrones de diseño y componentes

La arquitectura sigue una separación **MVC** entre interfaz, lógica de aplicación, dominio y persistencia. Los patrones GoF se emplean dentro del proceso JavaFX:

| Patrón GoF | Categoría | Implementación principal | Finalidad |
|---|---|---|---|
| **Factory Method** | Creacional | `FabricaProducto`, `FabricaMedicamento`, `FabricaAseo`, `FabricaMaternidad` | Construcción y validación de productos según su categoría |
| **Facade** | Estructural | `Inventario` | Centralizar operaciones del inventario y coordinar servicios |
| **Proxy** | Estructural | `ConsultaInventarioProxy` / `InventarioConsultas` | Consultas por farmacia, caché e invalidación |
| **Template Method** | Comportamiento | `CatalogoDAO` y DAO especializados | Reutilizar una secuencia común de persistencia |

El flujo general es **JavaFX/FXML → controladores → fachada/servicios → DAO → `ConexionBD`/HikariCP → PostgreSQL**. El sistema también incluye autenticación mediante BCrypt, estado de sesión, modelos de dominio y objetos DTO. Las fábricas permiten crear productos sin convertir los controladores en responsables de la persistencia.

La integración SMS dispone de un modo configurable y de un proceso `ProcesoSms` cuyo inicio requiere `SMS_WORKER_ENABLED=SI`; **no debe interpretarse como un servicio desplegado permanentemente en todas las instalaciones**. Más detalle en [componentes C4](docs/diagramas/DIAGRAMA_COMPONENTES_C4.md).

## Estructura del repositorio

CareStock organiza sus archivos siguiendo una arquitectura modular y por capas, separando la interfaz gráfica, la lógica de negocio, el acceso a datos, los modelos del dominio y los componentes de infraestructura.

El proyecto Maven principal se encuentra exclusivamente en `src/CareStock`. Esta organización permite centralizar las dependencias, facilitar la compilación y mantener una separación clara entre el código de la aplicación y los archivos de gestión del repositorio.

```text
FIS_2630_1204_G1/
│
├── .github/                              # Configuración de GitHub y automatización del proyecto
│   ├── ISSUE_TEMPLATE/                   # Plantillas para historias de usuario, bugs y tareas
│   └── workflows/                        # CI/CD, cobertura, calidad, Telegram y Releases
│       ├── ci.yml                        # Compilación y pruebas con Java 21 y Xvfb
│       ├── cd.yml                        # Construcción y publicación de JAR
│       ├── sonar.yml                     # Análisis SonarQube / Quality Gate
│       ├── pre-pr-coverage.yml           # Cobertura mínima del código nuevo (80 %)
│       ├── qa-metricas-calidad.yml       # LOC, complejidad ciclomática y anidamiento
│       ├── qa-sql-scripts.yml            # Comprobaciones de scripts e integración SQL/SMS
│       ├── telegram.yml                  # Avisos de actividad del repositorio
│       └── release-windows.yml           # Instalador y versión portable para Windows
│
├── conf/                                 # Archivos de configuración general y auxiliares del repositorio
│
├── docs/                                 # Documentación académica, técnica y funcional de CareStock
│   ├── architecture/                     # Decisiones y documentación arquitectónica
│   ├── diagramas/                         # Diagramas UML, C4 y modelos del sistema
│   │   └── imagenes/                     # Diagramas C4 exportados desde Visual Paradigm Online
│   ├── qa/                               # Planes de prueba, criterios de aceptación y evidencias de calidad
│   ├── scrum/                            # Planeación, seguimiento y retrospectivas de los sprints
│   └── user_guide/                       # Instrucciones de uso, configuración y ejecución de la aplicación
│
├── scripts/                              # Scripts auxiliares para desarrollo y mantenimiento del proyecto
│
├── src/                                  # Directorio que agrupa la aplicación y los recursos SQL
│   │
│   ├── CareStock/                        # Proyecto Maven oficial de la aplicación JavaFX
│   │   │
│   │   ├── .mvn/                         # Configuración interna del Maven Wrapper
│   │   │   └── wrapper/                  # Archivos para gestionar la versión de Maven utilizada
│   │   │
│   │   ├── config/                       # Configuración local externa al código Java
│   │   │   ├── conexion-local.properties.example
│   │   │   │                              # Plantilla de conexión sin credenciales reales
│   │   │   └── conexion-local.properties  # Credenciales locales de PostgreSQL/Neon
│   │   │                                  # Archivo privado: no debe versionarse en Git
│   │   │
│   │   ├── src/                          # Código fuente y pruebas del módulo Maven
│   │   │   │
│   │   │   ├── main/                     # Código principal y recursos de la aplicación
│   │   │   │   │
│   │   │   │   ├── java/                 # Clases Java y declaración del módulo JPMS
│   │   │   │   │   ├── module-info.java  # Dependencias, exports y opens del módulo Java
│   │   │   │   │   │
│   │   │   │   │   └── org/
│   │   │   │   │       └── example/
│   │   │   │   │           └── carestock/ # Paquete base de la aplicación
│   │   │   │   │               │
│   │   │   │   │               ├── config/
│   │   │   │   │               │   # Configuración de infraestructura, JDBC y HikariCP
│   │   │   │   │               │   # Administra conexiones hacia PostgreSQL/Neon
│   │   │   │   │               │
│   │   │   │   │               ├── controller/
│   │   │   │   │               │   # Controladores JavaFX vinculados a las vistas FXML
│   │   │   │   │               │   # Gestionan eventos, formularios y navegación
│   │   │   │   │               │   # Coordinan las interacciones con servicios y fachadas
│   │   │   │   │               │
│   │   │   │   │               ├── dao/
│   │   │   │   │               │   # Data Access Objects para persistencia y consultas SQL
│   │   │   │   │               │   # Contiene operaciones JDBC sobre tablas y registros
│   │   │   │   │               │   # Gestiona consultas, inserciones y actualizaciones
│   │   │   │   │               │
│   │   │   │   │               ├── dto/
│   │   │   │   │               │   # DTO para transportar información entre componentes
│   │   │   │   │               │   # Agrupa datos de catálogo, lotes e inventario
│   │   │   │   │               │   # Evita exponer estructuras de persistencia a las vistas
│   │   │   │   │               │
│   │   │   │   │               ├── exception/
│   │   │   │   │               │   # Excepciones personalizadas de CareStock
│   │   │   │   │               │   # Representa errores y violaciones de reglas de negocio
│   │   │   │   │               │
│   │   │   │   │               ├── factory/
│   │   │   │   │               │   # Fábricas de productos (Factory Method)
│   │   │   │   │               │
│   │   │   │   │               ├── facade/
│   │   │   │   │               │   # Fachadas de operaciones del dominio
│   │   │   │   │               │   # Simplifica la interacción entre controladores
│   │   │   │   │               │   # y la lógica de inventario y persistencia
│   │   │   │   │               │
│   │   │   │   │               ├── model/
│   │   │   │   │               │   # Entidades y objetos del dominio hospitalario
│   │   │   │   │               │   # Medicamentos, productos de aseo, maternidad y lotes
│   │   │   │   │               │   # Incluye entidades y validaciones del dominio
│   │   │   │   │               │
│   │   │   │   │               ├── proxy/
│   │   │   │   │               │   # Implementaciones del patrón Proxy y almacenamiento en caché
│   │   │   │   │               │   # Reutiliza resultados de consultas por farmacia
│   │   │   │   │               │   # Incluye políticas de expiración e invalidación
│   │   │   │   │               │
│   │   │   │   │               ├── service/
│   │   │   │   │               │   # Servicios de aplicación y reglas de negocio
│   │   │   │   │               │   # Coordina validaciones y operaciones del dominio
│   │   │   │   │               │
│   │   │   │   │               ├── sms/
│   │   │   │   │               │   # Notificaciones SMS y worker configurable
│   │   │   │   │               │
│   │   │   │   │               └── session/
│   │   │   │   │                   # Gestión del contexto de usuario autenticado
│   │   │   │   │                   # Conserva la identidad y farmacia activa
│   │   │   │   │                   # Facilita la segregación de operaciones por farmacia
│   │   │   │   │
│   │   │   │   └── resources/            # Recursos utilizados por la interfaz JavaFX
│   │   │   │                              # Archivos FXML, estilos CSS, imágenes y otros recursos
│   │   │   │
│   │   │   └── test/                     # Ubicación convencional de las pruebas Maven
│   │   │       └── java/                 # Pruebas unitarias e integración cuando estén implementadas
│   │   │
│   │   ├── .dockerignore                # Excluye archivos temporales y privados del contexto Docker
│   │   ├── Dockerfile                   # Construcción de la imagen de CareStock con Java 21 y JavaFX
│   │   ├── mvnw                         # Maven Wrapper para Linux, macOS y Git Bash
│   │   ├── mvnw.cmd                     # Maven Wrapper para Windows mediante CMD o PowerShell
│   │   └── pom.xml                      # Dependencias, plugins y configuración oficial de Maven
│   │
│   └── sql/                              # Scripts de inicialización y evolución de PostgreSQL
│       ├── ddl/                         # Definición y migración de esquemas, tablas e índices
│       ├── dml/                         # Inserción y actualización de datos iniciales
│       ├── procedures/                  # Procedimientos almacenados y operaciones SQL
│       └── test_data/                   # Datos destinados a escenarios de prueba
│
├── .gitignore                           # Reglas para excluir archivos generados y credenciales
├── .env.example                        # Referencia histórica de variables; no configura ConexionBD
├── compose.yaml                        # Orquestación de la ejecución opcional mediante Docker Compose
├── BOILERPLATE_template.md             # Plantilla de referencia para documentación o estructura
├── CHANGELOG.md                        # Registro histórico de modificaciones y versiones
├── LICENSE                             # Condiciones de licencia y uso del proyecto
└── README.md                           # Guía principal de arquitectura, instalación y ejecución
```

## Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/puj-course/FIS_2630_1204_G1.git
cd FIS_2630_1204_G1/src/CareStock
```

### 2. Crear la configuración local de PostgreSQL/Neon

La clase `ConexionBD` acepta **variables de entorno** o un **archivo de propiedades**. Si alguna de las variables `DB_URL`, `DB_USER` o `DB_PASSWORD` está definida, **las tres deben estar completas**: tienen prioridad sobre el archivo. Sin ellas, busca `conexion-local.properties` primero en `%APPDATA%\CareStock\` (si existe) y después en las rutas locales `config/` o `src/CareStock/config/`.

Desde `src/CareStock`:

```bash
cp config/conexion-local.properties.example config/conexion-local.properties
```

En PowerShell, el equivalente es:

```powershell
Copy-Item config/conexion-local.properties.example config/conexion-local.properties
```

Abre `config/conexion-local.properties` y completa tus valores locales:

```properties
# Archivo PRIVADO: no subir a Git
# Sustituir los siguientes valores por los de tu instancia.
db.url=jdbc:postgresql://HOST:5432/BASE_DE_DATOS?sslmode=require
db.user=USUARIO
db.password=TU_CONTRASENA_LOCAL

# Pool de conexiones HikariCP
db.pool.max=4
db.pool.min=0
```

- `db.url`, `db.user` y `db.password` son obligatorios.
- `db.pool.max` y `db.pool.min` son opcionales; en el código los valores predeterminados son `4` y `1`, respectivamente. La plantilla `.example` declara explícitamente `db.pool.min=0`, valor también permitido.
- El código valida que el máximo esté entre 1 y 10 y que el mínimo no exceda el máximo.
- **Nunca publiques el contenido real de este archivo**, ni lo adjuntes a Issues o Pull Requests.

### Alternativa: variables de entorno

En entornos donde prefieras no utilizar un archivo de propiedades, define conjuntamente `DB_URL`, `DB_USER` y `DB_PASSWORD`. No incluyas contraseñas reales en capturas, Issues ni commits. En PowerShell, para la sesión actual:

```powershell
$env:DB_URL = "jdbc:postgresql://HOST:5432/BASE_DE_DATOS?sslmode=require"
$env:DB_USER = "USUARIO"
$env:DB_PASSWORD = "TU_CONTRASENA_LOCAL"
```

Las variables completas sustituyen la configuración del archivo durante esa ejecución; si están incompletas, la aplicación devuelve un error de configuración.

### 3. Preparar la base de datos

La aplicación necesita que PostgreSQL contenga las tablas, relaciones, funciones y procedimientos correspondientes a la versión del proyecto. Consulta [Base de datos y scripts SQL](#base-de-datos-y-scripts-sql) antes de inicializar una instancia nueva.

## Compilación y ejecución

Ejecuta los comandos **desde `src/CareStock`**, no desde la raíz del repositorio.

### Git Bash / Linux / macOS

```bash
./mvnw clean verify
./mvnw javafx:run
```

### PowerShell (Windows)

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

`clean verify` limpia y compila el módulo, ejecutando además las verificaciones configuradas. `javafx:run` abre la aplicación JavaFX en el entorno de escritorio local.

> **Diferencia importante:** `BUILD SUCCESS` demuestra que la construcción terminó correctamente. Si Maven imprime `No tests to run`, no significa que se hayan ejecutado pruebas automatizadas.

### Ejecución en IntelliJ IDEA

1. Abre **`src/CareStock/pom.xml`** como proyecto Maven.
2. Configura el **Project SDK = JDK 21**.
3. Espera a que Maven descargue y sincronice las dependencias.
4. Comprueba que exista `config/conexion-local.properties`.
5. Ejecuta el objetivo Maven **`javafx:run`** o el punto de entrada `org.example.carestock.CareStockApp` con su configuración modular correspondiente.

## Ejecución con Docker

Docker es una alternativa de **construcción y comprobación en un entorno controlado**, no un requisito para usar CareStock en Windows.

Desde la **raíz del repositorio**:

```bash
docker compose config --quiet
docker compose build
docker compose up
```

La configuración Compose usa `src/CareStock` como contexto de construcción, y monta el archivo `src/CareStock/config/conexion-local.properties` en modo **solo lectura**. El archivo debe existir en el equipo anfitrión antes de iniciar el contenedor.

El contenedor usa **Xvfb**, un servidor gráfico virtual para JavaFX. Por tanto, la ventana puede ejecutarse dentro de Linux **sin mostrarse en el escritorio del anfitrión**. Para utilizar la interfaz de forma interactiva en Windows, se recomienda `./mvnw javafx:run` o `mvnw.cmd javafx:run`.

La ejecución de Docker no implica que exista despliegue productivo, acceso remoto a una GUI o distribución de un instalador.

## Base de datos y scripts SQL

Los archivos SQL están organizados principalmente en `src/sql/`:

```text
src/sql/
├── ddl/                 # Esquema y migraciones
├── dml/                 # Datos iniciales
├── procedures/          # Procedimientos almacenados
├── test_data/           # Datos y escenarios para pruebas
└── ...                  # Scripts auxiliares e históricos
```

Entre los scripts identificados se encuentran:

- `src/sql/ddl/01_schema_carestock.sql`
- `src/sql/ddl/02_functions_triggers.sql`
- `src/sql/ddl/04_multifarmacia_inventario.sql`
- `src/sql/ddl/06_crud_inventario_multifarmacia.sql`
- `src/sql/ddl/08_performance_indexes.sql`
- `src/sql/dml/01_seed_roles_permisos.sql`
- `src/sql/dml/02_seed_catalogo_base.sql`
- `src/sql/procedures/01_sp_ingreso_lotes.sql`

**No ejecutes todos los scripts indiscriminadamente ni asumas que cada uno es idempotente.** Verifica primero el esquema existente y el orden de migración aplicable; algunas migraciones dependen de objetos creados previamente o de ajustes realizados en otros sprints. No se debe ejecutar un *seed* de prueba sobre una base de datos con información real sin revisar su contenido.

## Pruebas y estado de calidad

CareStock utiliza **JUnit 5**, **Maven Surefire**, **JaCoCo** y análisis **SonarQube/SonarCloud**. Las pruebas de interfaz JavaFX en automatización se ejecutan bajo una pantalla virtual **Xvfb**.

En una ejecución local compartida por el equipo el **9 de noviembre de 2026** se registraron **1.164 pruebas, 0 fallos, 0 errores, 0 omitidas** y `BUILD SUCCESS`. Esta cifra es una **evidencia histórica**, no una garantía del estado de cualquier rama posterior: se debe repetir la verificación tras integrar cambios.

### Ejecutar pruebas y cobertura

Desde `src/CareStock`:

```bash
./mvnw clean verify
./mvnw jacoco:report
```

Los resultados se consultan en:

- `target/surefire-reports/` — informes de pruebas automatizadas.
- `target/site/jacoco/index.html` — cobertura local, cuando se ha generado el informe.
- GitHub Actions — evidencias y estado de los workflows del commit.

### Quality Gate y métricas

- El workflow **`pre-pr-coverage.yml`** compara el código Java nuevo frente a `main` en *pushes* a ramas distintas de `main` y exige **≥ 80 %** de cobertura estimada (líneas y ramas). Si un cambio no modifica código Java ejecutable, este control no aplica.
- **`sonar.yml`** ejecuta pruebas, produce el XML JaCoCo y solicita al servidor SonarQube el resultado del **Quality Gate**; el análisis depende de las variables y secretos de GitHub configurados.
- **`qa-metricas-calidad.yml`** calcula métricas estáticas de **longitud de código (LOC), complejidad ciclomática y profundidad de anidamiento**.
- Estas medidas de **código nuevo** no deben confundirse con la cobertura global del proyecto.

> **Importante:** `BUILD SUCCESS` acredita la ejecución satisfactoria del comando mostrado; para afirmar el número de pruebas efectivamente ejecutadas, consulta el resumen de Surefire. La aprobación del Quality Gate debe verificarse para el commit y la rama correspondientes.

### Verificación funcional recomendada

Además de las pruebas automatizadas, valida con una base apropiada de pruebas los flujos de autenticación, farmacia activa, catálogo, lotes, movimientos, dispensación, alertas, reposición y cualquier modalidad SMS habilitada. Las condiciones de seguridad, segregación de farmacias y consistencia de inventario deben verificarse con datos controlados.

## Integración y entrega continuas

La automatización se define en `.github/workflows/` y utiliza GitHub Actions:

| Workflow | Alcance principal |
|---|---|
| [`ci.yml`](.github/workflows/ci.yml) | Compilación, pruebas JavaFX con Xvfb y artefactos Maven/JUnit/JaCoCo |
| [`cd.yml`](.github/workflows/cd.yml) | Construye y publica el JAR como artefacto de CI; no despliega una API web |
| [`sonar.yml`](.github/workflows/sonar.yml) | Análisis de calidad y espera del Quality Gate de SonarQube |
| [`pre-pr-coverage.yml`](.github/workflows/pre-pr-coverage.yml) | Verificación previa de cobertura de nuevo código Java (≥ 80 %) |
| [`qa-metricas-calidad.yml`](.github/workflows/qa-metricas-calidad.yml) | LOC, complejidad ciclomática y anidamiento |
| [`qa-sql-scripts.yml`](.github/workflows/qa-sql-scripts.yml) | Validación de scripts y pruebas de integración con PostgreSQL |
| [`telegram.yml`](.github/workflows/telegram.yml) | Notificaciones de actividad y resultados al grupo de Telegram, si existen los secretos |
| [`release-windows.yml`](.github/workflows/release-windows.yml) | Prepara instalador y versión portable al publicar una Release válida |

La ejecución y los requisitos de cada workflow dependen del tipo de evento, la rama y la configuración de secretos. **Publicar un JAR en Actions no equivale a desplegar CareStock en producción.** La distribución de escritorio se trata por separado en [Distribución para Windows](#distribución-para-windows).

## Seguridad y buenas prácticas

- Nunca añadas credenciales reales a código fuente, README, capturas o registros de CI.
- Excluye `src/CareStock/config/conexion-local.properties` del control de versiones. El archivo `.example` debe contener **solo valores ficticios**.
- Si una credencial real se publicó previamente en Git o en un mensaje, **rótala en Neon/PostgreSQL**; borrarla del último commit no la retira automáticamente del historial.
- Respeta el contexto de la farmacia autenticada en consultas y escrituras.
- Invalida o actualiza las entradas del caché después de operaciones que cambien las existencias.
- No confundas compilar, ejecutar pruebas unitarias y validar la aplicación con una base de datos real: son verificaciones diferentes.
- No incluyas `target/`, `.idea/`, archivos `.class` ni configuraciones privadas en los commits.

## Equipo y documentación

### Equipo — Grupo 1

| Integrante | Rol | Perfil |
|---|---|---|
| Mateo Salazar Bogotá | Product Owner | [@Mateosalazar543](https://github.com/Mateosalazar543) |
| Laura Sofía Ortiz Gómez | Scrum Master | [@Lau1216](https://github.com/Lau1216) |
| Valentina Carrillo Peñuela | Diseñadora UI/UX | [@valcarrpe](https://github.com/valcarrpe) |
| Alejandro Rodríguez Molina | Desarrollador Backend | [@alejormolina3](https://github.com/alejormolina3) |
| Santiago Cadena Goyeneche | Ingeniero de Datos | [@Jsanti13](https://github.com/Jsanti13) |

### Recursos del proyecto

- [Repositorio GitHub](https://github.com/puj-course/FIS_2630_1204_G1)
- [Issues / backlog](https://github.com/puj-course/FIS_2630_1204_G1/issues)
- [Pull Requests](https://github.com/puj-course/FIS_2630_1204_G1/pulls)
- [GitHub Actions](https://github.com/puj-course/FIS_2630_1204_G1/actions)
- [`docs/`](docs/) — arquitectura, pruebas, Scrum y manuales
- [Diagramas C4 de contexto, contenedores y componentes](#arquitectura-c4-y-diagramas)
- [Documento técnico C4 nivel 3](docs/diagramas/DIAGRAMA_COMPONENTES_C4.md)
- [UML de clases](docs/diagramas/DIAGRAMA_CLASES_ACTUAL.md)
- [Despliegue C4](docs/diagramas/DIAGRAMA_DESPLIEGUE_C4.md)
- [`docs/user_guide/catalogo-productos.md`](docs/user_guide/catalogo-productos.md) — guía de uso del catálogo unificado de productos
- [`CHANGELOG.md`](CHANGELOG.md) — historial de cambios
- [`LICENSE`](LICENSE) — licencia del proyecto (MIT)

**Contexto académico:** Fundamentos de Ingeniería de Software, Pontificia Universidad Javeriana, Grupo `FIS_2630_1204_G1`.

---

**CareStock** — Desarrollo académico orientado a gestionar inventarios por farmacia con trazabilidad, modularidad y evolución incremental.

---

## Versiones y Releases

CareStock utiliza etiquetas de **versionado semántico** (`MAJOR.MINOR.PATCH`), con sufijos de prepublicación como `-beta.1` cuando correspondan.

La versión académica preliminar mencionada históricamente es **`v0.1.0-beta.1`**. La disponibilidad real de una versión y sus archivos se comprueba en [GitHub Releases](https://github.com/puj-course/FIS_2630_1204_G1/releases).

Para generar una distribución Windows desde una Release, el workflow exige que la etiqueta empiece por `v` y que **la versión del tag sin la `v` coincida exactamente con `src/CareStock/pom.xml`**. Por ejemplo, `v1.0.0` requiere `<version>1.0.0</version>` en el POM del commit etiquetado. Un proyecto cuyo POM todavía indica `1.0-SNAPSHOT` no cumple esa condición para ese tag.

Las versiones beta continúan siendo artefactos académicos; la publicación del código fuente o de un instalador no implica certificación de uso hospitalario.

## Distribución para Windows

El workflow [`release-windows.yml`](.github/workflows/release-windows.yml) se ejecuta al **publicar una GitHub Release con una etiqueta válida** (`v...`). Construye la aplicación para Windows mediante `jpackage` y, si todas las etapas terminan correctamente, adjunta:

- `CareStock-Setup-<versión>.exe` — instalador Windows.
- `CareStock-Portable-<versión>.zip` — paquete portable.

Los paquetes generados por `jpackage` incluyen un entorno de ejecución Java, por lo que el usuario final no tiene que instalar Maven ni un JDK por separado. **No se debe afirmar que los archivos existen para una versión concreta sin comprobar esa Release.**

### Configuración de la base de datos instalada

Las credenciales **no se incluyen en el instalador**. En Windows, la aplicación busca primero las variables `DB_URL`, `DB_USER` y `DB_PASSWORD` si están presentes y completas, o bien el archivo:

```text
%APPDATA%\CareStock\conexion-local.properties
```

Como referencia se puede copiar `src/CareStock/config/conexion-local.properties.example` y configurar `db.url`, `db.user` y `db.password`. El archivo real debe mantenerse fuera del repositorio.

### Flujo para publicar

1. Establecer la versión definitiva en `src/CareStock/pom.xml`.
2. Integrar y validar los cambios en la rama de publicación.
3. Etiquetar el commit con `v` seguida de **la misma versión del POM**.
4. Publicar la Release o prerelease en GitHub.
5. Confirmar el resultado del workflow y verificar los archivos adjuntos a la Release.

### Limitaciones

- La distribución se orienta inicialmente a **Windows x64**.
- El instalador se distribuye inicialmente **sin firma digital**.
- El empaquetado requiere validación en un equipo Windows limpio.
- La generación automática de paquetes **no implica certificación para producción**.

### Dedicatoria

La verdad, nunca pensé que terminaría escribiendo estas palabras. Sin embargo, después de más de catorce semanas desempeñándome como Product Owner de CareStock, siento que es importante detenerme un momento para expresar mi agradecimiento a todas las personas que hicieron parte de este proyecto.

Quiero agradecer de corazón a cada integrante del equipo por su esfuerzo, dedicación, compromiso y, sobre todo, por su disposición para aprender y mejorar constantemente.

No es un secreto que durante nuestros primeros sprints enfrentamos dificultades. Apenas estábamos aprendiendo a trabajar bajo la metodología Scrum, a organizarnos como equipo y a gestionar adecuadamente un repositorio en GitHub. Cometimos errores, tuvimos desacuerdos y encontramos obstáculos que, en su momento, parecían difíciles de superar. Sin embargo, cada una de esas experiencias nos permitió crecer, tanto individualmente como equipo.

Hoy, al mirar atrás, me doy cuenta de cuánto hemos avanzado. CareStock no representa únicamente líneas de código, diagramas, historias de usuario o entregas académicas. Representa semanas de esfuerzo, aprendizaje, paciencia y trabajo compartido. Es el resultado de un grupo de personas que, a pesar de las dificultades, continuó adelante con el propósito de construir algo cada vez mejor.

Por eso, quiero agradecer sinceramente a todos mis compañeros por su tiempo, sus aportes y por haber sido parte de este proceso. Más allá del resultado final o de la calificación que podamos obtener, me quedo con todo lo que aprendimos y con la satisfacción de ver hasta dónde hemos llegado juntos.

**Gracias por estas catorce semanas de trabajo, por cada reto superado y por hacer de CareStock mucho más que un proyecto universitario.**

*Con gratitud,*

**Mateo Salazar Bogotá**  
*Product Owner — CareStock*
