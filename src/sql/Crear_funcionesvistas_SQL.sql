CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =============================================================================
-- IMPORTANTE
-- =============================================================================
--
-- La creación de usuarios se implementa actualmente mediante:
--
--     fn_crear_usuario_jerarquico(...)
--
-- definida en:
--
--     ddl/05_superadmin_jerarquia_usuarios_issue_456.sql
--
-- No debe recrearse la antigua fn_crear_usuario porque permitía recibir
-- directamente el rol desde la aplicación.
-- =============================================================================


CREATE OR REPLACE FUNCTION fn_autenticar_usuario(
    p_email VARCHAR,
    p_password VARCHAR
)
RETURNS INTEGER
AS $$
DECLARE

    v_id_usuario INTEGER;
    v_password_hash VARCHAR;
    v_estado VARCHAR;

BEGIN

    SELECT
        id_usuario,
        password_hash,
        estado
    INTO
        v_id_usuario,
        v_password_hash,
        v_estado
    FROM USUARIOS
    WHERE LOWER(email) =
          LOWER(TRIM(p_email));


    IF NOT FOUND
       OR v_password_hash
          <> crypt(
                p_password,
                v_password_hash
             )
       OR v_estado <> 'ACTIVO'
    THEN

        RETURN NULL;

    END IF;


    RETURN v_id_usuario;

END;
$$ LANGUAGE plpgsql;
