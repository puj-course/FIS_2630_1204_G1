# Guía rápida para registrar un lote en CareStock

**Sprint:** 6
**Historia de Usuario:** HU.42
**Tarea:** #248
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 17 de septiembre de 2026

---

## 1. Objetivo

Redactar una guía clara y directa para que el Auxiliar de Farmacia pueda 
registrar un lote correctamente en CareStock, incluyendo ejemplos de 
errores comunes y cómo evitarlos.

---

## 2. ¿Cómo registrar un lote paso a paso?

### Paso 1: Abre CareStock

Inicia la aplicación y espera a que cargue el dashboard principal.

### Paso 2: Haz clic en "Agregar Medicamento"

En la parte superior derecha encontrarás el botón verde. Haz clic ahí para 
abrir el formulario de registro.

### Paso 3: Selecciona el medicamento

Escribe o selecciona el medicamento al que pertenece el lote. Recuerda: el 
medicamento debe existir previamente en el catálogo.

### Paso 4: Ingresa el número de lote

Escribe el número de lote tal como aparece en el empaque. Este número no 
puede repetirse para el mismo medicamento.

### Paso 5: Ingresa la cantidad

Escribe la cantidad de unidades que estás ingresando. Debe ser un número 
entero mayor a cero.

### Paso 6: Ingresa la fecha de vencimiento

Selecciona la fecha de vencimiento del lote. Debe ser una fecha posterior 
a hoy.

### Paso 7: Selecciona la ubicación física

Elige la ubicación donde quedará almacenado el lote (estante y nivel).

### Paso 8: Revisa los datos

Antes de guardar, verifica que todos los campos estén correctos.

### Paso 9: Haz clic en "Guardar"

El sistema validará los datos. Si todo está bien, verás un mensaje de 
confirmación.

### Paso 10: Verifica el stock actualizado

El stock total del medicamento se actualizará automáticamente. Puedes 
verlo en la tabla del dashboard.

---

## 3. Errores comunes y cómo evitarlos

### Error 1: El número de lote ya existe

**Qué pasa:** El sistema te muestra un mensaje diciendo que el lote ya 
está registrado para ese medicamento.

**Cómo evitarlo:** Antes de registrar, verifica que el número de lote no 
exista previamente. Si ya existe, usa otro número o revisa si el lote ya 
fue ingresado.

---

### Error 2: La fecha de vencimiento ya pasó

**Qué pasa:** El sistema bloquea el registro porque la fecha es anterior a 
hoy.

**Cómo evitarlo:** Revisa bien la fecha en el empaque. Siempre debe ser 
una fecha futura.

---

### Error 3: La cantidad es cero o negativa

**Qué pasa:** El sistema no permite guardar el lote.

**Cómo evitarlo:** Escribe siempre un número entero mayor a cero. Ejemplo: 
50, 100, 200.

---

### Error 4: La ubicación no existe

**Qué pasa:** El sistema no encuentra la ubicación seleccionada.

**Cómo evitarlo:** Selecciona una ubicación de la lista disponible. Si no 
aparece la que necesitas, pide que la registren primero.

---

### Error 5: El medicamento no está en el catálogo

**Qué pasa:** No puedes asociar el lote porque el medicamento no existe.

**Cómo evitarlo:** Verifica que el medicamento esté registrado en el 
catálogo antes de ingresar el lote. Si no está, regístralo primero.

---

## 4. Consejos finales

- Revisa siempre los datos antes de guardar.
- Si aparece un error, corrige el campo indicado y vuelve a intentarlo.
- Si no estás seguro de algo, consulta con el Jefe de Farmacia.
- No modifiques la base de datos directamente. Usa siempre la interfaz de 
CareStock.

---

**Fin del documento.**
