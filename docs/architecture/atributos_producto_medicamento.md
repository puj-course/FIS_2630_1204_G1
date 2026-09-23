# Atributos de `Producto` y `Medicamento` — CareStock

> Basado en el código real de `src/main/java/com/carestock/model/Producto.java`
> y `Medicamento.java`, ya mergeado en `main`.

## `Producto` (clase abstracta base)

| Atributo | Tipo Java | Descripción |
|---|---|---|
| `idMedicamento` | `Long` | Identificador único (corresponde a `id_medicamento` en la BD) |
| `codigoInvima` | `String` | Código de registro sanitario INVIMA |
| `nombreComercial` | `String` | Nombre comercial del producto |
| `principioActivo` | `String` | Principio activo |
| `concentracion` | `String` | Concentración del principio activo |
| `categoria` | `String` | Nombre de la categoría (ya resuelto por JOIN, no es un ID) |
| `stockMinimo` | `Integer` | Cantidad mínima antes de alerta de stock bajo |
| `stockTotal` | `Integer` | Cantidad total disponible actualmente |
| `estado` | `String` | Se fija en `"ACTIVO"` automáticamente en el constructor completo |

## `Medicamento` (atributos propios, además de los heredados)

| Atributo | Tipo Java | Descripción |
|---|---|---|
| `formaFarmaceutica` | `String` | Ej. Tableta, Jarabe |
| `presentacion` | `String` | Ej. "Caja x20 tabletas" |
| `requiereReceta` | `Boolean` | Indica si requiere receta médica. **Nota:** no tiene columna correspondiente en la base de datos — siempre queda en `false`, nunca se lee ni se guarda realmente |

## Valores por defecto observados

Cuando el código no recibe `formaFarmaceutica`/`presentacion` explícitamente,
usa el texto `"SIN ESPECIFICAR"` como valor por defecto (no los deja vacíos
ni en `null`).
