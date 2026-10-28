package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;

import java.sql.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public final class RepositorioPreferenciasSmsJdbc
        implements ServicioPreferenciasSms.Repositorio {

    @Override
    public ServicioPreferenciasSms.Cuenta cuenta(int id)
            throws SQLException {

        String sql = """
            SELECT u.id_usuario, u.id_farmacia,
                   u.estado AS usuario_estado,
                   r.nombre_rol,
                   f.estado AS farmacia_estado
            FROM usuarios u
            JOIN roles r ON r.id_rol = u.id_rol
            LEFT JOIN farmacias f
              ON f.id_farmacia = u.id_farmacia
            WHERE u.id_usuario = ?
            """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerCuenta(rs) : null;
            }
        }
    }

    private static ServicioPreferenciasSms.Cuenta leerCuenta(
            ResultSet rs) throws SQLException {

        return new ServicioPreferenciasSms.Cuenta(
                rs.getInt("id_usuario"),
                rs.getInt("id_farmacia"),
                rs.getString("nombre_rol"),
                "ACTIVO".equalsIgnoreCase(
                        rs.getString("usuario_estado")),
                "ACTIVA".equalsIgnoreCase(
                        rs.getString("farmacia_estado"))
        );
    }

    @Override
    public ServicioPreferenciasSms.Configuracion consultar(
            int usuario, int farmacia) throws SQLException {

        String sql = """
            SELECT id_destinatario, telefono, autorizado,
                   notifica_stock_critico,
                   notifica_vencimiento_critico,
                   horario_inicio, horario_fin, zona_horaria
            FROM sms_destinatarios
            WHERE id_usuario = ?
              AND id_farmacia = ?
              AND activo = TRUE
            """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuario);
            ps.setInt(2, farmacia);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Time desde = rs.getTime("horario_inicio");
                Time hasta = rs.getTime("horario_fin");

                return new ServicioPreferenciasSms.Configuracion(
                        rs.getLong("id_destinatario"),
                        usuario,
                        farmacia,
                        new ServicioPreferenciasSms.Solicitud(
                                rs.getString("telefono"),
                                rs.getBoolean("autorizado"),
                                rs.getBoolean("notifica_stock_critico"),
                                rs.getBoolean("notifica_vencimiento_critico"),
                                desde == null
                                        ? null : desde.toLocalTime(),
                                hasta == null
                                        ? null : hasta.toLocalTime(),
                                rs.getString("zona_horaria")
                        )
                );
            }
        }
    }

    @Override
    public void guardar(
            int administradorId,
            int usuarioId,
            int farmaciaId,
            ServicioPreferenciasSms.Solicitud solicitud)
            throws SQLException {

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

            try {
                comprobarPermisosBloqueados(
                        con, administradorId,
                        usuarioId, farmaciaId
                );

                Long id = buscarId(
                        con, usuarioId, farmaciaId, true, null
                );

                if (id == null) {
                    id = buscarId(
                            con, usuarioId, farmaciaId,
                            false, solicitud.telefono()
                    );
                }

                if (id == null) {
                    insertar(con, usuarioId, farmaciaId, solicitud);
                } else {
                    actualizar(con, id, solicitud);
                }

                con.commit();

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void comprobarPermisosBloqueados(
            Connection con, int adminId,
            int usuarioId, int farmaciaId) throws SQLException {

        String sql = """
            SELECT u.id_usuario, u.id_farmacia,
                   u.estado AS usuario_estado,
                   r.nombre_rol,
                   f.estado AS farmacia_estado
            FROM usuarios u
            JOIN roles r ON r.id_rol = u.id_rol
            JOIN farmacias f
              ON f.id_farmacia = u.id_farmacia
            WHERE u.id_usuario IN (?, ?)
            ORDER BY u.id_usuario
            FOR UPDATE OF u, f
            """;

        Map<Integer, ServicioPreferenciasSms.Cuenta> cuentas =
                new HashMap<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cuentas.put(
                            rs.getInt("id_usuario"),
                            leerCuenta(rs)
                    );
                }
            }
        }

        var admin = cuentas.get(adminId);
        var destino = cuentas.get(usuarioId);

        if (admin == null || destino == null
                || !admin.usuarioActivo()
                || !admin.farmaciaActiva()
                || !destino.usuarioActivo()
                || !destino.farmaciaActiva()
                || !"ADMINISTRADOR".equalsIgnoreCase(admin.rol())
                || admin.farmaciaId() != farmaciaId
                || destino.farmaciaId() != farmaciaId) {

            throw new SecurityException(
                    "Configuracion SMS no autorizada para esta farmacia"
            );
        }
    }

    private static Long buscarId(
            Connection con, int usuario, int farmacia,
            boolean activo, String telefono) throws SQLException {

        String sql = """
            SELECT id_destinatario FROM sms_destinatarios
            WHERE id_usuario = ?
              AND id_farmacia = ?
              AND activo = ?
            """ + (telefono == null ? "" : " AND telefono = ?")
              + " FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuario);
            ps.setInt(2, farmacia);
            ps.setBoolean(3, activo);

            if (telefono != null) {
                ps.setString(4, telefono);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static void insertar(
            Connection con, int usuario, int farmacia,
            ServicioPreferenciasSms.Solicitud s) throws SQLException {

        String sql = """
            INSERT INTO sms_destinatarios (
                id_usuario, id_farmacia, telefono,
                autorizado, autorizado_en, activo,
                notifica_stock_critico,
                notifica_vencimiento_critico,
                horario_inicio, horario_fin, zona_horaria
            )
            VALUES (
                ?, ?, ?, ?,
                CASE WHEN ? THEN NOW() ELSE NULL END,
                TRUE, ?, ?, ?, ?, ?
            )
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuario);
            ps.setInt(2, farmacia);
            ps.setString(3, s.telefono());
            ps.setBoolean(4, s.autorizado());
            ps.setBoolean(5, s.autorizado());
            ps.setBoolean(6, s.notificaStockCritico());
            ps.setBoolean(7, s.notificaVencimientoCritico());

            ponerHorario(ps, 8, s.horarioInicio());
            ponerHorario(ps, 9, s.horarioFin());

            ps.setString(10, s.zonaHoraria());
            ps.executeUpdate();
        }
    }

    private static void actualizar(
            Connection con, long id,
            ServicioPreferenciasSms.Solicitud s) throws SQLException {

        String sql = """
            UPDATE sms_destinatarios
            SET telefono = ?,
                autorizado = ?,
                autorizado_en =
                    CASE WHEN ?
                        THEN COALESCE(autorizado_en, NOW())
                        ELSE autorizado_en
                    END,
                activo = TRUE,
                notifica_stock_critico = ?,
                notifica_vencimiento_critico = ?,
                horario_inicio = ?,
                horario_fin = ?,
                zona_horaria = ?
            WHERE id_destinatario = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.telefono());
            ps.setBoolean(2, s.autorizado());
            ps.setBoolean(3, s.autorizado());
            ps.setBoolean(4, s.notificaStockCritico());
            ps.setBoolean(5, s.notificaVencimientoCritico());

            ponerHorario(ps, 6, s.horarioInicio());
            ponerHorario(ps, 7, s.horarioFin());

            ps.setString(8, s.zonaHoraria());
            ps.setLong(9, id);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "Destinatario no actualizado"
                );
            }
        }
    }

    private static void ponerHorario(
            PreparedStatement ps, int columna, LocalTime hora)
            throws SQLException {

        if (hora == null) {
            ps.setNull(columna, Types.TIME);
        } else {
            ps.setTime(columna, Time.valueOf(hora));
        }
    }
}
