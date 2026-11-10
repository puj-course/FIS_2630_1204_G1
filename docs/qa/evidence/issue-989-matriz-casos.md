# HU.137 - Issue #989: consulta de necesidades de reposicion

## Implementacion

- Servicio puro de Java `ServicioPlanificacionReposicion` y modelo inmutable `RegistroReposicion`.
- Solo lectura: consulta `ServicioAlertasInventario` / `AlertasInventarioDao` (vista `vw_alertas_inventario`) y `InventarioConsultas.compartida()` (`ConsultaInventarioProxy` / `ConsultaInventarioDAO`).
- La vista define las alertas de stock minimo para Medicamentos, Aseo y Maternidad. Se excluyen alertas de vencimiento. Nunca se recalculan las reglas SQL de creacion de alertas.
- Cantidad faltante: `Math.max(0, minimo - existencias)`; critico si existencias = 0, advertencia si existencias > 0 y <= minimo.
- La identidad unica es `(categoria, idProducto)`, nunca unicamente el codigo comercial.
- Consulta autorizada por la farmacia de `SesionUsuario` antes y despues de los accesos a datos, con el filtrado adicional `id_farmacia` de los DAO y proteccion del proxy.
- La vista de alertas es autoritativa para `existencias` y `umbral`; el proxy, que puede tener cache de hasta 30 segundos, complementa el codigo faltante sin sustituir dichos valores.
- Las existencias no se modifican: no se realizan INSERT, UPDATE ni DELETE.

## Umbral no configurado

Actualmente el esquema define `stock_minimo` como columna `NOT NULL DEFAULT 0` para las tres categorias. Por tanto, la base de datos no diferencia un minimo sin configurar de un cero configurado intencionalmente. No se usa ningun minimo arbitrario. Se conserva la regla de la vista: con existencias 0 y minimo 0 hay alerta critica, con **faltante 0**; con existencias positivas y minimo 0 no hay alerta. Si una fuente futura entregara `umbral = NULL`, se omite del plan por no contar con un objetivo valido. Distinguir "no configurado" de "0" requerira un cambio de esquema e historia de usuario especifica.

**Importante:** la HU.137 repone *hasta el minimo*; no aplica el factor 2 que usa `SugerenciaReposicion` en otra funcionalidad.

## Matriz de pruebas automatizadas

| Escenario | Resultado esperado |
|---|---|
| Medicamento sin stock y minimo positivo | Critica; faltante = minimo |
| Aseo en el minimo | Advertencia; faltante = 0 |
| Maternidad por debajo del minimo | Advertencia; faltante = diferencia |
| Existencias superiores al minimo | Excluido |
| Alerta de vencimiento | Excluida |
| Tres categorias con mismo codigo e id | Tres registros distintos |
| Misma categoria y codigo con id distinto | Registros distintos |
| Misma identidad repetida | Un solo registro |
| Minimo 0 y existencias 0 | Critica; faltante 0 |
| Minimo 0 y existencias positivas | Excluido |
| Umbral desconocido (`null` en fuente simulada) | Excluido sin asignacion arbitraria |
| Farmacia invalida, sin sesion o diferente | Rechazado antes de leer datos |
| Cambio de farmacia durante consulta | Rechaza el resultado |
| Proxy con valores de stock obsoletos | Mantiene cantidades de la vista |
| Codigo faltante en la vista | Recupera codigo del catalogo si existe |
| Validaciones del modelo | Rechaza stock negativo, faltante incoherente o severidad incorrecta |
| Fallo SQL | Propaga el error; sin escrituras |

## Evidencia de ejecucion

Al ejecutar en Git Bash `./mvnw -Dtest=ServicioPlanificacionReposicionTest,RegistroReposicionTest test` y `./mvnw clean verify` desde `src/CareStock`, adjuntar los archivos `issue-989-junit.txt` y `issue-989-verify.txt` de este mismo directorio. El informe JaCoCo se genera en `src/CareStock/target/site/jacoco/`.

El control del 80% de cobertura de codigo nuevo se comprueba en el workflow `.github/workflows/pre-pr-coverage.yml` al publicar la rama, antes de abrir el Pull Request. SonarCloud verificara el Quality Gate definitivo para el PR; esta matriz no sustituye esos controles.
