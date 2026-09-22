
CREATE TABLE IF NOT EXISTS log_accesos (
    id_acceso     INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    id_usuario    INTEGER NOT NULL,
    resultado     VARCHAR(20) NOT NULL,
    fecha_hora    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_log_accesos_resultado CHECK (resultado IN ('EXITOSO', 'FALLIDO')),
    CONSTRAINT fk_log_accesos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE INDEX IF NOT EXISTS ix_log_accesos_fecha ON log_accesos (fecha_hora);
CREATE INDEX IF NOT EXISTS ix_log_accesos_usuario ON log_accesos (id_usuario);



CREATE OR REPLACE FUNCTION fn_registrar_acceso(
    p_id_usuario INTEGER,
    p_resultado VARCHAR
) RETURNS INTEGER AS $$
DECLARE
    v_id_acceso INTEGER;
BEGIN
    IF p_resultado NOT IN ('EXITOSO', 'FALLIDO') THEN
        RAISE EXCEPTION 'El resultado debe ser EXITOSO o FALLIDO';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = p_id_usuario) THEN
        RAISE EXCEPTION 'El usuario indicado no existe';
    END IF;

    INSERT INTO log_accesos (id_usuario, resultado)
    VALUES (p_id_usuario, p_resultado)
    RETURNING id_acceso INTO v_id_acceso;

    RETURN v_id_acceso;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE VIEW vw_historial_accesos AS
SELECT
    la.id_acceso,
    la.id_usuario,
    u.nombre_completo,
    u.email,
    la.resultado,
    la.fecha_hora
FROM log_accesos la
JOIN usuarios u ON u.id_usuario = la.id_usuario
ORDER BY la.fecha_hora DESC;


SELECT table_name FROM information_schema.tables WHERE table_name = 'log_accesos';
SELECT proname FROM pg_proc WHERE proname = 'fn_registrar_acceso';
SELECT table_name FROM information_schema.views WHERE table_name = 'vw_historial_accesos';
