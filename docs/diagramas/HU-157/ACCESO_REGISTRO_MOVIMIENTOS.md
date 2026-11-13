# Cómo se registra hoy un movimiento de inventario

Este documento explica por qué ruta y con qué roles se registra hoy un movimiento de cada categoría, qué falta para registrar uno de aseo o maternidad sin una alerta y cómo podría hacerse. **La propuesta está pendiente de aprobación del equipo y no se implementa nada.** Verificado sobre el código y los scripts del repositorio en el commit `e8c3dc8` (13 de noviembre de 2026).

La matriz [`MATRIZ_MOVIMIENTOS_INVENTARIO.md`](../HU-XX/MATRIZ_MOVIMIENTOS_INVENTARIO.md) está publicada, pero describe el código del 19 de octubre (commit `e89a0fa`) y ya no coincide: dice que el ingreso de lotes y la dispensación no dejan registro, y que aseo y maternidad no tienen entradas ni salidas propias. Para el estado actual, usa este documento.

---

## 1. Rutas por categoría

| Categoría | Movimiento | Pantalla o acción | Clase | Qué queda guardado |
|---|---|---|---|---|
| Medicamentos | Entrada | Botón **+ Crear lote** del inventario | `ServicioCrudLotes.crear` | Un lote nuevo `DISPONIBLE`, el stock recalculado y un registro `ENTRADA` en `log_movimientos` con "Registro inicial del lote N" |
| Medicamentos | Salida | Botón **Dispensar medicamento** | `FarmacovigilanciaService.dispensarMedicamento` | Se descuenta del lote, se recalcula el stock y queda un registro `SALIDA` en `log_movimientos` con "Dispensación del lote N" |
| Medicamentos | Ajuste | **Ajustes de inventario**, por lote | `ServicioAjustesInventario` | Cambia la cantidad del lote y queda una fila en `auditoria_ajustes` con la cantidad anterior, la nueva, la diferencia y el motivo. No escribe en `log_movimientos` |
| Medicamentos | Ajuste sin cambio de unidades | **Editar lote**, **Retirar lote** y la sincronización de vencimientos | `ServicioCrudLotes` y `ControlVencimientosService` | Un registro `AJUSTE` en `log_movimientos`: con 0 unidades al editar o retirar, y con las unidades del lote al vencer |
| Aseo y maternidad | Entrada o salida | Pantalla **Movimientos** (sección 2) | `ServicioMovimientosProducto.registrar` | Cambia el stock del producto y queda una fila en `movimientos_productos` con el tipo, la cantidad, el stock anterior y posterior y el motivo, que es obligatorio |
| Aseo y maternidad | Ajuste | **Ajustes de inventario**, por producto | `ServicioAjustesInventario` | Cambia el stock y queda una fila en `auditoria_ajustes` |
| Aseo y maternidad | Cambio de stock sin registro | Formulario del **Catálogo**, al registrar o actualizar | `CatalogoDAO` | Cambia el stock y **no queda ningún registro** |

El botón **Historial de movimientos** une `log_movimientos`, `movimientos_productos` y `auditoria_ajustes`, así que muestra todas las filas anteriores menos el cambio hecho desde el catálogo, que no dejó nada.

Lo ejecuté con las sentencias SQL del código en una base de pruebas con los scripts `01` a `18`: cada ruta dejó una sola fila nueva, en la tabla indicada, y el cambio desde el catálogo no dejó ninguna.

---

## 2. Cómo se llega a la pantalla de Movimientos

1. En **Alertas**, una alerta de stock mínimo muestra el botón **Gestionar alerta** con la acción **Reponer existencias**, solo para un rol gestor de la misma farmacia.
2. `CareStockApp.mostrarGestionAlerta` abre `movimientos-view.fxml` si la alerta es de aseo o maternidad. Si es de un medicamento, abre el inventario con el aviso "seleccione 'Nuevo lote'".
3. `ControladorMovimientos.abrirDesdeAlerta` preselecciona la categoría, el producto y el tipo `ENTRADA`. No bloquea nada: se puede cambiar el producto, la categoría o el tipo.

