package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Medicamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<Medicamento> obtenerTodos() {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT m.id_medicamento, m.codigo_invima, m.nombre_comercial, " +
                     "m.principio_activo, m.concentracion, c.nombre_categoria, " +
                     "m.stock_total, m.stock_minimo " +
                     "FROM MEDICAMENTOS m " +
                     "JOIN CATEGORIAS c ON m.id_categoria = c.id_categoria " +
                     "ORDER BY m.id_medicamento ASC";

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
                    rs.getString("nombre_categoria"),
                    rs.getInt("stock_total"),
                    rs.getInt("stock_minimo")
                );
                lista.add(m);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al consultar en Neon: " + e.getMessage());
        }
        return lista;
    }

    public boolean agregarMedicamento(Medicamento m) {
        String sql = "INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, concentracion, id_categoria, stock_total, stock_minimo) " +
                     "VALUES (?, ?, ?, ?, (SELECT id_categoria FROM CATEGORIAS WHERE nombre_categoria = ?), ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getCodigoInvima());
            stmt.setString(2, m.getNombreComercial());
            stmt.setString(3, m.getPrincipioActivo());
            stmt.setString(4, m.getConcentracion());
            stmt.setString(5, m.getCategoria().toUpperCase());
            stmt.setInt(6, m.getStockTotal());
            stmt.setInt(7, m.getStockMinimo());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error al insertar en Neon: " + e.getMessage());
            return false;
        }
    }
}
