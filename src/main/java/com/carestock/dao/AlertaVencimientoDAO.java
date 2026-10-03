package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.AlertaVencimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AlertaVencimientoDAO {

    public List<AlertaVencimiento> listarTodas() throws SQLException {
        String sql = "SELECT * FROM vw_alertas_vencimiento ORDER BY dias_para_vencer ASC";
        List<AlertaVencimiento> alertas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                alertas.add(mapear(rs));
            }
        }
        return alertas;
    }

    public List<AlertaVencimiento> listarPorNivel(String nivelAlerta) throws SQLException {
        String sql = "SELECT * FROM vw_alertas_vencimiento WHERE nivel_alerta = ? " +
                "ORDER BY dias_para_vencer ASC";
        List<AlertaVencimiento> alertas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nivelAlerta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alertas.add(mapear(rs));
                }
            }
        }
        return alertas;
    }

    public List<AlertaVencimiento> listarPorFarmacia(int idFarmacia) throws SQLException {
        validarIdFarmacia(idFarmacia);

        String sql = "SELECT * FROM vw_alertas_vencimiento WHERE id_farmacia = ? " +
                "ORDER BY dias_para_vencer ASC";
        List<AlertaVencimiento> alertas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFarmacia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alertas.add(mapear(rs));
                }
            }
        }
        return alertas;
    }

    public List<AlertaVencimiento> listarPorFarmaciaYNivel(int idFarmacia, String nivelAlerta) throws SQLException {
        validarIdFarmacia(idFarmacia);

        String sql = "SELECT * FROM vw_alertas_vencimiento WHERE id_farmacia = ? AND nivel_alerta = ? " +
                "ORDER BY dias_para_vencer ASC";
        List<AlertaVencimiento> alertas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFarmacia);
            stmt.setString(2, nivelAlerta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alertas.add(mapear(rs));
                }
            }
        }
        return alertas;
    }

    private void validarIdFarmacia(int idFarmacia) {
        if (idFarmacia <= 0) {
            throw new IllegalArgumentException("idFarmacia debe ser mayor que cero.");
        }
    }

    private AlertaVencimiento mapear(ResultSet rs) throws SQLException {
        return new AlertaVencimiento(
                rs.getInt("id_lote"),
                rs.getInt("id_medicamento"),
                rs.getInt("id_farmacia"),
                rs.getString("nombre_comercial"),
                rs.getString("numero_lote"),
                rs.getInt("cantidad_actual"),
                rs.getDate("fecha_vencimiento").toLocalDate(),
                rs.getLong("dias_para_vencer"),
                rs.getString("nivel_alerta")
        );
    }
}
