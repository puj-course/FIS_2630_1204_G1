# Alertas de inventario: exportar y reponer

Guía para el personal de la farmacia.

## 1. Exportar las alertas a un archivo CSV (HU-142)

Sirve para llevar el listado de alertas a Excel, imprimirlo o enviarlo a quien hace los pedidos.

### Pasos

1. Entra a **Alertas de inventario** desde el panel principal.
2. (Opcional) Elige los filtros de **Tipo**, **Categoría** y **Severidad**. El archivo trae **solo las alertas que ves en la tabla**.
3. Pulsa **Exportar CSV**.
4. Escoge la carpeta y el nombre. CareStock propone `alertas-inventario-AAAAMMDD.csv` con la fecha de hoy.
5. Pulsa **Guardar**. Abajo de la tabla aparece: *Se exportaron N alertas a alertas-inventario-AAAAMMDD.csv.*



### Qué trae el archivo

| Columna | Contenido |
|---|---|
| Severidad | Crítica o Advertencia |
| Tipo | Stock mínimo o Vencimiento |
| Categoría | Medicamento, Aseo o Maternidad |
| Producto | Nombre del producto |
| Referencia | Código del producto o número de lote |
| Existencias | Unidades disponibles |
| Detalle | El mismo mensaje de la tabla |

- Separador: punto y coma (`;`), igual que el CSV de lotes.
- Codificación: UTF-8 con marca para que Excel muestre bien las tildes.
- Si un texto empieza con `=`, `+`, `-` o `@`, se le antepone una comilla simple para que Excel no lo ejecute como fórmula.

### Casos especiales

| Situación | Qué pasa |
|---|---|
| La tabla está vacía | Aparece *No hay alertas para exportar.* y no se abre el selector |
| Cierras el selector sin guardar | No pasa nada, no se crea archivo |
| No se puede escribir en la carpeta | Aparece *No se pudo guardar el archivo* con el motivo |
