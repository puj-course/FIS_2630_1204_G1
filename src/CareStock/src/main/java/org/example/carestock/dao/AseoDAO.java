package org.example.carestock.dao;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.AseoBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AseoDAO extends CatalogoDAO<Aseo> {

    @Override
    protected String tabla() {
        return "aseo";
    }

    @Override
    protected String columnaId() {
        return "id_aseo";
    }

    @Override
    protected String nombreProducto() {
        return "aseo";
    }

    @Override
    protected List<String> columnasEspecificas() {
        return List.of("tipo_aseo", "biodegradable", "componentes_activos");
    }

    @Override
    protected void asignarCampos(PreparedStatement ps, Aseo producto) throws SQLException {
        asignarCamposComunes(ps, producto.getCodigo(), producto.getNombre(),
                producto.getDescripcion(), producto.getPrecio(), producto.getStock());
        ps.setString(6, producto.getTipoAseo());
        ps.setBoolean(7, producto.isBiodegradable());
        ps.setString(8, producto.getComponentesActivos());
    }

    @Override
    protected RegistroCatalogo<Aseo> construir(ResultSet rs) throws SQLException {
        Aseo producto = new AseoBuilder()
                .setCodigo(rs.getString("codigo"))
                .setNombre(rs.getString("nombre"))
                .setDescripcion(rs.getString("descripcion"))
                .setPrecio(rs.getBigDecimal("precio").doubleValue())
                .setStock(rs.getInt("stock"))
                .tipoAseo(rs.getString("tipo_aseo"))
                .biodegradable(rs.getBoolean("biodegradable"))
                .componentesActivos(rs.getString("componentes_activos"))
                .build();
        return new RegistroCatalogo<>(rs.getInt("id_aseo"), producto, rs.getString("estado"));
    }
}
