package org.example.carestock.dao;

import org.example.carestock.dto.RegistroCatalogo;
import org.example.carestock.model.Aseo;
import org.example.carestock.factory.FabricaAseo;
import org.example.carestock.factory.FabricaProducto;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AseoDao extends CatalogoDao<Aseo> {
    private static final FabricaProducto<Aseo> FABRICA =
            new FabricaAseo();

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
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        double precio = rs.getBigDecimal("precio").doubleValue();
        int stock = rs.getInt("stock");
        String tipo = rs.getString("tipo_aseo");
        boolean biodegradable = rs.getBoolean("biodegradable");
        String componentes = rs.getString("componentes_activos");

        Aseo producto = FABRICA.crear(p -> {
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setTipoAseo(tipo);
            p.setBiodegradable(biodegradable);
            p.setComponentesActivos(componentes);
        });

        return new RegistroCatalogo<>(
                rs.getInt("id_aseo"), producto,
                rs.getString("estado"));
    }
}
