package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ServicioDirectorioSms {

    public record Destinatario(
            int idUsuario,
            String nombre,
            String email,
            String telefono,
            boolean autorizado,
            boolean configurado,
            boolean stock,
            boolean vencimientos
    ) {}

    private static SesionUsuario.DatosUsuario sesion() {
        var s = SesionUsuario.actual();

        if (!s.esAdministrador() || s.idFarmacia() == null
                || s.idFarmacia() <= 0) {
            throw new SecurityException(
                    "Solo un administrador puede configurar SMS.");
        }
        return s;
    }

    public List<Destinatario> listar() throws SQLException {
        var s = sesion();

        String sql = """
            SELECT u.id_usuario, u.nombre_completo, u.email,
                   d.id_destinatario, d.telefono,
                   d.autorizado, d.autorizado_en,
                   d.notifica_stock_critico,
                   d.notifica_vencimiento_critico
            FROM usuarios a
            JOIN roles r ON r.id_rol = a.id_rol
            JOIN farmacias f ON f.id_farmacia = a.id_farmacia
            JOIN usuarios u
              ON u.id_farmacia = a.id_farmacia
             AND u.estado = 'ACTIVO'
            LEFT JOIN sms_destinatarios d
              ON d.id_usuario = u.id_usuario
             AND d.id_farmacia = u.id_farmacia
             AND d.activo = TRUE
            WHERE a.id_usuario = ?
              AND a.id_farmacia = ?
              AND a.estado = 'ACTIVO'
              AND f.estado = 'ACTIVA'
              AND UPPER(r.nombre_rol) = 'ADMINISTRADOR'
            ORDER BY u.nombre_completo, u.id_usuario
            """;

        List<Destinatario> resultado = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.id());
            ps.setInt(2, s.idFarmacia());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean configurado =
                            rs.getObject("id_destinatario") != null;

                    resultado.add(new Destinatario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_completo"),
                            rs.getString("email"),
                            rs.getString("telefono"),
                            configurado
                                    && rs.getBoolean("autorizado")
                                    && rs.getTimestamp("autorizado_en") != null,
                            configurado,
                            rs.getBoolean("notifica_stock_critico"),
                            rs.getBoolean("notifica_vencimiento_critico")
                    ));
                }
            }
        }

        return resultado;
    }

    public void deshabilitar(int usuarioId) throws SQLException {
        var s = sesion();

        String sql = """
            UPDATE sms_destinatarios d
            SET activo = FALSE,
                autorizado = FALSE,
                notifica_stock_critico = FALSE,
                notifica_vencimiento_critico = FALSE
            WHERE d.id_usuario = ?
              AND d.id_farmacia = ?
              AND d.activo = TRUE
              AND EXISTS (
                  SELECT 1
                  FROM usuarios a
                  JOIN roles r ON r.id_rol = a.id_rol
                  JOIN farmacias f
                    ON f.id_farmacia = a.id_farmacia
                  JOIN usuarios u
                    ON u.id_usuario = d.id_usuario
                   AND u.id_farmacia = d.id_farmacia
                  WHERE a.id_usuario = ?
                    AND a.id_farmacia = d.id_farmacia
                    AND a.estado = 'ACTIVO'
                    AND u.estado = 'ACTIVO'
                    AND f.estado = 'ACTIVA'
                    AND UPPER(r.nombre_rol) = 'ADMINISTRADOR'
              )
            """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, s.idFarmacia());
            ps.setInt(3, s.id());

            if (ps.executeUpdate() != 1) {
                throw new SecurityException(
                        "Configuracion inexistente o no autorizada.");
            }
        }
    }
}
