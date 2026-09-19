# CareStock

CareStock es un proyecto académico para la gestión y trazabilidad de inventario de medicamentos. La versión actual integra una interfaz de escritorio JavaFX con PostgreSQL/Neon mediante JDBC y contiene el flujo de catálogo de medicamentos e ingreso de lotes.

## Equipo del proyecto — Grupo 1

|Nombre|Rol|GitHub / Perfil|
|-|-|-|
|Mateo Salazar Bogotá|Product Owner|@Mateosalazar543|
|Laura Sofía Ortiz Gómez|Scrum Master|@Lau1216|
|Valentina Carrillo Peñuela|Diseñadora UI/UX|@valcarrpe|
|Alejandro Rodríguez Molina|Desarrollador Backend|@alejormolina3|
|Santiago Cadena Goyeneche|Ingeniero de Datos|@Jsanti13|

## Estado funcional actual

* Dashboard JavaFX conectado a PostgreSQL.
* Consulta del catálogo de medicamentos.
* Registro de medicamentos.
* Filtro de medicamentos con stock crítico.
* Métrica de lotes próximos a vencer en 30 días.
* Registro de un nuevo lote mediante FXML.
* Validación de medicamento, número de lote, cantidad, vencimiento y ubicación.
* Uso del procedimiento `sp\_registrar\_nuevo\_lote` para registrar el lote y su trazabilidad.
* Actualización automática de `MEDICAMENTOS.stock\_total` mediante trigger SQL.
* Pruebas unitarias para las reglas principales del ingreso de lotes.

> El proyecto actual no depende de Spring Boot, Oracle, Python ni Docker para compilar o ejecutar la aplicación JavaFX.

## Estructura principal

```text
```text
FIS_2630_1204_G1/
├── .github/                     # Configuración de GitHub, plantillas de Issues/PRs y pipelines CI/CD
│   ├── ISSUE_TEMPLATE/          # Plantillas para bugs, nuevas funcionalidades e historias de usuario
│   └── workflows/               # Automatización de integración continua y despliegue
│
├── .mvn/
│   └── wrapper/                 # Configuración de Maven Wrapper para ejecutar Maven sin instalación global
│
├── conf/                        # Archivos de configuración general de la aplicación
│
├── docs/                        # Documentación técnica, arquitectónica y de gestión del proyecto
│   ├── architecture/            # Diagramas de arquitectura, despliegue, modelo ER y documentación técnica
│   ├── qa/                      # Pruebas de aceptación y documentación de calidad
│   ├── scrum/                   # Evidencias de Sprint Planning, Review y Retrospective
│   └── user_guide/              # Manuales de usuario e instrucciones de despliegue local
│
├── scripts/                     # Scripts para compilación, pruebas, ejecución y despliegue
│
├── src/                         # Código fuente, pruebas y scripts de base de datos
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── carestock/   # Código Java organizado por capas
│   │   │           ├── config/      # Configuración de conexión y servicios generales
│   │   │           ├── controller/  # Controladores de las interfaces JavaFX
│   │   │           ├── dao/         # Acceso y persistencia de datos
│   │   │           ├── model/       # Entidades y modelos del dominio
│   │   │           ├── service/     # Lógica de negocio
│   │   │           ├── utils/       # Validadores y utilidades
│   │   │           └── view/        # Componentes y vistas JavaFX
│   │   │
│   │   └── resources/           # Archivos FXML y recursos utilizados por JavaFX
│   │
│   ├── test/                    # Pruebas unitarias automatizadas con JUnit
│   │
│   └── sql/                     # Scripts relacionados con PostgreSQL / Neon DB
│       ├── ddl/                 # Definición de tablas y estructura de base de datos
│       ├── dml/                 # Inserción y manipulación de datos
│       ├── procedures/          # Procedimientos almacenados y lógica SQL
│       └── test_data/           # Datos destinados a pruebas
│
├── .env.example                 # Plantilla de variables de entorno requeridas por CareStock
├── .gitignore                   # Archivos y directorios excluidos del repositorio
├── BOILERPLATE_template.md      # Plantilla de referencia para la estructura del proyecto
├── CHANGELOG.md                 # Historial de cambios y versiones
├── LICENSE                      # Licencia del proyecto
├── mvnw                         # Maven Wrapper para Linux, macOS y Git Bash
├── mvnw.cmd                     # Maven Wrapper para Windows
├── pom.xml                      # Dependencias, plugins y configuración de Maven
└── README.md                    # Documentación principal del proyecto
```
## Tecnologías
* Java 17
* JavaFX 21
* FXML
* Maven
* JDBC
* PostgreSQL / Neon
* JUnit 5
* GitHub Actions

## Arquitectura del ingreso de lotes

```text
MainDashboardFX
      |
      v
