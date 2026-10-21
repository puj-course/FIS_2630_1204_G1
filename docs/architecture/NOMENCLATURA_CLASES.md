# Normalizacion de nombres - CareStock

Refactorizacion de nomenclatura sin modificar las reglas de negocio.

| Nombre anterior | Nombre nuevo |
|---|---|
| `CareStockApp` | `AplicacionCareStock` |
| `ConexionBD` | `ConexionBaseDatos` |
| `CatalogoController` | `ControladorCatalogo` |
| `ConsultaInventarioController` | `ControladorConsultaInventario` |
| `InventarioController` | `ControladorInventario` |
| `LoginController` | `ControladorInicioSesion` |
| `AseoDAO` | `AseoDao` |
| `CatalogoDAO` | `CatalogoDao` |
| `ConsultaInventarioBaseDatos` | `ConsultaInventarioBaseDatos` |
| `LoteDAO` | `LoteDao` |
| `LoteDAOImpl` | `LoteDaoJdbc` |
| `MaternidadDAO` | `MaternidadDao` |
| `MedicamentoCatalogoDAO` | `MedicamentoCatalogoDao` |
| `MedicamentoDAO` | `MedicamentoDao` |
| `MedicamentoDAOImpl` | `MedicamentoDaoJdbc` |
| `UsuarioDAO` | `UsuarioDao` |
| `UsuarioDAOImpl` | `UsuarioDaoJdbc` |
| `AuthenticationService` | `ServicioAutenticacion` |
| `ControlVencimientosService` | `ServicioControlVencimientos` |
| `FarmacovigilanciaService` | `ServicioFarmacovigilancia` |
| `RegistroLoteService` | `ServicioRegistroLote` |
| `HelloApplication` | `AplicacionEjemplo` |
| `HelloController` | `ControladorEjemplo` |
| `Launcher` | `LanzadorEjemplo` |

Paquete: `org.example.carestock.DataTransferObject` -> `org.example.carestock.dto`.

Verificar con `./mvnw clean verify` desde `src/CareStock`.
