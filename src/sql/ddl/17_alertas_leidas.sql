-- =============================================================================
-- HU.123: Marcar alertas como leídas o atendidas
-- Creación de la tabla para guardar el estado de las notificaciones
-- =============================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS ALERTAS_LEIDAS (
    id_alerta_leida INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      INT NOT NULL,
    id_farmacia     INT NOT NULL,
    tipo_alerta     VARCHAR(50) NOT NULL,
    referencia_id   INT NOT NULL,
    fecha_lectura   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_alertas_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS (id_usuario),
    CONSTRAINT fk_alertas_farmacia FOREIGN KEY (id_farmacia) REFERENCES FARMACIAS (id_farmacia),
    CONSTRAINT uq_alerta_usuario UNIQUE (id_usuario, tipo_alerta, referencia_id)
);

CREATE INDEX IF NOT EXISTS ix_alertas_usuario ON ALERTAS_LEIDAS (id_usuario);
CREATE INDEX IF NOT EXISTS ix_alertas_farmacia ON ALERTAS_LEIDAS (id_farmacia);

COMMIT;
