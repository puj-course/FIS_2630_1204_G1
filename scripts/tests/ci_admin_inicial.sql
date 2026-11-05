-- =========================================================
-- CareStock - Inicializacion de PostgreSQL para CI
-- Exclusivamente para base de datos temporal de pruebas.
-- Dependencia: 01_schema_carestock.sql
-- Se ejecuta antes de la migracion #456.
-- =========================================================

BEGIN;

-- Crear rol administrativo si no existe.
INSERT INTO roles (
    nombre_rol,
    descripcion
)
VALUES (
    'ADMINISTRADOR',
    'Administrador de farmacia'
)
ON CONFLICT (nombre_rol) DO NOTHING;

-- Crear administrador ficticio para la migracion #456.
-- QA_NO_LOGIN no es un hash BCrypt valido.
-- Este usuario no permite autenticacion real.
INSERT INTO usuarios (
    nombre_completo,
    email,
    password_hash,
    id_rol
)
SELECT
    'Admin CareStock',
    'admin@carestock.com',
    'QA_NO_LOGIN',
    id_rol
FROM roles
WHERE nombre_rol = 'ADMINISTRADOR'
ON CONFLICT (email) DO NOTHING;

-- Confirmar que la preparacion fue exitosa.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM usuarios
        WHERE LOWER(email) = 'admin@carestock.com'
    ) THEN
        RAISE EXCEPTION
            'CI: No fue posible crear Admin CareStock';
    END IF;
END $$;

COMMIT;
