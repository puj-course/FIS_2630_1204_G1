CREATE OR REPLACE VIEW vw_resumen_farmacias AS
SELECT
    f.id_farmacia,
    f.codigo,
    f.nombre,
    f.estado,
    COALESCE(SUM(m.stock_total), 0) AS stock_total,
    COUNT(m.id_medicamento) FILTER (WHERE m.estado = 'ACTIVO') AS medicamentos_activos,
    COUNT(m.id_medicamento) FILTER (
        WHERE m.estado = 'ACTIVO' AND m.stock_total <= m.stock_minimo
    ) AS alertas_criticas
FROM FARMACIAS f
LEFT JOIN MEDICAMENTOS m ON m.id_farmacia = f.id_farmacia
GROUP BY f.id_farmacia, f.codigo, f.nombre, f.estado
ORDER BY f.nombre;
