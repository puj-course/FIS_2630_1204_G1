BEGIN;

CREATE TABLE IF NOT EXISTS movimientos_catalogo (
    id_movimiento BIGSERIAL PRIMARY KEY,

    id_farmacia INTEGER NOT NULL,

    id_usuario INTEGER NOT NULL,

    id_aseo INTEGER,

    id_maternidad INTEGER,

    tipo_movimiento VARCHAR(20) NOT NULL,

    cantidad INTEGER NOT NULL,

    motivo VARCHAR(200),

    fecha_movimiento TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimiento_catalogo_farmacia
        FOREIGN KEY (id_farmacia)
        REFERENCES farmacias(id_farmacia),

    CONSTRAINT fk_movimiento_catalogo_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_movimiento_catalogo_aseo
        FOREIGN KEY (id_aseo)
        REFERENCES aseo(id_aseo),

    CONSTRAINT fk_movimiento_catalogo_maternidad
        FOREIGN KEY (id_maternidad)
        REFERENCES maternidad(id_maternidad),

    CONSTRAINT chk_movimiento_catalogo_tipo
        CHECK (
            tipo_movimiento IN (
                'ENTRADA',
                'SALIDA',
                'AJUSTE'
            )
        ),

    CONSTRAINT chk_movimiento_catalogo_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_movimiento_catalogo_producto
        CHECK (
            (id_aseo IS NOT NULL AND id_maternidad IS NULL)
            OR (id_aseo IS NULL AND id_maternidad IS NOT NULL)
        )
);

CREATE INDEX IF NOT EXISTS
    idx_movimientos_catalogo_farmacia
ON movimientos_catalogo(id_farmacia);

CREATE INDEX IF NOT EXISTS
    idx_movimientos_catalogo_fecha
ON movimientos_catalogo(fecha_movimiento);

COMMIT;
