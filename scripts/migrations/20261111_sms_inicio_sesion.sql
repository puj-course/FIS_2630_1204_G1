-- CareStock: suscripcion explicita a SMS de inicio de sesion.
-- Requiere migraciones 885 y 926. Ejecutar de forma controlada.
BEGIN;
ALTER TABLE sms_destinatarios
    ADD COLUMN IF NOT EXISTS notifica_inicio_sesion
        BOOLEAN NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN sms_destinatarios.notifica_inicio_sesion
    IS 'Autoriza recibir SMS de inicios de sesion de la propia farmacia';
COMMIT;
