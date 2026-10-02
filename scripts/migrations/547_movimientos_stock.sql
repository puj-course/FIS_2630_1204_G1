-- Issue #547
-- Kardex de movimientos de stock.
--
-- Cada movimiento debe registrarse dentro de la misma transacción
-- que modifica la cantidad del lote.

CREATE TABLE IF NOT EXISTS movimientos_stock (
    id_movimiento BIGSERIAL PRIMARY KEY,

    id_lote INTEGER NOT NULL,

    tipo_movimiento VARCHAR(20) NOT NULL,

    cantidad INTEGER NOT NULL,

    id_usuario INTEGER NOT NULL,

    fecha_movimiento TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimiento_lote
        FOREIGN KEY (id_lote)
        REFERENCES lotes(id_lote),

    CONSTRAINT fk_movimiento_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT chk_movimiento_tipo
        CHECK (
            tipo_movimiento IN (
                'ENTRADA',
                'SALIDA',
                'AJUSTE'
            )
        ),

    CONSTRAINT chk_movimiento_cantidad
        CHECK (cantidad > 0)
);

CREATE INDEX IF NOT EXISTS
    idx_movimientos_stock_lote
ON movimientos_stock(id_lote);

CREATE INDEX IF NOT EXISTS
    idx_movimientos_stock_fecha
ON movimientos_stock(fecha_movimiento);
