package org.example.carestock.service;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResolucionNavegacionAlertasTest {

    private static AlertaInventario alertaStock(Categoria categoria) {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, categoria,
                17, "COD-17", "Producto 17", null,
                2, 5, null, null, Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario alertaVencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO,
                44, "MED-8", "Producto 8", "LOT-44",
                3, null, LocalDate.now().plusDays(5), 5,
                Severidad.CRITICA, null, null, null);
    }

    @Test
    void resuelveStockDeLasTresCategorias() {
        for (Categoria categoria : Categoria.values()) {
            var destino = ResolucionNavegacionAlertas.resolver(
                    alertaStock(categoria), 9, 9);

            assertEquals(TipoAlerta.STOCK_MINIMO, destino.tipo());
            assertEquals(categoria, destino.categoria());
            assertEquals(17, destino.idReferencia());
            assertEquals(9, destino.idFarmacia());
        }
    }

    @Test
    void encuentraProductoPorIdCategoriaYCodigo() {
        var destino = ResolucionNavegacionAlertas.resolver(
                alertaStock(Categoria.ASEO), 9, 9);

        var otraCategoria = new ProductoInventario(
                17, "Maternidad", "COD-17",
                "Otro", "", 0, "ACTIVO");

        var otroCodigo = new ProductoInventario(
                17, "Aseo", "COD-99",
                "Otro", "", 0, "ACTIVO");

        var correcto = new ProductoInventario(
                17, "Aseo", "COD-17",
                "Producto 17", "", 2, "ACTIVO");

        assertEquals(correcto,
                ResolucionNavegacionAlertas.buscarProducto(
                        destino, List.of(
                                otraCategoria, otroCodigo, correcto))
                        .orElseThrow());

        assertTrue(ResolucionNavegacionAlertas.buscarProducto(
                destino, List.of(otroCodigo)).isEmpty());
    }

    @Test
    void resuelveLotePorIdYNumero() {
        var destino = ResolucionNavegacionAlertas.resolver(
                alertaVencimiento(), 9, 9);

        var equivocado = new Lote();
        equivocado.setIdLote(44);
        equivocado.setNumeroLote("OTRO");

        var correcto = new Lote();
        correcto.setIdLote(44);
        correcto.setNumeroLote("LOT-44");

        assertEquals(correcto,
                ResolucionNavegacionAlertas.buscarLote(
                        destino, List.of(equivocado, correcto))
                        .orElseThrow());

        assertTrue(ResolucionNavegacionAlertas.buscarLote(
                destino, List.of(equivocado)).isEmpty());
    }

    @Test
    void impideNavegarEntreFarmacias() {
        assertThrows(IllegalStateException.class,
                () -> ResolucionNavegacionAlertas.resolver(
                        alertaStock(Categoria.ASEO), 9, 10));

        assertThrows(IllegalStateException.class,
                () -> ResolucionNavegacionAlertas.resolver(
                        alertaStock(Categoria.ASEO), 9, null));

        assertThrows(IllegalStateException.class,
                () -> ResolucionNavegacionAlertas.resolver(
                        alertaStock(Categoria.ASEO), 0, 0));
    }

    @Test
    void rechazaVencimientoConCategoriaIncorrecta() {
        var invalida = new AlertaInventario(
                TipoAlerta.VENCIMIENTO, Categoria.ASEO,
                44, "COD", "Producto", "LOT-44",
                1, null, LocalDate.now(), 0,
                Severidad.CRITICA, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> ResolucionNavegacionAlertas.resolver(
                        invalida, 9, 9));
    }
}
