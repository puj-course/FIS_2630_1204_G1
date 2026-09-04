


-- 0. Eliminar tablas si ya existen (orden inverso por FKs)
DROP TABLE IF EXISTS shrinkage_events CASCADE;
DROP TABLE IF EXISTS motivos_descarte CASCADE;
DROP TABLE IF EXISTS lotes CASCADE;

-- 1. Tabla de Lotes (adaptación necesaria para trazabilidad)

CREATE TABLE lotes (
    id SERIAL PRIMARY KEY,
    medicamento_id INT NOT NULL,
    numero_lote VARCHAR(50) NOT NULL,
    cantidad_actual INT NOT NULL DEFAULT 0 CHECK (cantidad_actual >= 0),
    fecha_vencimiento DATE,
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
        CHECK (estado IN ('ACTIVO', 'AGOTADO', 'VENCIDO', 'DESCARTADO')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lote_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id),
    CONSTRAINT uq_lote_medicamento UNIQUE (medicamento_id, numero_lote)
);


-- 2. Catálogo de Motivos de Descarte / Merma

CREATE TABLE motivos_descarte (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    requiere_evidencia BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 3. Tabla Transaccional de Mermas (Shrinkage Events)

CREATE TABLE shrinkage_events (
    id SERIAL PRIMARY KEY,
    producto_id INT NOT NULL,          -- referencia a medicamentos
    lote_id INT NOT NULL,              -- referencia a lotes
    usuario_id INT NOT NULL,           -- quién registra el descarte
    motivo_id INT NOT NULL,            -- catálogo de motivos
    cantidad INT NOT NULL CHECK (cantidad > 0),
    valor_estimado NUMERIC(12,2),      -- costo estimado de la pérdida (opcional)
    observaciones TEXT,
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO'
        CHECK (estado IN ('REGISTRADO', 'ANULADO')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shrinkage_producto FOREIGN KEY (producto_id) REFERENCES medicamentos(id),
    CONSTRAINT fk_shrinkage_lote FOREIGN KEY (lote_id) REFERENCES lotes(id),
    CONSTRAINT fk_shrinkage_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_shrinkage_motivo FOREIGN KEY (motivo_id) REFERENCES motivos_descarte(id)
);

-- Índices para consultas frecuentes (reportes, filtros por fecha/producto/motivo)
CREATE INDEX idx_shrinkage_producto ON shrinkage_events(producto_id);
CREATE INDEX idx_shrinkage_lote ON shrinkage_events(lote_id);
CREATE INDEX idx_shrinkage_usuario ON shrinkage_events(usuario_id);
CREATE INDEX idx_shrinkage_motivo ON shrinkage_events(motivo_id);
CREATE INDEX idx_shrinkage_fecha ON shrinkage_events(fecha_evento);


-- 4. TRIGGER: Descontar stock automáticamente al registrar merma
--    y reflejar el evento en inventario_movimientos (auditoría)

CREATE OR REPLACE FUNCTION fn_procesar_shrinkage()
RETURNS TRIGGER AS $$
BEGIN

    IF (SELECT cantidad_actual FROM lotes WHERE id = NEW.lote_id) < NEW.cantidad THEN
        RAISE EXCEPTION 'Cantidad a descartar (%) supera la cantidad actual del lote %',
            NEW.cantidad, NEW.lote_id;
    END IF;


    UPDATE lotes
    SET cantidad_actual = cantidad_actual - NEW.cantidad,
        estado = CASE
            WHEN (cantidad_actual - NEW.cantidad) <= 0 THEN 'DESCARTADO'
            ELSE estado
        END
    WHERE id = NEW.lote_id;


    UPDATE medicamentos
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;

    -- Registrar en la bitácora general de movimientos (trazabilidad unificada)
    INSERT INTO inventario_movimientos (
        medicamento_id, usuario_id, tipo_movimiento, cantidad, observacion
    ) VALUES (
        NEW.producto_id,
        NEW.usuario_id,
        'AJUSTE',
        -NEW.cantidad,
        CONCAT('Merma/Descarte - Lote: ', NEW.lote_id, ' - Motivo ID: ', NEW.motivo_id,
               COALESCE(' - Obs: ' || NEW.observaciones, ''))
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shrinkage_after_insert
AFTER INSERT ON shrinkage_events
FOR EACH ROW
EXECUTE FUNCTION fn_procesar_shrinkage();


-- 5. DATOS INICIALES (Seed Data)


-- Catálogo de motivos de descarte
INSERT INTO motivos_descarte (codigo, nombre, descripcion, requiere_evidencia) VALUES
('VENCIMIENTO', 'Producto vencido', 'El medicamento superó su fecha de vencimiento', FALSE),
('DANIO_FISICO', 'Daño físico / rotura', 'Empaque roto, derrame o deterioro del producto', TRUE),
('CADENA_FRIO', 'Ruptura de cadena de frío', 'Producto expuesto a temperatura fuera de rango', TRUE),
('CONTAMINACION', 'Contaminación / sospecha de alteración', 'Producto contaminado o con signos de manipulación', TRUE),
('RETIRO_SANITARIO', 'Retiro por autoridad sanitaria', 'Alerta o retiro emitido por INVIMA u otra entidad', TRUE),
('ERROR_INVENTARIO', 'Ajuste por error de inventario', 'Diferencia detectada en conteo físico vs. sistema', FALSE),
('ROBO_PERDIDA', 'Robo o pérdida', 'Faltante sin justificación operativa identificada', TRUE);

-- Ejemplo de lote de prueba (asumiendo medicamento_id = 1 ya existe)
INSERT INTO lotes (medicamento_id, numero_lote, cantidad_actual, fecha_vencimiento) VALUES
(1, 'LOT-2026-001', 20, CURRENT_DATE + INTERVAL '15 days'),
(2, 'LOT-2026-002', 150, '2027-12-31');



-- 6. VISTA DE REPORTE: Mermas por producto y motivo

CREATE OR REPLACE VIEW vw_reporte_mermas AS
SELECT
    se.id AS evento_id,
    m.nombre AS medicamento,
    m.invima,
    l.numero_lote,
    l.fecha_vencimiento,
    u.nombre_completo AS registrado_por,
    md.nombre AS motivo,
    se.cantidad,
    se.valor_estimado,
    se.observaciones,
    se.fecha_evento
FROM shrinkage_events se
JOIN medicamentos m ON m.id = se.producto_id
JOIN lotes l ON l.id = se.lote_id
JOIN usuarios u ON u.id = se.usuario_id
JOIN motivos_descarte md ON md.id = se.motivo_id
WHERE se.estado = 'REGISTRADO'
ORDER BY se.fecha_evento DESC;


