package org.example.carestock.service;

import org.example.carestock.model.AjusteExistencias;

import java.util.Objects;

/**
 * Consultas SQL estaticas para productos de aseo y maternidad.
 * Los identificadores se seleccionan de constantes internas;
 * los valores de negocio se asignan mediante PreparedStatement.
 */
final class ConsultasSqlInventario {

    private ConsultasSqlInventario() {
    }

    static String bloquearMovimiento(
            ServicioMovimientosProducto.Categoria categoria) {

        return switch (Objects.requireNonNull(categoria)) {
            case ASEO -> """
                    SELECT stock FROM aseo
                    WHERE id_aseo = ? AND id_farmacia = ?
                      AND UPPER(TRIM(estado)) = 'ACTIVO'
                    FOR UPDATE
                    """;

            case MATERNIDAD -> """
                    SELECT stock FROM maternidad
                    WHERE id_maternidad = ? AND id_farmacia = ?
                      AND UPPER(TRIM(estado)) = 'ACTIVO'
                    FOR UPDATE
                    """;
        };
    }

    static String actualizarMovimiento(
            ServicioMovimientosProducto.Categoria categoria) {

        return switch (Objects.requireNonNull(categoria)) {
            case ASEO -> """
                    UPDATE aseo
                    SET stock = ?,
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    WHERE id_aseo = ? AND id_farmacia = ?
                      AND UPPER(TRIM(estado)) = 'ACTIVO'
                    """;

            case MATERNIDAD -> """
                    UPDATE maternidad
                    SET stock = ?,
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    WHERE id_maternidad = ? AND id_farmacia = ?
                      AND UPPER(TRIM(estado)) = 'ACTIVO'
                    """;
        };
    }

    static String bloquearAjuste(
            AjusteExistencias.Categoria categoria) {

        return switch (Objects.requireNonNull(categoria)) {
            case ASEO -> """
                    SELECT stock FROM aseo
                    WHERE id_aseo = ? AND id_farmacia = ?
                      AND UPPER(BTRIM(estado)) = 'ACTIVO'
                    FOR UPDATE
                    """;

            case MATERNIDAD -> """
                    SELECT stock FROM maternidad
                    WHERE id_maternidad = ? AND id_farmacia = ?
                      AND UPPER(BTRIM(estado)) = 'ACTIVO'
                    FOR UPDATE
                    """;

            case MEDICAMENTO ->
                throw new IllegalArgumentException(
                    "Los medicamentos se ajustan por lote."
                );
        };
    }

    static String actualizarAjuste(
            AjusteExistencias.Categoria categoria) {

        return switch (Objects.requireNonNull(categoria)) {
            case ASEO -> """
                    UPDATE aseo
                    SET stock = ?,
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    WHERE id_aseo = ?
                      AND id_farmacia = ?
                      AND stock = ?
                      AND UPPER(BTRIM(estado)) = 'ACTIVO'
                    """;

            case MATERNIDAD -> """
                    UPDATE maternidad
                    SET stock = ?,
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    WHERE id_maternidad = ?
                      AND id_farmacia = ?
                      AND stock = ?
                      AND UPPER(BTRIM(estado)) = 'ACTIVO'
                    """;

            case MEDICAMENTO ->
                throw new IllegalArgumentException(
                    "Los medicamentos se ajustan por lote."
                );
        };
    }
}
