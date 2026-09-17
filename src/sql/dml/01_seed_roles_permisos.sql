-- ==============================================================================
-- DML: Roles predeterminados del sistema
-- Ubicación: src/sql/dml/01_seed_roles_permisos.sql
-- ==============================================================================

INSERT INTO ROLES (nombre_rol, descripcion) VALUES
('ADMINISTRADOR', 'Acceso total a la configuración y gestión del sistema CareStock'),
('AUXILIAR_FARMACIA', 'Gestión de inventario, consulta de lotes e ingresos/salidas')
ON CONFLICT (nombre_rol) DO NOTHING;
