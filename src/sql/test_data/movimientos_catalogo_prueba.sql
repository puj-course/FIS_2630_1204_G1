BEGIN;

INSERT INTO FARMACIAS (codigo, nombre)
SELECT v.codigo, v.nombre
FROM (VALUES
    ('PRUEBA-A', 'Farmacia Prueba A'),
    ('PRUEBA-B', 'Farmacia Prueba B')
) AS v(codigo, nombre)
WHERE NOT EXISTS (
    SELECT 1 FROM FARMACIAS f WHERE f.codigo = v.codigo
);

INSERT INTO ASEO (codigo, nombre, descripcion, precio, stock, tipo_aseo, biodegradable, componentes_activos, estado, id_farmacia, id_usuario_creacion)
SELECT v.codigo, v.nombre, v.descripcion, v.precio, v.stock, v.tipo_aseo, v.biodegradable, v.componentes_activos, 'ACTIVO', f.id_farmacia, u.id_usuario
FROM (VALUES
    ('PRUEBA-A', 'ASEO-P-01', 'Jabón líquido', 'Jabón líquido para manos', 8500, 15, 'Higiene de manos', TRUE, 'Tensioactivos suaves'),
    ('PRUEBA-B', 'ASEO-P-01', 'Alcohol antiséptico', 'Alcohol antiséptico al 70%', 6200, 14, 'Desinfección', FALSE, 'Alcohol etílico')
) AS v(farmacia, codigo, nombre, descripcion, precio, stock, tipo_aseo, biodegradable, componentes_activos)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
WHERE NOT EXISTS (
    SELECT 1 FROM ASEO a
    WHERE a.id_farmacia = f.id_farmacia AND a.codigo = v.codigo
);

INSERT INTO MATERNIDAD (codigo, nombre, descripcion, precio, stock, etapa_recomendada, hipoalergenico, edad_gestacional_sugerida, estado, id_farmacia, id_usuario_creacion)
SELECT v.codigo, v.nombre, v.descripcion, v.precio, v.stock, v.etapa_recomendada, v.hipoalergenico, v.edad_gestacional_sugerida, 'ACTIVO', f.id_farmacia, u.id_usuario
FROM (VALUES
    ('PRUEBA-A', 'MAT-P-01', 'Pañales etapa 1', 'Pañales desechables recién nacido', 38000, 30, 'Recién nacido', TRUE, NULL::int),
    ('PRUEBA-B', 'MAT-P-01', 'Crema para pañalitis', 'Crema protectora para la piel del bebé', 14500, 8, 'Lactancia', TRUE, NULL::int)
) AS v(farmacia, codigo, nombre, descripcion, precio, stock, etapa_recomendada, hipoalergenico, edad_gestacional_sugerida)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
WHERE NOT EXISTS (
    SELECT 1 FROM MATERNIDAD p
    WHERE p.id_farmacia = f.id_farmacia AND p.codigo = v.codigo
);

INSERT INTO MOVIMIENTOS_CATALOGO (id_farmacia, id_usuario, id_aseo, tipo_movimiento, cantidad, motivo, fecha_movimiento)
SELECT f.id_farmacia, u.id_usuario, a.id_aseo, v.tipo, v.cantidad, v.motivo, CURRENT_TIMESTAMP - v.antiguedad
FROM (VALUES
    ('PRUEBA-A', 'ASEO-P-01', 'ENTRADA', 20, 'PRUEBA: compra inicial', INTERVAL '3 days'),
    ('PRUEBA-A', 'ASEO-P-01', 'SALIDA', 5, 'PRUEBA: venta en mostrador', INTERVAL '1 day'),
    ('PRUEBA-B', 'ASEO-P-01', 'ENTRADA', 15, 'PRUEBA: compra inicial', INTERVAL '3 days'),
    ('PRUEBA-B', 'ASEO-P-01', 'AJUSTE', 1, 'PRUEBA: ajuste por producto dañado', INTERVAL '1 day')
) AS v(farmacia, codigo, tipo, cantidad, motivo, antiguedad)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
INNER JOIN ASEO a
    ON a.id_farmacia = f.id_farmacia AND a.codigo = v.codigo
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
WHERE NOT EXISTS (
    SELECT 1 FROM MOVIMIENTOS_CATALOGO m
    WHERE m.id_farmacia = f.id_farmacia
      AND m.id_aseo = a.id_aseo
      AND m.tipo_movimiento = v.tipo
      AND m.motivo = v.motivo
);

INSERT INTO MOVIMIENTOS_CATALOGO (id_farmacia, id_usuario, id_maternidad, tipo_movimiento, cantidad, motivo, fecha_movimiento)
SELECT f.id_farmacia, u.id_usuario, p.id_maternidad, v.tipo, v.cantidad, v.motivo, CURRENT_TIMESTAMP - v.antiguedad
FROM (VALUES
    ('PRUEBA-A', 'MAT-P-01', 'ENTRADA', 30, 'PRUEBA: compra inicial', INTERVAL '3 days'),
    ('PRUEBA-A', 'MAT-P-01', 'AJUSTE', 2, 'PRUEBA: ajuste por conteo físico', INTERVAL '2 hours'),
    ('PRUEBA-B', 'MAT-P-01', 'ENTRADA', 12, 'PRUEBA: compra inicial', INTERVAL '3 days'),
    ('PRUEBA-B', 'MAT-P-01', 'SALIDA', 4, 'PRUEBA: venta en mostrador', INTERVAL '5 hours')
) AS v(farmacia, codigo, tipo, cantidad, motivo, antiguedad)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
INNER JOIN MATERNIDAD p
    ON p.id_farmacia = f.id_farmacia AND p.codigo = v.codigo
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
WHERE NOT EXISTS (
    SELECT 1 FROM MOVIMIENTOS_CATALOGO m
    WHERE m.id_farmacia = f.id_farmacia
      AND m.id_maternidad = p.id_maternidad
      AND m.tipo_movimiento = v.tipo
      AND m.motivo = v.motivo
);

COMMIT;
