

BEGIN;


-- 1) Nueva columna: presentacion

ALTER TABLE MEDICAMENTOS
    ADD COLUMN IF NOT EXISTS presentacion VARCHAR(100);

COMMENT ON COLUMN MEDICAMENTOS.presentacion IS
    'Presentación comercial del medicamento (ej. Caja x20 tabletas, Frasco x120ml)';


-- 2) Columnas de auditoría de creación

ALTER TABLE MEDICAMENTOS
    ADD COLUMN IF NOT EXISTS id_usuario_creacion INTEGER,
    ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_medicamento_usuario_creacion'
    ) THEN
        ALTER TABLE MEDICAMENTOS
            ADD CONSTRAINT fk_medicamento_usuario_creacion
            FOREIGN KEY (id_usuario_creacion)
            REFERENCES USUARIOS (id_usuario);
    END IF;
END $$;

COMMENT ON COLUMN MEDICAMENTOS.id_usuario_creacion IS
    'Usuario (Auxiliar de Farmacia) que registró el medicamento. Obligatorio.';
COMMENT ON COLUMN MEDICAMENTOS.fecha_creacion IS
    'Fecha y hora de creación del registro (auditoría)';


-- 3) Backfill de seguridad antes de aplicar NOT NULL

UPDATE MEDICAMENTOS
SET presentacion = 'SIN ESPECIFICAR'
WHERE presentacion IS NULL;


UPDATE MEDICAMENTOS
SET id_usuario_creacion = (
    SELECT id_usuario FROM USUARIOS WHERE email = 'admin@carestock.com'
)
WHERE id_usuario_creacion IS NULL;


-- 4) Constraints NOT NULL en campos obligatorios de la HU

ALTER TABLE MEDICAMENTOS
    ALTER COLUMN nombre_comercial     SET NOT NULL,
    ALTER COLUMN principio_activo     SET NOT NULL,
    ALTER COLUMN concentracion        SET NOT NULL,
    ALTER COLUMN forma_farmaceutica   SET NOT NULL,
    ALTER COLUMN presentacion         SET NOT NULL,
    ALTER COLUMN id_categoria         SET NOT NULL,
    ALTER COLUMN id_usuario_creacion  SET NOT NULL;

COMMIT;

