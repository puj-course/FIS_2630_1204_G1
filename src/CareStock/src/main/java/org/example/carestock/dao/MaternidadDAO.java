package org.example.carestock.dao;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.factory.FabricaMaternidad;
import org.example.carestock.factory.FabricaProducto;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class MaternidadDAO extends CatalogoDAO<Maternidad> {
    private static final FabricaProducto<Maternidad> FABRICA =
            new FabricaMaternidad();

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
        final int edadFinal = edad;

        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        double precio = rs.getBigDecimal("precio").doubleValue();
        int stock = rs.getInt("stock");
        String etapa = rs.getString("etapa_recomendada");
        boolean hipoalergenico = rs.getBoolean("hipoalergenico");

        Maternidad producto = FABRICA.crear(p -> {
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setEtapaRecomendada(etapa);
            p.setHipoalergenico(hipoalergenico);
            p.setEdadGestacionalSugerida(edadFinal);
        });

        return new RegistroCatalogo<>(
                rs.getInt("id_maternidad"), producto,
                rs.getString("estado"));
    }

    private static void establecerEdad(PreparedStatement ps, int indice, int edadGestacional)
            throws SQLException {
        if (edadGestacional == 0) ps.setNull(indice, Types.INTEGER);
        else ps.setInt(indice, edadGestacional);
    }
}
