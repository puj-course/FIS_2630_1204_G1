package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.MovimientoInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    public List<MovimientoInventario> listarPorFarmacia(
            int idFarmacia,
            String tipoMovimientoFiltro
    ) throws SQLException {

        String sql =
                "SELECT id_log, id_farmacia, tipo_movimiento, numero_lote, "
                + "medicamento, cantidad_afectada, usuario_responsable, "
                + "fecha_hora "
                + "FROM VW_MOVIMIENTOS_FARMACIA "
                + "WHERE id_farmacia = ? "
                + (tipoMovimientoFiltro != null ? "AND tipo_movimiento = ? " : "")
                + "ORDER BY fecha_hora DESC";

        List<MovimientoInventario> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFarmacia);

            if (tipoMovimientoFiltro != null) {
                stmt.setString(2, tipoMovimientoFiltro);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Timestamp fechaHora = rs.getTimestamp("fecha_hora");

                    movimientos.add(
                            new MovimientoInventario(
                                    rs.getInt("id_log"),
                                    rs.getInt("id_farmacia"),
                                    rs.getString("tipo_movimiento"),
                                    rs.getString("numero_lote"),
                                    rs.getString("medicamento"),
                                    rs.getInt("cantidad_afectada"),
                                    rs.getString("usuario_responsable"),
                                    fechaHora != null
                                            ? fechaHora.toLocalDateTime()
                                            : null
                            )
                    );
                }
            }
        }

        return movimientos;
    }
}
