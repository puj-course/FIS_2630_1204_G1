BEGIN;

CREATE TABLE IF NOT EXISTS movimientos_productos (
    id_movimiento    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_aseo          INTEGER,
    id_maternidad    INTEGER,
    id_farmacia      INTEGER      NOT NULL,
    id_usuario       INTEGER      NOT NULL,
    tipo_movimiento  VARCHAR(20)  NOT NULL,
    cantidad         INTEGER      NOT NULL,
    motivo           VARCHAR(250) NOT NULL,
    stock_anterior   INTEGER      NOT NULL,
    stock_posterior  INTEGER      NOT NULL,
    fecha_movimiento TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_prod_aseo
        FOREIGN KEY (id_aseo) REFERENCES aseo (id_aseo),
    CONSTRAINT fk_mov_prod_maternidad
        FOREIGN KEY (id_maternidad) REFERENCES maternidad (id_maternidad),
    CONSTRAINT fk_mov_prod_farmacia
        FOREIGN KEY (id_farmacia) REFERENCES farmacias (id_farmacia),
    CONSTRAINT fk_mov_prod_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario),
    CONSTRAINT ck_mov_prod_tipo
        CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA')),
    CONSTRAINT ck_mov_prod_cantidad
        CHECK (cantidad > 0),
    CONSTRAINT ck_mov_prod_motivo
        CHECK (LENGTH(TRIM(motivo)) > 0),
    CONSTRAINT ck_mov_prod_producto
        CHECK (
            (id_aseo IS NOT NULL AND id_maternidad IS NULL)
            OR (id_aseo IS NULL AND id_maternidad IS NOT NULL)
        ),
    CONSTRAINT ck_mov_prod_stock
        CHECK (
            stock_anterior >= 0
            AND stock_posterior >= 0
            AND (
                (tipo_movimiento = 'ENTRADA'
                    AND stock_posterior = stock_anterior + cantidad)
                OR (tipo_movimiento = 'SALIDA'
                    AND stock_posterior = stock_anterior - cantidad)
            )
        )
);

CREATE INDEX IF NOT EXISTS ix_mov_prod_farmacia_fecha
    ON movimientos_productos (id_farmacia, fecha_movimiento DESC);

CREATE INDEX IF NOT EXISTS ix_mov_prod_aseo
    ON movimientos_productos (id_aseo, fecha_movimiento DESC)
    WHERE id_aseo IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_mov_prod_maternidad
    ON movimientos_productos (id_maternidad, fecha_movimiento DESC)
    WHERE id_maternidad IS NOT NULL;

COMMIT;
