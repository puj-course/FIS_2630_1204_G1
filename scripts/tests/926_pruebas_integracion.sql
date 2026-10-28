-- Issue #926 - Pruebas de integracion PostgreSQL
-- Los cambios de esta prueba se revierten al finalizar.
-- Requiere los datos de 926_pre_migracion.sql.

BEGIN;

DO $$
DECLARE
    fa INTEGER;
    fb INTEGER;
    ua INTEGER;
    ub INTEGER;
    nuevo BIGINT;
    historico BIGINT;
BEGIN
    SELECT id_farmacia INTO fa FROM farmacias
    WHERE codigo = 'QA926-A'
      AND nombre = 'QA926 Farmacia A';

    SELECT id_farmacia INTO fb FROM farmacias
    WHERE codigo = 'QA926-B'
      AND nombre = 'QA926 Farmacia B';

    SELECT id_usuario INTO ua FROM usuarios
    WHERE email = 'qa926-a@example.invalid';

    SELECT id_usuario INTO ub FROM usuarios
    WHERE email = 'qa926-b@example.invalid';

    IF fa IS NULL OR fb IS NULL
       OR ua IS NULL OR ub IS NULL THEN
        RAISE EXCEPTION 'Faltan datos de preparacion QA926';
    END IF;

    -- BD-01: telefono E.164 valido
    INSERT INTO sms_destinatarios (
        id_usuario, id_farmacia, telefono
    )
    VALUES (ub, fb, '+573009991234')
    RETURNING id_destinatario INTO nuevo;

    RAISE NOTICE 'BD-01 OK: telefono valido';

    -- BD-02: telefono invalido
    BEGIN
        INSERT INTO sms_destinatarios (
            id_usuario, id_farmacia, telefono
        )
        VALUES (ub, fb, '300ABC');

        RAISE EXCEPTION 'BD-02 FALLO: acepto telefono invalido';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'BD-02 OK: telefono rechazado';
    END;

    -- BD-03: sin consentimiento no queda autorizado
    IF NOT EXISTS (
        SELECT 1 FROM sms_destinatarios
        WHERE id_destinatario = nuevo
          AND autorizado = FALSE
          AND autorizado_en IS NULL
    ) THEN
        RAISE EXCEPTION 'BD-03 FALLO: autorizacion implicita';
    END IF;

    RAISE NOTICE 'BD-03 OK: consentimiento requerido';

    -- BD-04: preferencias independientes
    UPDATE sms_destinatarios
    SET notifica_stock_critico = TRUE,
        notifica_vencimiento_critico = FALSE
    WHERE id_destinatario = nuevo;

    IF NOT EXISTS (
        SELECT 1 FROM sms_destinatarios
        WHERE id_destinatario = nuevo
          AND notifica_stock_critico = TRUE
          AND notifica_vencimiento_critico = FALSE
    ) THEN
        RAISE EXCEPTION 'BD-04 FALLO: preferencias';
    END IF;

    RAISE NOTICE 'BD-04 OK: preferencias persistidas';

    -- BD-05: horario nocturno y zona horaria
    UPDATE sms_destinatarios
    SET horario_inicio = '21:00',
        horario_fin = '06:00',
        zona_horaria = 'America/Bogota'
    WHERE id_destinatario = nuevo;

    IF NOT EXISTS (
        SELECT 1 FROM sms_destinatarios
        WHERE id_destinatario = nuevo
          AND horario_inicio = TIME '21:00'
          AND horario_fin = TIME '06:00'
          AND zona_horaria = 'America/Bogota'
    ) THEN
        RAISE EXCEPTION 'BD-05 FALLO: horario';
    END IF;

    RAISE NOTICE 'BD-05 OK: horario recuperado';

    -- BD-06: usuario de otra farmacia
    BEGIN
        INSERT INTO sms_destinatarios (
            id_usuario, id_farmacia, telefono
        )
        VALUES (ub, fa, '+573009991236');

        RAISE EXCEPTION 'BD-06 FALLO: farmacia cruzada';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'BD-06 OK: segregacion aplicada';
    END;

    -- BD-07: duplicado activo del mismo usuario
    BEGIN
        INSERT INTO sms_destinatarios (
            id_usuario, id_farmacia, telefono
        )
        VALUES (ub, fb, '+573009991237');

        RAISE EXCEPTION 'BD-07 FALLO: duplicado activo';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'BD-07 OK: duplicado rechazado';
    END;

    -- BD-08: actualizacion de configuracion
    UPDATE sms_destinatarios
    SET telefono = '+573009991235',
        notifica_stock_critico = FALSE,
        notifica_vencimiento_critico = TRUE
    WHERE id_destinatario = nuevo;

    IF NOT EXISTS (
        SELECT 1 FROM sms_destinatarios
        WHERE id_destinatario = nuevo
          AND telefono = '+573009991235'
          AND notifica_stock_critico = FALSE
          AND notifica_vencimiento_critico = TRUE
    ) THEN
        RAISE EXCEPTION 'BD-08 FALLO: actualizacion';
    END IF;

    RAISE NOTICE 'BD-08 OK: cambios conservados';

    -- BD-09: destinatario y alerta historicos
    SELECT id_destinatario INTO historico
    FROM sms_destinatarios
    WHERE id_usuario = ua
      AND id_farmacia = fa
      AND telefono = '+573001234567'
      AND autorizado = TRUE
      AND autorizado_en =
          TIMESTAMPTZ '2026-10-20 12:00:00+00'
      AND notifica_stock_critico = FALSE
      AND notifica_vencimiento_critico = FALSE;

    IF historico IS NULL OR NOT EXISTS (
        SELECT 1 FROM sms_alertas
        WHERE id_destinatario = historico
          AND id_farmacia = fa
          AND clave = 'QA926:LEGACY'
          AND mensaje = 'Alerta historica de prueba'
          AND estado = 'SIMULADA'
    ) THEN
        RAISE EXCEPTION 'BD-09 FALLO: datos historicos';
    END IF;

    RAISE NOTICE 'BD-09 OK: datos historicos preservados';

    -- BD-10: horario incompleto
    BEGIN
        UPDATE sms_destinatarios
        SET horario_fin = NULL
        WHERE id_destinatario = nuevo;

        RAISE EXCEPTION 'BD-10 FALLO: horario incompleto';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'BD-10 OK: horario invalido rechazado';
    END;

    -- BD-11: zona horaria inexistente
    BEGIN
        UPDATE sms_destinatarios
        SET zona_horaria = 'Fake/ZonaInexistente'
        WHERE id_destinatario = nuevo;

        RAISE EXCEPTION 'BD-11 FALLO: zona invalida';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'BD-11 OK: zona horaria rechazada';
    END;

    -- BD-12: alerta asociada a farmacia distinta
    BEGIN
        INSERT INTO sms_alertas (
            id_destinatario, id_farmacia,
            clave, mensaje
        )
        VALUES (
            historico, fb,
            'QA926:CRUZADA', 'Prueba de segregacion'
        );

        RAISE EXCEPTION 'BD-12 FALLO: alerta cruzada';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'BD-12 OK: alerta cruzada rechazada';
    END;

    RAISE NOTICE 'ISSUE #926: 12 PRUEBAS SATISFACTORIAS';
END $$;

-- Las inserciones y actualizaciones realizadas
-- en esta ejecucion no modifican los datos persistentes.
SELECT 'BD-01 a BD-12: OK' AS resultado;
ROLLBACK;
