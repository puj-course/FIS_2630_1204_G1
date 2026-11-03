package org.example.carestock.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorDispensacionTest {

    @Test
    void aceptaUnaCantidadValida() {
        assertEquals(3, ValidadorDispensacion.validarCantidad("3", 10));
        assertEquals(3, ValidadorDispensacion.validarCantidad("  3  ", 10));
    }

    @Test
    void aceptaTodasLasExistenciasDelLote() {
        assertEquals(10, ValidadorDispensacion.validarCantidad("10", 10));
    }

    @Test
    void rechazaTextoQueNoEsEntero() {
        NumberFormatException error = assertThrows(NumberFormatException.class,
                () -> ValidadorDispensacion.validarCantidad("abc", 10));
        assertEquals("La cantidad a dispensar debe ser un número entero válido.",
                error.getMessage());
        assertThrows(NumberFormatException.class,
                () -> ValidadorDispensacion.validarCantidad("2.5", 10));
        assertThrows(NumberFormatException.class,
                () -> ValidadorDispensacion.validarCantidad(null, 10));
    }

    @Test
    void rechazaCeroYNegativos() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> ValidadorDispensacion.validarCantidad("0", 10));
        assertEquals("La cantidad a dispensar debe ser mayor que cero.", cero.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> ValidadorDispensacion.validarCantidad("-4", 10));
    }

    @Test
    void rechazaCantidadMayorQueLasExistencias() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ValidadorDispensacion.validarCantidad("11", 10));
        assertEquals(
                "La cantidad a dispensar (11) supera las existencias del lote (10).",
                error.getMessage());
    }

    @Test
    void calculaElStockRestante() {
        assertEquals(7, ValidadorDispensacion.stockRestante(10, 3));
        assertEquals(0, ValidadorDispensacion.stockRestante(10, 10));
    }

    @Test
    void laConfirmacionMuestraLoteCantidadYStockQueQuedara() {
        String texto = ValidadorDispensacion.mensajeConfirmacion("L-77", 3, 10);

        assertTrue(texto.contains("Lote: L-77"));
        assertTrue(texto.contains("Cantidad a dispensar: 3"));
        assertTrue(texto.contains("Stock actual del lote: 10"));
        assertTrue(texto.contains("Stock que quedará: 7"));
    }

    @Test
    void elMensajeFinalIndicaElStockRestante() {
        String texto = ValidadorDispensacion.mensajeExito("L-77", 3, 10);

        assertTrue(texto.contains("Se dispensaron 3 unidades del lote L-77 correctamente."));
        assertTrue(texto.contains("Stock restante del lote: 7."));
    }
}
