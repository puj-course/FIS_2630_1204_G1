SELECT 'MEDICAMENTO_STOCK_TOTAL_DISTINTO_DE_LOTES' AS verificacion,
       'MEDICAMENTO' AS tipo_producto,
       m.id_medicamento AS id_producto,
       'stock_total=' || m.stock_total || ' lotes=' || COALESCE(s.total, 0) AS detalle
FROM medicamentos m
LEFT JOIN (
    SELECT id_medicamento, SUM(cantidad_actual) AS total
    FROM lotes
    WHERE estado_lote = 'DISPONIBLE'
      AND fecha_vencimiento > CURRENT_DATE
    GROUP BY id_medicamento
) s ON s.id_medicamento = m.id_medicamento
WHERE m.stock_total <> COALESCE(s.total, 0)

UNION ALL

SELECT 'LOTE_CON_CANTIDAD_NEGATIVA', 'MEDICAMENTO', l.id_lote,
       'cantidad_actual=' || l.cantidad_actual
FROM lotes l
WHERE l.cantidad_actual < 0

UNION ALL

SELECT 'LOTE_AGOTADO_CON_EXISTENCIAS', 'MEDICAMENTO', l.id_lote,
       'cantidad_actual=' || l.cantidad_actual
FROM lotes l
WHERE l.estado_lote = 'AGOTADO'
  AND l.cantidad_actual > 0

UNION ALL

SELECT 'ASEO_CON_STOCK_NEGATIVO', 'ASEO', a.id_aseo,
       'stock=' || a.stock
FROM aseo a
WHERE a.stock < 0

UNION ALL

SELECT 'MATERNIDAD_CON_STOCK_NEGATIVO', 'MATERNIDAD', p.id_maternidad,
       'stock=' || p.stock
FROM maternidad p
WHERE p.stock < 0

UNION ALL

SELECT 'ASEO_STOCK_DISTINTO_DEL_ULTIMO_MOVIMIENTO', 'ASEO', a.id_aseo,
       'stock=' || a.stock || ' ultimo_registrado=' || u.stock_final
FROM aseo a
INNER JOIN LATERAL (
    SELECT x.stock_final
    FROM (
        SELECT mp.fecha_movimiento AS fecha, mp.stock_posterior AS stock_final
        FROM movimientos_productos mp
        WHERE mp.id_aseo = a.id_aseo
        UNION ALL
        SELECT au.fecha_ajuste, au.cantidad_nueva
        FROM auditoria_ajustes au
        WHERE au.id_aseo = a.id_aseo
    ) x
    ORDER BY x.fecha DESC
    LIMIT 1
) u ON TRUE
WHERE a.stock <> u.stock_final

UNION ALL

SELECT 'MATERNIDAD_STOCK_DISTINTO_DEL_ULTIMO_MOVIMIENTO', 'MATERNIDAD', p.id_maternidad,
       'stock=' || p.stock || ' ultimo_registrado=' || u.stock_final
FROM maternidad p
INNER JOIN LATERAL (
    SELECT x.stock_final
    FROM (
        SELECT mp.fecha_movimiento AS fecha, mp.stock_posterior AS stock_final
        FROM movimientos_productos mp
        WHERE mp.id_maternidad = p.id_maternidad
        UNION ALL
        SELECT au.fecha_ajuste, au.cantidad_nueva
        FROM auditoria_ajustes au
        WHERE au.id_maternidad = p.id_maternidad
    ) x
    ORDER BY x.fecha DESC
    LIMIT 1
) u ON TRUE
WHERE p.stock <> u.stock_final

ORDER BY 1, 3;
