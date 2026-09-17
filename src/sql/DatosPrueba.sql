

-- Roles
INSERT INTO roles (nombre_rol, descripcion) VALUES
('ADMINISTRADOR', 'Acceso total al sistema'),
('FARMACEUTICO', 'Gestión de medicamentos, lotes y movimientos')
ON CONFLICT (nombre_rol) DO NOTHING;

-- Categorías
INSERT INTO categorias (nombre_categoria, descripcion) VALUES
('ANALGESICOS', 'Medicamentos para aliviar el dolor'),
('ANTIBIOTICOS', 'Tratamiento de infecciones bacterianas')
ON CONFLICT (nombre_categoria) DO NOTHING;

-- Ubicaciones
INSERT INTO ubicaciones (estante, nivel, descripcion) VALUES
('A-01', 'N1', 'Pasillo principal - Alta rotación')
ON CONFLICT (estante, nivel) DO NOTHING;

-- Usuarios
INSERT INTO usuarios (nombre_completo, email, password_hash, id_rol) VALUES
('Admin CareStock', 'admin@carestock.com', '$2a$12$hash_ejemplo',
 (SELECT id_rol FROM roles WHERE nombre_rol = 'ADMINISTRADOR'))
ON CONFLICT (email) DO NOTHING;

-- Medicamentos
INSERT INTO medicamentos (codigo_invima, nombre_comercial, principio_activo, concentracion, forma_farmaceutica, id_categoria, stock_minimo, stock_total, estado) VALUES
('INVIMA-2024M-001', 'Acetaminofén 500mg', 'Paracetamol', '500 mg', 'Tabletas',
 (SELECT id_categoria FROM categorias WHERE nombre_categoria = 'ANALGESICOS'), 100, 20, 'ACTIVO')
ON CONFLICT (codigo_invima) DO NOTHING;