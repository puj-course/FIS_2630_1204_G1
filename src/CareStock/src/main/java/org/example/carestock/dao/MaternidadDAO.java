package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.MaternidadBuilder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** DAO concreto: persistencia de Maternidad separada de su Builder. */
public class MaternidadDAO {

    public int guardar(Maternidad producto, int idFarmacia,
                       int idUsuario) throws Exception {
        if (producto == null) {
            throw new IllegalArgumentException("El producto de maternidad es obligatorio");
        }
        producto.validar();
        validarContexto(idFarmacia, idUsuario);

        String sql = "INSERT INTO maternidad (codigo, nombre, descripcion, precio, "
                + "stock, etapa_recomendada, hipoalergenico, "
                + "edad_gestacional_sugerida, id_farmacia, id_usuario_creacion, "
                + "estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO') "
                + "RETURNING id_maternidad";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim());
            ps.setString(2, producto.getNombre().trim());
            ps.setString(3, producto.getDescripcion().trim());
            ps.setBigDecimal(4, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(5, producto.getStock());
            ps.setString(6, producto.getEtapaRecomendada());
            ps.setBoolean(7, producto.isHipoalergenico());
            establecerEdad(ps, 8, producto.getEdadGestacionalSugerida());
            ps.setInt(9, idFarmacia);
            ps.setInt(10, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se generó ID para el producto de maternidad");
                }
                return rs.getInt("id_maternidad");
            }
        }
    }

    public RegistroCatalogo<Maternidad> buscarPorId(int idMaternidad,
                                                    int idFarmacia)
            throws SQLException {
        validarId(idMaternidad);
        validarFarmacia(idFarmacia);
        String sql = "SELECT * FROM maternidad WHERE id_maternidad = ? AND id_farmacia = ?";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMaternidad);
            ps.setInt(2, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<RegistroCatalogo<Maternidad>> listarPorFarmacia(int idFarmacia)
            throws SQLException {
        validarFarmacia(idFarmacia);
        List<RegistroCatalogo<Maternidad>> productos = new ArrayList<>();
        String sql = "SELECT * FROM maternidad WHERE id_farmacia = ? ORDER BY nombre";
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

    public boolean actualizar(int idMaternidad, Maternidad producto,
                              int idFarmacia, int idUsuario) throws Exception {
        validarId(idMaternidad);
        validarContexto(idFarmacia, idUsuario);
        if (producto == null) {
            throw new IllegalArgumentException("El producto de maternidad es obligatorio");
        }
        producto.validar();
        String sql = "UPDATE maternidad SET codigo = ?, nombre = ?, descripcion = ?, "
                + "precio = ?, stock = ?, etapa_recomendada = ?, hipoalergenico = ?, "
                + "edad_gestacional_sugerida = ?, id_usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "WHERE id_maternidad = ? AND id_farmacia = ? AND estado = 'ACTIVO'";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim());
            ps.setString(2, producto.getNombre().trim());
            ps.setString(3, producto.getDescripcion().trim());
            ps.setBigDecimal(4, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(5, producto.getStock());
            ps.setString(6, producto.getEtapaRecomendada());
            ps.setBoolean(7, producto.isHipoalergenico());
            establecerEdad(ps, 8, producto.getEdadGestacionalSugerida());
            ps.setInt(9, idUsuario);
            ps.setInt(10, idMaternidad);
            ps.setInt(11, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean desactivar(int idMaternidad, int idFarmacia,
                              int idUsuario) throws SQLException {
        validarId(idMaternidad);
        validarContexto(idFarmacia, idUsuario);
        String sql = "UPDATE maternidad SET estado = 'INACTIVO', "
                + "id_usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "WHERE id_maternidad = ? AND id_farmacia = ? AND estado = 'ACTIVO'";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idMaternidad);
            ps.setInt(3, idFarmacia);
            return ps.executeUpdate() == 1;
        }
    }

    private static RegistroCatalogo<Maternidad> mapear(ResultSet rs)
            throws SQLException {
        try {
            int edad = rs.getInt("edad_gestacional_sugerida");
            if (rs.wasNull()) edad = 0; // 0 significa 'no especificada' en el modelo actual.

            Maternidad producto = new MaternidadBuilder()
                    .setCodigo(rs.getString("codigo"))
                    .setNombre(rs.getString("nombre"))
                    .setDescripcion(rs.getString("descripcion"))
                    .setPrecio(rs.getBigDecimal("precio").doubleValue())
                    .setStock(rs.getInt("stock"))
                    .etapaRecomendada(rs.getString("etapa_recomendada"))
                    .hipoalergenico(rs.getBoolean("hipoalergenico"))
                    .edadGestacionalSugerida(edad)
                    .build();

            return new RegistroCatalogo<>(
                    rs.getInt("id_maternidad"), producto, rs.getString("estado"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new SQLException("Registro de maternidad inválido en la base de datos", e);
        }
    }

    private static void establecerEdad(PreparedStatement ps, int indice,
                                       int edadGestacional) throws SQLException {
        if (edadGestacional == 0) ps.setNull(indice, Types.INTEGER);
        else ps.setInt(indice, edadGestacional);
    }

    private static void validarId(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID de maternidad inválido");
    }

    private static void validarFarmacia(int idFarmacia) {
        if (idFarmacia <= 0) throw new IllegalArgumentException("Farmacia inválida");
    }

    private static void validarContexto(int idFarmacia, int idUsuario) {
        validarFarmacia(idFarmacia);
        if (idUsuario <= 0) throw new IllegalArgumentException("Usuario inválido");
    }
}
