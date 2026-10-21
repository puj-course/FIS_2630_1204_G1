package org.example.carestock.proxy;
import org.example.carestock.dto.ProductoInventario;
import java.sql.SQLException;
import java.util.List;
@FunctionalInterface
public interface ConsultaInventario {
    List<ProductoInventario> listarPorFarmacia(int idFarmacia)
            throws SQLException;
}