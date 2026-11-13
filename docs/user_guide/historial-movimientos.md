# Historial de movimientos

Guía para el personal de la farmacia.

## Para qué sirve (HU-157)

Te deja ver quién movió el inventario de tu farmacia, cuándo y por qué: entradas, salidas y ajustes de medicamentos, aseo y maternidad. Es una consulta de **solo lectura**: no cambia nada.

## Cómo llegar

1. En **Control de Inventario**, pulsa **Historial de movimientos**.
2. Para volver, pulsa **Volver al inventario**.

El botón solo lo ven los roles **ADMINISTRADOR** y **FARMACEUTICO** con farmacia asignada. Con cualquier otro rol, incluido SUPER_ADMIN, no aparece.

## Qué muestra la tabla

| Columna | Qué muestra |
|---|---|
| Fecha y hora | Cuándo se hizo, como `dd/MM/aaaa HH:mm` y con la hora de tu computador. Salen primero los más recientes |
| Producto / referencia | El nombre del producto y, entre paréntesis, el número de lote (medicamentos) o el código (aseo y maternidad) |
| Categoria | `MEDICAMENTO`, `ASEO` o `MATERNIDAD` |
| Tipo | `ENTRADA`, `SALIDA` o `AJUSTE` |
| Cantidad | Las unidades del movimiento. En un `AJUSTE`, la diferencia sin signo: no indica si subió o bajó |
| Usuario | La persona que hizo el movimiento |
| Motivo / detalle | La razón o el texto que guardó el sistema. Pasa el cursor para leerlo completo |

Con un clic en el título de una columna puedes cambiar el orden.

Ejemplo:

| Fecha y hora | Producto / referencia | Categoria | Tipo | Cantidad | Usuario | Motivo / detalle |
|---|---|---|---|---|---|---|
| 12/11/2026 17:30 | Jabón líquido 500 ml (ALERTAS-A-ASEO-01) | ASEO | AJUSTE | 2 | Ana Pérez | Dano en un empaque |
| 12/11/2026 11:20 | Jabón líquido 500 ml (ALERTAS-A-ASEO-01) | ASEO | ENTRADA | 20 | Ana Pérez | Reposicion por alerta de stock minimo |
| 11/11/2026 16:05 | Paracetamol 500 mg (LOT-2026-114) | MEDICAMENTO | AJUSTE | 2 | Ana Pérez | Conteo fisico |
| 11/11/2026 09:40 | Paracetamol 500 mg (LOT-2026-114) | MEDICAMENTO | SALIDA | 5 | Ana Pérez | Dispensación del lote LOT-2026-114 |
| 10/11/2026 08:15 | Paracetamol 500 mg (LOT-2026-114) | MEDICAMENTO | ENTRADA | 10 | Ana Pérez | Registro inicial del lote LOT-2026-114 |

## Cómo usar los filtros

| Filtro | Opciones |
|---|---|
| Categoria | Todos, `MEDICAMENTO`, `ASEO` y `MATERNIDAD` |
| Tipo de movimiento | Todos, `ENTRADA`, `SALIDA` y `AJUSTE` |
| Fecha desde y Fecha hasta | Un día en cada calendario. Los dos días elegidos **se incluyen**. Puedes usar solo una: **Fecha desde** muestra desde ese día en adelante, y **Fecha hasta**, hasta ese día |

Los filtros se aplican al instante, sin botón de buscar, y se combinan. Debajo de la tabla, **Mostrando: N / M** dice cuántos de los M movimientos cargados cumplen los filtros.

- **Limpiar filtros** vuelve a Todos y borra las fechas.
- **Actualizar historial** vuelve a consultar los movimientos. Úsalo para ver los más recientes.

Mientras consulta, los filtros y los botones se bloquean y la barra de estado dice *Consultando historial... N registros*.

## Casos especiales

| Situación | Qué pasa |
|---|---|
| Tu farmacia todavía no tiene movimientos | La tabla dice *No existen movimientos registrados.* y el estado, *No hay movimientos para la farmacia activa.* |
| Los filtros no encuentran nada | La tabla dice *No se encontraron movimientos con esos filtros.* y el estado, *Sin resultados. Modifica los filtros.* |
| **Fecha desde** es posterior a **Fecha hasta** | La tabla queda vacía y dice *El rango de fechas no es valido.* El estado, *La fecha inicial no puede superar la fecha final.* |
| No se puede consultar | La tabla dice *No se pudo cargar el historial.* y aparece un aviso *No se pudo consultar el historial:* con el motivo |

## De dónde salen los movimientos

El historial junta tres registros, y solo muestra los de tu farmacia:

| Registro | Qué aparece |
|---|---|
| `log_movimientos` | Los movimientos de lotes de medicamentos: `ENTRADA` al crear un lote, `SALIDA` al dispensar y `AJUSTE` al editar o retirar un lote y cuando un lote vence |
| `movimientos_productos` | Las entradas y salidas de aseo y maternidad que se registran en la pantalla **Movimientos** |
| `auditoria_ajustes` | Los ajustes de inventario de medicamentos, aseo y maternidad, siempre como `AJUSTE` |
