-- ==============================================================================
-- TEST DATA: Script de datos de prueba para la HU.34
-- Ubicación: src/sql/test_data/hu34_dataset_lotes.sql
-- ==============================================================================

-- 1. Medicamentos de prueba
INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, concentracion, forma_farmaceutica, id_categoria, stock_minimo, stock_total, estado) VALUES
('INVIMA-2024M-001', 'Acetaminofén 500mg', 'Paracetamol', '500 mg', 'Tableta', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'ANALGESICOS'), 50, 250, 'ACTIVO'),
('INVIMA-2024M-002', 'Amoxicilina 500mg', 'Amoxicilina', '500 mg', 'Cápsula', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'ANTIBIOTICOS'), 30, 100, 'ACTIVO'),
('INVIMA-2024M-003', 'Losartán 50mg', 'Losartán', '50 mg', 'Comprimido', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'CARDIOVASCULAR'), 20, 0, 'ACTIVO')
ON CONFLICT (codigo_invima) DO NOTHING;

-- 2. Lotes para Acetaminofén (Prueba con múltiples lotes)
INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote) VALUES
('LOT-ACET-2024-01', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-001'), 150, '2026-12-31', (SELECT id_ubicacion FROM UBICACIONES WHERE estante = 'Estante A' AND nivel = 'Nivel 1'), 'DISPONIBLE'),
('LOT-ACET-2024-02', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-001'), 100, '2025-08-15', (SELECT id_ubicacion FROM UBICACIONES WHERE estante = 'Estante B' AND nivel = 'Nivel 3'), 'DISPONIBLE'),
('LOT-ACET-VENCIDO-01', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-001'), 0, '2023-05-10', (SELECT id_ubicacion FROM UBICACIONES WHERE estante = 'Estante A' AND nivel = 'Nivel 1'), 'VENCIDO')
ON CONFLICT (numero_lote, id_medicamento) DO NOTHING;

-- 3. Lotes para Amoxicilina (Prueba con un solo lote)
INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote) VALUES
('LOT-AMOX-2024-01', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-002'), 100, '2027-01-20', (SELECT id_ubicacion FROM UBICACIONES WHERE estante = 'Estante C' AND nivel = 'Nivel 2'), 'DISPONIBLE')
ON CONFLICT (numero_lote, id_medicamento) DO NOTHING;
