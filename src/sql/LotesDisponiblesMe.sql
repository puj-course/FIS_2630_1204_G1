--HU.34
CREATE OR REPLACE VIEW vw_lotes_detalle AS
SELECT
    l.id_lote,
    l.numero_lote,
    m.id_medicamento,
    m.nombre_comercial AS medicamento,
    l.cantidad_actual,
    l.fecha_vencimiento,
    u.estante,
    u.nivel,
    l.estado_lote,
    l.fecha_ingreso
FROM lotes l
JOIN medicamentos m ON m.id_medicamento = l.id_medicamento
JOIN ubicaciones u ON u.id_ubicacion = l.id_ubicacion
WHERE l.estado_lote = 'DISPONIBLE'
ORDER BY m.nombre_comercial ASC, l.fecha_vencimiento ASC;