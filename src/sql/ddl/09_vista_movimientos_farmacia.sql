-- =============================================================================
-- Vista de movimientos de inventario por farmacia
--
-- LOG_MOVIMIENTOS no almacena id_farmacia directamente; se deduce a
-- través de LOTES -> MEDICAMENTOS -> id_farmacia, que es donde vive
-- la segregación real de cada registro de inventario.
-- =============================================================================

BEGIN;

CREATE OR REPLACE VIEW VW_MOVIMIENTOS_FARMACIA AS
SELECT
    lm.id_log,
    m.id_farmacia,
    lm.tipo_movimiento,
    l.numero_lote,
    m.nombre_comercial AS medicamento,
    lm.cantidad_afectada,
    u.nombre_completo AS usuario_responsable,
    lm.fecha_hora
FROM LOG_MOVIMIENTOS lm
INNER JOIN LOTES l
    ON l.id_lote = lm.id_lote
INNER JOIN MEDICAMENTOS m
    ON m.id_medicamento = l.id_medicamento
INNER JOIN USUARIOS u
    ON u.id_usuario = lm.id_usuario
ORDER BY lm.fecha_hora DESC;

COMMIT;
