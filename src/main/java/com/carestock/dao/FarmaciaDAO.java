package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Farmacia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FarmaciaDAO {

/**
 * Recupera todas las farmacias activas registradas en la base de datos.
 *
 * @return lista de farmacias activas ordenadas alfabéticamente por nombre.
 *         Si no existen farmacias activas, retorna una lista vacía.
 * @throws SQLException si ocurre un error al consultar la base de datos.
 */
public List<Farmacia> listarActivas() throws SQLException {

    String sql =
            "SELECT id_farmacia, codigo, nombre, estado "
            + "FROM FARMACIAS "
            + "WHERE estado = 'ACTIVA' "
            + "ORDER BY nombre ASC";

    List<Farmacia> farmacias = new ArrayList<>();

    try (
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
    ) {

        while (rs.next()) {
            farmacias.add(mapear(rs));
        }

    } catch (SQLException e) {
        throw new SQLException(
                "No fue posible consultar las farmacias activas.",
                e
        );
    }

    return farmacias;
}
    public Farmacia buscarPorId(
            int idFarmacia
    ) throws SQLException {

        String sql =
                "SELECT id_farmacia, codigo, nombre, estado "
                + "FROM FARMACIAS "
                + "WHERE id_farmacia = ?";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idFarmacia
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }

        return null;
    }

    public int crearSegura(
            int idUsuarioActor,
            String codigo,
            String nombre
    ) throws SQLException {

        String sql =
                "SELECT fn_crear_farmacia_segura(?, ?, ?)";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setString(
                    2,
                    codigo
            );

            stmt.setString(
                    3,
                    nombre
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {

                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No fue posible crear la farmacia."
        );
    }


    public List<Farmacia> buscarPorNombre(
            String filtro
    ) throws SQLException {

        String sql =
                "SELECT id_farmacia, codigo, nombre, estado "
                + "FROM FARMACIAS "
                + "WHERE (? IS NULL OR ? = '' OR nombre ILIKE '%' || ? || '%') "
                + "ORDER BY nombre ASC";

        List<Farmacia> farmacias = new ArrayList<>();

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, filtro);
            stmt.setString(2, filtro);
            stmt.setString(3, filtro);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {
                    farmacias.add(mapear(rs));
                }
            }
        }

        return farmacias;
    }

    private Farmacia mapear(
            ResultSet rs
    ) throws SQLException {

        return new Farmacia(
                rs.getInt("id_farmacia"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("estado")
        );
    }
}
