# Diagrama de clases (versión actual) — CareStock

Reemplaza al diagrama de `src/CareStock/src/Diagrama.puml`, que cubre solo 21 de las 41 clases principales. Código: `src/CareStock/src/main/java/org/example/carestock/` en `main`, con el cambio de la HU.94 (`CatalogoDAO`). Los números de línea corresponden a esa versión.

Cómo leerlo: `-->` es "tiene o crea una instancia de"; `..>` es "usa"; `..|>` es "implementa"; `--|>` es "extiende"; `o--` es "contiene una referencia". Los patrones de diseño van marcados entre `<<...>>`.

## 1. Presentación, sesión y fachada

```mermaid
classDiagram
    direction LR
    class CareStockApp {
        <<Singleton parcial>>
        +start()
        +mostrarLogin()
        +mostrarDashboard()
        +mostrarCatalogo()
        +mostrarCatalogoParaRegistro()
    }
    class SesionUsuario {
        +esAdministrador()
        +iniciar()
        +estaActiva()
        +actual()
        +usuarioParaFachada()
    }
    class LoginController {
        +initialize()
    }
    class InventarioController {
        +initialize()
    }
    class CatalogoController {
        +initialize()
        +abrirDesdeConsulta()
    }
    class ConsultaInventarioController {
        +initialize()
    }
    class AuthenticationService {
        +autenticar()
    }
    class Inventario {
        <<Facade>>
        +listarAseo()
        +registrarAseo()
        +listarLotes()
        +listarInventarioUnificado()
        +dispensarMedicamento()
    }
    class LecturaCampos {
        <<utilidad>>
        +precio()
        +entero()
        +enteroOpcional()
    }
    LoginController ..> CareStockApp
    LoginController ..> SesionUsuario
    InventarioController ..> CareStockApp
    InventarioController ..> SesionUsuario
    CatalogoController ..> CareStockApp
    CatalogoController ..> SesionUsuario
    ConsultaInventarioController ..> CareStockApp
    ConsultaInventarioController ..> SesionUsuario
    LoginController --> AuthenticationService
    InventarioController --> Inventario
    CatalogoController --> Inventario
    ConsultaInventarioController --> Inventario
    CatalogoController ..> LecturaCampos
    CareStockApp ..> SesionUsuario
```

## 2. Fachada, servicios, proxy y acceso a datos

