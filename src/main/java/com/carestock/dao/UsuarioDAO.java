package com.carestock.dao;

import com.carestock.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public int obtenerIdActivoPorEmail(String email) throws SQLException {
        String sql = "SELECT id_usuario FROM USUARIOS WHERE LOWER(email) = LOWER(?) AND estado = 'ACTIVO'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        }
        throw new SQLException("No existe un usuario ACTIVO con el correo configurado: " + email);
    }
}
