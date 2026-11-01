-- Issue #926 - Preparacion de datos historicos
-- EXCLUSIVAMENTE para una base PostgreSQL de pruebas.

BEGIN;

DO $$
DECLARE
    fa INTEGER;
    fb INTEGER;
    rol INTEGER;
    ua INTEGER;
    ub INTEGER;
    destinatario BIGINT;
BEGIN
    INSERT INTO roles (nombre_rol, descripcion)
    VALUES ('ADMINISTRADOR', 'Administrador de farmacia')
    ON CONFLICT (nombre_rol) DO NOTHING;

    SELECT id_rol INTO rol
    FROM roles WHERE nombre_rol = 'ADMINISTRADOR';

    INSERT INTO farmacias (codigo, nombre)
    SELECT 'QA926-A', 'QA926 Farmacia A'
    WHERE NOT EXISTS (
        SELECT 1 FROM farmacias
        WHERE codigo = 'QA926-A'
          AND nombre = 'QA926 Farmacia A'
    );

    INSERT INTO farmacias (codigo, nombre)
    SELECT 'QA926-B', 'QA926 Farmacia B'
    WHERE NOT EXISTS (
        SELECT 1 FROM farmacias
        WHERE codigo = 'QA926-B'
          AND nombre = 'QA926 Farmacia B'
    );

    SELECT id_farmacia INTO fa
    FROM farmacias
    WHERE codigo = 'QA926-A'
      AND nombre = 'QA926 Farmacia A';

    SELECT id_farmacia INTO fb
    FROM farmacias
    WHERE codigo = 'QA926-B'
      AND nombre = 'QA926 Farmacia B';

    INSERT INTO usuarios (
        nombre_completo, email, password_hash,
        id_rol, id_farmacia
    )
    VALUES (
        'Usuario QA A', 'qa926-a@example.invalid',
        'QA_NO_LOGIN', rol, fa
    )
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO usuarios (
        nombre_completo, email, password_hash,
        id_rol, id_farmacia
    )
    VALUES (
        'Usuario QA B', 'qa926-b@example.invalid',
        'QA_NO_LOGIN', rol, fb
    )
    ON CONFLICT (email) DO NOTHING;

    SELECT id_usuario INTO ua
    FROM usuarios WHERE email = 'qa926-a@example.invalid';

    SELECT id_usuario INTO ub
    FROM usuarios WHERE email = 'qa926-b@example.invalid';

    IF ua IS NULL OR ub IS NULL THEN
        RAISE EXCEPTION 'No se crearon los usuarios QA';
    END IF;

    -- Registro historico que debe sobrevivir a la migracion.
    INSERT INTO sms_destinatarios (
        id_usuario, id_farmacia, telefono,
        autorizado, autorizado_en, activo
    )
    VALUES (
        ua, fa, '+573001234567',
        TRUE, '2026-10-20 12:00:00+00', TRUE
    )
    ON CONFLICT (id_usuario, id_farmacia, telefono)
    DO NOTHING;

    SELECT id_destinatario INTO destinatario
    FROM sms_destinatarios
    WHERE id_usuario = ua
      AND id_farmacia = fa
      AND telefono = '+573001234567';

    INSERT INTO sms_alertas (
        id_destinatario, id_farmacia,
        clave, mensaje, estado
    )
    VALUES (
        destinatario, fa, 'QA926:LEGACY',
        'Alerta historica de prueba', 'SIMULADA'
    )
    ON CONFLICT (id_destinatario, clave)
    WHERE activa DO NOTHING;

    RAISE NOTICE 'Datos historicos QA926 preparados';
END $$;

COMMIT;
