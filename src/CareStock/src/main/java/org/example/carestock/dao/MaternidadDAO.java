package org.example.carestock.dao;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.MaternidadBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class MaternidadDAO extends CatalogoDAO<Maternidad> {

    @Override
    protected String tabla() {
        return "maternidad";
    }

    @Override
    protected String columnaId() {
        return "id_maternidad";
    }

    @Override
    protected String nombreProducto() {
        return "maternidad";
    }

    @Override
    protected List<String> columnasEspecificas() {
        return List.of("etapa_recomendada", "hipoalergenico", "edad_gestacional_sugerida");
    }

    @Override
    protected void asignarCampos(PreparedStatement ps, Maternidad producto) throws SQLException {
        asignarCamposComunes(ps, producto.getCodigo(), producto.getNombre(),
                producto.getDescripcion(), producto.getPrecio(), producto.getStock());
        ps.setString(6, producto.getEtapaRecomendada());
        ps.setBoolean(7, producto.isHipoalergenico());
        establecerEdad(ps, 8, producto.getEdadGestacionalSugerida());
    }

    @Override
    protected RegistroCatalogo<Maternidad> construir(ResultSet rs) throws SQLException {
        int edad = rs.getInt("edad_gestacional_sugerida");
        if (rs.wasNull()) edad = 0;

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

        return new RegistroCatalogo<>(rs.getInt("id_maternidad"), producto, rs.getString("estado"));
    }

    private static void establecerEdad(PreparedStatement ps, int indice, int edadGestacional)
            throws SQLException {
        if (edadGestacional == 0) ps.setNull(indice, Types.INTEGER);
        else ps.setInt(indice, edadGestacional);
    }
}
