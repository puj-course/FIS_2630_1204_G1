-- ==============================================================================
-- PROCEDIMIENTO ALMACENADO: Registro seguro e insumo de lotes e historial
-- Ubicación: src/sql/procedures/01_sp_ingreso_lotes.sql
-- ==============================================================================

CREATE OR REPLACE PROCEDURE sp_registrar_nuevo_lote(
    p_numero_lote VARCHAR,
    p_id_medicamento INT,
    p_cantidad INT,
    p_fecha_vencimiento DATE,
    p_id_ubicacion INT,
    p_id_usuario INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_lote INT;
BEGIN
    -- 1. Inserción del lote
    INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote)
    VALUES (p_numero_lote, p_id_medicamento, p_cantidad, p_fecha_vencimiento, p_id_ubicacion, 'DISPONIBLE')
    RETURNING id_lote INTO v_id_lote;

    -- 2. Registro en el log de auditoría / trazabilidad
    INSERT INTO LOG_MOVIMIENTOS (id_usuario, id_lote, tipo_movimiento, cantidad_afectada, detalle_cambio)
    VALUES (p_id_usuario, v_id_lote, 'ENTRADA', p_cantidad, 'Ingreso inicial del lote al inventario');
END;
$$;
