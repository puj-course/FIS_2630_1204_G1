

BEGIN;

-- 1) Quitar constraints NOT NULL agregadas
ALTER TABLE MEDICAMENTOS
    ALTER COLUMN nombre_comercial     DROP NOT NULL,
    ALTER COLUMN principio_activo     DROP NOT NULL,
    ALTER COLUMN concentracion        DROP NOT NULL,
    ALTER COLUMN forma_farmaceutica   DROP NOT NULL,
    ALTER COLUMN presentacion         DROP NOT NULL,
    ALTER COLUMN id_categoria         DROP NOT NULL,
    ALTER COLUMN id_usuario_creacion  DROP NOT NULL;

-- 2) Quitar FK de auditoría
ALTER TABLE MEDICAMENTOS
    DROP CONSTRAINT IF EXISTS fk_medicamento_usuario_creacion;

-- 3) Quitar columnas agregadas
ALTER TABLE MEDICAMENTOS
    DROP COLUMN IF EXISTS presentacion,
    DROP COLUMN IF EXISTS id_usuario_creacion,
    DROP COLUMN IF EXISTS fecha_creacion;

COMMIT;
