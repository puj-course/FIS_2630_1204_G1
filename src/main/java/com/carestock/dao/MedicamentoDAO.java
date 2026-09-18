package com.carestock.dao;

import com.carestock.config.AppConfig;
import com.carestock.config.DatabaseConfig;
import com.carestock.model.Medicamento;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<Medicamento> obtenerTodos() {
        try {
            return consultarMedicamentos(false);
        } catch (SQLException e) {
            System.err.println("Error al consultar medicamentos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicamento> obtenerActivos() throws SQLException {
        return consultarMedicamentos(true);
    }

    private List<Medicamento> consultarMedicamentos(boolean soloActivos) throws SQLException {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT m.id_medicamento, m.codigo_invima, m.nombre_comercial, " +
                     "m.principio_activo, m.concentracion, m.forma_farmaceutica, " +
                     "c.nombre_categoria AS categoria, m.stock_total, m.stock_minimo " +
                     "FROM MEDICAMENTOS m " +
                     "JOIN CATEGORIAS c ON m.id_categoria = c.id_categoria " +
                     (soloActivos ? "WHERE m.estado = 'ACTIVO' " : "") +
                     "ORDER BY m.nombre_comercial ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Medicamento m = new Medicamento(
                    rs.getLong("id_medicamento"),
                    rs.getString("codigo_invima"),
                    rs.getString("nombre_comercial"),
                    rs.getString("principio_activo"),
                    rs.getString("concentracion"),
                    rs.getString("categoria"),
                    rs.getInt("stock_total"),
                    rs.getInt("stock_minimo")
                );
                m.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
                lista.add(m);
            }
        }
        return lista;
    }

    public int obtenerTotalUnidadesStock() {
        String sql = "SELECT COALESCE(SUM(stock_total), 0) FROM MEDICAMENTOS";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener total de unidades: " + e.getMessage());
        }
        return 0;
    }

    public int obtenerAlertasCriticas() {
        String sql = "SELECT COUNT(*) FROM MEDICAMENTOS WHERE estado = 'ACTIVO' AND stock_total <= stock_minimo";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener alertas críticas: " + e.getMessage());
        }
        return 0;
    }

    public boolean guardar(Medicamento medicamento) {
        try {
            insertar(medicamento);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar medicamento: " + e.getMessage());
            return false;
        }
    }

    public boolean agregarMedicamento(Medicamento medicamento) {
        return guardar(medicamento);
    }

    public void insertar(Medicamento medicamento) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            int idCategoria = resolverCategoria(conn, medicamento.getCategoria());
            boolean tienePresentacion = columnaExiste(conn, "medicamentos", "presentacion");
            boolean tieneUsuarioCreacion = columnaExiste(conn, "medicamentos", "id_usuario_creacion");

            Integer idUsuario = null;
            if (tieneUsuarioCreacion) {
                idUsuario = resolverUsuario(conn, AppConfig.getCurrentUserEmail());
            }

            StringBuilder columnas = new StringBuilder(
                "codigo_invima, nombre_comercial, principio_activo, concentracion, " +
                "forma_farmaceutica, id_categoria, stock_total, stock_minimo, estado"
            );
            StringBuilder valores = new StringBuilder("?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO'");

            if (tienePresentacion) {
                columnas.append(", presentacion");
                valores.append(", ?");
            }
            if (tieneUsuarioCreacion) {
                columnas.append(", id_usuario_creacion");
                valores.append(", ?");
            }

            String sql = "INSERT INTO MEDICAMENTOS (" + columnas + ") VALUES (" + valores + ")";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int i = 1;
                stmt.setString(i++, medicamento.getCodigoInvima());
                stmt.setString(i++, medicamento.getNombreComercial());
                stmt.setString(i++, medicamento.getPrincipioActivo());
                stmt.setString(i++, medicamento.getConcentracion());
                stmt.setString(i++, valorNoVacio(medicamento.getFormaFarmaceutica(), "SIN ESPECIFICAR"));
                stmt.setInt(i++, idCategoria);
                stmt.setInt(i++, medicamento.getStockTotal() == null ? 0 : medicamento.getStockTotal());
                stmt.setInt(i++, medicamento.getStockMinimo() == null ? 0 : medicamento.getStockMinimo());

                if (tienePresentacion) {
                    stmt.setString(i++, valorNoVacio(medicamento.getPresentacion(), "SIN ESPECIFICAR"));
                }
                if (tieneUsuarioCreacion) {
                    if (idUsuario == null) stmt.setNull(i, Types.INTEGER); else stmt.setInt(i, idUsuario);
                }

                stmt.executeUpdate();
            }
        }
    }

    private int resolverCategoria(Connection conn, String categoria) throws SQLException {
        String sql = "SELECT id_categoria FROM CATEGORIAS WHERE UPPER(nombre_categoria) = UPPER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria == null ? "" : categoria.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("La categoría '" + categoria + "' no existe en CATEGORIAS.");
    }

    private int resolverUsuario(Connection conn, String email) throws SQLException {
        String sql = "SELECT id_usuario FROM USUARIOS WHERE LOWER(email) = LOWER(?) AND estado = 'ACTIVO'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No existe un usuario ACTIVO con el correo configurado: " + email);
    }

    private boolean columnaExiste(Connection conn, String tabla, String columna) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tabla, columna)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = metaData.getColumns(null, null, tabla.toUpperCase(), columna.toUpperCase())) {
            return rs.next();
        }
    }

    private String valorNoVacio(String valor, String porDefecto) {
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }
}
