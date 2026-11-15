# Movimientos: stock resultante

Guía para el personal de la farmacia.

## Para qué sirve (HU-153)

Antes de registrar un movimiento ves cuánto stock quedará. Así evitas errores de cantidad y no dejas existencias en negativo.

## Cómo funciona

1. En **Movimientos**, elige la **categoría** y el **producto**.
2. Elige **ENTRADA** o **SALIDA**.
3. Escribe la **cantidad**.
4. Junto a las existencias aparece **Stock resultante**, y se actualiza mientras escribes.



## Ejemplos

| Existencias | Movimiento | Cantidad | Se muestra |
|---|---|---|---|
| 10 | ENTRADA | 5 | Stock resultante: 15 |
| 10 | SALIDA | 3 | Stock resultante: 7 |
| 10 | SALIDA | 10 | Stock resultante: 0 |
| 6 | SALIDA | 10 | Stock resultante: -4 (stock insuficiente), con fondo coral |
| 10 | ENTRADA | (vacía) | Stock resultante: — |

## Cuándo aparece "—"

Cuando todavía falta el tipo de movimiento o la cantidad, o cuando la cantidad no es un número mayor que cero.

## Salida mayor que el stock

El aviso sale en coral suave y el sistema **no deja registrar** la salida: al pulsar Registrar te pide actualizar los productos o corregir la cantidad.

## Importante

Es solo una vista previa. Las reglas que validan el movimiento al guardar no cambian.
