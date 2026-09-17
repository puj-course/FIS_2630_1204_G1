# Matriz de Pruebas UAT - HU.36: Seleccionar únicamente medicamentos existentes al registrar un lote

**Issue Padre:** #176  
**Sub-Issue PO:** #214  
**Rol:** Product Owner / QA  

---

## 🎯 Escenarios de Prueba Aceptación (UAT)

### Escenario 1: Selección Válida desde el Catálogo (Camino Feliz)
* **Dado que** el auxiliar de farmacia está en la vista de registro de lotes.
* **Cuando** selecciona un medicamento existente de la lista desplegable/autocompletado.
* **Entonces** el sistema captura correctamente el ID del medicamento y permite guardar el lote sin errores.

### Escenario 2: Intento de Enviar Campo Vacío
* **Dado que** el usuario no selecciona ningún medicamento en la interfaz.
* **Cuando** intenta hacer clic en "Guardar Lote".
* **Entonces** el sistema bloquea la acción y despliega una alerta en JavaFX: `"Debe seleccionar un medicamento del catálogo para continuar."`

### Escenario 3: Búsqueda o Texto Inexistente en Autocompletado
* **Dado que** el usuario escribe el nombre de un medicamento inexistente o mal escrito en el buscador.
* **Cuando** el componente no encuentra coincidencias en la BD.
* **Entonces** la interfaz no permite confirmar la selección, deshabilita la opción de envío o resalta el campo en rojo.

---

## 📋 Checklist de Aprobación
- [x] Documentación publicada en el repositorio.
- [ ] Validación de la interfaz en JavaFX realizada conjuntamente con el equipo de desarrollo.
