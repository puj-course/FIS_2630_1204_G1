-- ==============================================================================
-- DDL: Esquema principal de la base de datos CareStock
-- Ubicación: src/sql/ddl/01_schema_carestock.sql
-- ==============================================================================

-- 1. TABLAS INDEPENDIENTES
CREATE TABLE IF NOT EXISTS ROLES (
    id_rol       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_rol   VARCHAR(50)  NOT NULL UNIQUE,
    descripcion  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS CATEGORIAS (
    id_categoria     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS UBICACIONES (
    id_ubicacion INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estante      VARCHAR(20)  NOT NULL,
    nivel        VARCHAR(20)  NOT NULL,
    descripcion  VARCHAR(255),
    CONSTRAINT uq_ubicacion UNIQUE (estante, nivel)
);

-- 2. TABLAS CON DEPENDENCIAS PRIMARIAS
CREATE TABLE IF NOT EXISTS USUARIOS (
    id_usuario      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    id_rol          INT          NOT NULL,
    estado          VARCHAR(20)  DEFAULT 'ACTIVO' NOT NULL,
    fecha_creacion  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES ROLES (id_rol),
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'))
);

CREATE TABLE IF NOT EXISTS MEDICAMENTOS (
    id_medicamento     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_invima      VARCHAR(50)  NOT NULL UNIQUE,
    nombre_comercial   VARCHAR(150) NOT NULL,
    principio_activo   VARCHAR(150) NOT NULL,
    concentracion      VARCHAR(50),
    forma_farmaceutica VARCHAR(50),
    id_categoria       INT          NOT NULL,
    stock_minimo       INT          DEFAULT 0 NOT NULL,
    stock_total        INT          DEFAULT 0 NOT NULL,
    estado             VARCHAR(20)  DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_medicamentos_categoria FOREIGN KEY (id_categoria) REFERENCES CATEGORIAS (id_categoria),
    CONSTRAINT ck_medicamentos_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'DESCONTINUADO')),
    CONSTRAINT ck_medicamentos_stock CHECK (stock_total >= 0)
);

-- 3. TABLAS DEPENDIENTES (TRANSACCIONALES)
CREATE TABLE IF NOT EXISTS LOTES (
    id_lote           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_lote       VARCHAR(50)  NOT NULL,
    id_medicamento    INT          NOT NULL,
    cantidad_actual   INT          DEFAULT 0 NOT NULL,
    fecha_vencimiento DATE         NOT NULL,
    id_ubicacion      INT          NOT NULL,
    id_usuario        INT          NOT NULL,
    estado_lote       VARCHAR(20)  DEFAULT 'DISPONIBLE' NOT NULL,
    fecha_ingreso     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_lotes_medicamento
        FOREIGN KEY (id_medicamento)
        REFERENCES MEDICAMENTOS (id_medicamento),

    CONSTRAINT fk_lotes_ubicacion
        FOREIGN KEY (id_ubicacion)
        REFERENCES UBICACIONES (id_ubicacion),

    CONSTRAINT fk_lotes_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES USUARIOS (id_usuario),

    CONSTRAINT ck_lotes_estado
        CHECK (estado_lote IN ('DISPONIBLE', 'AGOTADO', 'VENCIDO', 'RETENIDO')),

    CONSTRAINT ck_lotes_cantidad
        CHECK (cantidad_actual >= 0),

    CONSTRAINT uq_lotes_numero
        UNIQUE (numero_lote, id_medicamento)
);
CREATE TABLE IF NOT EXISTS LOG_MOVIMIENTOS (
    id_log            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario        INT          NOT NULL,
    id_lote           INT          NOT NULL,
    tipo_movimiento   VARCHAR(20)  NOT NULL,
    cantidad_afectada INT          NOT NULL,
    detalle_cambio    VARCHAR(500),
    fecha_hora        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS (id_usuario),
    CONSTRAINT fk_log_lote FOREIGN KEY (id_lote) REFERENCES LOTES (id_lote),
    CONSTRAINT ck_log_tipo CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE'))
);

-- 4. ÍNDICES
CREATE INDEX IF NOT EXISTS ix_lotes_vencimiento
    ON LOTES (fecha_vencimiento);

CREATE INDEX IF NOT EXISTS ix_lotes_usuario
    ON LOTES (id_usuario);

CREATE INDEX IF NOT EXISTS ix_log_fecha
    ON LOG_MOVIMIENTOS (fecha_hora);

CREATE INDEX IF NOT EXISTS ix_medicamentos_nombre
    ON MEDICAMENTOS (nombre_comercial);


-- =============================================================================
-- PREFERENCIAS INDIVIDUALES DE SESIÓN
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