```mermaid
classDiagram
    direction TB
    class Inventario {
        <<Facade>>
        +listarAseo()
        +listarLotes()
        +registrarLote()
        +dispensarMedicamento()
        +listarInventarioUnificado()
    }
    class FarmacovigilanciaService {
        +dispensarMedicamento()
    }
    class RegistroLoteService {
        +registrarMedicamentoLote()
    }
    class ControlVencimientosService {
        +sincronizar()
    }
    class InventarioConsultas {
        <<Singleton>>
        +compartida()
        +invalidarFarmacia()
        +invalidar()
    }
    class ConsultaInventario {
        <<interface>>
        +listarPorFarmacia()
    }
    class ConsultaInventarioProxy {
        <<Proxy>>
        +listarPorFarmacia()
        +invalidarFarmacia()
        +invalidar()
        +aciertos()
    }
    class ConsultaInventarioDAO {
        +listarPorFarmacia()
    }
    class AuthenticationService {
        +autenticar()
    }
    class UsuarioDAO {
        <<interface>>
        +buscarPorEmail()
    }
    class UsuarioDAOImpl {
        +buscarPorEmail()
    }
    class MedicamentoDAO {
        <<interface>>
        +guardar()
        +buscarPorId()
        +listarTodos()
        +actualizarEstado()
    }
    class MedicamentoDAOImpl {
        +guardar()
        +buscarPorId()
        +listarTodos()
    }
    class MedicamentoCatalogoDAO {
        +listarPorFarmacia()
        +actualizar()
        +desactivar()
    }
    class LoteDAOImpl {
        +guardar()
        +listarPorFarmacia()
        +actualizarCantidad()
    }
    class CatalogoDAO {
        <<abstract>>
        +guardar()
        +buscarPorId()
        +listarPorFarmacia()
        +actualizar()
        +desactivar()
    }
    class AseoDAO {
        #tabla()
        #asignarCampos()
        #construir()
    }
    class MaternidadDAO {
        #tabla()
        #asignarCampos()
        #construir()
    }
    class ConexionBD {
        +getConexion()
        +estadoPool()
        +cerrarPool()
    }
    Inventario --> AseoDAO
    Inventario --> MaternidadDAO
    Inventario --> MedicamentoDAOImpl
    Inventario --> MedicamentoCatalogoDAO
    Inventario --> LoteDAOImpl
    Inventario --> RegistroLoteService
    Inventario --> FarmacovigilanciaService
    Inventario --> ControlVencimientosService
    Inventario --> InventarioConsultas
    InventarioConsultas --> ConsultaInventarioProxy
    InventarioConsultas --> ConsultaInventarioDAO
    AuthenticationService --> UsuarioDAOImpl
    ConsultaInventarioProxy ..|> ConsultaInventario
    ConsultaInventarioDAO ..|> ConsultaInventario
    ConsultaInventarioProxy o-- ConsultaInventario : real
    UsuarioDAOImpl ..|> UsuarioDAO
    MedicamentoDAOImpl ..|> MedicamentoDAO
    AseoDAO --|> CatalogoDAO
    MaternidadDAO --|> CatalogoDAO
    LoteDAOImpl ..> ConexionBD
    MedicamentoDAOImpl ..> ConexionBD
    MedicamentoCatalogoDAO ..> ConexionBD
    UsuarioDAOImpl ..> ConexionBD
    CatalogoDAO ..> ConexionBD
    ConsultaInventarioDAO ..> ConexionBD
    FarmacovigilanciaService ..> ConexionBD
    RegistroLoteService ..> ConexionBD
    ControlVencimientosService ..> ConexionBD
    Inventario ..> ConexionBD
    class SesionUsuario {
        +iniciar()
        +estaActiva()
        +esAdministrador()
        +cerrar()
    }
    SesionUsuario ..> ConexionBD
    ConsultaInventarioProxy ..> SesionUsuario
    SesionUsuario ..> InventarioConsultas
```

## 3. Dominio y creación de productos

```mermaid
classDiagram
    direction TB
    class Validable {
        <<interface>>
        +validar()
        +esAptoParaDispensar()
    }
    class Producto {
        <<interface>>
        +reset()
        +setCodigo()
        +setNombre()
        +setDescripcion()
        +setPrecio()
    }
    class Medicamento {
        +validar()
        +esAptoParaDispensar()
    }
    class Lote {
        +validar()
        +esAptoParaDispensar()
    }
    class Aseo {
        +validar()
        +esAptoParaDispensar()
    }
    class Maternidad {
        +validar()
        +esAptoParaDispensar()
    }
    class Cosmetico {
        +validar()
        +esAptoParaDispensar()
    }
    class DispositivoMedico {
        +validar()
        +esAptoParaDispensar()
    }
    class MedicamentoBuilder {
        <<Builder>>
        +build()
    }
    class AseoBuilder {
        <<Builder>>
        +build()
    }
    class MaternidadBuilder {
        <<Builder>>
        +build()
    }
    class CosmeticoBuilder {
        <<Builder>>
        +build()
    }
    class DispositivoMedicoBuilder {
        <<Builder>>
        +build()
    }
    class ProductoDirector {
        <<Director>>
        +changeBuilder()
        +make()
    }
    Medicamento ..|> Validable
    Lote ..|> Validable
    Aseo ..|> Validable
    Maternidad ..|> Validable
    Cosmetico ..|> Validable
    DispositivoMedico ..|> Validable
    MedicamentoBuilder ..|> Producto
    AseoBuilder ..|> Producto
    MaternidadBuilder ..|> Producto
    CosmeticoBuilder ..|> Producto
    DispositivoMedicoBuilder ..|> Producto
    MedicamentoBuilder ..> Medicamento : crea
    AseoBuilder ..> Aseo : crea
    MaternidadBuilder ..> Maternidad : crea
    CosmeticoBuilder ..> Cosmetico : crea
    DispositivoMedicoBuilder ..> DispositivoMedico : crea
    ProductoDirector --> Producto
    ProductoDirector ..> AseoBuilder
    ProductoDirector ..> MaternidadBuilder
```

