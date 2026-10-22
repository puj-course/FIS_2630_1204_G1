package org.example.carestock.service;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.example.carestock.service.ServicioMovimientosProducto.Categoria.ASEO;
import static org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento.ENTRADA;
import static org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento.SALIDA;
import static org.junit.jupiter.api.Assertions.*;

class ServicioMovimientosProductoTest {

    @Test
    void entradaIncrementaElStock() throws Exception {
        assertEquals(
                25,
                ServicioMovimientosProducto.calcularStock(
                        20, ENTRADA, 5
                )
        );
    }

    @Test
    void salidaDisminuyeElStock() throws Exception {
        assertEquals(
                0,
                ServicioMovimientosProducto.calcularStock(
                        5, SALIDA, 5
                )
        );
    }

    @Test
    void rechazaSalidaSinExistencias() {
        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.calcularStock(
                        3, SALIDA, 4
                )
        );
    }

    @Test
    void rechazaDesbordamientoEnEntrada() {
        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.calcularStock(
                        Integer.MAX_VALUE, ENTRADA, 1
                )
        );
    }

    @Test
    void rechazaCantidadNoPositiva() {
        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 0, "Pedido", 1, 1
                )
        );
    }

    @Test
    void rechazaMovimientoSinMotivo() {
        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 1, "  ", 1, 1
                )
        );
    }

    @Test
    void normalizaMotivo() throws Exception {
        assertEquals(
                "Recepción",
                ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 1,
                        "  Recepción  ", 1, 1
                )
        );
    }

    @Test
    void rechazaProductoOContextoInvalidos() {
        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 0, ENTRADA, 1, "Pedido", 1, 1
                )
        );

        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 1, "Pedido", 1, -1
                )
        );
    }
}
