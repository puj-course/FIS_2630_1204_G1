-- =============================================================================
-- Issue #456
-- Contexto de farmacia + SUPER_ADMIN + creación jerárquica segura de usuarios
--
-- Dependencia:
--   04_multifarmacia_inventario.sql
-- =============================================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================================
-- 1. ROLES
-- ============================================================================

INSERT INTO ROLES (
    nombre_rol,
    descripcion
)
VALUES
(
    'SUPER_ADMIN',
    'Administrador global de CareStock'
),
(
    'ADMINISTRADOR',
    'Administrador de una farmacia'
),
(
    'FARMACEUTICO',
    'Usuario operativo de una farmacia'
)
ON CONFLICT (nombre_rol)
DO NOTHING;


-- ============================================================================
-- 2. CONTEXTO DE FARMACIA Y TRAZABILIDAD
-- ============================================================================

ALTER TABLE USUARIOS
    ADD COLUMN IF NOT EXISTS id_farmacia INTEGER;

ALTER TABLE USUARIOS
    ADD COLUMN IF NOT EXISTS id_usuario_creacion INTEGER;


DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_usuarios_farmacia'
    ) THEN

        ALTER TABLE USUARIOS
            ADD CONSTRAINT fk_usuarios_farmacia
            FOREIGN KEY (id_farmacia)
            REFERENCES FARMACIAS(id_farmacia);

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_usuarios_creador'
    ) THEN

        ALTER TABLE USUARIOS
            ADD CONSTRAINT fk_usuarios_creador
            FOREIGN KEY (id_usuario_creacion)
            REFERENCES USUARIOS(id_usuario);

    END IF;

END $$;


CREATE INDEX IF NOT EXISTS ix_usuarios_farmacia
    ON USUARIOS(id_farmacia);

CREATE INDEX IF NOT EXISTS ix_usuarios_creador
    ON USUARIOS(id_usuario_creacion);


-- ============================================================================
-- 3. USUARIOS EXISTENTES
--
-- Los usuarios existentes quedan temporalmente en Farmacia Principal.
-- Después se promueve Admin CareStock a SUPER_ADMIN y su farmacia queda NULL.
-- ============================================================================

UPDATE USUARIOS
SET id_farmacia = (
    SELECT id_farmacia
    FROM FARMACIAS
    WHERE codigo = 'SEDE-PRINCIPAL'
    LIMIT 1
)
WHERE id_farmacia IS NULL;


DO $$
DECLARE

    v_id_superadmin INTEGER;
    v_id_rol_superadmin INTEGER;

BEGIN

    SELECT id_rol
    INTO v_id_rol_superadmin
    FROM ROLES
    WHERE nombre_rol = 'SUPER_ADMIN';


    SELECT id_usuario
    INTO v_id_superadmin
    FROM USUARIOS
    WHERE LOWER(email) = LOWER('admin@carestock.com')
    ORDER BY id_usuario
    LIMIT 1;


    IF v_id_superadmin IS NULL THEN

        SELECT id_usuario
        INTO v_id_superadmin
        FROM USUARIOS
        WHERE LOWER(nombre_completo) =
              LOWER('Admin CareStock')
        ORDER BY id_usuario
        LIMIT 1;

    END IF;


    IF v_id_superadmin IS NULL THEN

        RAISE EXCEPTION
            'No se encontró Admin CareStock para promoverlo a SUPER_ADMIN';

    END IF;


    UPDATE USUARIOS
    SET
        id_rol = v_id_rol_superadmin,
        id_farmacia = NULL
    WHERE id_usuario = v_id_superadmin;

END $$;


-- ============================================================================
-- 4. ELIMINAR FUNCIONES ANTIGUAS DE CREACIÓN
--
-- Evita que el rol o farmacia sean enviados libremente desde Java.
-- ============================================================================

DROP FUNCTION IF EXISTS fn_crear_usuario(
    VARCHAR,
    VARCHAR,
    VARCHAR,
    INTEGER
);

DROP FUNCTION IF EXISTS fn_crear_usuario(
    VARCHAR,
    VARCHAR,
    VARCHAR,
    INTEGER,
    INTEGER
);


-- ============================================================================
-- 5. CREACIÓN JERÁRQUICA SEGURA
--
-- SUPER_ADMIN:
--     crea ADMINISTRADOR y selecciona farmacia.
--
-- ADMINISTRADOR:
--     crea FARMACEUTICO y hereda automáticamente su propia farmacia.
--
-- FARMACEUTICO:
--     no puede crear usuarios.
-- ============================================================================

CREATE OR REPLACE FUNCTION fn_crear_usuario_jerarquico(
    p_id_usuario_actor INTEGER,
    p_nombre_completo VARCHAR,
    p_email VARCHAR,
    p_password VARCHAR,
    p_id_farmacia_seleccionada INTEGER DEFAULT NULL
)
RETURNS INTEGER
AS $$
DECLARE

    v_rol_actor VARCHAR(50);
    v_farmacia_actor INTEGER;

    v_id_rol_destino INTEGER;
    v_id_farmacia_destino INTEGER;

    v_id_usuario_nuevo INTEGER;

