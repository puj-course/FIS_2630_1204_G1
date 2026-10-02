# Diagrama de Análisis (ECB) — HU.49

**Proyecto:** CareStock
**Historia de usuario:** HU.49 — Consultar el historial de inicios de sesión de los usuarios (#284)

## Diagrama

![Diagrama ECB](https://github.com/puj-course/FIS_2630_1204_G1/blob/Rama_Lau/docs/diagramas/HU49/ECB_HU49.drawio.png?raw=true)

## Explicación

El diagrama modela el flujo de la HU.49 separando la interfaz, la lógica y los datos.

**Límite (Boundary).** HistorialAccesosFX es la pantalla que muestra el historial. Contiene un ComboBox para filtrar por usuario y una TableView para mostrar los registros.

**Controles.** AccesoDAO accede a la vista vw_historial_accesos para obtener los registros. UsuarioDAO obtiene la lista de usuarios para el filtro.

**Entidades (Entity).** AccesoLog representa un registro del historial. Usuario representa al usuario para el filtro.

**Flujo.** El Administrador abre la pantalla (1). El sistema carga la lista de usuarios (2-3) y el historial (4-5).
