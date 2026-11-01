BEGIN;

CREATE OR REPLACE VIEW vw_alertas_inventario AS
SELECT
    a.id_farmacia,
    a.tipo_alerta,
    a.categoria,
    a.id_referencia,
    a.codigo,
    a.producto,
    a.numero_lote,
    a.existencias,
    a.umbral,
    a.fecha_vencimiento,
    a.dias_para_vencer,
    a.severidad,
    a.ultimo_movimiento_fecha,
    a.ultimo_movimiento_tipo,
    a.ultimo_movimiento_usuario
FROM (
    SELECT
        m.id_farmacia,
        'STOCK_MINIMO'::TEXT AS tipo_alerta,
        'MEDICAMENTO'::TEXT AS categoria,
        m.id_medicamento AS id_referencia,
        m.codigo_invima::TEXT AS codigo,
        m.nombre_comercial::TEXT AS producto,
        NULL::TEXT AS numero_lote,
        m.stock_total AS existencias,
        m.stock_minimo AS umbral,
        NULL::DATE AS fecha_vencimiento,
        NULL::INTEGER AS dias_para_vencer,
        CASE WHEN m.stock_total = 0 THEN 'CRITICA' ELSE 'ADVERTENCIA' END::TEXT AS severidad,
        u.fecha AS ultimo_movimiento_fecha,
        u.tipo AS ultimo_movimiento_tipo,
        u.usuario AS ultimo_movimiento_usuario
    FROM medicamentos m
    LEFT JOIN LATERAL (
        SELECT x.fecha, x.tipo, x.usuario
        FROM (
            SELECT lm.fecha_hora AS fecha,
                   lm.tipo_movimiento::TEXT AS tipo,
                   us.nombre_completo::TEXT AS usuario
            FROM log_movimientos lm
            INNER JOIN lotes l ON l.id_lote = lm.id_lote
            INNER JOIN usuarios us ON us.id_usuario = lm.id_usuario
            WHERE l.id_medicamento = m.id_medicamento
            UNION ALL
            SELECT au.fecha_ajuste,
                   'AJUSTE'::TEXT,
                   us.nombre_completo::TEXT
            FROM auditoria_ajustes au
            INNER JOIN lotes l ON l.id_lote = au.id_lote
            INNER JOIN usuarios us ON us.id_usuario = au.id_usuario
            WHERE l.id_medicamento = m.id_medicamento
        ) x
        ORDER BY x.fecha DESC
        LIMIT 1
    ) u ON TRUE
    WHERE UPPER(TRIM(m.estado)) = 'ACTIVO'
      AND m.stock_total <= m.stock_minimo

    UNION ALL

    SELECT
        p.id_farmacia,
        'STOCK_MINIMO'::TEXT,
        'ASEO'::TEXT,
        p.id_aseo,
        p.codigo::TEXT,
        p.nombre::TEXT,
        NULL::TEXT,
        p.stock,
        p.stock_minimo,
        NULL::DATE,
        NULL::INTEGER,
        CASE WHEN p.stock = 0 THEN 'CRITICA' ELSE 'ADVERTENCIA' END::TEXT,
        u.fecha,
        u.tipo,
        u.usuario
    FROM aseo p
    LEFT JOIN LATERAL (
        SELECT x.fecha, x.tipo, x.usuario
        FROM (
            SELECT mp.fecha_movimiento AS fecha,
                   mp.tipo_movimiento::TEXT AS tipo,
                   us.nombre_completo::TEXT AS usuario
            FROM movimientos_productos mp
            INNER JOIN usuarios us ON us.id_usuario = mp.id_usuario
            WHERE mp.id_aseo = p.id_aseo
            UNION ALL
            SELECT au.fecha_ajuste,
                   'AJUSTE'::TEXT,
                   us.nombre_completo::TEXT
            FROM auditoria_ajustes au
            INNER JOIN usuarios us ON us.id_usuario = au.id_usuario
            WHERE au.id_aseo = p.id_aseo
        ) x
        ORDER BY x.fecha DESC
        LIMIT 1
    ) u ON TRUE
    WHERE UPPER(TRIM(p.estado)) = 'ACTIVO'
      AND p.stock <= p.stock_minimo

    UNION ALL

    SELECT
        p.id_farmacia,
        'STOCK_MINIMO'::TEXT,
        'MATERNIDAD'::TEXT,
        p.id_maternidad,
        p.codigo::TEXT,
        p.nombre::TEXT,
        NULL::TEXT,
        p.stock,
        p.stock_minimo,
        NULL::DATE,
        NULL::INTEGER,
        CASE WHEN p.stock = 0 THEN 'CRITICA' ELSE 'ADVERTENCIA' END::TEXT,
        u.fecha,
        u.tipo,
        u.usuario
    FROM maternidad p
    LEFT JOIN LATERAL (
        SELECT x.fecha, x.tipo, x.usuario
        FROM (
            SELECT mp.fecha_movimiento AS fecha,
                   mp.tipo_movimiento::TEXT AS tipo,
                   us.nombre_completo::TEXT AS usuario
            FROM movimientos_productos mp
            INNER JOIN usuarios us ON us.id_usuario = mp.id_usuario
            WHERE mp.id_maternidad = p.id_maternidad
            UNION ALL
            SELECT au.fecha_ajuste,
                   'AJUSTE'::TEXT,
                   us.nombre_completo::TEXT
            FROM auditoria_ajustes au
            INNER JOIN usuarios us ON us.id_usuario = au.id_usuario
            WHERE au.id_maternidad = p.id_maternidad
        ) x
        ORDER BY x.fecha DESC
        LIMIT 1
    ) u ON TRUE
    WHERE UPPER(TRIM(p.estado)) = 'ACTIVO'
      AND p.stock <= p.stock_minimo

    UNION ALL

    SELECT
        m.id_farmacia,
        'VENCIMIENTO'::TEXT,
        'MEDICAMENTO'::TEXT,
        l.id_lote,
        m.codigo_invima::TEXT,
        m.nombre_comercial::TEXT,
        l.numero_lote::TEXT,
        l.cantidad_actual,
        NULL::INTEGER,
        l.fecha_vencimiento,
        (l.fecha_vencimiento - CURRENT_DATE),
        CASE
            WHEN l.fecha_vencimiento - CURRENT_DATE <= 15 THEN 'CRITICA'
            ELSE 'ADVERTENCIA'
        END::TEXT,
        u.fecha,
        u.tipo,
        u.usuario
    FROM lotes l
    INNER JOIN medicamentos m ON m.id_medicamento = l.id_medicamento
    LEFT JOIN LATERAL (
        SELECT x.fecha, x.tipo, x.usuario
        FROM (
            SELECT lm.fecha_hora AS fecha,
                   lm.tipo_movimiento::TEXT AS tipo,
                   us.nombre_completo::TEXT AS usuario
            FROM log_movimientos lm
            INNER JOIN usuarios us ON us.id_usuario = lm.id_usuario
            WHERE lm.id_lote = l.id_lote
            UNION ALL
            SELECT au.fecha_ajuste,
                   'AJUSTE'::TEXT,
                   us.nombre_completo::TEXT
            FROM auditoria_ajustes au
            INNER JOIN usuarios us ON us.id_usuario = au.id_usuario
            WHERE au.id_lote = l.id_lote
        ) x
        ORDER BY x.fecha DESC
        LIMIT 1
    ) u ON TRUE
    WHERE UPPER(TRIM(m.estado)) = 'ACTIVO'
      AND l.estado_lote = 'DISPONIBLE'
      AND l.cantidad_actual > 0
      AND l.fecha_vencimiento - CURRENT_DATE <= 30
) a;

COMMIT;