## 4. Cachés genéricas sin uso actual

```mermaid
classDiagram
    direction LR
    class VistasCache {
        +catalogo()
        +lotes()
        +invalidarFarmacia()
        +invalidarTodo()
        +metricas()
    }
    class CacheFarmacia~T~ {
        +obtener()
        +invalidarFarmacia()
        +invalidarTodo()
        +estadisticas()
    }
    class DatosCatalogo {
        <<record>>
    }
    class DatosLotes {
        <<record>>
    }
    VistasCache --> CacheFarmacia
    VistasCache ..> DatosCatalogo
    VistasCache ..> DatosLotes
```

Estas cuatro clases están en el repositorio, pero ninguna otra clase las usa todavía.

## 5. Dónde está cada patrón

| Patrón | Tipo | Clases | Qué se ve en el código |
|---|---|---|---|
| Builder con Director | Creacional | `Producto`, los 5 `...Builder` y `ProductoDirector` | Cada constructor implementa `Producto` y hace `new` de su entidad; el director recibe un constructor |
| Singleton | Creacional | `InventarioConsultas` | Constructor privado (L19) y un único `PROXY` estático creado en la línea 14 |
| Facade | Estructural | `Inventario` | Las tres pantallas hablan solo con él, y él crea y reparte el trabajo entre DAO y servicios |
| Proxy | Estructural | `ConsultaInventarioProxy` | Implementa `ConsultaInventario`, igual que `ConsultaInventarioDAO`, y guarda el real en el campo `real` (L51) |
| Template Method | Comportamiento | `CatalogoDAO`, `AseoDAO` y `MaternidadDAO` | `CatalogoDAO` define el flujo común en métodos `final` y las subclases completan los ganchos (`tabla`, `asignarCampos`, `construir`...) |

## 6. Relaciones y dónde verlas en el código

Ruta base: `src/CareStock/src/main/java/org/example/carestock/`.

### Presentación → fachada y servicios

| Clase | Archivo | Relación (línea) |
|---|---|---|
| `LoginController` | `controller/LoginController.java` | instancia `AuthenticationService` (L23); usa `SesionUsuario` (L75); usa `CareStockApp` (L78) |
| `InventarioController` | `controller/InventarioController.java` | instancia `Inventario` (L72); usa `SesionUsuario` (L69); usa `CareStockApp` (L224) |
| `CatalogoController` | `controller/CatalogoController.java` | instancia `Inventario` (L133); usa `SesionUsuario` (L134); usa `CareStockApp` (L541); instancia `AseoBuilder` (L774); instancia `MaternidadBuilder` (L801); usa `LecturaCampos` (L725) |
| `ConsultaInventarioController` | `controller/ConsultaInventarioController.java` | instancia `Inventario` (L86); usa `InventarioConsultas` (L407); usa `FiltroProductoInventario` (L178); usa `SesionUsuario` (L60); usa `CareStockApp` (L445) |
| `CareStockApp` | `CareStockApp.java` | usa `SesionUsuario` (L68) |

### Fachada, servicios, proxy y DAO