**No hay otra entrada.** El inventario no tiene un botón de Movimientos: su método `onAbrirMovimientos` existe y abre el formulario, pero ningún botón lo usa. La pantalla en sí no necesita una alerta, porque carga por sí sola las categorías Aseo y Maternidad, los tipos `ENTRADA` y `SALIDA` y los productos de la farmacia.

La vista `vw_alertas_inventario` genera la alerta de un producto de aseo o maternidad cuando está `ACTIVO` y `stock <= stock_minimo` (crítica si `stock = 0`).

---

## 3. Roles

| Ruta | Roles | Dónde se comprueba |
|---|---|---|
| **+ Crear lote**, **Editar lote** y **Retirar lote** | `ADMINISTRADOR`, `FARMACEUTICO` y `SUPER_ADMIN`, con farmacia | `ServicioCrudLotes.rolGestor`, que se repite en SQL dentro de la transacción |
| **Dispensar medicamento** | Los roles cuyo número (`id_rol`) esté en `CARESTOCK_ROLES_DISPENSACION`; sin la variable, ninguno | `FarmacovigilanciaService.validarOperador` |
| **Ajustes de inventario** | `ADMINISTRADOR` y `SUPER_ADMIN`, con farmacia | `ServicioAjustesInventario`, que se repite en SQL |
| Pantalla **Movimientos** | `ADMINISTRADOR`, `FARMACEUTICO` y `SUPER_ADMIN`, con farmacia | `ServicioMovimientosProducto`, con `PoliticaAccionesAlertas.gestorPermitido`, y se repite en SQL |
| **Catálogo** (stock de aseo y maternidad) | Solo `ADMINISTRADOR` | La pantalla, con `SesionUsuario.esAdministrador()`; `CatalogoDAO` no comprueba el rol |

La matriz completa de roles está en [`MATRIZ_PERMISOS_POR_ROL.md`](../HU-156/MATRIZ_PERMISOS_POR_ROL.md).

---

## 4. Lo que falta

- **Un movimiento de aseo o maternidad solo se puede registrar mientras exista una alerta,** es decir, mientras el stock no supere el mínimo. Una entrada por una compra, o una salida por uso interno, con el stock por encima del mínimo no tiene dónde registrarse.
- **Las otras dos formas de cambiar ese stock no lo reemplazan.** **Ajustes de inventario** solo lo usan `ADMINISTRADOR` y `SUPER_ADMIN`, y guarda una corrección, no una entrada o una salida. El **Catálogo** solo lo usa el `ADMINISTRADOR` y no deja registro.
- **El `FARMACEUTICO` puede registrar movimientos, pero solo llega a la pantalla con una alerta.** No usa Ajustes ni el catálogo.
- **En medicamentos no pasa:** **+ Crear lote** y **Dispensar medicamento** están siempre en el inventario.

---

## 5. Propuesta

**Estado: pendiente de aprobación del equipo.** El objetivo es registrar un movimiento de aseo o maternidad sin que exista una alerta.

| Opción | Qué cambia | Qué corrige |
|---|---|---|
| **A. Un botón Movimientos en el inventario** | Agregar a `inventario-view.fxml` un botón que use `onAbrirMovimientos`, que ya llama a `CareStockApp.mostrarFormularioMovimientos`, y mostrarlo con la misma condición que los botones de lotes (`puedeGestionarLotes`) | `ADMINISTRADOR`, `FARMACEUTICO` y `SUPER_ADMIN` registran una entrada o salida sin alerta, con motivo obligatorio y su fila en `movimientos_productos`. La pantalla y sus permisos ya existen; faltan las pruebas y la guía de usuario |
| **B. Registrar el movimiento desde el catálogo o la consulta unificada** | Un botón sobre el producto seleccionado que abra la pantalla con ese producto preseleccionado, como hoy hace la alerta | Lo mismo que A, y evita buscar el producto. Exige un método nuevo en `ControladorMovimientos` para recibirlo |
| **C. Complemento: no editar el stock desde el catálogo** | Que el formulario del catálogo guarde el stock de aseo y maternidad solo al registrar el producto, y no al actualizarlo | Cierra el cambio sin registro: todo cambio de stock pasaría por Movimientos o por Ajustes |
