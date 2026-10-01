-- =============================================================================
-- Corrección: código de sede reutilizable entre farmacias
--
-- El código identifica una sede dentro del contexto de una farmacia.
-- Por lo tanto, valores como SEDE-NORTE o SEDE-SUR pueden existir
-- en farmacias diferentes.
--
-- Se evita únicamente duplicar la combinación:
-- nombre farmacia + código sede.
-- =============================================================================

BEGIN;


-- =============================================================================
-- 1. Eliminar unicidad global del código
-- =============================================================================

ALTER TABLE FARMACIAS
    DROP CONSTRAINT IF EXISTS farmacias_codigo_key;


-- =============================================================================
-- 2. Unicidad compuesta
--
-- Permite:
-- Farmacia ABC + SEDE-NORTE
-- Farmacia XYZ + SEDE-NORTE
--
-- Impide:
-- Farmacia ABC + SEDE-NORTE
-- Farmacia ABC + SEDE-NORTE
-- =============================================================================

CREATE UNIQUE INDEX IF NOT EXISTS uq_farmacias_nombre_codigo
    ON FARMACIAS (
        UPPER(TRIM(nombre)),
        UPPER(TRIM(codigo))
    );


-- =============================================================================
-- 3. Actualizar creación segura de farmacias
-- =============================================================================

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
    -- Normalización
    -- -------------------------------------------------------------------------

    v_codigo_normalizado :=
        UPPER(TRIM(p_codigo));

    v_nombre_normalizado :=
        TRIM(p_nombre);


    -- -------------------------------------------------------------------------
    -- Validaciones
    -- -------------------------------------------------------------------------

    IF v_codigo_normalizado IS NULL
       OR v_codigo_normalizado = '' THEN

        RAISE EXCEPTION
            'El código de sede es obligatorio';

    END IF;


    IF v_nombre_normalizado IS NULL
       OR v_nombre_normalizado = '' THEN

        RAISE EXCEPTION
            'El nombre de farmacia es obligatorio';

    END IF;


    IF LENGTH(v_codigo_normalizado) > 40 THEN

        RAISE EXCEPTION
            'El código de sede no puede superar 40 caracteres';

    END IF;


    IF LENGTH(v_nombre_normalizado) > 150 THEN

        RAISE EXCEPTION
            'El nombre de farmacia no puede superar 150 caracteres';

    END IF;


    -- -------------------------------------------------------------------------
    -- La combinación farmacia + código de sede debe ser única.
    -- El mismo código puede existir en farmacias diferentes.
    -- -------------------------------------------------------------------------

    IF EXISTS (

        SELECT 1
        FROM FARMACIAS

        WHERE UPPER(TRIM(nombre)) =
              UPPER(TRIM(v_nombre_normalizado))

          AND UPPER(TRIM(codigo)) =
              v_codigo_normalizado

    ) THEN

        RAISE EXCEPTION
            'Ya existe una farmacia con ese nombre y código de sede';

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


COMMIT;
