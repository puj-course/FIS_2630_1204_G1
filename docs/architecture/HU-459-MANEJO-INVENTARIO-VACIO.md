# HU-459 - Manejo de inventario vacío y errores de contexto

## Objetivo

Diferenciar correctamente un inventario vacío de un fallo de
base de datos o de un contexto de sesión inválido.

La ausencia de registros constituye un resultado válido y no
debe tratarse como una excepción.

## 4.1 Diseño Basado en Modelos

Estados principales de la vista de inventario:

DATOS
- Existe sesión.
- Existe id_farmacia.
- La consulta fue satisfactoria.
- Existen registros.

VACIO
- Existe sesión.
- Existe id_farmacia.
- La consulta fue satisfactoria.
- La lista contiene cero registros.

SIN_CONTEXTO
- No existe sesión o no existe id_farmacia.
- No se ejecuta la consulta de inventario.

ERROR_DATOS
- Existe contexto válido.
- Ocurre SQLException.
- Se muestra un mensaje controlado.

## 4.2 Diseño estructural

Componentes:

- MainDashboardFX
  Gestiona el estado visual del TableView.

- GestionInventarioFX
  Gestiona estados vacíos y errores de las tablas
  de medicamentos y lotes.

- SessionContext
  Valida sesión y farmacia activa antes del acceso a datos.

- InventarioCrudService
  Aplica aislamiento por farmacia.

- UserMessageResolver
  Convierte excepciones técnicas en mensajes seguros
  para interfaz de usuario.

- DAO
  Mantiene el acceso JDBC y retorna listas vacías
  cuando no existen registros.

## 4.3 Diseño de comportamiento

Carga correcta con datos:

Usuario
 -> SessionContext
 -> id_farmacia
 -> DAO
 -> registros
 -> TableView

Inventario vacío:

Usuario
 -> SessionContext
 -> id_farmacia
 -> DAO
 -> lista vacía
 -> "Inventario vacío para esta sede"

Sesión inválida:

Sin sesión
 -> SessionContext
 -> bloqueo
 -> no se invoca DAO
 -> mensaje controlado

Error JDBC:

SessionContext válido
 -> DAO
 -> SQLException
 -> UserMessageResolver
 -> mensaje genérico
 -> no se exponen detalles internos

## 4.4 Patrones y estilos de diseño

### DAO

El acceso JDBC continúa aislado en la capa DAO.

### Session Context

El contexto de sesión controla usuario y farmacia antes
de cualquier consulta de inventario.

### Fail Fast

Una sesión inválida o una farmacia inexistente detienen
la operación antes de acceder a PostgreSQL.

### Sanitización de errores

Las excepciones técnicas se convierten en mensajes
controlados mediante UserMessageResolver.

### Multi-tenancy

Todas las consultas de inventario requieren id_farmacia
proveniente de la sesión activa.

### Separación entre estado vacío y error

Una lista con cero elementos es un resultado válido.
SQLException representa una falla técnica independiente.
