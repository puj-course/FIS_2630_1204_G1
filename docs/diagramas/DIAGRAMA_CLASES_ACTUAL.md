# Diagrama de clases (versión actual) — CareStock

Reemplaza al diagrama de `src/CareStock/src/Diagrama.puml`, que cubre solo 21 de las 41 clases principales. Código: `src/CareStock/src/main/java/org/example/carestock/` en `main`, con el cambio de la HU.94 (`CatalogoDao`). Los números de línea corresponden a esa versión.

Cómo leerlo: `-->` es "tiene o crea una instancia de"; `..>` es "usa"; `..|>` es "implementa"; `--|>` es "extiende"; `o--` es "contiene una referencia". Los patrones de diseño van marcados entre `<<...>>`.

## 1. Presentación, sesión y fachada

```mermaid
classDiagram
    direction LR
    class AplicacionCareStock {
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
    class ControladorInicioSesion {
        +initialize()
    }
    class ControladorInventario {
        +initialize()
    }
    class ControladorCatalogo {
        +initialize()
        +abrirDesdeConsulta()
    }
    class ControladorConsultaInventario {
        +initialize()
    }
    class ServicioAutenticacion {
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
    ControladorInicioSesion ..> AplicacionCareStock
    ControladorInicioSesion ..> SesionUsuario
    ControladorInventario ..> AplicacionCareStock
    ControladorInventario ..> SesionUsuario
    ControladorCatalogo ..> AplicacionCareStock
    ControladorCatalogo ..> SesionUsuario
    ControladorConsultaInventario ..> AplicacionCareStock
    ControladorConsultaInventario ..> SesionUsuario
    ControladorInicioSesion --> ServicioAutenticacion
    ControladorInventario --> Inventario
    ControladorCatalogo --> Inventario
    ControladorConsultaInventario --> Inventario
    ControladorCatalogo ..> LecturaCampos
    AplicacionCareStock ..> SesionUsuario
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
    class ServicioFarmacovigilancia {
        +dispensarMedicamento()
    }
    class ServicioRegistroLote {
        +registrarMedicamentoLote()
    }
    class ServicioControlVencimientos {
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
    class ConsultaInventarioBaseDatos {
        +listarPorFarmacia()
    }
    class ServicioAutenticacion {
        +autenticar()
    }
    class UsuarioDao {
        <<interface>>
        +buscarPorEmail()
    }
    class UsuarioDaoJdbc {
        +buscarPorEmail()
    }
    class MedicamentoDao {
        <<interface>>
        +guardar()
        +buscarPorId()
        +listarTodos()
        +actualizarEstado()
    }
    class MedicamentoDaoJdbc {
        +guardar()
        +buscarPorId()
        +listarTodos()
    }
    class MedicamentoCatalogoDao {
        +listarPorFarmacia()
        +actualizar()
        +desactivar()
    }
    class LoteDaoJdbc {
        +guardar()
        +listarPorFarmacia()
        +actualizarCantidad()
    }
    class CatalogoDao {
        <<abstract>>
        +guardar()
        +buscarPorId()
        +listarPorFarmacia()
        +actualizar()
        +desactivar()
    }
    class AseoDao {
        #tabla()
        #asignarCampos()
        #construir()
    }
    class MaternidadDao {
        #tabla()
        #asignarCampos()
        #construir()
    }
    class ConexionBaseDatos {
        +getConexion()
        +estadoPool()
        +cerrarPool()
    }
    Inventario --> AseoDao
    Inventario --> MaternidadDao
    Inventario --> MedicamentoDaoJdbc
    Inventario --> MedicamentoCatalogoDao
    Inventario --> LoteDaoJdbc
    Inventario --> ServicioRegistroLote
    Inventario --> ServicioFarmacovigilancia
    Inventario --> ServicioControlVencimientos
    Inventario --> InventarioConsultas
    InventarioConsultas --> ConsultaInventarioProxy
    InventarioConsultas --> ConsultaInventarioBaseDatos
    ServicioAutenticacion --> UsuarioDaoJdbc
    ConsultaInventarioProxy ..|> ConsultaInventario
    ConsultaInventarioBaseDatos ..|> ConsultaInventario
    ConsultaInventarioProxy o-- ConsultaInventario : real
    UsuarioDaoJdbc ..|> UsuarioDao
    MedicamentoDaoJdbc ..|> MedicamentoDao
    AseoDao --|> CatalogoDao
    MaternidadDao --|> CatalogoDao
    LoteDaoJdbc ..> ConexionBaseDatos
    MedicamentoDaoJdbc ..> ConexionBaseDatos
    MedicamentoCatalogoDao ..> ConexionBaseDatos
    UsuarioDaoJdbc ..> ConexionBaseDatos
    CatalogoDao ..> ConexionBaseDatos
    ConsultaInventarioBaseDatos ..> ConexionBaseDatos
    ServicioFarmacovigilancia ..> ConexionBaseDatos
    ServicioRegistroLote ..> ConexionBaseDatos
    ServicioControlVencimientos ..> ConexionBaseDatos
    Inventario ..> ConexionBaseDatos
    class SesionUsuario {
        +iniciar()
        +estaActiva()
        +esAdministrador()
        +cerrar()
    }
    SesionUsuario ..> ConexionBaseDatos
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
| Proxy | Estructural | `ConsultaInventarioProxy` | Implementa `ConsultaInventario`, igual que `ConsultaInventarioBaseDatos`, y guarda el real en el campo `real` (L51) |
| Template Method | Comportamiento | `CatalogoDao`, `AseoDao` y `MaternidadDao` | `CatalogoDao` define el flujo común en métodos `final` y las subclases completan los ganchos (`tabla`, `asignarCampos`, `construir`...) |

## 6. Relaciones y dónde verlas en el código

Ruta base: `src/CareStock/src/main/java/org/example/carestock/`.

### Presentación → fachada y servicios

| Clase | Archivo | Relación (línea) |
|---|---|---|
| `ControladorInicioSesion` | `controller/ControladorInicioSesion.java` | instancia `ServicioAutenticacion` (L23); usa `SesionUsuario` (L75); usa `AplicacionCareStock` (L78) |
| `ControladorInventario` | `controller/ControladorInventario.java` | instancia `Inventario` (L72); usa `SesionUsuario` (L69); usa `AplicacionCareStock` (L224) |
| `ControladorCatalogo` | `controller/ControladorCatalogo.java` | instancia `Inventario` (L133); usa `SesionUsuario` (L134); usa `AplicacionCareStock` (L541); instancia `AseoBuilder` (L774); instancia `MaternidadBuilder` (L801); usa `LecturaCampos` (L725) |
| `ControladorConsultaInventario` | `controller/ControladorConsultaInventario.java` | instancia `Inventario` (L86); usa `InventarioConsultas` (L407); usa `FiltroProductoInventario` (L178); usa `SesionUsuario` (L60); usa `AplicacionCareStock` (L445) |
| `AplicacionCareStock` | `AplicacionCareStock.java` | usa `SesionUsuario` (L68) |

### Fachada, servicios, proxy y DAO

| Clase | Archivo | Relación (línea) |
|---|---|---|
| `Inventario` | `facade/Inventario.java` | instancia `AseoDao` (L26); instancia `MaternidadDao` (L27); instancia `MedicamentoDaoJdbc` (L30); instancia `MedicamentoCatalogoDao` (L33); instancia `LoteDaoJdbc` (L35); instancia `ServicioRegistroLote` (L41); instancia `ServicioFarmacovigilancia` (L44); instancia `ServicioControlVencimientos` (L47); usa `InventarioConsultas` (L38); usa `SesionUsuario` (L60); usa `ConexionBaseDatos` (L75) |
| `InventarioConsultas` | `proxy/InventarioConsultas.java` | instancia `ConsultaInventarioProxy` (L14); instancia `ConsultaInventarioBaseDatos` (L15) |
| `ConsultaInventarioProxy` | `proxy/ConsultaInventarioProxy.java` | implementa `ConsultaInventario` (L28); usa `SesionUsuario` (L113) |
| `ConsultaInventarioBaseDatos` | `dao/ConsultaInventarioBaseDatos.java` | implementa `ConsultaInventario` (L18); usa `ConexionBaseDatos` (L70) |
| `ServicioAutenticacion` | `service/ServicioAutenticacion.java` | instancia `UsuarioDaoJdbc` (L14) |
| `SesionUsuario` | `session/SesionUsuario.java` | usa `InventarioConsultas` (L47); usa `ConexionBaseDatos` (L68); instancia `Usuario` (L117) |
| `ServicioFarmacovigilancia` | `service/ServicioFarmacovigilancia.java` | usa `ConexionBaseDatos` (L35) |
| `ServicioRegistroLote` | `service/ServicioRegistroLote.java` | usa `ConexionBaseDatos` (L23); usa `Lote` (L12) |
| `ServicioControlVencimientos` | `service/ServicioControlVencimientos.java` | usa `ConexionBaseDatos` (L22) |
| `UsuarioDaoJdbc` | `dao/UsuarioDaoJdbc.java` | implementa `UsuarioDao` (L10); usa `ConexionBaseDatos` (L17); instancia `Usuario` (L24) |
| `MedicamentoDaoJdbc` | `dao/MedicamentoDaoJdbc.java` | implementa `MedicamentoDao` (L13); usa `ConexionBaseDatos` (L24); instancia `Medicamento` (L169) |
| `MedicamentoCatalogoDao` | `dao/MedicamentoCatalogoDao.java` | usa `ConexionBaseDatos` (L22); instancia `Medicamento` (L101) |
| `LoteDaoJdbc` | `dao/LoteDaoJdbc.java` | usa `ConexionBaseDatos` (L19); instancia `Lote` (L141) |
| `CatalogoDao` | `dao/CatalogoDao.java` | usa `ConexionBaseDatos` (L43) |
| `AseoDao` | `dao/AseoDao.java` | extiende `CatalogoDao` (L12); instancia `AseoBuilder` (L45) |
| `MaternidadDao` | `dao/MaternidadDao.java` | extiende `CatalogoDao` (L13); instancia `MaternidadBuilder` (L49) |

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
- `ReglaNegocioException` y `LoteDao` están en su carpeta del módulo en este zip; no se dibujan en estos diagramas para no recargarlos.
- `VistasCache` y `CacheFarmacia` aún no tienen llamadores.
