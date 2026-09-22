
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION fn_crear_usuario(
    p_nombre_completo VARCHAR,
    p_email VARCHAR,
    p_password VARCHAR,
    p_id_rol INTEGER
) RETURNS INTEGER AS $$
DECLARE
    v_id_usuario INTEGER;
BEGIN
    -- Validación: longitud mínima de contraseña
    IF LENGTH(p_password) < 8 THEN
        RAISE EXCEPTION 'La contraseña debe tener al menos 8 caracteres';
    END IF;

    -- Validación: el rol debe existir
    IF NOT EXISTS (SELECT 1 FROM roles WHERE id_rol = p_id_rol) THEN
        RAISE EXCEPTION 'El rol indicado no existe';
    END IF;

    -- Inserta el usuario, cifrando la contraseña con bcrypt (gen_salt('bf'))
    INSERT INTO usuarios (nombre_completo, email, password_hash, id_rol)
    VALUES (
        p_nombre_completo,
        p_email,
        crypt(p_password, gen_salt('bf')),
        p_id_rol
    )
    RETURNING id_usuario INTO v_id_usuario;

    RETURN v_id_usuario;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION fn_autenticar_usuario(
    p_email VARCHAR,
    p_password VARCHAR
) RETURNS INTEGER AS $$
DECLARE
    v_id_usuario INTEGER;
    v_password_hash VARCHAR;
    v_estado VARCHAR;
BEGIN
    SELECT id_usuario, password_hash, estado
    INTO v_id_usuario, v_password_hash, v_estado
    FROM usuarios
    WHERE email = p_email;

    -- Si no existe el usuario, o la contraseña no coincide, o no está activo
    IF NOT FOUND
       OR v_password_hash <> crypt(p_password, v_password_hash)
       OR v_estado <> 'ACTIVO'
    THEN
        RETURN NULL;
    END IF;

    RETURN v_id_usuario;
END;
$$ LANGUAGE plpgsql;