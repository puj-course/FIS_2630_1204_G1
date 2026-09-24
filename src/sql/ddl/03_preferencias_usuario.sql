-- =============================================================================
-- CareStock
-- Preferencias individuales de seguridad de sesión
-- =============================================================================

CREATE TABLE IF NOT EXISTS PREFERENCIAS_USUARIO (

    id_usuario INT PRIMARY KEY,

    timeout_inactividad_activo BOOLEAN
        NOT NULL
        DEFAULT TRUE,

    timeout_inactividad_minutos INT
        NOT NULL
        DEFAULT 15,

    fecha_actualizacion TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_preferencias_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES USUARIOS (id_usuario)
        ON DELETE CASCADE,

    CONSTRAINT ck_timeout_inactividad_minutos
        CHECK (
            timeout_inactividad_minutos
            BETWEEN 1 AND 120
        )
);
