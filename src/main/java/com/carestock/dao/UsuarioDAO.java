package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Obtiene el ID de un usuario activo por su correo.
     * Este método se conserva porque ya es utilizado
     * por otros componentes del proyecto.
     */
    public int obtenerIdActivoPorEmail(String email) throws SQLException {

        String sql =
                "SELECT id_usuario " +
                "FROM USUARIOS " +
                "WHERE LOWER(email) = LOWER(?) " +
                "AND estado = 'ACTIVO'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        }

        throw new SQLException(
                "No existe un usuario ACTIVO con el correo configurado: " + email
        );
    }

    /**
     * Busca un usuario por correo electrónico.
     * Se utiliza durante el proceso de autenticación.
     */
    public Usuario buscarPorEmail(String email) throws SQLException {

        String sql =
                "SELECT " +
                "u.id_usuario, " +
                "u.nombre_completo, " +
                "u.email, " +
                "u.password_hash, " +
                "u.id_rol, " +
                "r.nombre_rol, " +
                "u.estado " +
                "FROM USUARIOS u " +
                "INNER JOIN ROLES r ON r.id_rol = u.id_rol " +
                "WHERE LOWER(u.email) = LOWER(?) " +
                "LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_completo"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getInt("id_rol"),
                            rs.getString("nombre_rol"),
                            rs.getString("estado")
                    );
                }
            }
        }

        return null;
    }
}
