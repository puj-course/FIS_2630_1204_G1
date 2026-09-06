package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Medicamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<Medicamento> obtenerTodos() {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT m.id_medicamento, m.codigo_invima, m.nombre_comercial, " +
                     "m.principio_activo, m.concentracion, c.nombre_categoria AS categoria, " +
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
                    rs.getString("categoria"),
                    rs.getInt("stock_total"),
                    rs.getInt("stock_minimo")
                );
                lista.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar Neon DB: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error al obtener total unidades: " + e.getMessage());
        }
        return 0;
    }

    public int obtenerAlertasCriticas() {
        String sql = "SELECT COUNT(*) FROM MEDICAMENTOS WHERE stock_total <= stock_minimo";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener alertas criticas: " + e.getMessage());
        }
        return 0;
    }

    public boolean guardar(Medicamento m) {
        return insertar(m);
    }

    public boolean agregarMedicamento(Medicamento m) {
        return insertar(m);
    }

    public boolean insertar(Medicamento m) {
        String sql = "INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, concentracion, id_categoria, stock_total, stock_minimo) VALUES (?, ?, ?, ?, 1, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getCodigoInvima());
            stmt.setString(2, m.getNombreComercial());
            stmt.setString(3, m.getPrincipioActivo());
            stmt.setString(4, m.getConcentracion());
            stmt.setInt(5, m.getStockTotal() != null ? m.getStockTotal() : 0);
            stmt.setInt(6, m.getStockMinimo() != null ? m.getStockMinimo() : 0);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar en Neon DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
