# Diagrama del flujo multi-farmacia en CareStock

**Historia de Usuario:** HU.66
**Tarea:** #497
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 03 de octubre de 2026

---

## 1. Objetivo

Crear un diagrama que represente el flujo completo de operación multi-farmacia en CareStock, desde el registro de farmacias hasta el filtrado de inventario por farmacia.

---

## 2. Diagrama del flujo

![Diagrama del flujo multi-farmacia](https://github.com/puj-course/FIS_2630_1204_G1/blob/Rama_Lau/docs/HU/HU66/Diagrama%20flujo%20multi-farmacia.png?raw=true)

---

## 3. Descripción del flujo

El flujo multi-farmacia permite que CareStock opere con múltiples sedes, garantizando la segregación de datos entre ellas.

**Registro de farmacias.** El SUPER_ADMIN registra una nueva farmacia con código único.

**Asociación de usuarios.** Cada usuario se asocia a una farmacia específica mediante `id_farmacia`.

**Inicio de sesión.** El usuario inicia sesión y `UserSession` guarda su `id_farmacia`.

**Filtrado de inventario.** Las consultas de inventario se filtran por `id_farmacia`, mostrando solo los datos de la farmacia del usuario activo.

---

## 4. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| `CrearUsuarioFX` | `view/CrearUsuarioFX.java` | Registro de farmacias y usuarios |
| `FarmaciaService` | `service/FarmaciaService.java` | Lógica de farmacias |
| `UsuarioService` | `service/UsuarioService.java` | Lógica de usuarios |
| `UserSession` | `session/UserSession.java` | Almacena el usuario con su farmacia |
| `MedicamentoDAO` | `dao/MedicamentoDAO.java` | Consultas filtradas por farmacia |
| `MainDashboardFX` | `view/MainDashboardFX.java` | Muestra el inventario filtrado |

---

**Fin del documento.**
