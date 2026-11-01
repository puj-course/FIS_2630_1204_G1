package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;

import java.sql.*;

/** Registra lote y actualiza stock_total en una sola transaccion JDBC. */
public class RegistroLoteService {

    public Lote registrarMedicamentoLote(Lote lote, int idUsuario, int idFarmacia) throws Exception {
        if (lote==null) throw new IllegalArgumentException("Lote obligatorio");
        if (lote.getCantidadActual() <= 0)
            throw new ReglaNegocioException("El lote debe ingresar con cantidad positiva");
        if (lote.getIdUbicacion()==null || lote.getIdUbicacion()<=0)
            throw new ReglaNegocioException("La ubicacion del lote es obligatoria");
        lote.setIdUsuario(idUsuario);
        if (lote.getEstadoLote()==null || lote.getEstadoLote().isBlank())
            lote.setEstadoLote("DISPONIBLE");
        lote.validar();

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                String verificar = "SELECT id_medicamento FROM medicamentos " +
                        "WHERE id_medicamento=? AND id_farmacia=? AND estado='ACTIVO' FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(verificar)) {
                    ps.setInt(1,lote.getIdMedicamento());
                    ps.setInt(2,idFarmacia);
                    try(ResultSet rs=ps.executeQuery()) {
                        if (!rs.next()) throw new ReglaNegocioException(
                                "Medicamento no existe, inactivo o pertenece a otra farmacia");
                    }
                }
                String insertar = "INSERT INTO lotes " +
                        "(numero_lote,id_medicamento,cantidad_actual,fecha_vencimiento," +
                        "id_ubicacion,estado_lote,id_usuario) VALUES (?,?,?,?,?,?,?) RETURNING id_lote";
                try (PreparedStatement ps = con.prepareStatement(insertar)) {
                    ps.setString(1,lote.getNumeroLote());
                    ps.setInt(2,lote.getIdMedicamento());
                    ps.setInt(3,lote.getCantidadActual());
                    ps.setDate(4,Date.valueOf(lote.getFechaVencimiento()));
                    ps.setInt(5,lote.getIdUbicacion());
                    ps.setString(6,lote.getEstadoLote());
                    ps.setInt(7,idUsuario);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new SQLException("No se pudo obtener id_lote");
                        lote.setIdLote(rs.getInt(1));
                    }
                }
                String recalcular = "UPDATE medicamentos SET stock_total = COALESCE((" +
                        "SELECT SUM(l.cantidad_actual) FROM lotes l " +
                        "WHERE l.id_medicamento = medicamentos.id_medicamento " +
                        "AND l.estado_lote = 'DISPONIBLE' " +
                        "AND l.fecha_vencimiento > CURRENT_DATE), 0), " +
                        "fecha_modificacion=NOW(), id_usuario_modificacion=? " +
                        "WHERE id_medicamento=? AND id_farmacia=?";
                try (PreparedStatement ps = con.prepareStatement(recalcular)) {
                    ps.setInt(1,idUsuario);
                    ps.setInt(2,lote.getIdMedicamento());
                    ps.setInt(3,idFarmacia);
                    if(ps.executeUpdate()!=1) throw new SQLException("No se pudo actualizar stock_total");
                }
                String registrarEntrada = "INSERT INTO log_movimientos " +
                        "(id_usuario,id_lote,tipo_movimiento,cantidad_afectada,detalle_cambio) " +
                        "VALUES (?,?,'ENTRADA',?,?)";
                try (PreparedStatement ps = con.prepareStatement(registrarEntrada)) {
                    ps.setInt(1,idUsuario);
                    ps.setInt(2,lote.getIdLote());
                    ps.setInt(3,lote.getCantidadActual());
                    ps.setString(4,"Entrada del lote " + lote.getNumeroLote());
                    ps.executeUpdate();
                }
                con.commit();
                return lote;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
