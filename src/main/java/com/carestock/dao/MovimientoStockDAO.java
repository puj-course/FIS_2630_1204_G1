package com.carestock.dao;

import com.carestock.model.DespachoLote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementación JDBC del kardex de movimientos de stock.
 *
 * Esta clase no controla commit, rollback ni cierre de Connection.
 * La frontera transaccional pertenece a la capa Service.
 */
public class MovimientoStockDAO
         {

    public void registrarSalida(
            Connection connection,
            DespachoLote despacho
    ) throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "La conexión no puede ser nula."
            );
        }

        if (despacho == null) {
            throw new IllegalArgumentException(
                    "El despacho no puede ser nulo."
            );
        }

        String sql =
                "INSERT INTO movimientos_stock ("
                        + "id_lote, "
                        + "tipo_movimiento, "
                        + "cantidad, "
                        + "id_usuario"
                        + ") VALUES (?, 'SALIDA', ?, ?)";

        try (
                PreparedStatement stmt =
                        connection.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    despacho.getIdLote()
            );

            stmt.setInt(
                    2,
                    despacho.getCantidad()
            );

            stmt.setInt(
                    3,
                    despacho.getIdUsuario()
            );

            int filas =
                    stmt.executeUpdate();

            if (filas != 1) {
                throw new SQLException(
                        "No fue posible registrar el movimiento en el kardex."
                );
            }
        }
    }
}