BEGIN

    -- ------------------------------------------------------------------------
    -- Actor autenticado y activo
    -- ------------------------------------------------------------------------

    SELECT
        r.nombre_rol,
        u.id_farmacia
    INTO
        v_rol_actor,
        v_farmacia_actor
    FROM USUARIOS u
    INNER JOIN ROLES r
        ON r.id_rol = u.id_rol
    WHERE u.id_usuario = p_id_usuario_actor
      AND u.estado = 'ACTIVO';


    IF NOT FOUND THEN

        RAISE EXCEPTION
            'El usuario creador no existe o no está activo';

    END IF;


    -- ------------------------------------------------------------------------
    -- Datos básicos
    -- ------------------------------------------------------------------------

    IF p_nombre_completo IS NULL
       OR LENGTH(TRIM(p_nombre_completo)) = 0 THEN

        RAISE EXCEPTION
            'El nombre completo es obligatorio';

    END IF;


    IF p_email IS NULL
       OR LENGTH(TRIM(p_email)) = 0 THEN

        RAISE EXCEPTION
            'El correo electrónico es obligatorio';

    END IF;


    IF LENGTH(p_password) < 8 THEN

        RAISE EXCEPTION
            'La contraseña debe tener al menos 8 caracteres';

    END IF;


    IF EXISTS (
        SELECT 1
        FROM USUARIOS
        WHERE LOWER(email) =
              LOWER(TRIM(p_email))
    ) THEN

        RAISE EXCEPTION
            'Ya existe un usuario con ese correo electrónico';

    END IF;


    -- ------------------------------------------------------------------------
    -- SUPER_ADMIN -> ADMINISTRADOR
    -- ------------------------------------------------------------------------

    IF UPPER(v_rol_actor) = 'SUPER_ADMIN' THEN

        IF p_id_farmacia_seleccionada IS NULL THEN

            RAISE EXCEPTION
                'SUPER_ADMIN debe seleccionar una farmacia';

        END IF;


        IF NOT EXISTS (
            SELECT 1
            FROM FARMACIAS
            WHERE id_farmacia =
                  p_id_farmacia_seleccionada
              AND estado = 'ACTIVA'
        ) THEN

            RAISE EXCEPTION
                'La farmacia seleccionada no existe o está inactiva';

        END IF;


        SELECT id_rol
        INTO v_id_rol_destino
        FROM ROLES
        WHERE nombre_rol = 'ADMINISTRADOR';


        v_id_farmacia_destino =
                p_id_farmacia_seleccionada;


    -- ------------------------------------------------------------------------
    -- ADMINISTRADOR -> FARMACEUTICO
    -- ------------------------------------------------------------------------

    ELSIF UPPER(v_rol_actor) = 'ADMINISTRADOR' THEN

        IF v_farmacia_actor IS NULL THEN

            RAISE EXCEPTION
                'El administrador no tiene una farmacia asociada';

        END IF;


        IF p_id_farmacia_seleccionada IS NOT NULL
           AND p_id_farmacia_seleccionada
               <> v_farmacia_actor THEN

            RAISE EXCEPTION
                'Un administrador no puede crear usuarios en otra farmacia';

        END IF;


        IF NOT EXISTS (
            SELECT 1
            FROM FARMACIAS
            WHERE id_farmacia =
                  v_farmacia_actor
              AND estado = 'ACTIVA'
        ) THEN

            RAISE EXCEPTION
                'La farmacia del administrador no está activa';

        END IF;


        SELECT id_rol
        INTO v_id_rol_destino
        FROM ROLES
        WHERE nombre_rol = 'FARMACEUTICO';


        v_id_farmacia_destino =
                v_farmacia_actor;


    -- ------------------------------------------------------------------------
    -- Cualquier otro rol
    -- ------------------------------------------------------------------------

    ELSE

        RAISE EXCEPTION
            'El rol % no está autorizado para crear usuarios',
            v_rol_actor;

    END IF;


    -- ------------------------------------------------------------------------
    -- Persistencia
    -- ------------------------------------------------------------------------

    INSERT INTO USUARIOS (
        nombre_completo,
        email,
        password_hash,
        id_rol,
        id_farmacia,
        id_usuario_creacion
    )
    VALUES (
        TRIM(p_nombre_completo),
        LOWER(TRIM(p_email)),
        crypt(
            p_password,
            gen_salt('bf')
        ),
        v_id_rol_destino,
        v_id_farmacia_destino,
        p_id_usuario_actor
    )
    RETURNING id_usuario
    INTO v_id_usuario_nuevo;


    RETURN v_id_usuario_nuevo;

END;
$$ LANGUAGE plpgsql;


COMMENT ON COLUMN USUARIOS.id_farmacia IS
    'Farmacia operativa del usuario. NULL únicamente para roles globales como SUPER_ADMIN.';

COMMENT ON COLUMN USUARIOS.id_usuario_creacion IS
    'Usuario autenticado responsable de crear este registro.';


COMMIT;
