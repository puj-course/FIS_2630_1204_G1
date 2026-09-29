-- =============================================================================
-- CRUD seguro de medicamentos y lotes por farmacia
--
-- Roles autorizados:
--   SUPER_ADMIN
--   ADMINISTRADOR
--
-- DELETE lógico:
--   MEDICAMENTOS -> INACTIVO
--   LOTES        -> RETENIDO
-- =============================================================================

BEGIN;

-- =============================================================================
-- 1. TRAZABILIDAD
-- =============================================================================

ALTER TABLE MEDICAMENTOS
    ADD COLUMN IF NOT EXISTS presentacion VARCHAR(150),
    ADD COLUMN IF NOT EXISTS id_usuario_creacion INTEGER,
    ADD COLUMN IF NOT EXISTS id_usuario_modificacion INTEGER,
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

ALTER TABLE LOTES
    ADD COLUMN IF NOT EXISTS id_usuario_modificacion INTEGER,
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;


DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_medicamentos_usuario_creacion'
    ) THEN

        ALTER TABLE MEDICAMENTOS
            ADD CONSTRAINT fk_medicamentos_usuario_creacion
            FOREIGN KEY (id_usuario_creacion)
            REFERENCES USUARIOS(id_usuario);

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_medicamentos_usuario_modificacion'
    ) THEN

        ALTER TABLE MEDICAMENTOS
            ADD CONSTRAINT fk_medicamentos_usuario_modificacion
            FOREIGN KEY (id_usuario_modificacion)
            REFERENCES USUARIOS(id_usuario);

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_lotes_usuario_modificacion'
    ) THEN

        ALTER TABLE LOTES
            ADD CONSTRAINT fk_lotes_usuario_modificacion
            FOREIGN KEY (id_usuario_modificacion)
            REFERENCES USUARIOS(id_usuario);

    END IF;

END $$;


-- =============================================================================
-- 2. FUNCIÓN CENTRAL DE AUTORIZACIÓN
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_validar_gestion_farmacia(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE

    v_rol VARCHAR(50);
    v_id_farmacia_actor INTEGER;

BEGIN

    IF p_id_farmacia IS NULL
       OR p_id_farmacia <= 0 THEN

        RAISE EXCEPTION
            'La farmacia indicada no es válida';

    END IF;


    SELECT
        r.nombre_rol,
        u.id_farmacia
    INTO
        v_rol,
        v_id_farmacia_actor
    FROM USUARIOS u
    INNER JOIN ROLES r
        ON r.id_rol = u.id_rol
    WHERE u.id_usuario = p_id_usuario_actor
      AND u.estado = 'ACTIVO';


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'El usuario actor no existe o no está activo';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM FARMACIAS
        WHERE id_farmacia = p_id_farmacia
          AND estado = 'ACTIVA'
    ) THEN

        RAISE EXCEPTION
            'La farmacia indicada no existe o está inactiva';

    END IF;


    -- SUPER_ADMIN puede operar cualquier farmacia activa.
    IF UPPER(v_rol) = 'SUPER_ADMIN' THEN
        RETURN;
    END IF;


    -- ADMINISTRADOR únicamente su propia farmacia.
    IF UPPER(v_rol) = 'ADMINISTRADOR' THEN

        IF v_id_farmacia_actor IS NULL
           OR v_id_farmacia_actor <> p_id_farmacia THEN

            RAISE EXCEPTION
                'El administrador no puede gestionar otra farmacia';

        END IF;

        RETURN;

    END IF;


    RAISE EXCEPTION
        'El rol % no está autorizado para gestionar inventario',
        v_rol;

END;
$$;


