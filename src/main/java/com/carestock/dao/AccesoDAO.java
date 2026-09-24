package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.AccesoLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class AccesoDAO {

    public void registrarAcceso(int idUsuario, String resultado) {

        String sql = "SELECT fn_registrar_acceso(?, ?)";

        try (
                Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, resultado);

            stmt.execute();

        } catch (SQLException e) {

            System.err.println(
                    "No fue posible registrar el acceso en la auditoría: "
                    + e.getMessage()
            );
        }
    }


    public List<AccesoLog> listarHistorial(
            Integer idUsuarioFiltro
    ) throws SQLException {

        String sql =
                "SELECT id_acceso, id_usuario, nombre_completo, email, "
                + "resultado, fecha_hora "
                + "FROM vw_historial_accesos "
                + (idUsuarioFiltro != null ? "WHERE id_usuario = ? " : "")
                + "ORDER BY fecha_hora DESC";

        List<AccesoLog> resultado = new ArrayList<>();

        try (
                Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            if (idUsuarioFiltro != null) {
                stmt.setInt(1, idUsuarioFiltro);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Timestamp fechaHora =
                            rs.getTimestamp("fecha_hora");

                    resultado.add(
                            new AccesoLog(
                                    rs.getInt("id_acceso"),
                                    rs.getInt("id_usuario"),
                                    rs.getString("nombre_completo"),
                                    rs.getString("email"),
                                    rs.getString("resultado"),
                                    fechaHora != null
                                            ? fechaHora.toLocalDateTime()
                                            : null
                            )
                    );
                }
            }
        }

        return resultado;
    }
}
