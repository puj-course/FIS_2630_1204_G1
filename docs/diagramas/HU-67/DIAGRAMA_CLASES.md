# HU-67 — Diagrama de Clases

## Objetivo

Representar los actores conceptuales y las clases involucradas en el filtrado automático del inventario según la farmacia asignada al usuario autenticado.

---

## 1. Actores involucrados

En CareStock se identifican los siguientes roles de usuario:

- Super Administrador.
- Administrador.
- Farmacéutico.

Para la HU-67 los actores directamente involucrados son **Administrador** y **Farmacéutico**, debido a que ambos operan dentro del contexto de una farmacia asignada.

```mermaid
classDiagram

    class UsuarioSistema {
        <<actor>>
    }

    class SuperAdministrador {
        <<actor>>
    }

    class Administrador {
        <<actor>>
    }

    class Farmaceutico {
        <<actor>>
    }

    UsuarioSistema <|-- SuperAdministrador
    UsuarioSistema <|-- Administrador
    UsuarioSistema <|-- Farmaceutico
```

> Esta generalización representa los roles que interactúan con CareStock.
> No implica necesariamente la existencia de clases Java independientes para cada rol.
> En la implementación actual, el rol se mantiene dentro del contexto del usuario autenticado.

---

## 2. Diagrama de Clases de la HU-67

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
        <<View>>
        -TableView~Medicamento~ tablaInventario
        -MedicamentoDAO medicamentoDAO
        +start(Stage) void
        +cargarDatosDesdeBD() void
        -mostrarInventarioVacio() void
    }

    class MedicamentoDAO {
        <<Model / DAO>>
        +obtenerPorFarmacia(int idFarmacia) List~Medicamento~
        +obtenerTotalStockPorFarmacia(int idFarmacia) int
        +obtenerAlertasPorFarmacia(int idFarmacia) int
    }

    class Medicamento {
        <<Entity>>
        -int idMedicamento
        -String nombre
        -String principioActivo
        -String presentacion
        +getIdMedicamento() int
        +getNombre() String
    }

    class Lote {
        <<Entity>>
        -int idLote
        -String numeroLote
        -int cantidad
        -LocalDate fechaVencimiento
        -int idFarmacia
        +getCantidad() int
        +getFechaVencimiento() LocalDate
    }

    class DatabaseConfig {
        <<Infrastructure>>
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

---

## 3. Relación entre los actores y las clases

Los actores no acceden directamente a las clases de persistencia.

El flujo correspondiente a la HU-67 es:

```text
Administrador / Farmacéutico
            ↓
      MainDashboardFX
            ↓
        UserSession
            ↓
       CurrentUser
            ↓
       idFarmacia
            ↓
      MedicamentoDAO
            ↓
     DatabaseConfig
            ↓
     PostgreSQL / Neon
```

El `idFarmacia` almacenado en el usuario autenticado actúa como contexto para restringir los registros visibles.

La consulta de inventario utiliza este valor para aplicar:

```sql
WHERE id_farmacia = ?
```

De esta forma, el usuario únicamente puede visualizar el inventario correspondiente a su farmacia.

---

## 4. Relaciones principales

### UserSession — CurrentUser

Relación de composición.

`UserSession` mantiene el contexto del usuario autenticado durante la sesión.

### CurrentUser — Farmacia

Relación de asociación.

El usuario puede tener una farmacia asignada mediante `idFarmacia`.

### MainDashboardFX — UserSession

Relación de dependencia.

La vista consulta la sesión para determinar el contexto del usuario autenticado.

### MainDashboardFX — MedicamentoDAO

Relación de dependencia.

La vista solicita los datos del inventario utilizando el identificador de la farmacia activa.

### MedicamentoDAO — DatabaseConfig

Relación de dependencia.

El DAO utiliza `DatabaseConfig` para establecer la conexión JDBC con PostgreSQL.

### Farmacia — Lote

Una farmacia puede contener cero o múltiples lotes.

### Medicamento — Lote

Un medicamento puede estar relacionado con cero o múltiples lotes.
