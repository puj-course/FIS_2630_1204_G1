-- =========================================================
-- CareStock - Issue #926
-- Persistencia de destinatarios y preferencias SMS
-- Requiere migracion #885
-- =========================================================

BEGIN;

SET LOCAL lock_timeout = '5s';

-- 1. Preferencias: deshabilitadas por defecto.
ALTER TABLE sms_destinatarios
    ADD COLUMN IF NOT EXISTS
        notifica_stock_critico BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS
        notifica_vencimiento_critico BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS
        horario_inicio TIME,
    ADD COLUMN IF NOT EXISTS
        horario_fin TIME,
    ADD COLUMN IF NOT EXISTS
        zona_horaria VARCHAR(64) NOT NULL DEFAULT 'America/Bogota';

-- 2. Validar registros historicos.
-- Nunca corregir o eliminar registros automaticamente.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM sms_destinatarios d
        LEFT JOIN usuarios u
          ON u.id_usuario = d.id_usuario
        WHERE u.id_usuario IS NULL
           OR u.id_farmacia IS DISTINCT FROM d.id_farmacia
    ) THEN
        RAISE EXCEPTION
            'Existen destinatarios con farmacia inconsistente';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM sms_destinatarios
        WHERE activo = TRUE
        GROUP BY id_usuario, id_farmacia
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Existen destinatarios activos duplicados';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM sms_alertas a
        JOIN sms_destinatarios d
          ON d.id_destinatario = a.id_destinatario
        WHERE a.id_farmacia <> d.id_farmacia
    ) THEN
        RAISE EXCEPTION
            'Existen alertas asociadas a otra farmacia';
    END IF;
END $$;

-- 3. Integridad entre usuarios, farmacias y alertas.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_usuarios_farmacia_sms_926'
          AND conrelid = 'usuarios'::regclass
    ) THEN
        ALTER TABLE usuarios
        ADD CONSTRAINT uq_usuarios_farmacia_sms_926
        UNIQUE (id_usuario, id_farmacia);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_sms_usuario_farmacia_926'
          AND conrelid = 'sms_destinatarios'::regclass
    ) THEN
        ALTER TABLE sms_destinatarios
        ADD CONSTRAINT fk_sms_usuario_farmacia_926
        FOREIGN KEY (id_usuario, id_farmacia)
        REFERENCES usuarios(id_usuario, id_farmacia)
        ON UPDATE RESTRICT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_sms_destinatario_farmacia_926'
          AND conrelid = 'sms_destinatarios'::regclass
    ) THEN
        ALTER TABLE sms_destinatarios
        ADD CONSTRAINT uq_sms_destinatario_farmacia_926
        UNIQUE (id_destinatario, id_farmacia);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_sms_alerta_farmacia_926'
          AND conrelid = 'sms_alertas'::regclass
    ) THEN
        ALTER TABLE sms_alertas
        ADD CONSTRAINT fk_sms_alerta_farmacia_926
        FOREIGN KEY (id_destinatario, id_farmacia)
        REFERENCES sms_destinatarios(
            id_destinatario, id_farmacia
        )
        ON UPDATE RESTRICT;
    END IF;
END $$;

-- 4. Un solo destinatario activo por usuario y farmacia.
CREATE UNIQUE INDEX IF NOT EXISTS
    uq_sms_destinatario_activo_926
ON sms_destinatarios(id_usuario, id_farmacia)
WHERE activo = TRUE;

-- 5. Validar horarios completos.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_sms_horario_926'
          AND conrelid = 'sms_destinatarios'::regclass
    ) THEN
        ALTER TABLE sms_destinatarios
        ADD CONSTRAINT ck_sms_horario_926
        CHECK (
            (horario_inicio IS NULL AND horario_fin IS NULL)
            OR
            (
                horario_inicio IS NOT NULL
                AND horario_fin IS NOT NULL
                AND horario_inicio <> horario_fin
            )
        );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_sms_zona_no_vacia_926'
          AND conrelid = 'sms_destinatarios'::regclass
    ) THEN
        ALTER TABLE sms_destinatarios
        ADD CONSTRAINT ck_sms_zona_no_vacia_926
        CHECK (LENGTH(BTRIM(zona_horaria)) > 0);
    END IF;
END $$;

-- 6. Validar zona horaria PostgreSQL.
CREATE OR REPLACE FUNCTION fn_sms_validar_zona_926()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_timezone_names
        WHERE name = NEW.zona_horaria
    ) THEN
        RAISE EXCEPTION
            'Zona horaria no reconocida: %',
            NEW.zona_horaria
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS
    trg_sms_validar_zona_926 ON sms_destinatarios;

CREATE TRIGGER trg_sms_validar_zona_926
BEFORE INSERT OR UPDATE OF zona_horaria
ON sms_destinatarios
FOR EACH ROW
EXECUTE FUNCTION fn_sms_validar_zona_926();

-- 7. Documentacion del esquema.
COMMENT ON COLUMN sms_destinatarios.notifica_stock_critico
IS 'Preferencia para SMS de stock critico';

COMMENT ON COLUMN sms_destinatarios.notifica_vencimiento_critico
IS 'Preferencia para SMS de vencimiento critico';

COMMENT ON COLUMN sms_destinatarios.horario_inicio
IS 'Inicio de ventana para notificaciones no urgentes';

COMMENT ON COLUMN sms_destinatarios.horario_fin
IS 'Fin de ventana para notificaciones no urgentes';

COMMENT ON COLUMN sms_destinatarios.zona_horaria
IS 'Zona horaria IANA del horario configurado';

COMMIT;
