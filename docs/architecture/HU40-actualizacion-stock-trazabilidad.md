# Documentación de la actualización del stock total y la generación de 
trazabilidad

**Sprint:** 6
**Historia de Usuario:** HU.40
**Tarea:** #241
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 17 de septiembre de 2026

---

## 1. Objetivo

Documentar cómo se actualiza el stock total de un medicamento y cómo se 
genera el registro de trazabilidad cuando se ingresa un nuevo lote en 
CareStock.

---

## 2. Actualización del stock total

Cuando se registra un nuevo lote en la tabla `LOTES`, el sistema actualiza 
automáticamente el stock total del medicamento asociado. Esto se logra 
mediante un trigger definido en la base de datos.

### 2.1. Función del trigger

La función `fn_actualizar_stock_total_medicamento()` se ejecuta después de 
insertar o actualizar un lote. Su lógica es la siguiente:

1. Suma la columna `cantidad_actual` de todos los lotes del medicamento 
que tengan estado `DISPONIBLE`.
2. Actualiza el campo `stock_total` en la tabla `MEDICAMENTOS` con el 
resultado de esa suma.
3. Si no hay lotes disponibles, el stock total se establece en 0.

### 2.2. Trigger asociado

El trigger `trg_actualizar_stock_total` se dispara después de las 
operaciones `INSERT` o `UPDATE` sobre la columna `cantidad_actual` o 
`estado_lote` de la tabla `LOTES`. Esto garantiza que el stock total 
siempre esté sincronizado con la realidad del inventario.

### 2.3. Resultado

Después de registrar un lote, el campo `stock_total` del medicamento 
refleja la suma exacta de las cantidades disponibles en todos sus lotes. 
Si el lote se agota o se marca como vencido, el stock total se ajusta 
automáticamente.

---

## 3. Generación del registro de trazabilidad

Cada vez que se registra un nuevo lote, el sistema genera un registro en 
la tabla `LOG_MOVIMIENTOS` para dejar evidencia de la operación. Esto se 
realiza mediante el procedimiento almacenado `sp_registrar_nuevo_lote`.

### 3.1. Procedimiento almacenado

El procedimiento `sp_registrar_nuevo_lote` recibe los siguientes 
parámetros:

- Número de lote
- ID del medicamento
- Cantidad
- Fecha de vencimiento
- ID de la ubicación
- ID del usuario que registra

### 3.2. Pasos del procedimiento

1. Inserta el nuevo lote en la tabla `LOTES` con estado `DISPONIBLE`.
2. Obtiene el ID del lote recién insertado.
3. Inserta un registro en la tabla `LOG_MOVIMIENTOS` con:
   - ID del usuario
   - ID del lote
   - Tipo de movimiento: `ENTRADA`
   - Cantidad afectada
   - Detalle: "Ingreso inicial del lote al inventario"
   - Fecha y hora automática

### 3.3. Resultado

Cada ingreso de lote queda registrado en `LOG_MOVIMIENTOS` con todos los 
datos necesarios para auditoría: quién lo hizo, cuándo, qué lote, cuánta 
cantidad y el tipo de movimiento. Esto permite rastrear cualquier 
operación sobre el inventario.

---

## 4. Componentes involucrados

| Capa | Componente |
|------|------------|
| Base de datos | `02_functions_triggers.sql` |
| Base de datos | `01_sp_ingreso_lotes.sql` |
| Base de datos | `01_schema_carestock.sql` |

---

**Fin del documento.**
