# Flujo del estado de un lote por vencimiento

Cómo cambia el estado de un lote cuando llega su fecha de vencimiento: quién lo detecta, qué escribe en la base de datos y cómo lo muestra la pantalla de inventario. Verificado sobre el código del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Diagrama de secuencia

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as InventarioController
    participant I as Inventario
    participant S as ControlVencimientosService
    participant DB as PostgreSQL

    U->>C: Abre la pantalla, pulsa Actualizar tabla o dispensa
    C->>I: listarLotes() desde cargarDatosTabla()
    I->>S: sincronizar(idFarmacia, idUsuario)
    S->>DB: setAutoCommit(false), inicia la transacción
    S->>DB: SELECT de los medicamentos de la farmacia con lotes DISPONIBLES vencidos, FOR UPDATE
    alt Ningún lote vencido
        S->>DB: COMMIT
        S-->>I: 0
    else Hay lotes vencidos
        S->>DB: UPDATE lotes SET estado_lote = VENCIDO
        S->>DB: INSERT INTO log_movimientos un AJUSTE por cada lote con cantidad mayor que cero
        S->>DB: UPDATE medicamentos SET stock_total = suma de los lotes DISPONIBLES vigentes
        S->>DB: COMMIT
        S-->>I: cantidad de lotes cambiados
        I->>I: InventarioConsultas.invalidarFarmacia(idFarmacia)
    end
    I->>DB: loteDAO.listarPorFarmacia(idFarmacia)
    DB-->>I: lotes de la farmacia
    I-->>C: lista de lotes
    C->>C: estadoMostrado: DESHABILITADO si la fecha es hoy o anterior
    C->>C: Tooltip de la fecha y color de la fila
    C->>C: Contador de lotes vencidos
    C-->>U: Tabla de lotes
    Note over S,DB: Ante cualquier error se hace ROLLBACK y no cambia nada
```

Un lote vencido es el que tiene fecha de vencimiento **hoy o anterior**. La interfaz pinta de rojo su fila, pone un tooltip en la fecha ("Vence en N días", "Vence hoy" o "Vencido hace N días") y pinta de naranja las filas de los lotes que no están `DISPONIBLE`.

---

## 2. Qué queda registrado

| Situación | ¿Queda registro en `log_movimientos`? |
|---|---|
| Lote que pasa a `VENCIDO` con cantidad mayor que cero | **Sí.** Un `AJUSTE` con la cantidad del lote, el detalle "Deshabilitacion automatica por vencimiento" y el usuario que cargó los lotes |
| Lote que pasa a `VENCIDO` con cantidad 0 | **No.** El cambio queda solo en el lote, en `id_usuario_modificacion` y `fecha_modificacion` |
| Recálculo del stock del medicamento | **No** |
| Lote próximo a vencer que todavía no vence | **No.** No cambia nada |
| Mostrar el estado, el tooltip, el color o el contador | **No** |

---

## 3. Cuándo ocurre

- **No es un proceso diario.** La sincronización solo se ejecuta cuando `InventarioController` carga los lotes con `cargarDatosTabla()`: al abrir la pantalla, al pulsar "Actualizar tabla" y después de dispensar. Ninguna otra clase la invoca y no hay ningún temporizador.
- **Un lote vencido sigue `DISPONIBLE` en la base de datos** hasta que alguien carga los lotes de esa farmacia.
- **Solo procesa la farmacia del usuario**, y el `AJUSTE` queda a nombre de ese usuario, no de un usuario del sistema.
