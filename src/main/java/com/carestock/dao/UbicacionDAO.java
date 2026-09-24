package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Ubicacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UbicacionDAO {

    public List<Ubicacion> obtenerTodas() throws SQLException {
        String sql = "SELECT id_ubicacion, estante, nivel, descripcion " +
                     "FROM UBICACIONES ORDER BY estante, nivel";
        List<Ubicacion> ubicaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ubicaciones.add(new Ubicacion(
                    rs.getInt("id_ubicacion"),
                    rs.getString("estante"),
                    rs.getString("nivel"),
                    rs.getString("descripcion")
                ));
            }
        }
        return ubicaciones;
    }
}
