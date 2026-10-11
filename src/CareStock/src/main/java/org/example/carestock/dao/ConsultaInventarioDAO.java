package org.example.carestock.dao;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.config.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO concreto, de solo lectura, para las tres categorias de una misma farmacia. */
public class ConsultaInventarioDAO {

    /**
     * Une el catalogo, sin cruzar farmacias ni confundir un stock de cero con
     * una tabla sin resultados. Los codigos INVIMA se presentan como el
     * identificador disponible para medicamentos en el esquema actual.
     */
    private static final String SQL = """
        SELECT 'Medicamento' AS categoria,
               m.id_medicamento AS id,
               m.codigo_invima AS codigo,
               m.nombre_comercial AS nombre,
               COALESCE(NULLIF(BTRIM(m.presentacion), ''),
                        NULLIF(BTRIM(m.forma_farmaceutica), ''),
                        'No registrada') AS presentacion,
               COALESCE(m.stock_total, 0) AS existencia,
               m.estado AS estado
        FROM medicamentos m
        WHERE m.id_farmacia = ?

        UNION ALL

        SELECT 'Aseo' AS categoria,
               a.id_aseo AS id,
               a.codigo AS codigo,
               a.nombre AS nombre,
               'Unidad por definir' AS presentacion,
               COALESCE(a.stock, 0) AS existencia,
               a.estado AS estado
        FROM aseo a
        WHERE a.id_farmacia = ?

        UNION ALL

        SELECT 'Maternidad' AS categoria,
               mt.id_maternidad AS id,
               mt.codigo AS codigo,
               mt.nombre AS nombre,
               'Unidad por definir' AS presentacion,
               COALESCE(mt.stock, 0) AS existencia,
               mt.estado AS estado
        FROM maternidad mt
        WHERE mt.id_farmacia = ?

        ORDER BY categoria, nombre, codigo
        """;

    public List<ProductoInventario> listarPorFarmacia(int idFarmacia) throws SQLException {
        if (idFarmacia <= 0) throw new IllegalArgumentException("Farmacia invalida");
        List<ProductoInventario> productos = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL)) {
            ps.setInt(1, idFarmacia);
            ps.setInt(2, idFarmacia);
            ps.setInt(3, idFarmacia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoInventario(
                            rs.getInt("id"),
                            rs.getString("categoria"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("presentacion"),
                            rs.getInt("existencia"),
                            rs.getString("estado")
                    ));
                }
            }
        }
        return productos;
    }
}
