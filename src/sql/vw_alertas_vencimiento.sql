-- ==============================================================================
-- Vista: clasificación de alertas de vencimiento (HU.39 / subissue 39.1)
-- Ubicación: src/sql/
-- Depende de: LOTES y MEDICAMENTOS (ya existentes en 01_schema_carestock.sql)
-- ==============================================================================

CREATE OR REPLACE VIEW vw_alertas_vencimiento AS
SELECT
    l.id_lote,
    l.id_medicamento,
    m.nombre_comercial,
    l.numero_lote,
    l.cantidad_actual,
    l.fecha_vencimiento,
    (l.fecha_vencimiento - CURRENT_DATE) AS dias_para_vencer,
    CASE
        WHEN l.fecha_vencimiento < CURRENT_DATE THEN 'VENCIDO'
        WHEN l.fecha_vencimiento - CURRENT_DATE <= 15 THEN 'ROJO'
        WHEN l.fecha_vencimiento - CURRENT_DATE <= 30 THEN 'AMARILLO'
        ELSE 'VERDE'
    END AS nivel_alerta
FROM LOTES l
JOIN MEDICAMENTOS m ON m.id_medicamento = l.id_medicamento
WHERE l.estado_lote = 'DISPONIBLE'
  AND l.cantidad_actual > 0;
