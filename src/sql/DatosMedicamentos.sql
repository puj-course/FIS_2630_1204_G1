INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, concentracion, id_categoria, stock_total, stock_minimo) VALUES
('INVIMA-2024M-001', 'Acetaminofén 500mg', 'Paracetamol', '500 mg', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'ANALGESICOS'), 20, 100),
('INVIMA-2024M-002', 'Amoxicilina 500mg', 'Amoxicilina', '500 mg', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'ANTIBIOTICOS'), 150, 50),
('INVIMA-2024M-003', 'Losartán 50mg', 'Losartán', '50 mg', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'CARDIOVASCULAR'), 15, 60)
ON CONFLICT (codigo_invima) DO NOTHING;