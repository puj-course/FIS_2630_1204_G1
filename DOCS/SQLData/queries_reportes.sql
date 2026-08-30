-- ========================================================
-- CareStock Database - Consultas de Alerta de Inventario
-- ========================================================

-- 1. Alerta de Medicamentos en Stock Crítico / Bajo
SELECT 
    m.codigo_invima,
    m.nombre_comercial,
    m.principio_activo,
    c.nombre_categoria,
    m.stock_total,
    m.stock_minimo,
    (m.stock_minimo - m.stock_total) AS unidades_faltantes
FROM MEDICAMENTOS m
JOIN CATEGORIAS c ON m.id_categoria = c.id_categoria
WHERE m.stock_total <= m.stock_minimo
  AND m.estado = 'ACTIVO'
ORDER BY m.stock_total ASC;
