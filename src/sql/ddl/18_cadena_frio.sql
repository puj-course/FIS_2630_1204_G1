-- =============================================================================
-- HU.150: Alerta visual y bloqueo por "Falta de Cadena de Frío"
-- =============================================================================

BEGIN;

ALTER TABLE MEDICAMENTOS
    ADD COLUMN IF NOT EXISTS requiere_refrigeracion BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN MEDICAMENTOS.requiere_refrigeracion IS
    'Indica si el medicamento requiere almacenamiento en cadena de frío.';

COMMIT;
