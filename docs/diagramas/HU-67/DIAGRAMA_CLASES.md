# HU-67 — Diagrama de Clases

## Objetivo

Representar las clases involucradas en el filtrado automático del inventario según la farmacia asignada al usuario autenticado.

```mermaid
classDiagram

    class UserSession {
        -CurrentUser currentUser
        -UserSession instance
        +getInstance() UserSession
        +getCurrentUser() CurrentUser
        +isLoggedIn() boolean
        +clearSession() void
    }

    class CurrentUser {
        -int idUsuario
        -String nombre
        -String email
        -String rol
        -Integer idFarmacia
        +getIdUsuario() int
        +getNombre() String
        +getRol() String
        +getIdFarmacia() Integer
        +tieneFarmaciaAsignada() boolean
    }

    class Farmacia {
        -int idFarmacia
        -String codigo
        -String nombre
        -String estado
        +getIdFarmacia() int
        +getNombre() String
        +isActiva() boolean
    }

    class MainDashboardFX {
        -TableView~Medicamento~ tablaInventario
        -MedicamentoDAO medicamentoDAO
        +start(Stage) void
        +cargarDatosDesdeBD() void
        -mostrarInventarioVacio() void
    }

    class MedicamentoDAO {
        +obtenerPorFarmacia(int idFarmacia) List~Medicamento~
        +obtenerTotalStockPorFarmacia(int idFarmacia) int
        +obtenerAlertasPorFarmacia(int idFarmacia) int
    }

    class Medicamento {
        -int idMedicamento
        -String nombre
        -String principioActivo
        -String presentacion
        +getIdMedicamento() int
        +getNombre() String
    }

    class Lote {
        -int idLote
        -String numeroLote
        -int cantidad
        -LocalDate fechaVencimiento
        -int idFarmacia
        +getCantidad() int
        +getFechaVencimiento() LocalDate
    }

    class DatabaseConfig {
        +getConnection() Connection
    }

    UserSession "1" *-- "0..1" CurrentUser : mantiene

    CurrentUser "0..*" --> "0..1" Farmacia : asignado a

    MainDashboardFX ..> UserSession : consulta sesión

    MainDashboardFX ..> MedicamentoDAO : solicita inventario

    MedicamentoDAO ..> DatabaseConfig : obtiene conexión

    MedicamentoDAO ..> Medicamento : construye

    Farmacia "1" --> "0..*" Lote : contiene

    Medicamento "1" --> "0..*" Lote : posee
```

## Relaciones principales

**UserSession — CurrentUser**

Existe una relación de composición porque la sesión mantiene el contexto del usuario autenticado.

**CurrentUser — Farmacia**

El usuario puede tener una farmacia asignada. El `idFarmacia` del usuario determina los datos a los cuales puede acceder.

**MainDashboardFX — UserSession**

La vista obtiene el contexto del usuario activo antes de consultar el inventario.

**MainDashboardFX — MedicamentoDAO**

La vista/controlador solicita los registros correspondientes a la farmacia activa.

**MedicamentoDAO — DatabaseConfig**

El DAO obtiene una conexión JDBC para realizar la consulta en PostgreSQL.

**Farmacia — Lote**

Una farmacia puede almacenar cero o múltiples lotes.

**Medicamento — Lote**

Un medicamento puede estar asociado a múltiples lotes.
