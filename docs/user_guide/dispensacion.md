# Dispensación de medicamentos

Guía para el personal de la farmacia.

## Cómo dispensar (HU-144)

1. En **Control de Inventario**, busca el lote en la tabla **Inventario de Lotes** (más abajo, **Cómo elegir el lote**).
2. Haz clic en la fila del lote. Debajo de la tabla, **Lote seleccionado** lo muestra, por ejemplo `ID 7 | LOT-123 (Med #12) - Stock: 50`.
3. Escribe la **Cantidad a dispensar**.
4. Pulsa **Dispensar medicamento**.
5. Aparece una ventana **¿Confirmar la dispensación?** con:
   - el número de lote,
   - la cantidad a dispensar,
   - el stock actual del lote,
   - el **stock que quedará**.
6. Pulsa **Aceptar** para dispensar o **Cancelar** para volver sin cambios.
7. Al terminar, **Dispensación Exitosa** indica las unidades dispensadas y el **stock restante del lote**. La tabla se actualiza y ya no queda ningún lote seleccionado.

## Validaciones antes de confirmar

| Situación | Mensaje |
|---|---|
| No hay lote seleccionado | Por favor seleccione un lote directamente de la tabla. |
| El lote no es apto (vencido, retenido o agotado) | No se puede dispensar un lote vencido, retenido o agotado. |
| Campo vacío | Por favor ingrese la cantidad a dispensar. |
| No es un número entero (`abc`, `2.5`) | La cantidad a dispensar debe ser un número entero válido. |
| Es 0 o negativa | La cantidad a dispensar debe ser mayor que cero. |
| Supera las existencias del lote | La cantidad a dispensar (11) supera las existencias del lote (10). |

En todos estos casos **no se descuenta stock** y la cantidad escrita se conserva para corregirla.

## Cómo elegir el lote (HU-155)

### La tabla

**Inventario de Lotes** muestra los lotes de **todos los medicamentos de tu farmacia, juntos**, sin agrupar. Salen ordenados por **fecha de vencimiento** (arriba, el que vence primero) y, si coinciden, por ID de lote. Con un clic en el título de una columna puedes cambiar el orden.

| Columna | Qué muestra |
|---|---|
| ID Lote y Número de Lote | El identificador interno y el número del lote |
| ID Medicamento | El **número** del medicamento, no su nombre. Para saber cuál es, búscalo en el **Catálogo**, en la columna **ID** junto al **Nombre** |
| Cantidad | Las unidades del lote |
| Fecha Vencimiento | Al pasar el cursor, indica "Vence en N días", "Vence hoy" o "Vencido hace N días" |
| Estado Sanitario | `RETENIDO`; `DESHABILITADO` si vence hoy o antes; si no, el estado guardado (`DISPONIBLE`, `AGOTADO` o `VENCIDO`) |
| ❄️ | Reservada para la cadena de frío; hoy no muestra datos |

Las filas de los lotes que vencen hoy o antes salen en el color de fila crítico.

### Cómo buscar

Escribe en **Buscar por lote, medicamento o estado**. La tabla deja los lotes cuyo texto **contiene** lo que escribiste:

| Buscas por | Ejemplo | Qué muestra |
|---|---|---|
| Número de lote | `L03` | Los lotes cuyo número contiene `L03` |
| ID del medicamento | `3` | Los lotes de los medicamentos cuyo ID contiene un 3: el 3, el 13, el 30... |
| Estado | `retenido` | Los lotes con ese estado guardado |

La búsqueda por estado usa el estado **guardado**, no lo que dice la columna: un lote vencido que sigue `DISPONIBLE` se ve como `DESHABILITADO`, pero aparece al buscar `disponible`. Los botones **Ver aptos →**, **Ver vencidos →** y **Ver todos →** filtran por estado, y **Actualizar tabla** vuelve a cargar los lotes. También puedes filtrar por categoría y por la fecha en que **ingresó** el lote (no la de vencimiento).

### Qué es un lote apto

Un lote apto es el que se puede dispensar. Cumple las tres condiciones:

- su estado guardado es `DISPONIBLE`,
- tiene unidades (**Cantidad** mayor que 0),
- su fecha de vencimiento es **posterior a hoy**, con la fecha de Colombia. Un lote que vence hoy ya no es apto.

Si seleccionas un lote que no es apto y pulsas **Dispensar medicamento**, aparece **Lote deshabilitado** y no se dispensa.

### Lo que el sistema no hace hoy

- **No avisa si otro lote vence antes.** Puedes dispensar cualquier lote apto aunque otro del mismo medicamento venza primero, y ninguna ventana te lo advierte. La ayuda que existe es el botón **Sugerir lote**: al pulsarlo, selecciona el lote apto que vence primero (ver `sugerir-lote-fefo.md`). No dispensa nada.
- **No preselecciona ningún lote.** Al abrir la pantalla y cada vez que la tabla se recarga, **Lote seleccionado** dice *Ninguno* y tienes que elegir la fila. Una excepción: si llegas desde una alerta con **Gestionar lote**, la tabla muestra solo ese lote y lo deja seleccionado.

## Qué hacer si aparece un bloqueo (HU-155)

Para dispensar, el rol de tu usuario debe estar en la variable de entorno `CARESTOCK_ROLES_DISPENSACION`, que configura quien administra el sistema. Si algo lo impide, el bloqueo aparece **después de pulsar Aceptar**, en un cuadro de advertencia titulado **ALERTA DE FARMACOVIGILANCIA**, aunque no sea una regla clínica. No se descuenta stock.

| Mensaje | Qué significa | Qué hacer |
|---|---|---|
| `Usuario inactivo o sin farmacia activa asignada` | Tu usuario o tu farmacia no están activos, o no tienes farmacia. Es la primera comprobación | Pide que te activen o te asignen una farmacia, y vuelve a iniciar sesión |
| `Dispensacion bloqueada: configure CARESTOCK_ROLES_DISPENSACION` | La variable no existe o está vacía: **nadie** puede dispensar | Avisa a quien administra el sistema para que la defina |
| `Usuario inactivo o farmacia no autorizada` | Lo mismo que el primero, comprobado otra vez al dispensar | Igual que el primero |
| `Su rol no permite dispensar medicamentos` | La variable existe, pero el número de tu rol no está en la lista | Pide que agreguen el número de tu rol (el `id_rol`, no el nombre) o que te asignen otro rol |

Quien administra el sistema encuentra el formato y los pasos en [Configuración de los roles autorizados para dispensar](../diagramas/HU-147/CONFIGURACION_ROLES_DISPENSACION.md). Como es una variable de entorno, después de cambiarla hay que reiniciar la aplicación.

Los avisos del lote, como `Lote vencido: <número>`, `Stock insuficiente en el lote <número>` o `Medicamento inactivo` (que puede salir aunque el lote sea apto), son reglas del lote. Pulsa **Actualizar tabla** y elige otro lote.

## Qué no cambia

- Si una regla bloquea la dispensación, se muestra la **ALERTA DE FARMACOVIGILANCIA** y no se descuenta stock.
- Cancelar la confirmación no registra ningún movimiento.
