package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Medicamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Operaciones de catalogo; no modifica existencias ni movimientos de lotes. */
public class MedicamentoCatalogoDAO {

    public List<Medicamento> listarPorFarmacia(int idFarmacia) throws SQLException {
        validarId(idFarmacia, "farmacia");
        String sql = "SELECT * FROM medicamentos WHERE id_farmacia = ? "
                + "ORDER BY nombre_comercial, id_medicamento";
        List<Medicamento> resultado = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    public boolean actualizar(int idMedicamento, Medicamento producto,
                              int idFarmacia, int idUsuario) throws Exception {
        validarId(idMedicamento, "medicamento");
        validarId(idFarmacia, "farmacia");
        validarId(idUsuario, "usuario");
        validarCampos(producto);

        // El stock total, la farmacia y el estado no se cambian desde el catalogo.
        String sql = "UPDATE medicamentos SET codigo_invima = ?, nombre_comercial = ?, "
                + "principio_activo = ?, concentracion = ?, forma_farmaceutica = ?, "
                + "id_categoria = ?, stock_minimo = ?, presentacion = ?, precio = ?, "
                + "id_usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "WHERE id_medicamento = ? AND id_farmacia = ? AND estado = 'ACTIVO'";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigoInvima().trim());
            ps.setString(2, producto.getNombreComercial().trim());
            ps.setString(3, producto.getPrincipioActivo().trim());
            ps.setString(4, producto.getConcentracion());
            ps.setString(5, producto.getFormaFarmaceutica());
            ps.setInt(6, producto.getIdCategoria());
            ps.setInt(7, producto.getStockMinimo());
            ps.setString(8, producto.getPresentacion());
            if (producto.getPrecio() == null) ps.setNull(9, java.sql.Types.NUMERIC);
            else ps.setBigDecimal(9, producto.getPrecio());
            ps.setInt(10, idUsuario);
            ps.setInt(11, idMedicamento);
            ps.setInt(12, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean desactivar(int idMedicamento, int idFarmacia, int idUsuario)
            throws SQLException {
        validarId(idMedicamento, "medicamento");
        validarId(idFarmacia, "farmacia");
        validarId(idUsuario, "usuario");
        String sql = "UPDATE medicamentos m SET estado = 'INACTIVO', "
                + "id_usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "WHERE m.id_medicamento = ? AND m.id_farmacia = ? "
                + "AND m.estado = 'ACTIVO' AND m.stock_total = 0 "
                + "AND NOT EXISTS (SELECT 1 FROM lotes l "
                + "WHERE l.id_medicamento = m.id_medicamento AND l.cantidad_actual > 0)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idMedicamento);
            ps.setInt(3, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    /** Valida los campos que son obligatorios para registrar/editar el catalogo. */
    public static void validarCampos(Medicamento m) throws Exception {
        if (m == null) throw new IllegalArgumentException("Medicamento requerido");
        if (m.getCodigoInvima() == null || m.getCodigoInvima().isBlank())
            throw new IllegalArgumentException("Registro INVIMA obligatorio");
        if (m.getNombreComercial() == null || m.getNombreComercial().isBlank())
            throw new IllegalArgumentException("Nombre comercial obligatorio");
        if (m.getPrincipioActivo() == null || m.getPrincipioActivo().isBlank())
            throw new IllegalArgumentException("Principio activo obligatorio");
        if (m.getIdCategoria() == null || m.getIdCategoria() <= 0)
            throw new IllegalArgumentException("Seleccione una categoria del medicamento");
        if (m.getStockMinimo() < 0)
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        m.validar();
    }

    private static Medicamento mapear(ResultSet rs) throws SQLException {
        Medicamento m = new Medicamento();
        m.setIdMedicamento(rs.getInt("id_medicamento"));
        m.setCodigoInvima(rs.getString("codigo_invima"));
        m.setNombreComercial(rs.getString("nombre_comercial"));
        m.setPrincipioActivo(rs.getString("principio_activo"));
        m.setConcentracion(rs.getString("concentracion"));
        m.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
        m.setIdCategoria(rs.getInt("id_categoria"));
        m.setStockMinimo(rs.getInt("stock_minimo"));
        m.setStockTotal(rs.getInt("stock_total"));
        m.setEstado(rs.getString("estado"));
        int idFarmacia = rs.getInt("id_farmacia");
        m.setIdFarmacia(rs.wasNull() ? null : idFarmacia);
        m.setPresentacion(rs.getString("presentacion"));
        m.setPrecio(rs.getBigDecimal("precio"));
        int idUsuario = rs.getInt("id_usuario_creacion");
        m.setIdUsuarioCreacion(rs.wasNull() ? null : idUsuario);
        idUsuario = rs.getInt("id_usuario_modificacion");
        m.setIdUsuarioModificacion(rs.wasNull() ? null : idUsuario);
        Timestamp ts = rs.getTimestamp("fecha_modificacion");
        if (ts != null) m.setFechaModificacion(ts.toLocalDateTime());
        return m;
    }

    private static void validarId(int id, String campo) {
        if (id <= 0) throw new IllegalArgumentException("ID de " + campo + " invalido");
    }
}
