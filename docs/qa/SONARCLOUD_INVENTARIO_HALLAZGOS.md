# Inventario de hallazgos SonarCloud - CareStock

Rama analizada: `main`.

Total de hallazgos abiertos: **203**.

## Distribucion por prioridad

- MEDIUM: 126
- LOW: 29
- HIGH: 27
- INFO: 20
- BLOCKER: 1

## Archivos con mayor cantidad de hallazgos

- `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java`: 11
- `src/main/java/org/example/carestock/controller/ControladorCatalogo.java`: 10
- `src/main/java/org/example/carestock/controller/ControladorInventario.java`: 10
- `src/main/java/org/example/carestock/dao/LoteDAO.java`: 10
- `src/main/java/org/example/carestock/PruebaConexion.java`: 8
- `src/main/java/org/example/carestock/service/ServicioCrudLotes.java`: 8
- `src/main/java/org/example/carestock/controller/ControladorAjustes.java`: 7
- `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java`: 7
- `src/main/java/org/example/carestock/dao/MedicamentoDAO.java`: 7
- `src/main/java/org/example/carestock/DataTransferObject/ProductoInventario.java`: 5
- `src/main/java/org/example/carestock/dao/LoteDAOImpl.java`: 5
- `src/main/java/org/example/carestock/CareStockApp.java`: 4
- `src/main/java/org/example/carestock/controller/ControladorPreferenciasSms.java`: 4
- `src/main/java/org/example/carestock/dao/MedicamentoDAOImpl.java`: 4
- `src/main/java/org/example/carestock/service/ControlVencimientosService.java`: 4
- `src/main/java/org/example/carestock/sms/PruebaSmsConsola.java`: 4
- `src/main/java/org/example/carestock/sms/ServicioAlertasSms.java`: 4
- `src/test/java/org/example/carestock/DataTransferObject/DatosTransferenciaTest.java`: 4
- `src/test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java`: 4
- `src/main/java/org/example/carestock/config/ConexionBD.java`: 3

## Inventario completo

