BEGIN;

CREATE OR REPLACE VIEW VW_MOVIMIENTOS_CATALOGO_FARMACIA AS
SELECT
    mc.id_movimiento,
    mc.id_farmacia,
    CASE
        WHEN mc.id_aseo IS NOT NULL THEN 'ASEO'
        ELSE 'MATERNIDAD'
    END AS categoria,
    COALESCE(a.nombre, p.nombre) AS producto,
    mc.tipo_movimiento,
    mc.cantidad,
    mc.motivo,
    u.nombre_completo AS usuario_responsable,
    mc.fecha_movimiento
FROM MOVIMIENTOS_CATALOGO mc
INNER JOIN USUARIOS u
    ON u.id_usuario = mc.id_usuario
LEFT JOIN ASEO a
    ON a.id_aseo = mc.id_aseo
    AND a.id_farmacia = mc.id_farmacia
LEFT JOIN MATERNIDAD p
    ON p.id_maternidad = mc.id_maternidad
    AND p.id_farmacia = mc.id_farmacia
ORDER BY mc.fecha_movimiento DESC, mc.id_movimiento DESC;

COMMIT;
