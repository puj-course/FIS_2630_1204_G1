package org.example.carestock.dao;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Validable;
import org.example.carestock.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class CatalogoDAO<T extends Validable> {
    protected abstract String tabla();
    protected abstract String columnaId();
    protected abstract String nombreProducto();
    protected abstract List<String> columnasEspecificas();
    protected abstract void asignarCampos(PreparedStatement ps, T producto) throws SQLException;
    protected abstract RegistroCatalogo<T> construir(ResultSet rs) throws SQLException;

    public final int guardar(T producto, int idFarmacia, int idUsuario) throws SQLException, ReglaNegocioException {
        validarProducto(producto);
        producto.validar();
        validarContexto(idFarmacia, idUsuario);

        int cantidad = columnasEspecificas().size();
        String sql = ConsultasSqlCatalogo.guardar(tabla());

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarCampos(ps, producto);
            ps.setInt(6 + cantidad, idFarmacia);
            ps.setInt(7 + cantidad, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se generó ID para el producto de " + nombreProducto());
                }
                return rs.getInt(columnaId());
            }
        }
    }

    public final RegistroCatalogo<T> buscarPorId(int id, int idFarmacia) throws SQLException {
        validarId(id);
        validarFarmacia(idFarmacia);
        String sql = ConsultasSqlCatalogo.buscarPorId(tabla());
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public final List<RegistroCatalogo<T>> listarPorFarmacia(int idFarmacia) throws SQLException {
        validarFarmacia(idFarmacia);
        List<RegistroCatalogo<T>> productos = new ArrayList<>();
        String sql = ConsultasSqlCatalogo.listarPorFarmacia(tabla());
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

    public final boolean actualizar(int id, T producto, int idFarmacia, int idUsuario) throws SQLException, ReglaNegocioException {
        validarId(id);
        validarContexto(idFarmacia, idUsuario);
        validarProducto(producto);
        producto.validar();

        int cantidad = columnasEspecificas().size();
        String sql = ConsultasSqlCatalogo.actualizar(tabla());

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarCampos(ps, producto);
            ps.setInt(6 + cantidad, idUsuario);
            ps.setInt(7 + cantidad, id);
            ps.setInt(8 + cantidad, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    public final boolean desactivar(int id, int idFarmacia, int idUsuario) throws SQLException {
        validarId(id);
        validarContexto(idFarmacia, idUsuario);
        String sql = ConsultasSqlCatalogo.desactivar(tabla());
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, id);
            ps.setInt(3, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    protected final void asignarCamposComunes(PreparedStatement ps, String codigo, String nombre,
                                              String descripcion, double precio, int stock)
            throws SQLException {
        ps.setString(1, codigo.trim());
        ps.setString(2, nombre.trim());
        ps.setString(3, descripcion.trim());
        ps.setBigDecimal(4, BigDecimal.valueOf(precio));
        ps.setInt(5, stock);
    }

    private RegistroCatalogo<T> mapear(ResultSet rs) throws SQLException {
        try {
            return construir(rs);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new SQLException("Registro de " + nombreProducto() + " inválido en la base de datos", e);
        }
    }

    private void validarProducto(T producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto de " + nombreProducto() + " es obligatorio");
        }
    }

    private void validarId(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID de " + nombreProducto() + " inválido");
    }

    private static void validarFarmacia(int idFarmacia) {
        if (idFarmacia <= 0) throw new IllegalArgumentException("Farmacia inválida");
    }

    private static void validarContexto(int idFarmacia, int idUsuario) {
        validarFarmacia(idFarmacia);
        if (idUsuario <= 0) throw new IllegalArgumentException("Usuario inválido");
    }
}