# Entrada y salida de un lote: ingreso y dispensación

Flujo del ingreso de un lote y de la dispensación, y si cada uno registra un movimiento de inventario. Verificado sobre el código del repositorio en el commit `e89a0fa` (19 de octubre de 2026). Ambos servicios ejecutan su propia transacción con SQL directo sobre `lotes` y `medicamentos`, sin pasar por `LoteDAOImpl`.

---

## 1. Ingreso de un lote

```mermaid
sequenceDiagram
    participant L as Llamador
    participant I as Inventario
    participant S as RegistroLoteService
    participant DB as PostgreSQL

    L->>I: registrarLote(lote)
    I->>S: registrarMedicamentoLote(lote, idUsuario, idFarmacia)
    Note over S: Valida cantidad positiva,<br/>ubicación obligatoria y lote.validar()
    S->>DB: setAutoCommit(false), inicia la transacción
    S->>DB: SELECT del medicamento de la farmacia, ACTIVO, FOR UPDATE
    alt No existe, está inactivo o es de otra farmacia
        S->>DB: ROLLBACK
        S-->>I: ReglaNegocioException
    else El medicamento es válido
        S->>DB: INSERT INTO lotes
        S->>DB: UPDATE medicamentos SET stock_total = stock_total + cantidad
        S->>DB: COMMIT
        S-->>I: lote con su id
    end
    Note over S,DB: No se escribe en log_movimientos<br/>ni en movimientos_stock
```

**¿Registra un movimiento?** No. Además, ninguna clase llama hoy a `Inventario.registrarLote`.

---

## 2. Dispensación

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as InventarioController
    participant I as Inventario
    participant S as FarmacovigilanciaService
    participant DB as PostgreSQL

    U->>C: Pulsa Dispensar (onDispensar)
    C->>I: dispensarMedicamento(idLote, cantidad)
    I->>S: dispensarMedicamento(idLote, cantidad, idFarmacia, idUsuario)
    S->>DB: setAutoCommit(false), inicia la transacción
    S->>DB: validarOperador y SELECT del lote y su medicamento FOR UPDATE
    alt Falla una regla de la dispensación
        S->>DB: ROLLBACK
        S-->>C: ReglaNegocioException
        C-->>U: Alerta de farmacovigilancia
    else Se cumplen todas las reglas
        S->>DB: UPDATE lotes SET cantidad_actual y estado_lote
        S->>DB: UPDATE medicamentos SET stock_total = stock_total - cantidad
        S->>DB: COMMIT
        C-->>U: Alerta de dispensación exitosa y recarga de la tabla
    end
    Note over S,DB: No se escribe en log_movimientos<br/>ni en movimientos_stock
```

**¿Registra un movimiento?** No.

---

## 3. Funciones SQL que sí registran el movimiento

`fn_registrar_lote` inserta una `ENTRADA` en `log_movimientos` y `fn_despachar_lote` inserta una `SALIDA`, pero **ninguna clase Java las invoca**.
