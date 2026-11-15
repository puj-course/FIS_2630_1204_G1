package org.example.carestock.service;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VistaPreviaStockTest {

    @Test
    void laEntradaSumaLaCantidad() {
        assertEquals(OptionalInt.of(15), VistaPreviaStock.stockResultante(10, "ENTRADA", "5"));
    }

    @Test
    void laSalidaRestaLaCantidad() {
        assertEquals(OptionalInt.of(7), VistaPreviaStock.stockResultante(10, "SALIDA", "3"));
        assertEquals(OptionalInt.of(0), VistaPreviaStock.stockResultante(10, "SALIDA", "10"));
    }

    @Test
    void ignoraEspaciosEnLaCantidad() {
        assertEquals(OptionalInt.of(12), VistaPreviaStock.stockResultante(10, "ENTRADA", " 2 "));
    }

    @Test
    void cantidadVaciaNulaONoNumericaNoSeCalcula() {
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, "ENTRADA", ""));
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, "ENTRADA", null));
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, "ENTRADA", "abc"));
    }

    @Test
    void cantidadCeroONegativaNoSeCalcula() {
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, "SALIDA", "0"));
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, "SALIDA", "-3"));
    }

    @Test
    void tipoDesconocidoONuloNoSeCalcula() {
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, null, "5"));
        assertEquals(OptionalInt.empty(), VistaPreviaStock.stockResultante(10, "AJUSTE", "5"));
    }

    @Test
    void unaEntradaQueDesbordaNoSeCalcula() {
        assertEquals(OptionalInt.empty(),
                VistaPreviaStock.stockResultante(Integer.MAX_VALUE, "ENTRADA", "1"));
    }

    @Test
    void unaSalidaMayorQueElStockQuedaNegativa() {
        assertEquals(OptionalInt.of(-4), VistaPreviaStock.stockResultante(6, "SALIDA", "10"));
        assertTrue(VistaPreviaStock.quedaNegativo(6, "SALIDA", "10"));
    }

    @Test
    void quedaNegativoEsFalsoCuandoNoHayProblema() {
        assertFalse(VistaPreviaStock.quedaNegativo(10, "SALIDA", "10"));
        assertFalse(VistaPreviaStock.quedaNegativo(10, "ENTRADA", "5"));
        assertFalse(VistaPreviaStock.quedaNegativo(10, "SALIDA", ""));
    }

    @Test
    void elTextoMuestraNumeroRayaOAviso() {
        assertEquals("Stock resultante: 15", VistaPreviaStock.texto(10, "ENTRADA", "5"));
        assertEquals("Stock resultante: —", VistaPreviaStock.texto(10, "ENTRADA", ""));
        assertEquals("Stock resultante: -4 (stock insuficiente)",
                VistaPreviaStock.texto(6, "SALIDA", "10"));
    }
}
