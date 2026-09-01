-- ============================================================
-- CareStock — Script DDL (PostgreSQL)
-- Sistema de inventario y trazabilidad de medicamentos
-- ============================================================
-- Motor elegido: PostgreSQL. Se fija este motor (y no MySQL)
-- porque el script de seed/DML ya usa sintaxis exclusiva de
-- Postgres (ON CONFLICT, INTERVAL, EXCLUDED). Si en algún
-- momento se migra a MySQL, ese seed también debe reescribirse.
--
-- Orden de creación: respeta las dependencias de FK del modelo
-- ER (tablas sin dependencias primero).
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1. ROLES
-- ------------------------------------------------------------
CREATE TABLE ROLES (
    id_rol      SERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uq_roles_nombre UNIQUE (nombre_rol)
);

-- ------------------------------------------------------------
-- 2. CATEGORIAS
-- ------------------------------------------------------------
CREATE TABLE CATEGORIAS (
    id_categoria     SERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL,
    descripcion      VARCHAR(255),
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre_categoria)
);

-- ------------------------------------------------------------
-- 3. UBICACIONES
-- ------------------------------------------------------------
CREATE TABLE UBICACIONES (
    id_ubicacion SERIAL PRIMARY KEY,
    estante      VARCHAR(20) NOT NULL,
    nivel        VARCHAR(20) NOT NULL,
    descripcion  VARCHAR(255),
    CONSTRAINT uq_ubicaciones_estante_nivel UNIQUE (estante, nivel)
);

-- ------------------------------------------------------------
-- 4. USUARIOS (depende de ROLES)
-- ------------------------------------------------------------
CREATE TABLE USUARIOS (
    id_usuario      SERIAL PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    -- VARCHAR(60): un hash bcrypt válido mide exactamente 60
    -- caracteres. Fijar la longitud detecta hashes truncados o
    -- placeholders inválidos en tiempo de inserción.
    password_hash   VARCHAR(60) NOT NULL,
    id_rol          INTEGER NOT NULL,
    estado          VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol)
        REFERENCES ROLES (id_rol) ON DELETE RESTRICT,
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX idx_usuarios_id_rol ON USUARIOS (id_rol);

-- ------------------------------------------------------------
-- 5. MEDICAMENTOS (depende de CATEGORIAS)
-- ------------------------------------------------------------
CREATE TABLE MEDICAMENTOS (
    id_medicamento     SERIAL PRIMARY KEY,
    codigo_invima      VARCHAR(50) NOT NULL,
    nombre_comercial   VARCHAR(150) NOT NULL,
    principio_activo   VARCHAR(150) NOT NULL,
    concentracion      VARCHAR(50),
    forma_farmaceutica VARCHAR(50),
    id_categoria       INTEGER NOT NULL,
    stock_minimo       INTEGER NOT NULL DEFAULT 0,
    stock_total         INTEGER NOT NULL DEFAULT 0,
    estado              VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_medicamentos_invima UNIQUE (codigo_invima),
    CONSTRAINT fk_medicamentos_categoria FOREIGN KEY (id_categoria)
        REFERENCES CATEGORIAS (id_categoria) ON DELETE RESTRICT,
    CONSTRAINT ck_medicamentos_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT ck_medicamentos_stock_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT ck_medicamentos_stock_total CHECK (stock_total >= 0)
);

CREATE INDEX idx_medicamentos_id_categoria ON MEDICAMENTOS (id_categoria);

-- ------------------------------------------------------------
-- 6. LOTES (depende de MEDICAMENTOS y UBICACIONES)
-- ------------------------------------------------------------
CREATE TABLE LOTES (
    id_lote           SERIAL PRIMARY KEY,
    numero_lote       VARCHAR(50) NOT NULL,
    id_medicamento    INTEGER NOT NULL,
    cantidad_actual   INTEGER NOT NULL DEFAULT 0,
    fecha_vencimiento DATE NOT NULL,
    id_ubicacion      INTEGER NOT NULL,
    estado_lote       VARCHAR(15) NOT NULL DEFAULT 'DISPONIBLE',
    fecha_ingreso     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Un mismo numero_lote puede repetirse entre medicamentos
    -- distintos (lo asigna el proveedor); lo que debe ser único
    -- es la combinación numero_lote + medicamento.
    CONSTRAINT uq_lotes_numero_medicamento UNIQUE (numero_lote, id_medicamento),
    CONSTRAINT fk_lotes_medicamento FOREIGN KEY (id_medicamento)
        REFERENCES MEDICAMENTOS (id_medicamento) ON DELETE RESTRICT,
    CONSTRAINT fk_lotes_ubicacion FOREIGN KEY (id_ubicacion)
        REFERENCES UBICACIONES (id_ubicacion) ON DELETE RESTRICT,
    CONSTRAINT ck_lotes_cantidad CHECK (cantidad_actual >= 0),
    CONSTRAINT ck_lotes_estado CHECK (estado_lote IN ('DISPONIBLE', 'RESERVADO', 'AGOTADO', 'VENCIDO'))
);

CREATE INDEX idx_lotes_id_medicamento ON LOTES (id_medicamento);
CREATE INDEX idx_lotes_id_ubicacion ON LOTES (id_ubicacion);
CREATE INDEX idx_lotes_fecha_vencimiento ON LOTES (fecha_vencimiento);

-- ------------------------------------------------------------
-- 7. LOG_MOVIMIENTOS (depende de USUARIOS y LOTES)
-- ------------------------------------------------------------
CREATE TABLE LOG_MOVIMIENTOS (
    id_log            SERIAL PRIMARY KEY,
    id_usuario        INTEGER NOT NULL,
    id_lote           INTEGER NOT NULL,
    tipo_movimiento   VARCHAR(10) NOT NULL,
    cantidad_afectada INTEGER NOT NULL,
    detalle_cambio    VARCHAR(255),
    fecha_hora        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- ON DELETE RESTRICT (no CASCADE): un log de auditoría no
    -- debe poder desaparecer porque se borre el usuario o el
    -- lote que lo originó. Las bajas de usuario deben hacerse
    -- por estado = 'INACTIVO', nunca por DELETE físico.
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario)
        REFERENCES USUARIOS (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_log_lote FOREIGN KEY (id_lote)
        REFERENCES LOTES (id_lote) ON DELETE RESTRICT,
    CONSTRAINT ck_log_tipo CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    CONSTRAINT ck_log_cantidad CHECK (cantidad_afectada <> 0)
);

CREATE INDEX idx_log_id_usuario ON LOG_MOVIMIENTOS (id_usuario);
CREATE INDEX idx_log_id_lote ON LOG_MOVIMIENTOS (id_lote);
CREATE INDEX idx_log_fecha_hora ON LOG_MOVIMIENTOS (fecha_hora);

COMMIT;
