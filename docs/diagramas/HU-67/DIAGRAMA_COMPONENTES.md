# HU-67 — Diagrama de Componentes

## Objetivo

Representar los componentes involucrados en la consulta y visualización segregada del inventario por farmacia.

```mermaid
flowchart LR

    ACTOR["👤
    Administrador /
    Farmacéutico"]

    subgraph CARESTOCK["CareStock"]

        UI["<<component>>
        Presentación JavaFX
        MainDashboardFX"]

        SESSION["<<component>>
        Gestión de Sesión
        UserSession"]

        INVENTORY["<<component>>
        Consulta de Inventario
        MedicamentoDAO / LoteDAO"]

        CONFIG["<<component>>
        Acceso a Datos
        DatabaseConfig"]

    end

    DB[("<<external service>>
    Neon PostgreSQL")]

    ACTOR -->|"Consulta inventario"| UI

    UI -->|"IContextoSesion
    getCurrentUser()"| SESSION

    SESSION -->|"idFarmacia"| UI

    UI -->|"IConsultaInventario
    obtenerPorFarmacia(idFarmacia)"| INVENTORY

    INVENTORY -->|"IConexionBD"| CONFIG

    CONFIG -->|"JDBC / TLS"| DB

    DB -->|"Registros
    WHERE id_farmacia = ?"| INVENTORY

    INVENTORY -->|"List<Medicamento>"| UI

    UI -->|"TableView filtrada"| ACTOR
```

## Interfaces

### IContextoSesion

Provista por el componente de gestión de sesión.

Permite obtener:

```text
CurrentUser
idUsuario
rol
idFarmacia
```

### IConsultaInventario

Provista por el componente de persistencia del inventario.

Operación principal:

```text
obtenerPorFarmacia(idFarmacia)
```

### IConexionBD

Utilizada por los DAO para establecer comunicación con PostgreSQL mediante JDBC.

## Responsabilidad por componente

### Presentación JavaFX

Responsable de mostrar la tabla de inventario y manejar el estado vacío.

### Gestión de Sesión

Mantiene el usuario autenticado y su contexto de farmacia.

### Consulta de Inventario

Ejecuta las consultas de lectura restringidas por `id_farmacia`.

### Acceso a Datos

Administra la conexión JDBC con la base de datos.

### Neon PostgreSQL

Almacena la información persistente correspondiente a usuarios, farmacias, medicamentos, lotes e inventario.
