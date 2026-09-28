-- ==============================================================================
-- Roles predeterminados CareStock
-- ==============================================================================

INSERT INTO ROLES (
    nombre_rol,
    descripcion
)
VALUES
(
    'SUPER_ADMIN',
    'Administrador global de CareStock'
),
(
    'ADMINISTRADOR',
    'Administrador responsable de una farmacia'
),
(
    'FARMACEUTICO',
    'Usuario operativo asociado a una farmacia'
)
ON CONFLICT (nombre_rol)
DO NOTHING;
