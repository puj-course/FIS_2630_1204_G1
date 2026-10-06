# HU-67 - Inventario automático por farmacia activa

## Objetivo

Cargar automáticamente en JavaFX el inventario correspondiente
a la farmacia asociada al usuario autenticado, eliminando la
selección manual de farmacia y evitando acceso accidental a
inventarios pertenecientes a otros tenants.

## 4.1 Diseño Basado en Modelos (MBD)

La farmacia activa forma parte del contexto de sesión del usuario.

Modelo conceptual:

Usuario
  -> pertenece a Farmacia
  -> inicia sesión
  -> UserSession conserva id_farmacia
  -> las vistas consultan únicamente esa farmacia

El identificador de farmacia no se obtiene desde controles
visuales ni desde datos ingresados manualmente por el usuario.

## 4.2 Diseño estructural

Componentes involucrados:

- UserSession
  Mantiene el usuario autenticado y su id_farmacia.

- SessionContext
  Centraliza la obtención segura de la farmacia activa.

- MainDashboardFX
  Carga automáticamente el inventario al inicializar la vista.

- GestionInventarioFX
  Utiliza exclusivamente la farmacia correspondiente a la sesión.

- MedicamentoDAO
  Ejecuta las consultas filtradas mediante id_farmacia.

- InventarioCrudService
  Evita operaciones sobre farmacias diferentes a la sesión activa.

Relación principal:

MainDashboardFX
    -> SessionContext
        -> UserSession

MainDashboardFX
    -> MedicamentoDAO
        -> PostgreSQL / Neon

GestionInventarioFX
    -> SessionContext
        -> UserSession

GestionInventarioFX
    -> InventarioCrudService
        -> InventarioCrudDAO
            -> PostgreSQL / Neon

## 4.3 Diseño de comportamiento

Secuencia de carga:

1. El usuario inicia sesión.
2. UserSession almacena el usuario y su id_farmacia.
3. Se abre MainDashboardFX.
4. MainDashboardFX ejecuta cargarDatosDesdeBD().
5. SessionContext obtiene el id_farmacia de la sesión.
6. MedicamentoDAO consulta exclusivamente esa farmacia.
7. Los medicamentos obtenidos se cargan en ObservableList.
8. TableView refleja automáticamente la colección.
9. Al cerrar sesión, UserSession elimina el usuario anterior.
10. Un nuevo inicio de sesión establece un nuevo id_farmacia.
11. El nuevo Dashboard carga el inventario correspondiente al nuevo usuario.

## 4.4 Patrones y estilos de diseño

### DAO

El acceso a PostgreSQL permanece encapsulado en las clases DAO.

### Singleton

UserSession mantiene una única sesión activa durante la ejecución.

### Session Context

SessionContext centraliza la identidad del usuario y farmacia
activos, evitando repetir lógica de resolución en las vistas.

### Separación de responsabilidades

JavaFX se encarga de presentación.
SessionContext se encarga del contexto autenticado.
Service aplica reglas de negocio y aislamiento.
DAO realiza persistencia.

### Multi-tenancy por farmacia

id_farmacia funciona como clave de aislamiento lógico entre
inventarios, evitando que una sesión consulte o gestione el
inventario de otra farmacia.

## 4.5 Movimientos de inventario por farmacia

LOG_MOVIMIENTOS no almacena id_farmacia directamente. La farmacia
de cada movimiento se deduce mediante:

LOG_MOVIMIENTOS
    -> LOTES
        -> MEDICAMENTOS
            -> id_farmacia

Por esta razón se creó la vista VW_MOVIMIENTOS_FARMACIA, que
resuelve esta cadena de relaciones una sola vez y expone
directamente id_farmacia, tipo de movimiento, lote, medicamento,
cantidad afectada, usuario responsable y fecha y hora.

Componentes involucrados:

- MovimientoInventario
  Modelo que representa un movimiento ya resuelto con su farmacia.

- MovimientoDAO
  Consulta VW_MOVIMIENTOS_FARMACIA filtrando por id_farmacia y,
  opcionalmente, por tipo de movimiento.

- MovimientosFarmaciaFX
  Pantalla que obtiene el id_farmacia de UserSession y muestra
  únicamente los movimientos de la farmacia activa.

Relación principal:

MovimientosFarmaciaFX
    -> UserSession
        -> id_farmacia

MovimientosFarmaciaFX
    -> MovimientoDAO
        -> VW_MOVIMIENTOS_FARMACIA
            -> PostgreSQL / Neon
