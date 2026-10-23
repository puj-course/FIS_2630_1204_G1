SELECT 'STOCK_NEGATIVO' AS problema,
       'aseo' AS tabla,
       a.id_aseo::text AS id_registro,
       a.id_farmacia AS id_farmacia,
       'stock = ' || a.stock AS detalle
FROM aseo a
WHERE a.stock < 0

UNION ALL

SELECT 'STOCK_NEGATIVO',
       'maternidad',
       p.id_maternidad::text,
       p.id_farmacia,
       'stock = ' || p.stock
FROM maternidad p
WHERE p.stock < 0

UNION ALL

SELECT 'CANTIDAD_NEGATIVA',
       'lotes',
       l.id_lote::text,
       m.id_farmacia,
       'cantidad_actual = ' || l.cantidad_actual
FROM lotes l
LEFT JOIN medicamentos m ON m.id_medicamento = l.id_medicamento
WHERE l.cantidad_actual < 0

UNION ALL

SELECT 'PRODUCTO_SIN_FARMACIA',
       'aseo',
       a.id_aseo::text,
       a.id_farmacia,
       CASE WHEN a.id_farmacia IS NULL
            THEN 'id_farmacia vacío'
            ELSE 'la farmacia ' || a.id_farmacia || ' no existe'
       END
FROM aseo a
LEFT JOIN farmacias f ON f.id_farmacia = a.id_farmacia
WHERE f.id_farmacia IS NULL

UNION ALL

SELECT 'PRODUCTO_SIN_FARMACIA',
       'maternidad',
       p.id_maternidad::text,
       p.id_farmacia,
       CASE WHEN p.id_farmacia IS NULL
            THEN 'id_farmacia vacío'
            ELSE 'la farmacia ' || p.id_farmacia || ' no existe'
       END
FROM maternidad p
LEFT JOIN farmacias f ON f.id_farmacia = p.id_farmacia
WHERE f.id_farmacia IS NULL

UNION ALL

SELECT 'PRODUCTO_SIN_FARMACIA',
       'medicamentos',
       m.id_medicamento::text,
       m.id_farmacia,
       CASE WHEN m.id_farmacia IS NULL
            THEN 'id_farmacia vacío'
            ELSE 'la farmacia ' || m.id_farmacia || ' no existe'
       END
FROM medicamentos m
LEFT JOIN farmacias f ON f.id_farmacia = m.id_farmacia
WHERE f.id_farmacia IS NULL

UNION ALL

SELECT 'LOTE_SIN_MEDICAMENTO',
       'lotes',
       l.id_lote::text,
       NULL::int,
       'id_medicamento = ' || COALESCE(l.id_medicamento::text, 'vacío')
FROM lotes l
LEFT JOIN medicamentos m ON m.id_medicamento = l.id_medicamento
WHERE m.id_medicamento IS NULL

UNION ALL

SELECT 'CODIGO_REPETIDO',
       'aseo',
       string_agg(a.id_aseo::text, ', ' ORDER BY a.id_aseo),
       a.id_farmacia,
       'código ' || TRIM(a.codigo) || ' en ' || count(*) || ' registros'
FROM aseo a
GROUP BY a.id_farmacia, TRIM(a.codigo)
HAVING count(*) > 1

UNION ALL

SELECT 'CODIGO_REPETIDO',
       'maternidad',
       string_agg(p.id_maternidad::text, ', ' ORDER BY p.id_maternidad),
       p.id_farmacia,
       'código ' || TRIM(p.codigo) || ' en ' || count(*) || ' registros'
FROM maternidad p
GROUP BY p.id_farmacia, TRIM(p.codigo)
HAVING count(*) > 1

ORDER BY problema, tabla, id_registro;
