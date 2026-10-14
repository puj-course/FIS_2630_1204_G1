package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

/** Dispensacion atomica, con control de usuario, farmacia y rol. */
public class FarmacovigilanciaService {

    /**
     * Firma antigua, conservada solamente para detectar llamadas inseguras.
     * No se debe permitir dispensar sin contexto de usuario/farmacia.
     */
    @Deprecated
    public void dispensarMedicamento(int idLote, int cantidad)
            throws ReglaNegocioException {
        throw new ReglaNegocioException(
                "Dispensacion no autorizada: utilice la fachada Inventario");
    }

    public void dispensarMedicamento(int idLote, int cantidad,
                                     int idFarmacia, int idUsuario) throws Exception {
        if (idLote <= 0 || cantidad <= 0 || idFarmacia <= 0 || idUsuario <= 0) {
            throw new ReglaNegocioException(
                    "Lote, cantidad, usuario y farmacia deben ser validos");
        }

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                validarOperador(con, idUsuario, idFarmacia);

                String sql = """
                        SELECT l.id_medicamento, l.numero_lote, l.cantidad_actual,
                               l.fecha_vencimiento, l.estado_lote,
                               m.id_farmacia, m.estado AS estado_medicamento
                        FROM lotes l
                        INNER JOIN medicamentos m
                            ON m.id_medicamento = l.id_medicamento
                        WHERE l.id_lote = ?
                        FOR UPDATE OF l, m
                        """;

                int idMedicamento;
                int disponible;
                String numeroLote;
                String estadoLote;
                String estadoMedicamento;
                int farmaciaLote;
                LocalDate vencimiento;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idLote);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("Lote no encontrado");
                        }
                        idMedicamento = rs.getInt("id_medicamento");
                        disponible = rs.getInt("cantidad_actual");
                        numeroLote = rs.getString("numero_lote");
                        estadoLote = rs.getString("estado_lote");
                        estadoMedicamento = rs.getString("estado_medicamento");
                        farmaciaLote = rs.getInt("id_farmacia");
                        Date fecha = rs.getDate("fecha_vencimiento");
                        vencimiento = fecha == null ? null : fecha.toLocalDate();
                    }
                }

                if (farmaciaLote != idFarmacia) {
                    throw new ReglaNegocioException(
                            "No puede dispensar lotes de otra farmacia");
                }
                if (!"ACTIVO".equalsIgnoreCase(estadoMedicamento == null
                        ? "" : estadoMedicamento.trim())) {
                    throw new ReglaNegocioException("Medicamento inactivo");
                }
                if (!"DISPONIBLE".equalsIgnoreCase(estadoLote == null
                        ? "" : estadoLote.trim())) {
                    throw new ReglaNegocioException("Lote no disponible: " + numeroLote);
                }
                if (vencimiento == null || !vencimiento.isAfter(LocalDate.now())) {
                    throw new ReglaNegocioException("Lote vencido: " + numeroLote);
                }
                if (disponible < cantidad) {
                    throw new ReglaNegocioException(
                            "Stock insuficiente en el lote " + numeroLote);
                }

                int restante = disponible - cantidad;
                String modificarLote = """
                        UPDATE lotes
                        SET cantidad_actual = ?, estado_lote = ?,
                            id_usuario_modificacion = ?, fecha_modificacion = NOW()
                        WHERE id_lote = ?
                        """;
                try (PreparedStatement ps = con.prepareStatement(modificarLote)) {
                    ps.setInt(1, restante);
                    ps.setString(2, restante == 0 ? "AGOTADO" : "DISPONIBLE");
                    ps.setInt(3, idUsuario);
                    ps.setInt(4, idLote);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se actualizo el lote");
                    }
                }

                String modificarMedicamento = """
                        UPDATE medicamentos
                        SET stock_total = stock_total - ?,
                            id_usuario_modificacion = ?, fecha_modificacion = NOW()
                        WHERE id_medicamento = ? AND id_farmacia = ?
                          AND stock_total >= ?
                        """;
                try (PreparedStatement ps = con.prepareStatement(modificarMedicamento)) {
                    ps.setInt(1, cantidad);
                    ps.setInt(2, idUsuario);
                    ps.setInt(3, idMedicamento);
                    ps.setInt(4, idFarmacia);
                    ps.setInt(5, cantidad);
                    if (ps.executeUpdate() != 1) {
                        throw new ReglaNegocioException(
                                "Stock inconsistente: revisar existencias antes de dispensar");
                    }
                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void validarOperador(Connection con, int idUsuario, int idFarmacia)
            throws Exception {
        String permitidos = System.getenv("CARESTOCK_ROLES_DISPENSACION");
        if (permitidos == null || permitidos.isBlank()) {
            throw new ReglaNegocioException(
                    "Dispensacion bloqueada: configure CARESTOCK_ROLES_DISPENSACION");
        }

        String sql = """
                SELECT u.id_rol
                FROM usuarios u
                INNER JOIN farmacias f ON f.id_farmacia = u.id_farmacia
                WHERE u.id_usuario = ? AND u.id_farmacia = ?
                  AND UPPER(TRIM(u.estado)) = 'ACTIVO'
                  AND UPPER(TRIM(f.estado)) = 'ACTIVA'
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Usuario inactivo o farmacia no autorizada");
                }
                String rol = Integer.toString(rs.getInt("id_rol"));
                boolean autorizado = Arrays.stream(permitidos.split(","))
                        .map(String::trim)
                        .anyMatch(rol::equals);
                if (!autorizado) {
                    throw new ReglaNegocioException(
                            "Su rol no permite dispensar medicamentos");
                }
            }
        }
    }
}
