package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;

import java.sql.*;
import java.time.LocalDate;

/** Dispensacion de lotes y actualizacion de stock de medicamentos en una transaccion. */
public class FarmacovigilanciaService {

    /**
     * Compatibilidad con la pantalla original. Debe migrarse a la
     * version con farmacia/usuario al conectar InventarioFacade.
     */
    @Deprecated
    public void dispensarMedicamento(int idLote, int cantidad) throws Exception {
        dispensarInterno(idLote, cantidad, null, null);
    }

    public void dispensarMedicamento(int idLote, int cantidad, int idFarmacia, int idUsuario)
            throws Exception {
        if (idFarmacia <= 0 || idUsuario <= 0)
            throw new ReglaNegocioException("Contexto de usuario/farmacia invalido");
        dispensarInterno(idLote, cantidad, idFarmacia, idUsuario);
    }

    private void dispensarInterno(int idLote, int cantidad, Integer farmacia, Integer usuario)
            throws Exception {
        if (cantidad <= 0)
            throw new ReglaNegocioException("La cantidad a dispensar debe ser positiva");

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                String consultar = "SELECT l.id_medicamento, l.numero_lote, l.cantidad_actual, " +
                        "l.fecha_vencimiento, l.estado_lote, m.id_farmacia " +
                        "FROM lotes l JOIN medicamentos m ON m.id_medicamento=l.id_medicamento " +
                        "WHERE l.id_lote=? FOR UPDATE OF l, m";
                int idMedicamento;
                int stock;
                String estado;
                String numero;
                LocalDate vencimiento;
                int farmaciaLote;
                try (PreparedStatement ps=con.prepareStatement(consultar)) {
                    ps.setInt(1,idLote);
                    try (ResultSet rs=ps.executeQuery()) {
                        if (!rs.next()) throw new ReglaNegocioException("Lote no encontrado");
                        idMedicamento=rs.getInt("id_medicamento");
                        numero=rs.getString("numero_lote");
                        stock=rs.getInt("cantidad_actual");
                        estado=rs.getString("estado_lote");
                        Date v=rs.getDate("fecha_vencimiento");
                        vencimiento=v == null ? null : v.toLocalDate();
                        farmaciaLote=rs.getInt("id_farmacia");
                    }
                }
                if (farmacia != null && farmacia != farmaciaLote)
                    throw new ReglaNegocioException("El lote pertenece a otra farmacia");
                if (!"DISPONIBLE".equalsIgnoreCase(estado))
                    throw new ReglaNegocioException("Lote no disponible: " + numero);
                if (vencimiento == null || !vencimiento.isAfter(LocalDate.now()))
                    throw new ReglaNegocioException("Lote vencido: " + numero);
                if (stock < cantidad)
                    throw new ReglaNegocioException("Cantidad insuficiente en el lote " + numero);

                int restante=stock-cantidad;
                String updLote = "UPDATE lotes SET cantidad_actual=?, estado_lote=?, " +
                        "fecha_modificacion=NOW(), id_usuario_modificacion=? WHERE id_lote=?";
                try (PreparedStatement ps=con.prepareStatement(updLote)) {
                    ps.setInt(1,restante);
                    ps.setString(2,restante==0 ? "AGOTADO" : "DISPONIBLE");
                    if (usuario==null) ps.setNull(3,Types.INTEGER);
                    else ps.setInt(3,usuario);
                    ps.setInt(4,idLote);
                    if(ps.executeUpdate()!=1) throw new SQLException("No se actualizo el lote");
                }
                String updMed = "UPDATE medicamentos SET stock_total=stock_total-?, " +
                        "fecha_modificacion=NOW() WHERE id_medicamento=? AND stock_total>=?";
                try (PreparedStatement ps=con.prepareStatement(updMed)) {
                    ps.setInt(1,cantidad);
                    ps.setInt(2,idMedicamento);
                    ps.setInt(3,cantidad);
                    if(ps.executeUpdate()!=1)
                        throw new ReglaNegocioException(
                                "Stock total inconsistente; revisar existencias antes de dispensar");
                }
                con.commit();
            } catch(Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
