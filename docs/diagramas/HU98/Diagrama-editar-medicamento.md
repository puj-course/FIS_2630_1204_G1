# Diagrama de Secuencia - Editar Medicamento

![Diagrama de secuencia de editar medicamento](https://raw.githubusercontent.com/puj-course/FIS_2630_1204_G1/Rama_Lau/docs/diagramas/HU98/Diagrama_editar_medicamento.png)
## Descripción

El diagrama muestra el flujo de edición de un medicamento en el catálogo, desde que el usuario selecciona un producto hasta que se actualiza en la base de datos.

## Participantes

| Participante | Rol |
|--------------|-----|
| `Usuario` | Actor que edita el medicamento |
| `CatalogoController` | Controlador de la vista de catálogo |
| `Inventario` | Fachada que agrupa las operaciones |
| `SesionUsuario` | Gestiona la sesión activa |
| `MedicamentoCatalogoDAO` | DAO que actualiza el medicamento |
| `Base de Datos` | PostgreSQL/Neon |
| `InventarioConsultas` | Consultas auxiliares |

## Flujo principal

1. **Usuario** selecciona un medicamento en la tabla.
2. **CatalogoController** llama a `llenarFormulario(medicamento)`.
3. **Usuario** modifica los campos y pulsa "Actualizar".
4. **CatalogoController** construye el medicamento con el Builder.
5. **CatalogoController** llama a `actualizarMedicamento(id, medicamento)`.
6. **Inventario** llama a `actualizar(id, medicamento, idFarmacia, idUsuario)`.
7. **MedicamentoCatalogoDAO** valida los campos con `validarCampos(medicamento)`.
8. **Base de Datos** ejecuta `UPDATE medicamentos de la farmacia`.
9. **Inventario** invalida la caché con `invalidar()`.
10. **CatalogoController** recarga la tabla y muestra "Producto actualizado correctamente".

## Casos alternativos

| Caso | Acción |
|------|--------|
| Sin producto seleccionado | Advertencia: seleccione un producto |
| Validación fallida | Excepción → alerta: no se pudo actualizar |
| `ok` es `false` | Advertencia: registro inexistente, inactivo o de otra farmacia |

## Relación con el código

| Clase | Método |
|-------|--------|
| `CatalogoController` | `onActualizar()`, `construirMedicamento()`, `llenarFormulario()` |
| `Inventario` | `actualizarMedicamento(id, medicamento)` |
| `MedicamentoCatalogoDAO` | `actualizar(id, producto, idFarmacia, idUsuario)`, `validarCampos()` |
| `SesionUsuario` | `actual()` |
| `CareStockApp` | Navegación |
