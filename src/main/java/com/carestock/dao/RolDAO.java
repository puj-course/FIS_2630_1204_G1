package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    public List<Rol> listarTodos() throws SQLException {

        String sql =
                "SELECT id_rol, nombre_rol "
                + "FROM ROLES "
                + "ORDER BY nombre_rol ASC";

        List<Rol> roles = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(
                        new Rol(
                                rs.getInt("id_rol"),
                                rs.getString("nombre_rol")
                        )
                );
            }
        }

        return roles;
    }
}
