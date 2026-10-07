package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Medicamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAOImpl implements MedicamentoDAO {

    @Override
    public void guardar(Medicamento medicamento) throws Exception {
        medicamento.validar();

        String sql = "INSERT INTO medicamentos (codigo_invima, nombre_comercial, principio_activo, " +
                "concentracion, forma_farmaceutica, id_categoria, stock_minimo, stock_total, " +
                "estado, id_farmacia, presentacion, id_usuario_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, medicamento.getCodigoInvima());
            ps.setString(2, medicamento.getNombreComercial());
            ps.setString(3, medicamento.getPrincipioActivo());
            ps.setString(4, medicamento.getConcentracion());
            ps.setString(5, medicamento.getFormaFarmaceutica());

            if (medicamento.getIdCategoria() != null) ps.setInt(6, medicamento.getIdCategoria());
            else ps.setNull(6, Types.INTEGER);

            ps.setInt(7, medicamento.getStockMinimo());
            ps.setInt(8, medicamento.getStockTotal());
            ps.setString(9, medicamento.getEstado() != null ? medicamento.getEstado() : "ACTIVO");

            if (medicamento.getIdFarmacia() != null) ps.setInt(10, medicamento.getIdFarmacia());
            else ps.setNull(10, Types.INTEGER);

            ps.setString(11, medicamento.getPresentacion());

            if (medicamento.getIdUsuarioCreacion() != null) ps.setInt(12, medicamento.getIdUsuarioCreacion());
            else ps.setNull(12, Types.INTEGER);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    medicamento.setIdMedicamento(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Medicamento buscarPorId(int idMedicamento) throws Exception {
        String sql = "SELECT * FROM medicamentos WHERE id_medicamento = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMedicamento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Medicamento buscarPorCodigoInvima(String codigoInvima) throws Exception {
        String sql = "SELECT * FROM medicamentos WHERE codigo_invima = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigoInvima);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Medicamento> listarTodos() throws Exception {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM medicamentos ORDER BY nombre_comercial ASC";

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
    public void actualizarEstado(int idMedicamento, String nuevoEstado) throws Exception {
        String sql = "UPDATE medicamentos SET estado = ?, fecha_modificacion = NOW() WHERE id_medicamento = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMedicamento);
            ps.executeUpdate();
        }
    }

    private Medicamento mapearResultSet(ResultSet rs) throws SQLException {
        Medicamento m = new Medicamento();
        m.setIdMedicamento(rs.getInt("id_medicamento"));
        m.setCodigoInvima(rs.getString("codigo_invima"));
        m.setNombreComercial(rs.getString("nombre_comercial"));
        m.setPrincipioActivo(rs.getString("principio_activo"));
        m.setConcentracion(rs.getString("concentracion"));
        m.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));

        int idCat = rs.getInt("id_categoria");
        m.setIdCategoria(rs.wasNull() ? null : idCat);

        m.setStockMinimo(rs.getInt("stock_minimo"));
        m.setStockTotal(rs.getInt("stock_total"));
        m.setEstado(rs.getString("estado"));

        int idFarm = rs.getInt("id_farmacia");
        m.setIdFarmacia(rs.wasNull() ? null : idFarm);

        m.setPresentacion(rs.getString("presentacion"));

        int idUserC = rs.getInt("id_usuario_creacion");
        m.setIdUsuarioCreacion(rs.wasNull() ? null : idUserC);

        int idUserM = rs.getInt("id_usuario_modificacion");
        m.setIdUsuarioModificacion(rs.wasNull() ? null : idUserM);

        Timestamp ts = rs.getTimestamp("fecha_modificacion");
        if (ts != null) {
            m.setFechaModificacion(ts.toLocalDateTime());
        }

        return m;
    }
}