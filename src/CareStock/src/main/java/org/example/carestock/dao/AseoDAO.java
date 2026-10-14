package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.AseoBuilder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO concreto: no exige IDs ni datos de sesión en el modelo Aseo. */
public class AseoDAO {

    public int guardar(Aseo producto, int idFarmacia, int idUsuario) throws Exception {
        if (producto == null) {
            throw new IllegalArgumentException("El producto de aseo es obligatorio");
        }
        producto.validar();
        validarContexto(idFarmacia, idUsuario);

        String sql = "INSERT INTO aseo (codigo, nombre, descripcion, precio, stock, "
                + "tipo_aseo, biodegradable, componentes_activos, id_farmacia, "
                + "id_usuario_creacion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO') RETURNING id_aseo";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim());
            ps.setString(2, producto.getNombre().trim());
            ps.setString(3, producto.getDescripcion().trim());
            ps.setBigDecimal(4, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(5, producto.getStock());
            ps.setString(6, producto.getTipoAseo());
            ps.setBoolean(7, producto.isBiodegradable());
            ps.setString(8, producto.getComponentesActivos());
            ps.setInt(9, idFarmacia);
            ps.setInt(10, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se generó ID para el producto de aseo");
                }
                return rs.getInt("id_aseo");
            }
        }
    }

    public RegistroCatalogo<Aseo> buscarPorId(int idAseo, int idFarmacia)
            throws SQLException {
        validarId(idAseo);
        validarFarmacia(idFarmacia);
        String sql = "SELECT * FROM aseo WHERE id_aseo = ? AND id_farmacia = ?";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAseo);
            ps.setInt(2, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<RegistroCatalogo<Aseo>> listarPorFarmacia(int idFarmacia)
            throws SQLException {
        validarFarmacia(idFarmacia);
        List<RegistroCatalogo<Aseo>> productos = new ArrayList<>();
        String sql = "SELECT * FROM aseo WHERE id_farmacia = ? ORDER BY nombre";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
        }
        return productos;
    }

    public boolean actualizar(int idAseo, Aseo producto, int idFarmacia,
                              int idUsuario) throws Exception {
        validarId(idAseo);
        validarContexto(idFarmacia, idUsuario);
        if (producto == null) {
            throw new IllegalArgumentException("El producto de aseo es obligatorio");
        }
        producto.validar();

        String sql = "UPDATE aseo SET codigo = ?, nombre = ?, descripcion = ?, "
                + "precio = ?, stock = ?, tipo_aseo = ?, biodegradable = ?, "
                + "componentes_activos = ?, id_usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "WHERE id_aseo = ? AND id_farmacia = ? AND estado = 'ACTIVO'";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim());
            ps.setString(2, producto.getNombre().trim());
            ps.setString(3, producto.getDescripcion().trim());
            ps.setBigDecimal(4, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(5, producto.getStock());
            ps.setString(6, producto.getTipoAseo());
            ps.setBoolean(7, producto.isBiodegradable());
            ps.setString(8, producto.getComponentesActivos());
            ps.setInt(9, idUsuario);
            ps.setInt(10, idAseo);
            ps.setInt(11, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean desactivar(int idAseo, int idFarmacia,
                              int idUsuario) throws SQLException {
        validarId(idAseo);
        validarContexto(idFarmacia, idUsuario);
        String sql = "UPDATE aseo SET estado = 'INACTIVO', "
                + "id_usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "WHERE id_aseo = ? AND id_farmacia = ? AND estado = 'ACTIVO'";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idAseo);
            ps.setInt(3, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    private static RegistroCatalogo<Aseo> mapear(ResultSet rs) throws SQLException {
        try {
            // El DAO utiliza el Builder existente en lugar de mutar el modelo.
            Aseo producto = new AseoBuilder()
                    .setCodigo(rs.getString("codigo"))
                    .setNombre(rs.getString("nombre"))
                    .setDescripcion(rs.getString("descripcion"))
                    .setPrecio(rs.getBigDecimal("precio").doubleValue())
                    .setStock(rs.getInt("stock"))
                    .tipoAseo(rs.getString("tipo_aseo"))
                    .biodegradable(rs.getBoolean("biodegradable"))
                    .componentesActivos(rs.getString("componentes_activos"))
                    .build();
            return new RegistroCatalogo<>(
                    rs.getInt("id_aseo"), producto, rs.getString("estado"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new SQLException("Registro de aseo inválido en la base de datos", e);
        }
    }

    private static void validarId(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID de aseo inválido");
    }

    private static void validarFarmacia(int idFarmacia) {
        if (idFarmacia <= 0) throw new IllegalArgumentException("Farmacia inválida");
    }

    private static void validarContexto(int idFarmacia, int idUsuario) {
        validarFarmacia(idFarmacia);
        if (idUsuario <= 0) throw new IllegalArgumentException("Usuario inválido");
    }
}
