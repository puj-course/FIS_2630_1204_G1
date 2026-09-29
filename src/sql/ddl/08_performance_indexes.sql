-- =============================================================================
-- CareStock
-- Optimización de consultas frecuentes
-- =============================================================================

-- Login y búsqueda case-insensitive de usuarios
CREATE INDEX CONCURRENTLY IF NOT EXISTS
    ix_usuarios_email_lower
ON USUARIOS (
    LOWER(email)
);


-- Consultas de medicamentos por farmacia y estado
CREATE INDEX CONCURRENTLY IF NOT EXISTS
    ix_medicamentos_farmacia_estado_nombre
ON MEDICAMENTOS (
    id_farmacia,
    estado,
    nombre_comercial
);


-- Consultas de lotes por medicamento,
-- disponibilidad y vencimiento
CREATE INDEX CONCURRENTLY IF NOT EXISTS
    ix_lotes_medicamento_estado_vencimiento
ON LOTES (
    id_medicamento,
    estado_lote,
    fecha_vencimiento
);


-- Recarga de lotes ordenados por ingreso
CREATE INDEX CONCURRENTLY IF NOT EXISTS
    ix_lotes_medicamento_fecha_ingreso
ON LOTES (
    id_medicamento,
    fecha_ingreso DESC,
    id_lote DESC
);


ANALYZE USUARIOS;
ANALYZE MEDICAMENTOS;
ANALYZE LOTES;
