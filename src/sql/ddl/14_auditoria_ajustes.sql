BEGIN;

CREATE TABLE IF NOT EXISTS auditoria_ajustes (
    id_ajuste         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_lote           INTEGER,
    id_aseo           INTEGER,
    id_maternidad     INTEGER,
    id_usuario        INTEGER      NOT NULL,
    id_farmacia       INTEGER      NOT NULL,
    motivo            VARCHAR(250) NOT NULL,
    cantidad_anterior INTEGER      NOT NULL,
    cantidad_nueva    INTEGER      NOT NULL,
    diferencia        INTEGER      NOT NULL,
    fecha_ajuste      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aud_ajuste_lote
        FOREIGN KEY (id_lote) REFERENCES lotes (id_lote),
    CONSTRAINT fk_aud_ajuste_aseo
        FOREIGN KEY (id_aseo) REFERENCES aseo (id_aseo),
    CONSTRAINT fk_aud_ajuste_maternidad
        FOREIGN KEY (id_maternidad) REFERENCES maternidad (id_maternidad),
    CONSTRAINT fk_aud_ajuste_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_aud_ajuste_farmacia
        FOREIGN KEY (id_farmacia) REFERENCES farmacias (id_farmacia),
    CONSTRAINT ck_aud_ajuste_motivo
        CHECK (LENGTH(TRIM(motivo)) > 0),
    CONSTRAINT ck_aud_ajuste_referencia
        CHECK (
            (id_lote IS NOT NULL)::INTEGER
            + (id_aseo IS NOT NULL)::INTEGER
            + (id_maternidad IS NOT NULL)::INTEGER = 1
        ),
    CONSTRAINT ck_aud_ajuste_cantidades
        CHECK (
            cantidad_anterior >= 0
            AND cantidad_nueva >= 0
            AND diferencia <> 0
            AND diferencia = cantidad_nueva - cantidad_anterior
        )
);

CREATE INDEX IF NOT EXISTS ix_aud_ajuste_farmacia_fecha
    ON auditoria_ajustes (id_farmacia, fecha_ajuste DESC);

CREATE INDEX IF NOT EXISTS ix_aud_ajuste_lote
    ON auditoria_ajustes (id_lote)
    WHERE id_lote IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_aud_ajuste_aseo
    ON auditoria_ajustes (id_aseo)
    WHERE id_aseo IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_aud_ajuste_maternidad
    ON auditoria_ajustes (id_maternidad)
    WHERE id_maternidad IS NOT NULL;

COMMIT;