| Prioridad | Archivo y linea | Regla | Hallazgo |
|---|---|---|---|
| BLOCKER | `src/main/java/org/example/carestock/sms/ServicioSmsTwilio.java:35` | `java:S1845` | Rename field "cuenta" to prevent any misunderstanding/clash with field "CUENTA". |
| HIGH | `src/main/java/org/example/carestock/CareStockApp.java:36` | `java:S2696` | Make the enclosing method "static" or remove this set. |
| HIGH | `src/main/java/org/example/carestock/config/ConexionBD.java:44` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 17 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/config/ConexionBD.java:85` | `java:S1192` | Define a constant instead of duplicating this literal "conexion-local.properties" 3 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ConsultaInventarioController.java:123` | `java:S1192` | Define a constant instead of duplicating this literal "Medicamento" 3 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:91` | `java:S1192` | Define a constant instead of duplicating this literal "Selecciona un producto o lote." 3 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:145` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 31 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:148` | `java:S1192` | Define a constant instead of duplicating this literal "La sesion cambio. Vuelve a iniciar sesion." 3 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:170` | `java:S1192` | Define a constant instead of duplicating this literal "ACTIVO" 3 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:315` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 17 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorAlertas.java:141` | `java:S3252` | Use static access with "javafx.animation.Animation" for "INDEFINITE". |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:153` | `java:S1192` | Define a constant instead of duplicating this literal "Medicamentos" 10 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:155` | `java:S1192` | Define a constant instead of duplicating this literal "Maternidad" 8 times. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:224` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 32 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorInventario.java:82` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 25 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorMovimientos.java:135` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 28 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorMovimientos.java:328` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 21 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/controller/ControladorPreferenciasSms.java:75` | `java:S1192` | Define a constant instead of duplicating this literal "America/Bogota" 3 times. |
| HIGH | `src/main/java/org/example/carestock/controller/GestorLotesDialogo.java:52` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 32 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/dao/MedicamentoCatalogoDAO.java:19` | `java:S1192` | Define a constant instead of duplicating this literal "farmacia" 3 times. |
| HIGH | `src/main/java/org/example/carestock/model/Aseo.java:18` | `java:S1186` | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| HIGH | `src/main/java/org/example/carestock/model/Aseo.java:10` | `java:S1192` | Define a constant instead of duplicating this literal "ACTIVO" 3 times. |
| HIGH | `src/main/java/org/example/carestock/model/Cosmetico.java:14` | `java:S1186` | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| HIGH | `src/main/java/org/example/carestock/model/Maternidad.java:14` | `java:S1186` | Add a nested comment explaining why this method is empty, throw an UnsupportedOperationException or complete the implementation. |
| HIGH | `src/main/java/org/example/carestock/proxy/CacheFarmacia.java:56` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 48 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/proxy/ConsultaInventarioProxy.java:135` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 21 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:28` | `java:S3776` | Refactor this method to reduce its Cognitive Complexity from 16 to the 15 allowed. |
| HIGH | `src/main/java/org/example/carestock/sms/EnvioProtegidoSms.java:139` | `java:S1192` | Define a constant instead of duplicating this literal "FALLIDA" 3 times. |
| MEDIUM | `src/main/java/org/example/carestock/CareStockApp.java:314` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/CareStockApp.java:363` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/DataTransferObject/ProductoInventario.java:17` | `java:S4165` | Remove this useless assignment; "categoria" already holds the assigned value along all execution paths. |
| MEDIUM | `src/main/java/org/example/carestock/DataTransferObject/ProductoInventario.java:18` | `java:S4165` | Remove this useless assignment; "codigo" already holds the assigned value along all execution paths. |
| MEDIUM | `src/main/java/org/example/carestock/DataTransferObject/ProductoInventario.java:19` | `java:S4165` | Remove this useless assignment; "nombre" already holds the assigned value along all execution paths. |
| MEDIUM | `src/main/java/org/example/carestock/DataTransferObject/ProductoInventario.java:20` | `java:S4165` | Remove this useless assignment; "presentacion" already holds the assigned value along all execution paths. |
| MEDIUM | `src/main/java/org/example/carestock/DataTransferObject/ProductoInventario.java:21` | `java:S4165` | Remove this useless assignment; "estado" already holds the assigned value along all execution paths. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:34` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:14` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:18` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:20` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:22` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:24` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/PruebaConexion.java:37` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/config/PrecalentadorConexionBD.java:42` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/config/PrecalentadorConexionBD.java:49` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ConsultaInventarioController.java:363` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ConsultaInventarioController.java:409` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:936` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ControladorInventario.java:376` | `java:S3358` | Extract this nested ternary operation into an independent statement. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ControladorInventario.java:451` | `java:S1141` | Extract this nested try block into a separate method. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ControladorInventario.java:455` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ControladorPreferenciasSms.java:96` | `java:S3358` | Extract this nested ternary operation into an independent statement. |
| MEDIUM | `src/main/java/org/example/carestock/controller/ValidacionFormularioSms.java:25` | `java:S107` | Method has 8 parameters, which is greater than 7 authorized. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:12` | `java:S6355` | Add 'since' and/or 'forRemoval' arguments to the @Deprecated annotation. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:16` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:7` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:8` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:9` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:13` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:13` | `java:S1123` | Add the missing @deprecated Javadoc tag. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:18` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAO.java:19` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAOImpl.java:107` | `java:S6355` | Add 'since' and/or 'forRemoval' arguments to the @Deprecated annotation. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAOImpl.java:109` | `java:S1123` | Add the missing @deprecated Javadoc tag. |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAOImpl.java:47` | `java:S6905` | Don't use the query "SELECT *". |
| MEDIUM | `src/main/java/org/example/carestock/dao/LoteDAOImpl.java:64` | `java:S6905` | Don't use the query "SELECT *". |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:13` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:14` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:8` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:9` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:10` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:11` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAO.java:12` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAOImpl.java:149` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAOImpl.java:63` | `java:S6905` | Don't use the query "SELECT *". |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAOImpl.java:79` | `java:S6905` | Don't use the query "SELECT *". |
| MEDIUM | `src/main/java/org/example/carestock/dao/MedicamentoDAOImpl.java:96` | `java:S6905` | Don't use the query "SELECT *". |
| MEDIUM | `src/main/java/org/example/carestock/dao/UsuarioDAO.java:10` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/facade/Inventario.java:142` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/facade/Inventario.java:107` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/model/Medicamento.java:29` | `java:S107` | Constructor has 12 parameters, which is greater than 7 authorized. |
| MEDIUM | `src/main/java/org/example/carestock/proxy/CacheFarmacia.java:137` | `java:S1181` | Catch Exception instead of Throwable. |
| MEDIUM | `src/main/java/org/example/carestock/proxy/ConsultaInventarioProxy.java:272` | `java:S1181` | Catch Exception instead of Throwable. |
| MEDIUM | `src/main/java/org/example/carestock/service/ControlVencimientosService.java:15` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ControlVencimientosService.java:109` | `java:S6909` | Move this loop-invariant setter invocation out of this loop. |
| MEDIUM | `src/main/java/org/example/carestock/service/ControlVencimientosService.java:139` | `java:S6909` | Move this loop-invariant setter invocation out of this loop. |
| MEDIUM | `src/main/java/org/example/carestock/service/ControlVencimientosService.java:141` | `java:S6909` | Move this loop-invariant setter invocation out of this loop. |
| MEDIUM | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:22` | `java:S1123` | Add the missing @deprecated Javadoc tag. |
| MEDIUM | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:158` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:21` | `java:S6355` | Add 'since' and/or 'forRemoval' arguments to the @Deprecated annotation. |
| MEDIUM | `src/main/java/org/example/carestock/service/RegistroLoteService.java:12` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioAjustesInventario.java:196` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioAjustesInventario.java:235` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioAlertasInventario.java:33` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:85` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:114` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:287` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:451` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:531` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioMovimientosProducto.java:242` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioMovimientosProducto.java:278` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/service/ServicioMovimientosProducto.java:338` | `java:S107` | Method has 10 parameters, which is greater than 7 authorized. |
| MEDIUM | `src/main/java/org/example/carestock/session/SesionUsuario.java:36` | `java:S112` | Replace generic exceptions with specific library exceptions or a custom exception. |
| MEDIUM | `src/main/java/org/example/carestock/sms/DespachadorProtegidoSms.java:18` | `java:S107` | Method has 8 parameters, which is greater than 7 authorized. |
| MEDIUM | `src/main/java/org/example/carestock/sms/EnvioProtegidoSms.java:182` | `java:S1141` | Extract this nested try block into a separate method. |
| MEDIUM | `src/main/java/org/example/carestock/sms/EnvioProtegidoSms.java:200` | `java:S3358` | Extract this nested ternary operation into an independent statement. |
| MEDIUM | `src/main/java/org/example/carestock/sms/FormateadorAlertasSms.java:34` | `java:S3358` | Extract this nested ternary operation into an independent statement. |
| MEDIUM | `src/main/java/org/example/carestock/sms/MigracionSms.java:49` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ProcesoSms.java:44` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ProcesoSms.java:61` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/PruebaSmsConsola.java:11` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/PruebaSmsConsola.java:27` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/PruebaSmsConsola.java:33` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/PruebaSmsConsola.java:42` | `java:S106` | Replace this use of System.err by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/RegistrarDestinatarioSms.java:65` | `java:S106` | Replace this use of System.out by a logger. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ReglasNotificacionSms.java:11` | `java:S107` | Method has 8 parameters, which is greater than 7 authorized. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ServicioAlertasSms.java:289` | `java:S6912` | Use "addBatch" and "executeBatch" to execute multiple SQL statements in a single call. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ServicioAlertasSms.java:293` | `java:S6909` | Move this loop-invariant setter invocation out of this loop. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ServicioAlertasSms.java:294` | `java:S6909` | Move this loop-invariant setter invocation out of this loop. |
| MEDIUM | `src/main/java/org/example/carestock/sms/ServicioAlertasSms.java:309` | `java:S6912` | Use "addBatch" and "executeBatch" to execute multiple SQL statements in a single call. |
| MEDIUM | `src/test/java/org/example/carestock/DataTransferObject/DatosTransferenciaTest.java:25` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/DataTransferObject/DatosTransferenciaTest.java:26` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/DataTransferObject/DatosTransferenciaTest.java:42` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/DataTransferObject/DatosTransferenciaTest.java:43` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/controller/ControladorAlertasCoberturaMainTest.java:57` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/controller/ControladorAlertasCoberturaMainTest.java:91` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/dao/CatalogoDaoJdbcTest.java:204` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/dao/CatalogoDaoJdbcTest.java:206` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/dao/ConsultaInventarioDaoCoberturaTest.java:157` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java:188` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java:190` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java:192` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/factory/FabricaProductoTest.java:47` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/model/AjusteExistenciasTest.java:43` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/model/ProductosBasicosTest.java:98` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/proxy/CacheFarmaciaTest.java:152` | `java:S2925` | Remove this use of "Thread.sleep()". |
| MEDIUM | `src/test/java/org/example/carestock/proxy/ConsultaInventarioProxyTest.java:43` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/AuthenticationServiceTest.java:115` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesJdbcTest.java:197` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:38` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:43` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:50` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:54` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:64` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:68` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:75` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:124` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:128` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:137` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/sms/RepositorioPreferenciasSmsJdbcCoberturaTest.java:255` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/sms/RepositorioPreferenciasSmsJdbcCoberturaTest.java:272` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/sms/ServicioAlertasSmsCoberturaTest.java:360` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/sms/ServicioPreferenciasSmsTest.java:75` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/sms/ServicioPreferenciasSmsTest.java:95` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| MEDIUM | `src/test/java/org/example/carestock/sms/ServicioPreferenciasSmsTest.java:110` | `java:S5778` | Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception. |
| LOW | `puj-course_FIS_2630_1204_G1:-` | `java:S120` | Rename package "org.example.carestock.DataTransferObject" to match the regular expression '^[a-z_]+(\.[a-z_][a-z0-9_]*)*$'. |
| LOW | `src/main/java/org/example/carestock/CareStockApp.java:366` | `java:S4507` | Make sure this debug feature is deactivated before delivering the code in production. |
| LOW | `src/main/java/org/example/carestock/PruebaConexion.java:38` | `java:S4507` | Make sure this debug feature is deactivated before delivering the code in production. |
| LOW | `src/main/java/org/example/carestock/config/ConexionBD.java:21` | `java:S3077` | Use a thread-safe type; adding "volatile" is not enough to make this field thread-safe. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:99` | `java:S6353` | Use concise character class syntax '\\d' instead of '[0-9]'. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorAlertas.java:63` | `java:S1450` | Remove the "refresco" field and declare it as a local variable in the relevant methods. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:30` | `java:S1128` | Remove this unused import 'javafx.animation.PauseTransition'. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:31` | `java:S1128` | Remove this unused import 'javafx.util.Duration'. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:51` | `java:S1659` | Declare "colCodigo" and all following declarations on a separate line. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:55` | `java:S1659` | Declare "lblEstado" and all following declarations on a separate line. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:59` | `java:S1659` | Declare "txtNombre" and all following declarations on a separate line. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorCatalogo.java:63` | `java:S1659` | Declare "panelPrecioStock" on a separate line. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorInventario.java:23` | `java:S1128` | Remove this unused import 'org.example.carestock.proxy.InventarioConsultas'. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorMovimientos.java:106` | `java:S6353` | Use concise character class syntax '\\d' instead of '[0-9]'. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorPreferenciasSms.java:15` | `java:S1128` | Remove this unused import 'java.util.List'. |
| LOW | `src/main/java/org/example/carestock/controller/ControladorPreferenciasSms.java:95` | `java:S9435` | Invert this negated condition and swap the two branches of the ternary. |
| LOW | `src/main/java/org/example/carestock/controller/LoginController.java:37` | `java:S5411` | Use a primitive boolean expression here. |
| LOW | `src/main/java/org/example/carestock/controller/LoginController.java:84` | `java:S4507` | Make sure this debug feature is deactivated before delivering the code in production. |
| LOW | `src/main/java/org/example/carestock/dao/UsuarioDAOImpl.java:37` | `java:S2737` | Add logic to this catch clause or eliminate it and rethrow the exception automatically. |
| LOW | `src/main/java/org/example/carestock/session/SesionUsuario.java:18` | `java:S3077` | Use a thread-safe type; adding "volatile" is not enough to make this field thread-safe. |
| LOW | `src/main/java/org/example/carestock/sms/ValidacionSms.java:8` | `java:S6353` | Use concise character class syntax '\\d' instead of '[0-9]'. |
| LOW | `src/test/java/org/example/carestock/controller/ControladoresProductosCoberturaTest.java:40` | `java:S1130` | Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from method's body. |
| LOW | `src/test/java/org/example/carestock/controller/ControladoresProductosCoberturaTest.java:67` | `java:S1130` | Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from method's body. |
| LOW | `src/test/java/org/example/carestock/controller/ControladoresProductosCoberturaTest.java:94` | `java:S1130` | Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from method's body. |
| LOW | `src/test/java/org/example/carestock/controller/NavegacionRefactorizadaTest.java:9` | `java:S1128` | Remove this unused import 'org.junit.jupiter.api.Assertions'. |
| LOW | `src/test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java:33` | `java:S1130` | Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from method's body. |
| LOW | `src/test/java/org/example/carestock/facade/InventarioFachadaCoberturaTest.java:319` | `java:S1130` | Remove the declaration of thrown exception 'java.lang.Exception', as it cannot be thrown from method's body. |
| LOW | `src/test/java/org/example/carestock/service/ServicioCrudLotesJdbcTest.java:44` | `java:S1481` | Remove this unused "sql" local variable. |
| LOW | `src/test/java/org/example/carestock/service/ServicioCrudLotesValidacionesTest.java:4` | `java:S1128` | Remove this unused import 'org.example.carestock.model.Lote'. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorAjustes.java:220` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorAlertas.java:171` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorInventario.java:135` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorInventario.java:245` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorInventario.java:260` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorInventario.java:275` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/ControladorInventario.java:462` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/GestorLotesDialogo.java:125` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/controller/GestorLotesDialogo.java:264` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/dao/AlertasInventarioDao.java:-` | `java:S2143` | Use the "java.time" API for date and time. |
| INFO | `src/main/java/org/example/carestock/dao/LoteDAO.java:13` | `java:S1133` | Do not forget to remove this deprecated code someday. |
| INFO | `src/main/java/org/example/carestock/dao/LoteDAOImpl.java:109` | `java:S1133` | Do not forget to remove this deprecated code someday. |
| INFO | `src/main/java/org/example/carestock/model/Lote.java:49` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/model/Lote.java:60` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:-` | `java:S2143` | Use the "java.time" API for date and time. |
| INFO | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:22` | `java:S1133` | Do not forget to remove this deprecated code someday. |
| INFO | `src/main/java/org/example/carestock/service/FarmacovigilanciaService.java:88` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:151` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:195` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
| INFO | `src/main/java/org/example/carestock/service/ServicioCrudLotes.java:457` | `java:S8688` | Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. |
