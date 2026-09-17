-- ==============================================================================
-- SCRIPT DE DATOS DE PRUEBA: HU.35 - Validaciones de Lotes
-- Ubicación: src/sql/test_data/hu35_dataset_validaciones.sql
-- Base de Datos: PostgreSQL (Neon DB)
-- ==============================================================================

-- 1. Insertar Medicamento base para pruebas de validación
INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, concentracion, forma_farmaceutica, id_categoria, stock_minimo, stock_total, estado) VALUES
('INVIMA-2024M-010', 'Ibuprofeno 800mg', 'Ibuprofeno', '800 mg', 'Tableta', (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = 'ANALGESICOS'), 20, 100, 'ACTIVO')
ON CONFLICT (codigo_invima) DO NOTHING;

-- 2. Lote Existente Base (Servirá para probar el intento de DUPLICADO)
-- Intento de Insertar: Lote 'LOT-IBU-EXISTENTE' para Ibuprofeno 800mg
INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote) VALUES
(
    'LOT-IBU-EXISTENTE', 
    (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-010'), 
    100, 
    '2027-06-30', 
    (SELECT id_ubicacion FROM UBICACIONES WHERE estante = 'Estante A' AND nivel = 'Nivel 1'), 
    'DISPONIBLE'
)
ON CONFLICT (numero_lote, id_medicamento) DO NOTHING;

-- ==============================================================================
-- CASOS DE PRUEBA DE VALIDACIÓN (Para ser probados desde JavaFX o consola SQL)
-- ==============================================================================

/*
 CASO DE PRUEBA 1: Lote Duplicado para el mismo medicamento
 EXPECTATIVO BD: Viola la restricción UNIQUE 'uq_lotes_numero'.
 EXPECTATIVO UI: JavaFX debe interceptar o capturar la excepción y mostrar:
 "El número de lote 'LOT-IBU-EXISTENTE' ya se encuentra registrado para este medicamento."

 INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote) VALUES
 ('LOT-IBU-EXISTENTE', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-010'), 50, '2028-01-01', 1, 'DISPONIBLE');
*/

/*
 CASO DE PRUEBA 2: Fecha de Vencimiento Vencida o Pasada
 EXPECTATIVO UI: La vista/controlador en JavaFX debe bloquear el botón antes del INSERT:
 "La fecha de vencimiento debe ser posterior a la fecha actual."

 INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote) VALUES
 ('LOT-IBU-PASADO', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-010'), 50, '2023-01-01', 1, 'DISPONIBLE');
*/

/*
 CASO DE PRUEBA 3: Cantidad Menor o Igual a Cero
 EXPECTATIVO BD: Viola la restricción CHECK 'ck_lotes_cantidad'.
 EXPECTATIVO UI: JavaFX debe validar el campo numérico:
 "La cantidad ingresada debe ser un número entero mayor a 0."

 INSERT INTO LOTES (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, id_ubicacion, estado_lote) VALUES
 ('LOT-IBU-CERO', (SELECT id_medicamento FROM MEDICAMENTOS WHERE codigo_invima = 'INVIMA-2024M-010'), 0, '2027-12-31', 1, 'DISPONIBLE');
*/
