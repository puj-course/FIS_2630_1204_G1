package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario buscarPorEmail(String email) throws Exception {
        // Consulta adaptada exactamente a las columnas de tu tabla usuarios en Neon
        String sql = "SELECT id_usuario, nombre_completo, email, password_hash, id_rol, estado FROM usuarios WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNombre(rs.getString("nombre_completo"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setPassword(rs.getString("password_hash"));
                    usuario.setRol(rs.getString("id_rol"));

                    // Opcional: imprimir en consola para depurar qué trajo de Neon
                    System.out.println("[DAO] Usuario encontrado en BD: " + usuario.getEmail() + " | Estado: " + rs.getString("estado"));

                    return usuario;
                }
            }
        } catch (Exception e) {
            System.err.println("[DAO ERROR] Falló la consulta SQL: " + e.getMessage());
            throw e;
        }

        System.out.println("[DAO] No se encontró ningún registro para el correo: " + email);
        return null;
    }
}