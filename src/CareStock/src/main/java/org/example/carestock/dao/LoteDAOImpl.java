package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Lote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoteDAOImpl implements LoteDAO {

    @Override
    public void guardar(Lote lote) throws Exception {
        lote.validar();

        String sql = "INSERT INTO lotes (numero_lote, id_medicamento, cantidad_actual, fecha_vencimiento, " +
                "id_ubicacion, estado_lote, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, lote.getNumeroLote());
            ps.setInt(2, lote.getIdMedicamento());
            ps.setInt(3, lote.getCantidadActual());
            ps.setDate(4, Date.valueOf(lote.getFechaVencimiento()));

            if (lote.getIdUbicacion() != null) ps.setInt(5, lote.getIdUbicacion());
            else ps.setNull(5, Types.INTEGER);

            ps.setString(6, lote.getEstadoLote() != null ? lote.getEstadoLote() : "DISPONIBLE");

            if (lote.getIdUsuario() != null) ps.setInt(7, lote.getIdUsuario());
            else ps.setNull(7, Types.INTEGER);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    lote.setIdLote(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Lote buscarPorId(int idLote) throws Exception {
        String sql = "SELECT * FROM lotes WHERE id_lote = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Lote> listarPorMedicamento(int idMedicamento) throws Exception {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT * FROM lotes WHERE id_medicamento = ? ORDER BY fecha_vencimiento ASC";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMedicamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public List<Lote> listarTodos() throws Exception {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT * FROM lotes ORDER BY fecha_vencimiento ASC";

        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    @Override
    public void actualizarCantidad(int idLote, int nuevaCantidad) throws Exception {
        String sql = "UPDATE lotes SET cantidad_actual = ?, fecha_modificacion = NOW() WHERE id_lote = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, nuevaCantidad);
            ps.setInt(2, idLote);
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizarEstado(int idLote, String nuevoEstado) throws Exception {
        String sql = "UPDATE lotes SET estado_lote = ?, fecha_modificacion = NOW() WHERE id_lote = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idLote);
            ps.executeUpdate();
        }
    }

    private Lote mapearResultSet(ResultSet rs) throws SQLException {
        Lote l = new Lote();
        l.setIdLote(rs.getInt("id_lote"));
        l.setNumeroLote(rs.getString("numero_lote"));
        l.setIdMedicamento(rs.getInt("id_medicamento"));
        l.setCantidadActual(rs.getInt("cantidad_actual"));

        Date dateVenc = rs.getDate("fecha_vencimiento");
        if (dateVenc != null) l.setFechaVencimiento(dateVenc.toLocalDate());

        int idUbic = rs.getInt("id_ubicacion");
        l.setIdUbicacion(rs.wasNull() ? null : idUbic);

        l.setEstadoLote(rs.getString("estado_lote"));

        Timestamp tsIngreso = rs.getTimestamp("fecha_ingreso");
        if (tsIngreso != null) l.setFechaIngreso(tsIngreso.toLocalDateTime());

        int idUser = rs.getInt("id_usuario");
        l.setIdUsuario(rs.wasNull() ? null : idUser);

        int idUserM = rs.getInt("id_usuario_modificacion");
        l.setIdUsuarioModificacion(rs.wasNull() ? null : idUserM);

        Timestamp tsModif = rs.getTimestamp("fecha_modificacion");
        if (tsModif != null) l.setFechaModificacion(tsModif.toLocalDateTime());

        return l;
    }
}