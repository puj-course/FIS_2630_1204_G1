# Trazabilidad de los criterios de aceptación de HU.92

Estado de cada criterio de la historia *Registrar un producto desde un único formulario*, verificado sobre `main` en el commit `f8e6e6f` (16 de octubre de 2026). Los detalles de la pantalla están en [`FORMULARIO_UNICO_REGISTRO.md`](FORMULARIO_UNICO_REGISTRO.md) y los de la fachada en [`FACHADA_INVENTARIO.md`](FACHADA_INVENTARIO.md).

---

## 1. Criterios de aceptación

| Criterio | Estado | Evidencia |
|---|---|---|
| Formulario único con selector de categoría | **Cumplido** | `CatalogoController` y `catalogo-view.fxml`: el selector tiene Medicamentos, Aseo y Maternidad, y los campos propios cambian según la categoría |
| Disponible solo para el rol `ADMINISTRADOR` | **Pendiente** | `CareStockApp.mostrarCatalogo` solo comprueba que haya una sesión activa; ningún control del formulario consulta el rol (issue #720) |
| Campos obligatorios: el mensaje indica cuáles faltan | **Parcial** | Los datos de texto se validan con mensajes que nombran el dato. Si el precio, el stock o el stock mínimo están vacíos o no son numéricos, el usuario ve el texto técnico de Java, por ejemplo `empty String` (issue #720) |
| Código único | **Sin verificar** | El código no revisa duplicados ni traduce el error de la base de datos. Medicamentos tiene un índice único por farmacia y registro INVIMA (`uq_medicamentos_farmacia_invima`). Para aseo y maternidad ningún script del repositorio crea las tablas, por lo que no se puede confirmar que tengan esa restricción |
| Farmacia asignada | **Cumplido** | `Inventario.farmaciaActual()` toma la farmacia del usuario autenticado y el producto se guarda en ella; el formulario no la pide |
| Visible al instante | **Cumplido en el catálogo** | Después de registrar, `onGuardar` limpia el formulario y recarga la tabla con `cargarTabla()`. La pantalla de inventario muestra lotes, no el catálogo |

---

## 2. Pendientes para cerrar la historia

1. **Restringir el catálogo al rol `ADMINISTRADOR`** (issue #720).
2. **Mostrar mensajes claros en los campos numéricos** (issue #720).
3. **Definir qué ve el usuario con un código repetido.** Hoy, si la base lo rechaza, el mensaje sería el del driver de PostgreSQL. Además falta versionar el script de las tablas `aseo` y `maternidad`, que [`COMPONENTE_BASE_DATOS.md`](../COMPONENTE_BASE_DATOS.md) ya señala como ausente, para poder confirmar su restricción de código único.
