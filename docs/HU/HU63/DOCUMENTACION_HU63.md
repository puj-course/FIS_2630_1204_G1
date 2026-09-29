## Mensaje de Error — Operación Revertida (HU-63)

### Texto oficial
"No se pudo completar la operación. Ningún dato fue guardado, intenta nuevamente."

### Cuándo se usa
En cualquier operación crítica de varios pasos (ej. registrar lote + actualizar stock + registrar trazabilidad) donde, si un paso falla, el sistema debe deshacer todo para no dejar información a medias.

### Estilo
Reutiliza el estilo de error coral (#F0A8A8) ya definido en HU-43.

![Mockup mensaje operación revertida](./mockup-error-transaccional-hu63.png)



### Interacción del Usuario
* **Cierre manual:** El mensaje de error incluirá un ícono de "X" en la esquina superior derecha para que el usuario pueda descartarlo manualmente de forma rápida.
* **Auto-descarte:** Si no es cerrado por el usuario, el mensaje desaparecerá automáticamente después de 8 segundos para no obstruir permanentemente la vista del formulario.
