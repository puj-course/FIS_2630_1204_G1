BEGIN;

CREATE TABLE IF NOT EXISTS aseo (
    id_aseo                 INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo                  VARCHAR(50)    NOT NULL,
    nombre                  VARCHAR(150)   NOT NULL,
    descripcion             TEXT           NOT NULL,
    precio                  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    stock                   INT            NOT NULL DEFAULT 0,
    tipo_aseo               VARCHAR(100),
    biodegradable           BOOLEAN        NOT NULL DEFAULT FALSE,
    componentes_activos     TEXT,
    estado                  VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO',
    id_farmacia             INT            NOT NULL,
    id_usuario_creacion     INT,
    id_usuario_modificacion INT,
    fecha_modificacion      TIMESTAMP,
    CONSTRAINT fk_aseo_farmacia
        FOREIGN KEY (id_farmacia) REFERENCES FARMACIAS (id_farmacia),
    CONSTRAINT fk_aseo_usuario_creacion
        FOREIGN KEY (id_usuario_creacion) REFERENCES USUARIOS (id_usuario),
    CONSTRAINT fk_aseo_usuario_modificacion
        FOREIGN KEY (id_usuario_modificacion) REFERENCES USUARIOS (id_usuario),
    CONSTRAINT uq_aseo_farmacia_codigo UNIQUE (id_farmacia, codigo),
    CONSTRAINT ck_aseo_precio CHECK (precio >= 0),
    CONSTRAINT ck_aseo_stock CHECK (stock >= 0),
    CONSTRAINT ck_aseo_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE IF NOT EXISTS maternidad (
    id_maternidad             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo                    VARCHAR(50)    NOT NULL,
    nombre                    VARCHAR(150)   NOT NULL,
    descripcion               TEXT           NOT NULL,
    precio                    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    stock                     INT            NOT NULL DEFAULT 0,
    etapa_recomendada         VARCHAR(100),
    hipoalergenico            BOOLEAN        NOT NULL DEFAULT FALSE,
    edad_gestacional_sugerida INT,
    estado                    VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO',
    id_farmacia               INT            NOT NULL,
    id_usuario_creacion       INT,
    id_usuario_modificacion   INT,
    fecha_modificacion        TIMESTAMP,
    CONSTRAINT fk_maternidad_farmacia
        FOREIGN KEY (id_farmacia) REFERENCES FARMACIAS (id_farmacia),
    CONSTRAINT fk_maternidad_usuario_creacion
        FOREIGN KEY (id_usuario_creacion) REFERENCES USUARIOS (id_usuario),
    CONSTRAINT fk_maternidad_usuario_modificacion
        FOREIGN KEY (id_usuario_modificacion) REFERENCES USUARIOS (id_usuario),
    CONSTRAINT uq_maternidad_farmacia_codigo UNIQUE (id_farmacia, codigo),
    CONSTRAINT ck_maternidad_precio CHECK (precio >= 0),
    CONSTRAINT ck_maternidad_stock CHECK (stock >= 0),
    CONSTRAINT ck_maternidad_edad
        CHECK (edad_gestacional_sugerida IS NULL OR edad_gestacional_sugerida >= 0),
    CONSTRAINT ck_maternidad_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

COMMIT;
