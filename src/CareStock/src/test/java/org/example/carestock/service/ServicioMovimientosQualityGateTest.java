package org.example.carestock.service;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.example.carestock.service.ServicioMovimientosProducto.Categoria.*;
import static org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento.*;

class ServicioMovimientosQualityGateTest {

    @Test
    void rechazaCategoriaNula() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        null, 1, ENTRADA, 2, "Ingreso", 1, 7));
    }

    @Test
    void rechazaTipoMovimientoNulo() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, null, 2, "Ingreso", 1, 7));
    }

    @Test
    void rechazaUsuarioInvalido() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 2, "Ingreso", 0, 7));
    }

    @Test
    void rechazaFarmaciaInvalida() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 2, "Ingreso", 1, 0));
    }

    @Test
    void rechazaProductoNegativo() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, -1, ENTRADA, 2, "Ingreso", 1, 7));
    }

    @Test
    void rechazaCantidadNegativa() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, -2, "Ingreso", 1, 7));
    }

    @Test
    void rechazaMotivoNulo() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 2, null, 1, 7));
    }

    @Test
    void rechazaMotivoMayorAlLimite() {
        String motivo = "X".repeat(251);

        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 2, motivo, 1, 7));
    }

    @Test
    void aceptaMotivoDe250Caracteres() throws Exception {
        String motivo = "A".repeat(250);

        assertEquals(motivo,
                ServicioMovimientosProducto.validarSolicitud(
                        ASEO, 1, ENTRADA, 2, motivo, 1, 7));
    }

    @Test
    void aceptaMovimientosDeMaternidad() throws Exception {
        assertEquals("Reposicion",
                ServicioMovimientosProducto.validarSolicitud(
                        MATERNIDAD, 5, ENTRADA, 10,
                        "  Reposicion  ", 2, 7));
    }

    @Test
    void rechazaStockAnteriorNegativo() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.calcularStock(
                        -1, ENTRADA, 1));
    }

    @Test
    void rechazaCalculoConMovimientoNulo() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.calcularStock(
                        10, null, 1));
    }

    @Test
    void rechazaCalculoConCantidadCero() {
        assertThrows(ReglaNegocioException.class,
                () -> ServicioMovimientosProducto.calcularStock(
                        10, ENTRADA, 0));
    }

    @Test
    void permiteSalidaParcial() throws Exception {
        assertEquals(7,
                ServicioMovimientosProducto.calcularStock(
                        10, SALIDA, 3));
    }

    @Test
    void permiteEntradaHastaLimiteEntero() throws Exception {
        assertEquals(Integer.MAX_VALUE,
                ServicioMovimientosProducto.calcularStock(
                        Integer.MAX_VALUE - 1, ENTRADA, 1));
    }
}
