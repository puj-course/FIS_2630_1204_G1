# Comportamiento de `Medicamento` — constructores y métodos

## Constructores disponibles

1. **`Medicamento()`** — vacío, llama a `super()`. No inicializa
   `formaFarmaceutica`, `presentacion` ni `requiereReceta`.

2. **`Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial, String principioActivo, String concentracion, String categoria, Integer stockTotal, Integer stockMinimo)`**
   Constructor "corto". Delega en el constructor completo (abajo) fijando
   `formaFarmaceutica = "SIN ESPECIFICAR"` y `presentacion = "SIN ESPECIFICAR"`
   por defecto.

3. **`Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial, String principioActivo, String concentracion, String categoria, Integer stockTotal, Integer stockMinimo, String formaFarmaceutica, String presentacion)`**
   Constructor completo — el que debería usarse al leer un medicamento real
   desde la base de datos, ya que sí recibe forma farmacéutica y
   presentación reales en vez de asumir un valor por defecto.

4. **`Medicamento(String codigoInvima, String nombreComercial, String formaFarmaceutica)`**
   Constructor simplificado, pensado para creación rápida/pruebas: rellena
   `idMedicamento = 0L`, `principioActivo`/`concentracion` vacíos,
   `categoria = "General"`, `stockTotal = 0`, `stockMinimo = 10` y
   `presentacion = "SIN ESPECIFICAR"`.

En los 4 casos, `requiereReceta` siempre queda en `false` — ningún
constructor lo recibe como parámetro.

## Método `toString()`

Sobrescrito para mostrar `"<nombreComercial> (<concentracion>)"`, o solo
`"<nombreComercial>"` si la concentración está vacía o es `null`. Se usa
para mostrar el medicamento de forma legible en componentes de la interfaz
(ej. un ComboBox de selección de medicamento).

## Puntos a tener en cuenta para quien use esta clase

- Si necesitas construir un `Medicamento` con datos reales desde la BD,
  usa el constructor completo (#3) — los otros dos rellenan campos con
  valores por defecto que pueden no ser correctos para un registro real.
- `requiereReceta` no es funcional todavía — si una HU futura necesita
  este dato, hay que agregar la columna en `MEDICAMENTOS` y actualizar el
  DAO para leerla/escribirla.
