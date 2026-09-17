-- ==============================================================================
-- DDL: Funciones y Triggers para automatización de inventario
-- Ubicación: src/sql/ddl/02_functions_triggers.sql
-- ==============================================================================

-- Función para actualizar el stock total de un medicamento al modificar sus lotes
CREATE OR REPLACE FUNCTION fn_actualizar_stock_total_medicamento()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE MEDICAMENTOS
    SET stock_total = COALESCE((
        SELECT SUM(cantidad_actual)
        FROM LOTES
        WHERE id_medicamento = NEW.id_medicamento AND estado_lote = 'DISPONIBLE'
    ), 0)
    WHERE id_medicamento = NEW.id_medicamento;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger disparado tras insertar o actualizar la cantidad de un lote
CREATE OR REPLACE TRIGGER trg_actualizar_stock_total
AFTER INSERT OR UPDATE OF cantidad_actual, estado_lote ON LOTES
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_stock_total_medicamento();
