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
    -- Validación: cantidad mayor que cero
    IF p_cantidad <= 0 THEN
        RAISE EXCEPTION 'La cantidad debe ser mayor que cero';
    END IF;

    -- Validación: fecha de vencimiento no permitida (ya vencida o de hoy)
    IF p_fecha_vencimiento <= CURRENT_DATE THEN
        RAISE EXCEPTION 'La fecha de vencimiento debe ser posterior a la fecha actual';
    END IF;

    -- Validación: el medicamento debe existir y estar activo
    IF NOT EXISTS (
        SELECT 1 FROM medicamentos
        WHERE id_medicamento = p_id_medicamento AND estado = 'ACTIVO'
    ) THEN
        RAISE EXCEPTION 'El medicamento no existe o no está activo';
    END IF;

    -- Validación: la ubicación debe existir
    IF NOT EXISTS (
        SELECT 1 FROM ubicaciones WHERE id_ubicacion = p_id_ubicacion
    ) THEN
        RAISE EXCEPTION 'La ubicación indicada no existe';
    END IF;

    -- Inserta el lote (uq_lotes_numero bloquea duplicados por medicamento)
    INSERT INTO lotes (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion)
    VALUES (p_numero_lote, p_id_medicamento, p_cantidad, p_fecha_vencimiento, p_id_ubicacion)
    RETURNING id_lote INTO v_id_lote;

    -- Actualiza el stock total del medicamento
    UPDATE medicamentos
    SET stock_total = stock_total + p_cantidad
    WHERE id_medicamento = p_id_medicamento;

    -- Registra la trazabilidad de la operación
    INSERT INTO log_movimientos (id_usuario, id_lote, tipo_movimiento, cantidad_afectada, detalle_cambio)
    VALUES (p_id_usuario, v_id_lote, 'ENTRADA', p_cantidad, 'Ingreso de nuevo lote: ' || p_numero_lote);

    RETURN v_id_lote;
END;
$$ LANGUAGE plpgsql;