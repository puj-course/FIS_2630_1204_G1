CREATE OR REPLACE VIEW vw_medicamentos_activos AS
SELECT
    id_medicamento,
    codigo_invima,
    nombre_comercial,
    principio_activo,
    concentracion,
    forma_farmaceutica,
    id_categoria,
    stock_total,
    stock_minimo
FROM medicamentos
WHERE estado = 'ACTIVO'
ORDER BY nombre_comercial ASC;
