# Modos del formulario: registro y edición

Cómo el formulario único del catálogo cambia entre registrar un producto y editar uno existente. La estructura de la pantalla está en [`FORMULARIO_UNICO_REGISTRO.md`](FORMULARIO_UNICO_REGISTRO.md) y el flujo de editar un medicamento en [`Diagrama-editar-medicamento.md`](../HU98/Diagrama-editar-medicamento.md).

---

## 1. Cómo se pasa de un modo a otro

No existe un botón "Editar": el modo depende de si hay una fila seleccionada en la tabla de productos.

| Modo | Cómo se entra | Título del formulario | Contenido |
|---|---|---|---|
| Registro | Al abrir la pantalla, al pulsar Nuevo, al cambiar de categoría y después de registrar, actualizar o desactivar con éxito | Nuevo producto | Vacío |
| Edición | Al seleccionar una fila de la tabla | Editando registro #id | Los datos del producto seleccionado |

---

## 2. Qué hace cada botón en cada modo

| Botón | En modo registro | En modo edición |
|---|---|---|
| **Nuevo** | Limpia el formulario | Sale del modo edición: limpia el formulario y quita la selección |
| **Registrar** | Crea el producto | Crea **un producto nuevo** con los datos que muestra el formulario; no se bloquea |
| **Actualizar** | Advertencia: "Seleccione un producto para actualizar" | Guarda los cambios y vuelve al modo registro; si no se puede actualizar, muestra una advertencia y sigue en edición |
| **Desactivar** | Advertencia: "Seleccione un producto" | Pide confirmación ("¿Desactivar producto #id?"), desactiva el producto y vuelve al modo registro; si no se puede, muestra una advertencia y sigue en edición |

---

## 3. Estado de los casos a validar

| Caso | Estado | Evidencia |
|---|---|---|
| Edición: el formulario muestra los datos del producto seleccionado | **Cumplido** | `configurarSeleccion` llama a `llenarFormulario` al seleccionar una fila |
| Estado: activar o desactivar se refleja en la tabla | **Parcial** | Desactivar recarga la tabla; no existe un botón para activar |
| Acceso por rol: un `FARMACEUTICO` no ve editar ni cambiar el estado | **Pendiente** | Las opciones están siempre visibles y ningún control consulta el rol (issue #720) |

---

## 4. Observaciones

Verificadas en el código:

1. **Ningún botón se habilita o deshabilita según el modo.** Registrar, Actualizar y Desactivar están siempre disponibles. En la vista, solo el botón Nuevo (`btnLimpiar`) tiene `fx:id`, por lo que el controlador no puede cambiar el estado de los demás.
2. **En modo edición, Registrar duplica el producto mostrado.** El método `onGuardar` no consulta la fila seleccionada.
3. **No existen botones "Editar", "Activar" ni de eliminar.** La única acción sobre el estado es Desactivar.
