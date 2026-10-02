# Diagrama de Componentes — HU.49

**Proyecto:** CareStock
**Historia de usuario:** HU.49 — Consultar el historial de inicios de sesión de los usuarios (#284)

## Diagrama

![Diagrama de Componentes](ComponentesHU49.drawio.png)

## Explicación

El diagrama muestra cómo se organiza CareStock en módulos para soportar la consulta del historial de accesos.

**Componentes.**
- **Vista:** pantalla HistorialAccesosFX.
- **Historial:** AccesoDAO, encargado de consultar el historial.
- **Usuarios:** UsuarioDAO, encargado de listar usuarios.
- **Configuración BD:** conexión a la base de datos.

**Interfaces provistas.** Historial ofrece Historial; Usuarios ofrece UsuarioDAO; Configuración ofrece Conexion.

**Interfaces requeridas.** Vista necesita Historial y UsuarioDAO. Historial y Usuarios necesitan Conexion.

**Servicio externo.** Neon PostgreSQL almacena la vista vw_historial_accesos.
