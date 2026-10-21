package org.example.carestock.service;

import org.example.carestock.config.ConexionBaseDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ServicioControlVencimientos {

    public int sincronizar(int idFarmacia, int idUsuario)
            throws Exception {

        if (idFarmacia <= 0 || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "Farmacia y usuario obligatorios");
        }

        try (Connection con = ConexionBaseDatos.getConexion()) {
            boolean autoCommitAnterior = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                List<Integer> medicamentos = new ArrayList<>();

                // Bloquear los medicamentos afectados.
                String bloquear = """
                    SELECT m.id_medicamento
                    FROM medicamentos m
                    WHERE m.id_farmacia = ?
                      AND EXISTS (
                          SELECT 1 FROM lotes l
                          WHERE l.id_medicamento = m.id_medicamento
                            AND l.estado_lote = 'DISPONIBLE'
                            AND l.fecha_vencimiento <= CURRENT_DATE
                      )
                    ORDER BY m.id_medicamento
                    FOR UPDATE OF m
                    """;

                try (PreparedStatement ps =
                             con.prepareStatement(bloquear)) {

                    ps.setInt(1, idFarmacia);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            medicamentos.add(rs.getInt(1));
                        }
                    }
                }

                if (medicamentos.isEmpty()) {
                    con.commit();
                    return 0;
                }

                List<int[]> lotesCambiados = new ArrayList<>();

                // Cambiar a VENCIDO solamente los lotes
                // que todavia figuran como DISPONIBLE.
                String caducar = """
                    UPDATE lotes l
                    SET estado_lote = 'VENCIDO',
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    FROM medicamentos m
                    WHERE m.id_medicamento = l.id_medicamento
                      AND m.id_farmacia = ?
                      AND l.estado_lote = 'DISPONIBLE'
                      AND l.fecha_vencimiento <= CURRENT_DATE
                    RETURNING l.id_lote, l.cantidad_actual
                    """;

                try (PreparedStatement ps =
                             con.prepareStatement(caducar)) {

                    ps.setInt(1, idUsuario);
                    ps.setInt(2, idFarmacia);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            lotesCambiados.add(new int[]{
                                    rs.getInt(1),
                                    rs.getInt(2)
                            });
                        }
                    }
                }

                // Registrar el ajuste para trazabilidad.
                String auditoria = """
                    INSERT INTO log_movimientos
                        (id_usuario, id_lote, tipo_movimiento,
                         cantidad_afectada, detalle_cambio)
                    VALUES (?, ?, 'AJUSTE', ?,
                            'Deshabilitacion automatica por vencimiento')
                    """;

                try (PreparedStatement ps =
                             con.prepareStatement(auditoria)) {

                    for (int[] lote : lotesCambiados) {
                        if (lote[1] <= 0) continue;

                        ps.setInt(1, idUsuario);
                        ps.setInt(2, lote[0]);
                        ps.setInt(3, lote[1]);
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                // Recalcular stock apto a partir de lotes vigentes.
                // No restar manualmente para evitar doble descuento.
                String reconciliar = """
                    UPDATE medicamentos m
                    SET stock_total = COALESCE((
                          SELECT SUM(l.cantidad_actual)
                          FROM lotes l
                          WHERE l.id_medicamento = m.id_medicamento
                            AND l.estado_lote = 'DISPONIBLE'
                            AND l.fecha_vencimiento > CURRENT_DATE
                        ), 0),
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    WHERE m.id_medicamento = ?
                      AND m.id_farmacia = ?
                    """;

                try (PreparedStatement ps =
                             con.prepareStatement(reconciliar)) {

                    for (int idMedicamento : medicamentos) {
                        ps.setInt(1, idUsuario);
                        ps.setInt(2, idMedicamento);
                        ps.setInt(3, idFarmacia);
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
                return lotesCambiados.size();

            } catch (Exception ex) {
                try {
                    con.rollback();
                } catch (SQLException falloRollback) {
                    ex.addSuppressed(falloRollback);
                }
                throw ex;
            } finally {
                con.setAutoCommit(autoCommitAnterior);
            }
        }
    }
}