| Clase | Archivo | Relación (línea) |
|---|---|---|
| `Inventario` | `facade/Inventario.java` | instancia `AseoDAO` (L26); instancia `MaternidadDAO` (L27); instancia `MedicamentoDAOImpl` (L30); instancia `MedicamentoCatalogoDAO` (L33); instancia `LoteDAOImpl` (L35); instancia `RegistroLoteService` (L41); instancia `FarmacovigilanciaService` (L44); instancia `ControlVencimientosService` (L47); usa `InventarioConsultas` (L38); usa `SesionUsuario` (L60); usa `ConexionBD` (L75) |
| `InventarioConsultas` | `proxy/InventarioConsultas.java` | instancia `ConsultaInventarioProxy` (L14); instancia `ConsultaInventarioDAO` (L15) |
| `ConsultaInventarioProxy` | `proxy/ConsultaInventarioProxy.java` | implementa `ConsultaInventario` (L28); usa `SesionUsuario` (L113) |
| `ConsultaInventarioDAO` | `dao/ConsultaInventarioDAO.java` | implementa `ConsultaInventario` (L18); usa `ConexionBD` (L70) |
| `AuthenticationService` | `service/AuthenticationService.java` | instancia `UsuarioDAOImpl` (L14) |
| `SesionUsuario` | `session/SesionUsuario.java` | usa `InventarioConsultas` (L47); usa `ConexionBD` (L68); instancia `Usuario` (L117) |
| `FarmacovigilanciaService` | `service/FarmacovigilanciaService.java` | usa `ConexionBD` (L35) |
| `RegistroLoteService` | `service/RegistroLoteService.java` | usa `ConexionBD` (L23); usa `Lote` (L12) |
| `ControlVencimientosService` | `service/ControlVencimientosService.java` | usa `ConexionBD` (L22) |
| `UsuarioDAOImpl` | `dao/UsuarioDAOImpl.java` | implementa `UsuarioDAO` (L10); usa `ConexionBD` (L17); instancia `Usuario` (L24) |
| `MedicamentoDAOImpl` | `dao/MedicamentoDAOImpl.java` | implementa `MedicamentoDAO` (L13); usa `ConexionBD` (L24); instancia `Medicamento` (L169) |
| `MedicamentoCatalogoDAO` | `dao/MedicamentoCatalogoDAO.java` | usa `ConexionBD` (L22); instancia `Medicamento` (L101) |
| `LoteDAOImpl` | `dao/LoteDAOImpl.java` | usa `ConexionBD` (L19); instancia `Lote` (L141) |
| `CatalogoDAO` | `dao/CatalogoDAO.java` | usa `ConexionBD` (L43) |
| `AseoDAO` | `dao/AseoDAO.java` | extiende `CatalogoDAO` (L12); instancia `AseoBuilder` (L45) |
| `MaternidadDAO` | `dao/MaternidadDAO.java` | extiende `CatalogoDAO` (L13); instancia `MaternidadBuilder` (L49) |

### Dominio y creación

| Clase | Archivo | Relación (línea) |
|---|---|---|
| `Medicamento` | `model/Medicamento.java` | implementa `Validable` (L7); instancia `MedicamentoBuilder` (L81) |
| `Lote` | `model/Lote.java` | implementa `Validable` (L8) |
| `Aseo` | `model/Aseo.java` | implementa `Validable` (L5) |
| `Maternidad` | `model/Maternidad.java` | implementa `Validable` (L5) |
| `Cosmetico` | `model/Cosmetico.java` | implementa `Validable` (L5) |
| `DispositivoMedico` | `model/DispositivoMedico.java` | implementa `Validable` (L7) |
| `MedicamentoBuilder` | `model/MedicamentoBuilder.java` | implementa `Producto` (L4); instancia `Medicamento` (L13) |
| `AseoBuilder` | `model/AseoBuilder.java` | implementa `Producto` (L6); instancia `Aseo` (L16) |
| `MaternidadBuilder` | `model/MaternidadBuilder.java` | implementa `Producto` (L5); instancia `Maternidad` (L15) |
| `CosmeticoBuilder` | `model/CosmeticoBuilder.java` | implementa `Producto` (L4); instancia `Cosmetico` (L11) |
| `DispositivoMedicoBuilder` | `model/DispositivoMedicoBuilder.java` | implementa `Producto` (L3); instancia `DispositivoMedico` (L13) |
| `ProductoDirector` | `model/ProductoDirector.java` | usa `Producto` (L3); usa `AseoBuilder` (L51); usa `MaternidadBuilder` (L75) |

### Cachés genéricas (sin uso actual)

| Clase | Archivo | Relación (línea) |
|---|---|---|
| `VistasCache` | `proxy/VistasCache.java` | usa `CacheFarmacia` (L13); usa `DatosCatalogo` (L13); usa `DatosLotes` (L18) |

## 7. Observaciones

- Hay un patrón de comportamiento explícito (Template Method). La rúbrica pide dos por tipo, y falta un segundo de comportamiento.
- `ReglaNegocioException` y `LoteDAO` están en su carpeta del módulo en este zip; no se dibujan en estos diagramas para no recargarlos.
- `VistasCache` y `CacheFarmacia` aún no tienen llamadores.
