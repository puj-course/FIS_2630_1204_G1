# Flujo de Despacho Asistido (FEFO)

Este documento describe la secuencia de pasos para el despacho asistido de medicamentos en **CareStock**.

---

## Pasos del Flujo

1. **Búsqueda:** El usuario busca el medicamento por su nombre comercial o principio activo.
2. **Consulta:** El sistema muestra los lotes disponibles ordenados del más próximo a vencer al más lejano.
3. **Selección:** El usuario selecciona el lote a despachar (por defecto, el primero de la lista).
4. **Cantidad:** El usuario ingresa la cantidad a despachar.
5. **Validación:** El sistema valida que haya stock suficiente en ese lote.
6. **Confirmación:** El usuario confirma el despacho.
7. **Registro:** El sistema registra el movimiento con fecha/hora actual y actualiza el stock del lote.
