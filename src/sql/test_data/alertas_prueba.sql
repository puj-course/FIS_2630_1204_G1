BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM USUARIOS) THEN
        RAISE EXCEPTION 'alertas_prueba.sql requiere al menos un usuario';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'aseo'
          AND column_name = 'stock_minimo'
    ) THEN
        RAISE EXCEPTION 'alertas_prueba.sql requiere el script 15_stock_minimo_aseo_maternidad.sql';
    END IF;
    IF to_regclass('vw_alertas_inventario') IS NULL THEN
        RAISE EXCEPTION 'alertas_prueba.sql requiere el script 16_vista_alertas_inventario.sql';
    END IF;
END $$;

INSERT INTO FARMACIAS (codigo, nombre)
SELECT v.codigo, v.nombre
FROM (VALUES
    ('ALERTAS-A', 'Farmacia Alertas A'),
    ('ALERTAS-B', 'Farmacia Alertas B')
) AS v(codigo, nombre)
WHERE NOT EXISTS (
    SELECT 1 FROM FARMACIAS f WHERE f.codigo = v.codigo
);

INSERT INTO CATEGORIAS (nombre_categoria, descripcion)
SELECT 'Alertas de prueba', 'Categoría de los medicamentos de alertas_prueba.sql'
WHERE NOT EXISTS (
    SELECT 1 FROM CATEGORIAS c WHERE c.nombre_categoria = 'Alertas de prueba'
);

INSERT INTO UBICACIONES (estante, nivel, descripcion)
SELECT 'ALERTAS', 'N1', 'Ubicación de los lotes de alertas_prueba.sql'
WHERE NOT EXISTS (
    SELECT 1 FROM UBICACIONES ub WHERE ub.estante = 'ALERTAS' AND ub.nivel = 'N1'
);

INSERT INTO ASEO (codigo, nombre, descripcion, precio, stock, stock_minimo, estado, id_farmacia, id_usuario_creacion)
SELECT v.codigo, v.nombre, v.descripcion, v.precio, v.stock, v.stock_minimo, 'ACTIVO', f.id_farmacia, u.id_usuario
FROM (VALUES
    ('ALERTAS-A', 'ALERTAS-A-ASEO-01', 'Alertas: aseo sin existencias', 'Producto de aseo sin existencias', 8500, 0, 5),
    ('ALERTAS-B', 'ALERTAS-B-ASEO-01', 'Alertas: aseo bajo el mínimo', 'Producto de aseo bajo el mínimo', 6200, 2, 6)
) AS v(farmacia, codigo, nombre, descripcion, precio, stock, stock_minimo)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
ON CONFLICT (id_farmacia, codigo) DO UPDATE
SET stock = EXCLUDED.stock,
    stock_minimo = EXCLUDED.stock_minimo,
    estado = 'ACTIVO';

INSERT INTO MATERNIDAD (codigo, nombre, descripcion, precio, stock, stock_minimo, estado, id_farmacia, id_usuario_creacion)
SELECT v.codigo, v.nombre, v.descripcion, v.precio, v.stock, v.stock_minimo, 'ACTIVO', f.id_farmacia, u.id_usuario
FROM (VALUES
    ('ALERTAS-A', 'ALERTAS-A-MAT-01', 'Alertas: maternidad bajo el mínimo', 'Producto de maternidad bajo el mínimo', 38000, 3, 8)
) AS v(farmacia, codigo, nombre, descripcion, precio, stock, stock_minimo)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
ON CONFLICT (id_farmacia, codigo) DO UPDATE
SET stock = EXCLUDED.stock,
    stock_minimo = EXCLUDED.stock_minimo,
    estado = 'ACTIVO';

INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, id_categoria, stock_minimo, estado, id_farmacia)
SELECT v.codigo, v.nombre, 'Principio de prueba', c.id_categoria, v.stock_minimo, 'ACTIVO', f.id_farmacia
FROM (VALUES
    ('ALERTAS-A', 'ALERTAS-A-MED-01', 'Alertas: medicamento sin existencias', 10),
    ('ALERTAS-A', 'ALERTAS-A-MED-02', 'Alertas: medicamento bajo el mínimo', 20),
    ('ALERTAS-A', 'ALERTAS-A-MED-03', 'Alertas: lote vencido', 0),
    ('ALERTAS-A', 'ALERTAS-A-MED-04', 'Alertas: lote que vence en 10 días', 0),
    ('ALERTAS-A', 'ALERTAS-A-MED-05', 'Alertas: lote que vence en 20 días', 0),
    ('ALERTAS-A', 'ALERTAS-A-MED-06', 'Alertas: lote que vence en 60 días', 0),
    ('ALERTAS-B', 'ALERTAS-B-MED-01', 'Alertas: lote que vence en 25 días', 0)
) AS v(farmacia, codigo, nombre, stock_minimo)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
INNER JOIN CATEGORIAS c
    ON c.nombre_categoria = 'Alertas de prueba'
ON CONFLICT (id_farmacia, codigo_invima) DO UPDATE
SET stock_minimo = EXCLUDED.stock_minimo,
    estado = 'ACTIVO';

INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, id_usuario, estado_lote)
SELECT v.numero_lote, m.id_medicamento, v.cantidad, CURRENT_DATE + v.dias, ub.id_ubicacion, u.id_usuario, 'DISPONIBLE'
FROM (VALUES
    ('ALERTAS-A', 'ALERTAS-A-MED-02', 'ALERTAS-A-L02', 8, 90),
    ('ALERTAS-A', 'ALERTAS-A-MED-03', 'ALERTAS-A-L03', 20, -5),
    ('ALERTAS-A', 'ALERTAS-A-MED-03', 'ALERTAS-A-L03B', 50, 90),
    ('ALERTAS-A', 'ALERTAS-A-MED-04', 'ALERTAS-A-L04', 30, 10),
    ('ALERTAS-A', 'ALERTAS-A-MED-05', 'ALERTAS-A-L05', 25, 20),
    ('ALERTAS-A', 'ALERTAS-A-MED-06', 'ALERTAS-A-L06', 40, 60),
    ('ALERTAS-B', 'ALERTAS-B-MED-01', 'ALERTAS-B-L01', 15, 25)
) AS v(farmacia, medicamento, numero_lote, cantidad, dias)
INNER JOIN FARMACIAS f
    ON f.codigo = v.farmacia
INNER JOIN MEDICAMENTOS m
    ON m.id_farmacia = f.id_farmacia AND m.codigo_invima = v.medicamento
INNER JOIN UBICACIONES ub
    ON ub.estante = 'ALERTAS' AND ub.nivel = 'N1'
CROSS JOIN (
    SELECT id_usuario FROM USUARIOS ORDER BY id_usuario LIMIT 1
) u
ON CONFLICT (numero_lote, id_medicamento) DO UPDATE
SET cantidad_actual = EXCLUDED.cantidad_actual,
    fecha_vencimiento = EXCLUDED.fecha_vencimiento,
    estado_lote = 'DISPONIBLE';

COMMIT;
