# Descripción técnica del flujo de ingreso de lote

**Sprint:** 6
**Historia de Usuario:** HU.40
**Tarea:** #240
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 17 de septiembre de 2026

---

## 1. Objetivo

Redactar la descripción técnica del flujo de ingreso de lote, incluyendo 
las validaciones de número de lote, fecha de vencimiento y ubicación 
física.

---

## 2. Descripción técnica del flujo

El flujo de ingreso de lote en CareStock inicia en la interfaz JavaFX, 
donde el usuario (Auxiliar de Farmacia) selecciona la opción de registrar 
un nuevo lote desde el dashboard principal. El sistema despliega un 
formulario que solicita los datos del lote: medicamento asociado, número 
de lote, cantidad, fecha de vencimiento y ubicación física.

Una vez el usuario ingresa los datos y confirma la operación, el sistema 
valida la información antes de enviarla a la base de datos. Si todas las 
validaciones son exitosas, el lote se inserta en la tabla `LOTES` de 
PostgreSQL (NeonDB) y se ejecuta el trigger que actualiza automáticamente 
el stock total del medicamento en la tabla `MEDICAMENTOS`. Finalmente, se 
registra el movimiento en la tabla `LOG_MOVIMIENTOS` para dejar 
trazabilidad de la operación.

---

## 3. Validaciones del flujo

### 3.1. Validación del número de lote

El número de lote es obligatorio y debe cumplir con el formato definido 
para CareStock. El sistema verifica que no exista un lote con el mismo 
número para el mismo medicamento, ya que la base de datos cuenta con la 
restricción `UNIQUE (numero_lote, id_medicamento)`. Si el número ya 
existe, el sistema bloquea el registro y muestra un mensaje indicando que 
el lote ya está registrado para ese medicamento.

### 3.2. Validación de la fecha de vencimiento

La fecha de vencimiento es obligatoria y debe ser posterior a la fecha 
actual. El sistema rechaza cualquier lote cuya fecha de vencimiento ya 
haya pasado, ya que no tendría sentido ingresar un producto vencido al 
inventario. Esta validación se realiza tanto en la interfaz JavaFX como en 
la base de datos, donde la columna `fecha_vencimiento` es `NOT NULL`.

### 3.3. Validación de la ubicación física

La ubicación física es obligatoria y debe corresponder a una ubicación 
existente en la tabla `UBICACIONES`. El sistema verifica que la ubicación 
seleccionada sea válida antes de permitir el registro del lote. Si la 
ubicación no existe, el sistema bloquea la operación y muestra un mensaje 
de error. Esta validación se garantiza mediante la restricción `FOREIGN 
KEY (id_ubicacion) REFERENCES UBICACIONES (id_ubicacion)`.

---

## 4. Componentes involucrados

| Capa | Componente |
|------|------------|
| Interfaz | `MainDashboardFX.java` |
| Interfaz | `FormularioMedicamentoDialog.java` |
| Modelo | `Medicamento.java` |
| Acceso a datos | `MedicamentoDAO.java` |
| Configuración | `DatabaseConfig.java` |
| Base de datos | `01_schema_carestock.sql` |
| Base de datos | `01_sp_ingreso_lotes.sql` |
| Base de datos | `02_functions_triggers.sql` |

---

**Fin del documento.**
