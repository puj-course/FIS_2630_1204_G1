# Diagrama de Clases — HU.49

**Proyecto:** CareStock
**Historia de usuario:** HU.49 — Consultar el historial de inicios de sesión de los usuarios (#284)

## Diagrama

![Diagrama de Clases](https://github.com/puj-course/FIS_2630_1204_G1/blob/Rama_Lau/docs/diagramas/Clases_HU49.drawio.png?raw=true)

## Explicación

El diagrama modela las clases que participan en la consulta del historial de accesos.

**HistorialAccesosFX.** Es el límite (Boundary) que muestra la tabla con el historial. Contiene un TableView para mostrar los datos, un ComboBox para filtrar por usuario y un Label para el estado vacío.

**AccesoDAO.** Es el control que accede a la base de datos. Su método listarHistorial consulta la vista vw_historial_accesos y devuelve una lista de AccesoLog.

**UsuarioDAO.** Es el control que obtiene la lista de usuarios para el filtro. Su método listarTodos devuelve todos los usuarios.

**AccesoLog.** Es la entidad que representa un registro del historial. Contiene el ID del acceso, el ID del usuario, su nombre, email, resultado y fecha/hora.

**Usuario.** Es la entidad que representa al usuario para el filtro del ComboBox.

**Principios SOLID aplicados:**
- **SRP:** AccesoDAO solo se encarga del acceso a datos de accesos; UsuarioDAO solo de usuarios; HistorialAccesosFX solo de la interfaz.
