# CareStock

**Gestión y trazabilidad de inventario hospitalario por farmacia**

CareStock es una aplicación académica de escritorio desarrollada en **Java 21 y JavaFX 21** para administrar productos e inventario en farmacias. Utiliza **PostgreSQL (Neon)** mediante **JDBC**, con un pool de conexiones **HikariCP**, autenticación de usuarios, contexto de sesión y consultas de inventario segregadas por farmacia.

El catálogo contempla **medicamentos, productos de aseo y productos de maternidad**. El proyecto incorpora componentes de los patrones de diseño **Builder, Facade y Proxy**, además de mecanismos de caché para consultas.

> **Estado del proyecto:** prototipo académico en evolución. La compilación Maven se ha verificado localmente; no todas las funcionalidades, flujos ni pruebas están certificados para uso productivo hospitalario. El `.jar` generado es un artefacto de compilación y no se presenta como instalador autónomo.

## Contenido

- [Funcionalidades](#funcionalidades)
- [Tecnologías](#tecnologías)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Instalación y configuración](#instalación-y-configuración)
- [Compilación y ejecución](#compilación-y-ejecución)
- [Ejecución con Docker](#ejecución-con-docker)
- [Base de datos y scripts SQL](#base-de-datos-y-scripts-sql)
- [Pruebas y estado de calidad](#pruebas-y-estado-de-calidad)
- [Integración y entrega continuas](#integración-y-entrega-continuas)
- [Seguridad y buenas prácticas](#seguridad-y-buenas-prácticas)
- [Equipo y documentación](#equipo-y-documentación)

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
- Modelos y constructores (*builders*) para representar productos con diferentes atributos.

### Inventario y lotes

- Visualización de lotes y sus datos de identificación, cantidades, vencimientos y estado.
- Consulta unificada de existencias de medicamentos, aseo y maternidad, asociadas a la farmacia activa.
- Filtros de búsqueda y categoría en la consulta de inventario.
- Componentes de lógica de dominio y persistencia para registro y seguimiento de lotes y movimientos.
- Infraestructura SQL para inventario y segmentación por farmacia.

> **Alcance:** la presencia de una clase, procedimiento SQL o historia de usuario no garantiza, por sí sola, que todos los escenarios de negocio estén conectados y validados desde la interfaz. Cada flujo debe comprobarse con sus criterios de aceptación.

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
| Patrones utilizados en el código | Builder, Facade, Proxy |
| Pruebas (dependencias) | JUnit 5 / Maven Surefire |
| CI/CD | GitHub Actions |
| Contenerización opcional | Docker / Docker Compose |

**CareStock no requiere Spring Boot, Oracle ni Python para ejecutar su aplicación JavaFX.** Docker es opcional para el uso local; sí se necesita una base PostgreSQL accesible para los flujos que consultan datos.

## Estructura del repositorio

CareStock organiza sus archivos siguiendo una arquitectura modular y por capas, separando la interfaz gráfica, la lógica de negocio, el acceso a datos, los modelos del dominio y los componentes de infraestructura.

El proyecto Maven principal se encuentra exclusivamente en `src/CareStock`. Esta organización permite centralizar las dependencias, facilitar la compilación y mantener una separación clara entre el código de la aplicación y los archivos de gestión del repositorio.

```text
FIS_2630_1204_G1/
│
├── .github/                              # Configuración de GitHub y automatización del proyecto
│   ├── ISSUE_TEMPLATE/                   # Plantillas para historias de usuario, bugs y tareas
│   └── workflows/                        # Pipelines de integración y entrega continua
│       ├── ci.yml                        # Compilación y verificación automática con Maven y Java 21
│       └── cd.yml                        # Empaquetado y publicación de artefactos mediante GitHub Actions
│
├── conf/                                 # Archivos de configuración general y auxiliares del repositorio
│
├── docs/                                 # Documentación académica, técnica y funcional de CareStock
│   ├── architecture/                     # Diseño arquitectónico, diagramas UML y modelos del sistema
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
│   │   │   │   │               ├── DataTransferObject/
│   │   │   │   │               │   # DTO para transportar información entre componentes
│   │   │   │   │               │   # Agrupa datos de catálogo, lotes e inventario
│   │   │   │   │               │   # Evita exponer estructuras de persistencia a las vistas
│   │   │   │   │               │
│   │   │   │   │               ├── exception/
│   │   │   │   │               │   # Excepciones personalizadas de CareStock
│   │   │   │   │               │   # Representa errores y violaciones de reglas de negocio
│   │   │   │   │               │
│   │   │   │   │               ├── facade/
│   │   │   │   │               │   # Fachadas de operaciones del dominio
│   │   │   │   │               │   # Simplifica la interacción entre controladores
│   │   │   │   │               │   # y la lógica de inventario y persistencia
│   │   │   │   │               │
│   │   │   │   │               ├── model/
│   │   │   │   │               │   # Entidades y objetos del dominio hospitalario
│   │   │   │   │               │   # Medicamentos, productos de aseo, maternidad y lotes
│   │   │   │   │               │   # Incluye validaciones y constructores Builder
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

La clase `ConexionBD` lee **`config/conexion-local.properties`**, relativa al directorio de ejecución del proyecto. La configuración de la base de datos **no se toma de `DB_URL`, `DB_USER` o `DB_PASSWORD`** en esta implementación.

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
- `db.pool.max` y `db.pool.min` son opcionales; los valores predeterminados son `4` y `0` respectivamente.
- El código valida que el máximo esté entre 1 y 10 y que el mínimo no exceda el máximo.
- **Nunca publiques el contenido real de este archivo**, ni lo adjuntes a Issues o Pull Requests.

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

El proyecto declara dependencias de **JUnit 5** y utiliza **Maven Surefire**. Sin embargo, la última compilación compartida del proyecto indicó:

```text
[INFO] Compiling 52 source files
[INFO] No tests to run.
[INFO] BUILD SUCCESS
```

Por lo tanto:

- **Compilación:** comprobada localmente para la versión integrada con Java 21.
- **Pruebas automatizadas ejecutadas:** no demostradas por ese resultado de Maven.
- **Validación funcional:** comprobar manualmente el inicio de sesión, la farmacia activa, el catálogo, los lotes, la consulta unificada y las operaciones que modifican existencias.
- **Validación concurrente y de caché:** requiere pruebas específicas de aislamiento por farmacia, expiración, invalidación y consistencia tras escrituras.

Persisten advertencias no bloqueantes observadas en el registro compartido: módulo automático `jbcrypt-0.4.jar` y uso de API obsoleta en `PruebaConexion.java`.

### Comprobaciones recomendadas

```bash
# Desde src/CareStock
./mvnw clean verify

# Desde la raíz del repositorio, con Docker disponible
docker compose config --quiet
docker compose build
```

Antes de abrir un Pull Request, revisa los criterios de aceptación de la historia de usuario y registra las evidencias realmente obtenidas; no marques pruebas no ejecutadas como superadas.

## Integración y entrega continuas

Los flujos de GitHub Actions se encuentran en `.github/workflows/`:

- **`ci.yml`:** prepara Java 21 y ejecuta `./mvnw -B clean verify` desde `src/CareStock` en los eventos configurados.
- **`cd.yml`:** ejecuta `./mvnw -B clean package` y publica los JAR construidos como artefactos de GitHub Actions.

La publicación de un JAR como artefacto **no equivale a desplegar la aplicación en un servidor**, ni garantiza que el JAR pueda ejecutarse sin el entorno JavaFX correspondiente.

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
- [`CHANGELOG.md`](CHANGELOG.md) — historial de cambios
- [`LICENSE`](LICENSE) — licencia del proyecto (MIT)

**Contexto académico:** Fundamentos de Ingeniería de Software, Pontificia Universidad Javeriana, Grupo `FIS_2630_1204_G1`.

---

**CareStock** — Desarrollo académico orientado a gestionar inventarios por farmacia con trazabilidad, modularidad y evolución incremental.
