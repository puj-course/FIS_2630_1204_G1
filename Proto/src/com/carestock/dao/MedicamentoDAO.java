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
        String sql = "SELECT m.id_medicamento, m.codigo_invima, m.nombre_comercial, m.principio_activo, " +
                     "m.concentracion, c.nombre_categoria AS categoria, m.stock_total, m.stock_minimo " +
                     "FROM MEDICAMENTOS m JOIN CATEGORIAS c ON m.id_categoria = c.id_categoria";

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
            System.err.println("Error al consultar la base de datos: " + e.getMessage());
        }

        return lista;
    }
}
