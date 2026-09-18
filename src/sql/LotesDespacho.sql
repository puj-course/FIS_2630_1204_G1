CREATE OR REPLACE FUNCTION fn_despachar_lote(
    p_id_lote INTEGER,
    p_cantidad INTEGER,
    p_id_usuario INTEGER
) RETURNS VOID AS $$
DECLARE
    v_cantidad_actual INTEGER;
    v_numero_lote VARCHAR;
BEGIN
    -- Validación: cantidad mayor que cero
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

    -- Validación: no se puede despachar más de lo disponible
    IF p_cantidad > v_cantidad_actual THEN
        RAISE EXCEPTION 'Stock insuficiente en el lote: disponible %, solicitado %', v_cantidad_actual, p_cantidad;
    END IF;

    -- Descuenta la cantidad del lote
    UPDATE lotes
    SET cantidad_actual = cantidad_actual - p_cantidad,
        estado_lote = CASE
            WHEN cantidad_actual - p_cantidad = 0 THEN 'AGOTADO'
            ELSE estado_lote
        END
    WHERE id_lote = p_id_lote;

    -- Registra la trazabilidad de la operación
    INSERT INTO log_movimientos (id_usuario, id_lote, tipo_movimiento, cantidad_afectada, detalle_cambio)
    VALUES (p_id_usuario, p_id_lote, 'SALIDA', p_cantidad, 'Despacho de lote: ' || v_numero_lote);

END;
$$ LANGUAGE plpgsql;