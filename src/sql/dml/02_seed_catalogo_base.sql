-- ==============================================================================
-- DML: Datos base requeridos por la aplicación (Categorías y Ubicaciones iniciales)
-- Ubicación: src/sql/dml/02_seed_catalogo_base.sql
-- ==============================================================================

INSERT INTO CATEGORIAS (nombre_categoria, descripcion) VALUES
('ANALGESICOS', 'Medicamentos para el control y alivio del dolor'),
('ANTIBIOTICOS', 'Tratamiento de infecciones bacterianas'),
('CARDIOVASCULAR', 'Tratamiento para la presión arterial y enfermedades cardíacas')
ON CONFLICT (nombre_categoria) DO NOTHING;

INSERT INTO UBICACIONES (estante, nivel, descripcion) VALUES
('Estante A', 'Nivel 1', 'Bodega Principal - Almacenamiento General'),
('Estante B', 'Nivel 3', 'Bodega Secundaria - Controlados'),
('Estante C', 'Nivel 2', 'Estantería Central - Cadena de Frío')
ON CONFLICT (estante, nivel) DO NOTHING;
