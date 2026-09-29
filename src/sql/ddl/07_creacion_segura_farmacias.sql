-- =============================================================================
-- Creación segura de farmacias desde CareStock
-- Solo SUPER_ADMIN
-- =============================================================================

BEGIN;

ALTER TABLE FARMACIAS
    ADD COLUMN IF NOT EXISTS id_usuario_creacion INTEGER;

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_farmacias_usuario_creacion'
    ) THEN

        ALTER TABLE FARMACIAS
            ADD CONSTRAINT fk_farmacias_usuario_creacion
            FOREIGN KEY (id_usuario_creacion)
            REFERENCES USUARIOS(id_usuario);

    END IF;

END $$;


CREATE INDEX IF NOT EXISTS ix_farmacias_usuario_creacion
    ON FARMACIAS(id_usuario_creacion);


CREATE OR REPLACE FUNCTION fn_crear_farmacia_segura(
    p_id_usuario_actor INTEGER,
    p_codigo VARCHAR,
    p_nombre VARCHAR
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE

    v_rol_actor VARCHAR(50);
    v_id_farmacia INTEGER;
    v_codigo_normalizado VARCHAR(40);
    v_nombre_normalizado VARCHAR(150);

BEGIN

    -- -------------------------------------------------------------------------
    -- Validar actor
    -- -------------------------------------------------------------------------

    SELECT r.nombre_rol
    INTO v_rol_actor
    FROM USUARIOS u
    INNER JOIN ROLES r
        ON r.id_rol = u.id_rol
    WHERE u.id_usuario = p_id_usuario_actor
      AND u.estado = 'ACTIVO';


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'El usuario autenticado no existe o no está activo';

    END IF;


    IF UPPER(v_rol_actor) <> 'SUPER_ADMIN' THEN

        RAISE EXCEPTION
            'Solo SUPER_ADMIN puede crear farmacias';

    END IF;


    -- -------------------------------------------------------------------------
    -- Normalización y validaciones
    -- -------------------------------------------------------------------------

    v_codigo_normalizado :=
        UPPER(TRIM(p_codigo));

    v_nombre_normalizado :=
        TRIM(p_nombre);


    IF v_codigo_normalizado IS NULL
       OR v_codigo_normalizado = '' THEN

        RAISE EXCEPTION
            'El código de farmacia es obligatorio';

    END IF;


    IF v_nombre_normalizado IS NULL
       OR v_nombre_normalizado = '' THEN

        RAISE EXCEPTION
            'El nombre de farmacia es obligatorio';

    END IF;


    IF LENGTH(v_codigo_normalizado) > 40 THEN

        RAISE EXCEPTION
            'El código de farmacia no puede superar 40 caracteres';

    END IF;


    IF LENGTH(v_nombre_normalizado) > 150 THEN

        RAISE EXCEPTION
            'El nombre de farmacia no puede superar 150 caracteres';

    END IF;


    IF EXISTS (
        SELECT 1
        FROM FARMACIAS
        WHERE UPPER(codigo) =
              v_codigo_normalizado
    ) THEN

        RAISE EXCEPTION
            'Ya existe una farmacia con ese código';

    END IF;


    -- -------------------------------------------------------------------------
    -- Crear farmacia
    -- -------------------------------------------------------------------------

    INSERT INTO FARMACIAS (
        codigo,
        nombre,
        estado,
        id_usuario_creacion
    )
    VALUES (
        v_codigo_normalizado,
        v_nombre_normalizado,
        'ACTIVA',
        p_id_usuario_actor
    )
    RETURNING id_farmacia
    INTO v_id_farmacia;


    RETURN v_id_farmacia;

END;
$$;


COMMENT ON COLUMN FARMACIAS.id_usuario_creacion IS
    'SUPER_ADMIN responsable de crear la farmacia.';


COMMIT;
