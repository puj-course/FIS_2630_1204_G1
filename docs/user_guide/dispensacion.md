# Dispensación de medicamentos

Guía para el personal de la farmacia.

## Cómo dispensar (HU-144)

1. En el panel principal, selecciona el **lote** en la tabla.
2. Escribe la **cantidad** a dispensar.
3. Pulsa **Dispensar**.
4. Aparece una ventana **¿Confirmar la dispensación?** con:
   - el número de lote,
   - la cantidad a dispensar,
   - el stock actual del lote,
   - el **stock que quedará**.
5. Pulsa **Aceptar** para dispensar o **Cancelar** para volver sin cambios.
6. Al terminar, el mensaje indica el **stock restante del lote**.



## Validaciones de la cantidad

| Situación | Mensaje |
|---|---|
| Campo vacío | Por favor ingrese la cantidad a dispensar. |
| No es un número entero (`abc`, `2.5`) | La cantidad a dispensar debe ser un número entero válido. |
| Es 0 o negativa | La cantidad a dispensar debe ser mayor que cero. |
| Supera las existencias del lote | La cantidad a dispensar (11) supera las existencias del lote (10). |

En todos estos casos **no se descuenta stock** y la cantidad escrita se conserva para corregirla.

## Qué no cambia

- Un lote vencido, retenido o agotado sigue sin poder dispensarse.
- Las reglas de farmacovigilancia se siguen aplicando al confirmar; si una regla bloquea la dispensación, se muestra la ALERTA DE FARMACOVIGILANCIA y no se descuenta stock.
- Cancelar la confirmación no registra ningún movimiento.
