# Segregación por farmacia: trazabilidad y brechas

Relaciona cada exigencia de segregación con su diseño, su código y su evidencia, y declara lo que falta. Código: `src/CareStock/src/main/java/org/example/carestock/` en `main`, con el cambio de la HU.94 (`CatalogoDAO`). Los números de línea corresponden a esa versión.

## 1. Matriz de trazabilidad

| Exigencia | Diseño | Código | Base de datos | Evidencia | Estado |
|---|---|---|---|---|---|
| Cada usuario pertenece a una farmacia | `SEGREGACION_FARMACIA_BASE_DATOS.md` | `SesionUsuario.iniciar` (L55) | `usuarios.id_farmacia` | Lectura de código | Cumple |
| La farmacia no la decide la pantalla | `SEGREGACION_FARMACIA_APLICACION.md` | `Inventario.farmaciaActual` (L59 a L75) | `usuarios` y `farmacias` | Lectura de código | Cumple |
| Los productos de aseo y maternidad se filtran por farmacia | Patrón Template Method | `CatalogoDAO` (L62, L76, L100 y L118) | `aseo.id_farmacia` y `maternidad.id_farmacia` | Lectura de código; prueba de equivalencia de la HU.94 | Cumple en el código; sin script de tablas |
| Los medicamentos se filtran por farmacia | `SEGREGACION_FARMACIA_APLICACION.md` | `MedicamentoCatalogoDAO` (L19, L44 y L71) | `medicamentos.id_farmacia` | Lectura de código | Cumple |
| Los lotes se filtran por farmacia | Igual | `LoteDAOImpl.listarPorFarmacia` (L90) | Unión con `medicamentos` | Lectura de código | Cumple |
| La sincronización de vencimientos solo toca la propia farmacia | Igual | `ControlVencimientosService` (L33) | `lotes` y `medicamentos` | Lectura de código | Cumple |
| El inventario unificado se filtra y se controla | Patrón Proxy | `ConsultaInventarioDAO` (L31, L43 y L55); `ConsultaInventarioProxy` (L122 y L130) | Tres tablas | `ConsultaInventarioProxyTest.deniegaOtraFarmaciaAunqueExistaCache` | Cumple, con prueba automática |
| Dispensar y registrar lotes solo en la propia farmacia | `SEGREGACION_FARMACIA_APLICACION.md` | `FarmacovigilanciaService` (L76 a L78 y `validarOperador` L142); `RegistroLoteService` (L27) | `lotes`, `medicamentos` y `usuarios` | Lectura de código | Cumple; la dispensación exige configurar `CARESTOCK_ROLES_DISPENSACION` |
| Solo el administrador modifica productos | `DIAGRAMA_COMPONENTES_GENERAL.md`, flechas 1, 10 y 14 | `CareStockApp` (L144); `CatalogoController` (L152 y L1075) | Rol del usuario | Lectura de código | Cumple en las pantallas; no en la fachada |
| El código INVIMA no se repite dentro de una farmacia | `SEGREGACION_FARMACIA_BASE_DATOS.md` | No hay validación previa en el código | Índice `(id_farmacia, codigo_invima)` | Script `ddl/04` | Lo garantiza la base de datos, con error técnico para el usuario |

## 2. Brechas

| # | Brecha | Evidencia | Riesgo |
|---|---|---|---|
| 1 | Las tablas `aseo` y `maternidad` no tienen script en el repositorio | Búsqueda en `src/sql` y `scripts` | Quien clone el proyecto no podrá reproducir esas tablas ni sus restricciones |
| 2 | Hay operaciones de DAO que **no filtran por farmacia** | `LoteDAOImpl.listarTodos`, `listarPorMedicamento`, `actualizarCantidad` y `actualizarEstado`; `MedicamentoDAOImpl.listarTodos` y `buscarPorCodigoInvima` | Hoy ninguna pantalla las llama (solo `PruebaConexion` usa `listarTodos`), pero un uso futuro rompería la segregación |
| 3 | Las pruebas automáticas cubren el proxy y el filtro de la consulta, pero no los DAO ni la fachada | `src/test` solo tiene `ConsultaInventarioProxyTest` (7 pruebas) y `FiltroProductoInventarioTest` (4 pruebas) | Una regresión en `CatalogoDAO`, `MedicamentoCatalogoDAO` o `Inventario` no se detectaría |
| 4 | El código INVIMA repetido no se avisa antes de guardar | Ver la última fila de la matriz | El usuario vería un error de base de datos |
| 5 | El permiso de edición por rol `ADMINISTRADOR` solo se comprueba en las pantallas | `esAdministrador` no se llama en `Inventario` ni en los DAO (la dispensación, en cambio, valida el rol en el servicio) | Quien use la fachada sin pasar por las pantallas puede modificar productos |
| 6 | `VistasCache` y `CacheFarmacia` existen pero ninguna clase las usa | Búsqueda de usos en `src/main` | Código sin efecto que puede confundir; si se activa, necesitará su propio control de farmacia y pruebas |

## 3. Cómo comprobar la segregación a mano

Con dos usuarios de farmacias distintas, A y B, y datos en ambas:

1. Inicia sesión como el usuario de A y abre el catálogo: solo deben aparecer los productos de A.
2. Registra un producto de aseo en A. Cierra sesión.
3. Inicia sesión como el usuario de B: el producto de A **no** debe aparecer.
4. En el inventario, los lotes de B no deben incluir ninguno de A.
5. Abre la consulta unificada con cada usuario: cada uno debe ver solo su farmacia.
6. Si tienes acceso a los ids, intenta dispensar un lote de A siendo B (por ejemplo desde un entorno de pruebas): debe rechazarse con "No puede dispensar lotes de otra farmacia".
7. Con un usuario que no sea administrador, abre el catálogo: el formulario debe quedar desactivado y mostrar el modo consulta.

## 4. Pruebas que convendría escribir

| Prueba | Qué comprueba |
|---|---|
| `CatalogoDAO` con una conexión simulada | Que `buscarPorId`, `actualizar` y `desactivar` envían `id_farmacia` en la consulta |
| `Inventario` con DAO simulados | Que toda operación usa la farmacia de `farmaciaActual` y no la de un parámetro |
| `ControlVencimientosService` con una conexión simulada | Que las tres sentencias llevan el filtro de farmacia |
