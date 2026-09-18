# Pasos críticos para registrar un lote correctamente

**Sprint:** 6
**Historia de Usuario:** HU.42
**Tarea:** #247
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 17 de septiembre de 2026

---

## 1. Objetivo

Identificar los pasos críticos que un Auxiliar de Farmacia debe seguir 
para registrar un lote correctamente en CareStock, evitando errores y 
garantizando la integridad del inventario.

---

## 2. Pasos críticos identificados

| # | Paso crítico | Descripción |
|---|--------------|-------------|
| 1 | Abrir el formulario de ingreso | El Auxiliar debe ingresar a la 
aplicación y seleccionar la opción "Agregar Medicamento" desde el 
dashboard. |
| 2 | Seleccionar el medicamento | El medicamento debe existir previamente 
en la base de datos. No se puede registrar un lote para un medicamento que 
no esté en el catálogo. |
| 3 | Ingresar el número de lote | El número de lote es obligatorio y debe 
ser único para ese medicamento. No puede repetirse. |
| 4 | Ingresar la cantidad | La cantidad debe ser un número entero mayor 
que cero. No se permiten valores negativos ni cero. |
| 5 | Ingresar la fecha de vencimiento | La fecha debe ser posterior a la 
fecha actual. No se permiten fechas vencidas. |
| 6 | Seleccionar la ubicación física | La ubicación debe existir en la 
tabla `UBICACIONES`. No se puede asignar una ubicación inexistente. |
| 7 | Verificar los datos antes de guardar | El Auxiliar debe revisar que 
todos los campos estén correctos antes de confirmar el registro. |
| 8 | Confirmar el registro | Al guardar, el sistema valida los datos y, 
si todo está correcto, inserta el lote y actualiza el stock. |
| 9 | Verificar el mensaje de confirmación | El sistema muestra un mensaje 
de éxito. Si aparece un error, el Auxiliar debe corregir los datos y 
volver a intentarlo. |

---

## 3. Errores comunes a evitar

| # | Error | Cómo evitarlo |
|---|-------|---------------|
| 1 | Número de lote duplicado | Verificar que el número no exista 
previamente para ese medicamento. |
| 2 | Fecha de vencimiento vencida | Verificar que la fecha sea posterior 
a la fecha actual. |
| 3 | Cantidad en cero o negativa | Ingresar siempre un número entero 
mayor a cero. |
| 4 | Ubicación inexistente | Seleccionar una ubicación de la lista 
disponible. |
| 5 | Medicamento no registrado | Verificar que el medicamento exista en 
el catálogo antes de registrar el lote. |

---

**Fin del documento.**