-- =============================================================================
-- 3. MEDICAMENTO - CREATE
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_crear_medicamento_seguro(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER,
    p_codigo_invima VARCHAR,
    p_nombre_comercial VARCHAR,
    p_principio_activo VARCHAR,
    p_concentracion VARCHAR,
    p_forma_farmaceutica VARCHAR,
    p_presentacion VARCHAR,
    p_categoria VARCHAR,
    p_stock_minimo INTEGER
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE

    v_id_categoria INTEGER;
    v_id_medicamento INTEGER;

BEGIN

    PERFORM fn_validar_gestion_farmacia(
        p_id_usuario_actor,
        p_id_farmacia
    );


    IF COALESCE(TRIM(p_codigo_invima), '') = '' THEN
        RAISE EXCEPTION 'El código INVIMA es obligatorio';
    END IF;


    IF COALESCE(TRIM(p_nombre_comercial), '') = '' THEN
        RAISE EXCEPTION 'El nombre comercial es obligatorio';
    END IF;


    IF COALESCE(TRIM(p_principio_activo), '') = '' THEN
        RAISE EXCEPTION 'El principio activo es obligatorio';
    END IF;


    IF p_stock_minimo IS NULL
       OR p_stock_minimo < 0 THEN

        RAISE EXCEPTION
            'El stock mínimo no puede ser negativo';

    END IF;


    SELECT id_categoria
    INTO v_id_categoria
    FROM CATEGORIAS
    WHERE UPPER(nombre_categoria)
          = UPPER(TRIM(p_categoria));


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'La categoría % no existe',
            p_categoria;

    END IF;


    INSERT INTO MEDICAMENTOS (
        codigo_invima,
        nombre_comercial,
        principio_activo,
        concentracion,
        forma_farmaceutica,
        presentacion,
        id_categoria,
        stock_minimo,
        stock_total,
        estado,
        id_farmacia,
        id_usuario_creacion,
        id_usuario_modificacion,
        fecha_modificacion
    )
    VALUES (
        TRIM(p_codigo_invima),
        TRIM(p_nombre_comercial),
        TRIM(p_principio_activo),
        NULLIF(TRIM(p_concentracion), ''),
        NULLIF(TRIM(p_forma_farmaceutica), ''),
        NULLIF(TRIM(p_presentacion), ''),
        v_id_categoria,
        p_stock_minimo,
        0,
        'ACTIVO',
        p_id_farmacia,
        p_id_usuario_actor,
        p_id_usuario_actor,
        CURRENT_TIMESTAMP
    )
    RETURNING id_medicamento
    INTO v_id_medicamento;


    RETURN v_id_medicamento;

END;
$$;


-- =============================================================================
-- 4. MEDICAMENTO - UPDATE
--
-- stock_total no puede modificarse manualmente.
-- id_farmacia tampoco puede modificarse.
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_actualizar_medicamento_seguro(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER,
    p_id_medicamento INTEGER,
    p_codigo_invima VARCHAR,
    p_nombre_comercial VARCHAR,
    p_principio_activo VARCHAR,
    p_concentracion VARCHAR,
    p_forma_farmaceutica VARCHAR,
    p_presentacion VARCHAR,
    p_categoria VARCHAR,
    p_stock_minimo INTEGER
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE

    v_id_categoria INTEGER;
    v_id_farmacia_real INTEGER;

BEGIN

    PERFORM fn_validar_gestion_farmacia(
        p_id_usuario_actor,
        p_id_farmacia
    );


    SELECT id_farmacia
    INTO v_id_farmacia_real
    FROM MEDICAMENTOS
    WHERE id_medicamento = p_id_medicamento;


    IF NOT FOUND
       OR v_id_farmacia_real <> p_id_farmacia THEN

        RAISE EXCEPTION
            'El medicamento no pertenece a la farmacia seleccionada';

    END IF;


    IF COALESCE(TRIM(p_codigo_invima), '') = ''
       OR COALESCE(TRIM(p_nombre_comercial), '') = ''
       OR COALESCE(TRIM(p_principio_activo), '') = '' THEN

        RAISE EXCEPTION
            'Los datos principales del medicamento son obligatorios';

    END IF;


    IF p_stock_minimo IS NULL
       OR p_stock_minimo < 0 THEN

        RAISE EXCEPTION
            'El stock mínimo no puede ser negativo';

    END IF;


    SELECT id_categoria
    INTO v_id_categoria
    FROM CATEGORIAS
    WHERE UPPER(nombre_categoria)
          = UPPER(TRIM(p_categoria));


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'La categoría % no existe',
            p_categoria;

    END IF;


    UPDATE MEDICAMENTOS
    SET
        codigo_invima = TRIM(p_codigo_invima),
        nombre_comercial = TRIM(p_nombre_comercial),
        principio_activo = TRIM(p_principio_activo),
        concentracion =
            NULLIF(TRIM(p_concentracion), ''),
        forma_farmaceutica =
            NULLIF(TRIM(p_forma_farmaceutica), ''),
        presentacion =
            NULLIF(TRIM(p_presentacion), ''),
        id_categoria = v_id_categoria,
        stock_minimo = p_stock_minimo,
        id_usuario_modificacion = p_id_usuario_actor,
        fecha_modificacion = CURRENT_TIMESTAMP
    WHERE id_medicamento = p_id_medicamento
      AND id_farmacia = p_id_farmacia;

END;
$$;


-- =============================================================================
-- 5. MEDICAMENTO - DELETE LÓGICO / REACTIVACIÓN
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_cambiar_estado_medicamento_seguro(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER,
    p_id_medicamento INTEGER,
    p_nuevo_estado VARCHAR
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE

    v_estado VARCHAR(20);

BEGIN

    PERFORM fn_validar_gestion_farmacia(
        p_id_usuario_actor,
        p_id_farmacia
    );


    v_estado :=
        UPPER(TRIM(p_nuevo_estado));


    IF v_estado NOT IN (
        'ACTIVO',
        'INACTIVO'
    ) THEN

        RAISE EXCEPTION
            'Estado de medicamento inválido';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM MEDICAMENTOS
        WHERE id_medicamento = p_id_medicamento
          AND id_farmacia = p_id_farmacia
    ) THEN

        RAISE EXCEPTION
            'El medicamento no pertenece a la farmacia seleccionada';

    END IF;


    IF v_estado = 'INACTIVO'
       AND EXISTS (
            SELECT 1
            FROM LOTES l
            WHERE l.id_medicamento = p_id_medicamento
              AND l.estado_lote = 'DISPONIBLE'
              AND l.cantidad_actual > 0
       ) THEN

        RAISE EXCEPTION
            'No se puede desactivar un medicamento con lotes disponibles. Retenga o agote primero sus lotes';

    END IF;


    UPDATE MEDICAMENTOS
    SET
        estado = v_estado,
        id_usuario_modificacion = p_id_usuario_actor,
        fecha_modificacion = CURRENT_TIMESTAMP
    WHERE id_medicamento = p_id_medicamento
      AND id_farmacia = p_id_farmacia;

END;
$$;


-- =============================================================================
-- 6. LOTE - CREATE
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_crear_lote_seguro(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER,
    p_id_medicamento INTEGER,
    p_numero_lote VARCHAR,
    p_cantidad INTEGER,
    p_fecha_vencimiento DATE,
    p_id_ubicacion INTEGER
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE

    v_id_lote INTEGER;

BEGIN

    PERFORM fn_validar_gestion_farmacia(
        p_id_usuario_actor,
        p_id_farmacia
    );


    IF NOT EXISTS (
        SELECT 1
        FROM MEDICAMENTOS
        WHERE id_medicamento = p_id_medicamento
          AND id_farmacia = p_id_farmacia
          AND estado = 'ACTIVO'
    ) THEN

        RAISE EXCEPTION
            'El medicamento no pertenece a la farmacia o no está activo';

    END IF;


    IF COALESCE(TRIM(p_numero_lote), '') = '' THEN
        RAISE EXCEPTION 'El número de lote es obligatorio';
    END IF;


    IF p_cantidad IS NULL
       OR p_cantidad <= 0 THEN

        RAISE EXCEPTION
            'La cantidad inicial debe ser mayor que cero';

    END IF;


    IF p_fecha_vencimiento IS NULL
       OR p_fecha_vencimiento <= CURRENT_DATE THEN

        RAISE EXCEPTION
            'La fecha de vencimiento debe ser futura';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM UBICACIONES
        WHERE id_ubicacion = p_id_ubicacion
    ) THEN

        RAISE EXCEPTION
            'La ubicación indicada no existe';

    END IF;


    INSERT INTO LOTES (
        numero_lote,
        id_medicamento,
        cantidad_actual,
        fecha_vencimiento,
        id_ubicacion,
        id_usuario,
        estado_lote,
        id_usuario_modificacion,
        fecha_modificacion
    )
    VALUES (
        TRIM(p_numero_lote),
        p_id_medicamento,
        p_cantidad,
        p_fecha_vencimiento,
        p_id_ubicacion,
        p_id_usuario_actor,
        'DISPONIBLE',
        p_id_usuario_actor,
        CURRENT_TIMESTAMP
    )
    RETURNING id_lote
    INTO v_id_lote;


    INSERT INTO LOG_MOVIMIENTOS (
        id_usuario,
        id_lote,
        tipo_movimiento,
        cantidad_afectada,
        detalle_cambio
    )
    VALUES (
        p_id_usuario_actor,
        v_id_lote,
        'ENTRADA',
        p_cantidad,
        'Creación de lote desde gestión multifarmacia'
    );


    RETURN v_id_lote;

END;
$$;


-- =============================================================================
-- 7. LOTE - UPDATE
--
-- No se permite mover un lote a otro medicamento ni a otra farmacia.
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_actualizar_lote_seguro(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER,
    p_id_lote INTEGER,
    p_numero_lote VARCHAR,
    p_cantidad INTEGER,
    p_fecha_vencimiento DATE,
    p_id_ubicacion INTEGER
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE

    v_cantidad_anterior INTEGER;
    v_estado_anterior VARCHAR(20);

BEGIN

    PERFORM fn_validar_gestion_farmacia(
        p_id_usuario_actor,
        p_id_farmacia
    );


    SELECT
        l.cantidad_actual,
        l.estado_lote
    INTO
        v_cantidad_anterior,
        v_estado_anterior
    FROM LOTES l
    INNER JOIN MEDICAMENTOS m
        ON m.id_medicamento =
           l.id_medicamento
    WHERE l.id_lote = p_id_lote
      AND m.id_farmacia =
          p_id_farmacia;


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'El lote no pertenece a la farmacia seleccionada';

    END IF;


    IF COALESCE(TRIM(p_numero_lote), '') = '' THEN
        RAISE EXCEPTION 'El número de lote es obligatorio';
    END IF;


    IF p_cantidad IS NULL
       OR p_cantidad < 0 THEN

        RAISE EXCEPTION
            'La cantidad no puede ser negativa';

    END IF;


    IF p_fecha_vencimiento IS NULL THEN

        RAISE EXCEPTION
            'La fecha de vencimiento es obligatoria';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM UBICACIONES
        WHERE id_ubicacion =
              p_id_ubicacion
    ) THEN

        RAISE EXCEPTION
            'La ubicación indicada no existe';

    END IF;


    UPDATE LOTES
    SET
        numero_lote =
            TRIM(p_numero_lote),

        cantidad_actual =
            p_cantidad,

        fecha_vencimiento =
            p_fecha_vencimiento,

        id_ubicacion =
            p_id_ubicacion,

        estado_lote =
            CASE
                WHEN v_estado_anterior = 'RETENIDO'
                    THEN 'RETENIDO'

                WHEN p_cantidad = 0
                    THEN 'AGOTADO'

                WHEN p_fecha_vencimiento <= CURRENT_DATE
                    THEN 'VENCIDO'

                ELSE 'DISPONIBLE'
            END,

        id_usuario_modificacion =
            p_id_usuario_actor,

        fecha_modificacion =
            CURRENT_TIMESTAMP

    WHERE id_lote =
          p_id_lote;


    INSERT INTO LOG_MOVIMIENTOS (
        id_usuario,
        id_lote,
        tipo_movimiento,
        cantidad_afectada,
        detalle_cambio
    )
    VALUES (
        p_id_usuario_actor,
        p_id_lote,
        'AJUSTE',
        ABS(
            p_cantidad
            - v_cantidad_anterior
        ),
        'Edición administrativa del lote. Cantidad anterior: '
        || v_cantidad_anterior
        || ', cantidad nueva: '
        || p_cantidad
    );

END;
$$;


-- =============================================================================
-- 8. LOTE - DELETE LÓGICO / REACTIVACIÓN
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_cambiar_estado_lote_seguro(
    p_id_usuario_actor INTEGER,
    p_id_farmacia INTEGER,
    p_id_lote INTEGER,
    p_nuevo_estado VARCHAR
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE

    v_estado VARCHAR(20);
    v_cantidad INTEGER;
    v_fecha DATE;
    v_estado_medicamento VARCHAR(20);

BEGIN

    PERFORM fn_validar_gestion_farmacia(
        p_id_usuario_actor,
        p_id_farmacia
    );


    v_estado :=
        UPPER(TRIM(p_nuevo_estado));


    IF v_estado NOT IN (
        'RETENIDO',
        'DISPONIBLE'
    ) THEN

        RAISE EXCEPTION
            'Solo se permite retener o reactivar un lote';

    END IF;


    SELECT
        l.cantidad_actual,
        l.fecha_vencimiento,
        m.estado
    INTO
        v_cantidad,
        v_fecha,
        v_estado_medicamento
    FROM LOTES l
    INNER JOIN MEDICAMENTOS m
        ON m.id_medicamento =
           l.id_medicamento
    WHERE l.id_lote = p_id_lote
      AND m.id_farmacia =
          p_id_farmacia;


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'El lote no pertenece a la farmacia seleccionada';

    END IF;


    IF v_estado = 'DISPONIBLE' THEN

        IF v_estado_medicamento <> 'ACTIVO' THEN

            RAISE EXCEPTION
                'No se puede reactivar un lote de un medicamento inactivo';

        END IF;


        IF v_cantidad <= 0 THEN

            RAISE EXCEPTION
                'No se puede reactivar un lote sin existencias';

        END IF;


        IF v_fecha <= CURRENT_DATE THEN

            RAISE EXCEPTION
                'No se puede reactivar un lote vencido';

        END IF;

    END IF;


    UPDATE LOTES
    SET
        estado_lote = v_estado,
        id_usuario_modificacion =
            p_id_usuario_actor,
        fecha_modificacion =
            CURRENT_TIMESTAMP
    WHERE id_lote = p_id_lote;


    INSERT INTO LOG_MOVIMIENTOS (
        id_usuario,
        id_lote,
        tipo_movimiento,
        cantidad_afectada,
        detalle_cambio
    )
    VALUES (
        p_id_usuario_actor,
        p_id_lote,
        'AJUSTE',
        0,
        'Cambio de estado administrativo del lote a '
        || v_estado
    );

END;
$$;


COMMIT;
