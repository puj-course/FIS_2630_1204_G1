CREATE OR REPLACE FUNCTION fn_validar_rol(
    p_id_usuario INTEGER,
    p_roles_permitidos VARCHAR[]
) RETURNS VOID AS $$
DECLARE
    v_nombre_rol VARCHAR;
    v_estado VARCHAR;
BEGIN
    -- Obtiene el rol y estado del usuario
    SELECT r.nombre_rol, u.estado
    INTO v_nombre_rol, v_estado
    FROM usuarios u
    JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.id_usuario = p_id_usuario;

    -- Validación: el usuario debe existir
    IF NOT FOUND THEN
        RAISE EXCEPTION 'El usuario indicado no existe';
    END IF;

    -- Validación: el usuario debe estar activo
    IF v_estado <> 'ACTIVO' THEN
        RAISE EXCEPTION 'El usuario no está activo (estado actual: %)', v_estado;
    END IF;

    -- Validación: el rol del usuario debe estar en la lista de roles permitidos
    IF NOT (v_nombre_rol = ANY (p_roles_permitidos)) THEN
        RAISE EXCEPTION 'Acceso denegado: el rol % no está autorizado para esta operación', v_nombre_rol;
    END IF;

END;
$$ LANGUAGE plpgsql;



SELECT u.id_usuario, u.nombre_completo, r.nombre_rol, u.estado
FROM usuarios u
JOIN roles r ON r.id_rol = u.id_rol;



CREATE OR REPLACE FUNCTION fn_registrar_lote(
    p_numero_lote VARCHAR,
    p_id_medicamento INTEGER,
    p_cantidad INTEGER,
    p_fecha_vencimiento DATE,
    p_id_ubicacion INTEGER,
    p_id_usuario INTEGER
) RETURNS INTEGER AS $$
DECLARE
    v_id_lote INTEGER;
BEGIN
    -- Validación de acceso por rol (nueva capa de seguridad)
    PERFORM fn_validar_rol(p_id_usuario, ARRAY['ADMINISTRADOR', 'FARMACEUTICO']);

    IF p_cantidad <= 0 THEN
        RAISE EXCEPTION 'La cantidad debe ser mayor que cero';
    END IF;

    IF p_fecha_vencimiento <= CURRENT_DATE THEN
        RAISE EXCEPTION 'La fecha de vencimiento debe ser posterior a la fecha actual';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM medicamentos
        WHERE id_medicamento = p_id_medicamento AND estado = 'ACTIVO'
    ) THEN
        RAISE EXCEPTION 'El medicamento no existe o no está activo';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM ubicaciones WHERE id_ubicacion = p_id_ubicacion
    ) THEN
        RAISE EXCEPTION 'La ubicación indicada no existe';
    END IF;

    INSERT INTO lotes (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion)
    VALUES (p_numero_lote, p_id_medicamento, p_cantidad, p_fecha_vencimiento, p_id_ubicacion)
    RETURNING id_lote INTO v_id_lote;

    UPDATE medicamentos
    SET stock_total = stock_total + p_cantidad
    WHERE id_medicamento = p_id_medicamento;

    INSERT INTO log_movimientos (id_usuario, id_lote, tipo_movimiento, cantidad_afectada, detalle_cambio)
    VALUES (p_id_usuario, v_id_lote, 'ENTRADA', p_cantidad, 'Ingreso de nuevo lote: ' || p_numero_lote);

    RETURN v_id_lote;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION fn_despachar_lote(
    p_id_lote INTEGER,
    p_cantidad INTEGER,
    p_id_usuario INTEGER
) RETURNS VOID AS $$
DECLARE
    v_cantidad_actual INTEGER;
    v_numero_lote VARCHAR;
BEGIN
    -- Validación de acceso por rol (nueva capa de seguridad)
    PERFORM fn_validar_rol(p_id_usuario, ARRAY['ADMINISTRADOR', 'FARMACEUTICO']);

    IF p_cantidad <= 0 THEN
        RAISE EXCEPTION 'La cantidad a despachar debe ser mayor que cero';
    END IF;

    SELECT cantidad_actual, numero_lote
    INTO v_cantidad_actual, v_numero_lote
    FROM lotes
    WHERE id_lote = p_id_lote
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El lote indicado no existe';
    END IF;

    IF p_cantidad > v_cantidad_actual THEN
        RAISE EXCEPTION 'Stock insuficiente en el lote: disponible %, solicitado %', v_cantidad_actual, p_cantidad;
    END IF;

    UPDATE lotes
    SET cantidad_actual = cantidad_actual - p_cantidad,
        estado_lote = CASE
            WHEN cantidad_actual - p_cantidad = 0 THEN 'AGOTADO'
            ELSE estado_lote
        END
    WHERE id_lote = p_id_lote;

    INSERT INTO log_movimientos (id_usuario, id_lote, tipo_movimiento, cantidad_afectada, detalle_cambio)
    VALUES (p_id_usuario, p_id_lote, 'SALIDA', p_cantidad, 'Despacho de lote: ' || v_numero_lote);

END;
$$ LANGUAGE plpgsql;

SELECT proname FROM pg_proc WHERE proname = 'fn_validar_rol';
SELECT proname FROM pg_proc WHERE proname IN ('fn_validar_rol', 'fn_registrar_lote', 'fn_despachar_lote');