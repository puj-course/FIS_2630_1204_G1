CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS ix_usuarios_nombre_trgm
ON USUARIOS USING gin (nombre_completo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_usuarios_rol
ON USUARIOS (id_rol);