IngresoLote.fxml
      |
      v
IngresoLoteController
      |
      +--> MedicamentoDAO ------> MEDICAMENTOS
      +--> UbicacionDAO --------> UBICACIONES
      |
      v
IngresoLoteValidator
      |
      v
IngresoLoteService
      |
      +--> UsuarioDAO ----------> USUARIOS
      +--> LoteDAO
              |
              v
      sp\_registrar\_nuevo\_lote
              |
              +--> LOTES
              +--> LOG\_MOVIMIENTOS
              |
              v
      trg\_actualizar\_stock\_total
              |
              v
         MEDICAMENTOS
```

## Requisitos

1. JDK 17 o superior.
2. Maven 3.9 o superior.
3. Una base PostgreSQL con el esquema CareStock.
4. Variables de entorno de conexión.

## Configuración de la base de datos

No se almacenan contraseñas dentro del código. Use `.env.example` únicamente como referencia y defina estas variables en su sistema:

```text
DB\_URL=jdbc:postgresql://HOST:5432/neondb?sslmode=require
DB\_USER=usuario\_postgresql
DB\_PASSWORD=contraseña\_postgresql
CARESTOCK\_USER\_EMAIL=admin@carestock.com
```

`CARESTOCK\_USER\_EMAIL` debe corresponder a un registro de `USUARIOS` con estado `ACTIVO`; se utiliza para la trazabilidad de los movimientos mientras no exista un módulo de autenticación completo.

### PowerShell (Windows)

```powershell
$env:DB\_URL="jdbc:postgresql://HOST:5432/neondb?sslmode=require"
$env:DB\_USER="usuario\_postgresql"
$env:DB\_PASSWORD="contraseña\_postgresql"
$env:CARESTOCK\_USER\_EMAIL="admin@carestock.com"
```

### Bash (Linux/macOS/Git Bash)

```bash
export DB\_URL='jdbc:postgresql://HOST:5432/neondb?sslmode=require'
export DB\_USER='usuario\_postgresql'
export DB\_PASSWORD='contraseña\_postgresql'
export CARESTOCK\_USER\_EMAIL='admin@carestock.com'
```

## Preparar la base de datos

Ejecute los scripts SQL en este orden cuando cree una base nueva:

```text
src/sql/ddl/01\_schema\_carestock.sql
src/sql/ddl/02\_functions\_triggers.sql
src/sql/dml/01\_seed\_roles\_permisos.sql
src/sql/dml/02\_seed\_catalogo\_base.sql
src/sql/DatosPrueba.sql
src/sql/procedures/01\_sp\_ingreso\_lotes.sql
```

Si la base existente ya contiene las migraciones adicionales de `MEDICAMENTOS`, el DAO detecta las columnas `presentacion` e `id\_usuario\_creacion` y las completa al insertar.

## Compilar y probar

```bash
mvn clean verify
```

## Ejecutar la interfaz JavaFX

```bash
mvn javafx:run
```

En Windows también puede usar:

```powershell
.\\scripts\\run.ps1
```

## Flujo de ingreso de lote

1. Abra CareStock.
2. Pulse **Ingresar lote**.
3. Seleccione un medicamento activo.
4. Digite el número de lote y la cantidad.
5. Seleccione una fecha de vencimiento futura.
6. Seleccione una ubicación física.
7. Pulse **Guardar lote**.
8. La aplicación invoca el procedimiento almacenado, registra la trazabilidad y el trigger recalcula el stock total.

## Seguridad

* No suba `.env` ni credenciales reales a GitHub.
* Si una contraseña estuvo previamente publicada en el repositorio o en su historial Git, debe rotarse en Neon/PostgreSQL.
* El ZIP preparado para distribución no incluye la carpeta `.git` del equipo local.

## CI/CD

El workflow `ci.yml` ejecuta:

```bash
mvn -B clean verify
```

El workflow `cd.yml` empaqueta el proyecto con Maven y publica el `.jar` generado como artefacto de GitHub Actions.

## Contexto académico

* Asignatura: Fundamentos de Ingeniería de Software
* Pontificia Universidad Javeriana
* Grupo: FIS\_2630\_1204\_G1

