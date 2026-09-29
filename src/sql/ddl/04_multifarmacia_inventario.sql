-- ==============================================================================
-- HU-67 / Issue #457
-- Aislamiento del inventario por farmacia
-- ==============================================================================

BEGIN;

-- 1. Catálogo de farmacias.
CREATE TABLE IF NOT EXISTS FARMACIAS (
    id_farmacia    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo         VARCHAR(40)  NOT NULL UNIQUE,
    nombre         VARCHAR(150) NOT NULL,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_farmacias_estado
        CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);

-- Sede inicial para conservar los datos actuales.
INSERT INTO FARMACIAS (
    codigo,
    nombre
)
VALUES (
    'SEDE-PRINCIPAL',
    'Farmacia Principal'
)
ON CONFLICT (codigo) DO NOTHING;

-- Función temporal para conservar compatibilidad con las inserciones
-- actuales mientras se completa la asociación usuario -> farmacia.
CREATE OR REPLACE FUNCTION fn_id_farmacia_principal()
RETURNS INTEGER
LANGUAGE SQL
STABLE
AS $$
    SELECT id_farmacia
    FROM FARMACIAS
    WHERE codigo = 'SEDE-PRINCIPAL'
    LIMIT 1
$$;

-- 2. Incorporar farmacia al inventario.
ALTER TABLE MEDICAMENTOS
    ADD COLUMN IF NOT EXISTS id_farmacia INTEGER;

-- Los registros históricos quedan inicialmente asociados
-- con la Farmacia Principal.
UPDATE MEDICAMENTOS
SET id_farmacia = fn_id_farmacia_principal()
WHERE id_farmacia IS NULL;

-- Mientras terminamos el contexto multifarmacia,
-- nuevos registros se asignan a la sede principal.
ALTER TABLE MEDICAMENTOS
    ALTER COLUMN id_farmacia
    SET DEFAULT fn_id_farmacia_principal();

ALTER TABLE MEDICAMENTOS
    ALTER COLUMN id_farmacia
    SET NOT NULL;

-- Clave foránea.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_medicamentos_farmacia'
    ) THEN

        ALTER TABLE MEDICAMENTOS
            ADD CONSTRAINT fk_medicamentos_farmacia
            FOREIGN KEY (id_farmacia)
            REFERENCES FARMACIAS (id_farmacia);

    END IF;
END $$;

-- 3. En un sistema multifarmacia el mismo código INVIMA
-- puede existir en más de una sede.
ALTER TABLE MEDICAMENTOS
    DROP CONSTRAINT IF EXISTS medicamentos_codigo_invima_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_medicamentos_farmacia_invima
    ON MEDICAMENTOS (
        id_farmacia,
        codigo_invima
    );

CREATE INDEX IF NOT EXISTS ix_medicamentos_farmacia
    ON MEDICAMENTOS (
        id_farmacia
    );

COMMENT ON COLUMN MEDICAMENTOS.id_farmacia IS
    'Farmacia propietaria del registro de inventario del medicamento.';

COMMIT;
