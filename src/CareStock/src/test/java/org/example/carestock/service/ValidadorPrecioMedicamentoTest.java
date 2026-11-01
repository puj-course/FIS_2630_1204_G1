package org.example.carestock.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorPrecioMedicamentoTest {

    @Test
    void precioNuloEsOpcional() {
        assertNull(ValidadorPrecioMedicamento.convertir(null));
    }

    @Test
    void precioVacioEsOpcional() {
        assertNull(ValidadorPrecioMedicamento.convertir("   "));
    }

    @Test
    void aceptaPrecioConDosDecimales() {
        assertEquals(
                new BigDecimal("15000.25"),
                ValidadorPrecioMedicamento.convertir("15000.25")
        );
    }

    @Test
    void aceptaPrecioCero() {
        assertEquals(
                BigDecimal.ZERO,
                ValidadorPrecioMedicamento.convertir("0")
        );
    }

    @Test
    void aceptaDiezDigitosEnteros() {
        assertEquals(
                new BigDecimal("9999999999.99"),
                ValidadorPrecioMedicamento.convertir(
                        "9999999999.99")
        );
    }

    @Test
    void eliminaEspaciosExteriores() {
        assertEquals(
                new BigDecimal("1250.50"),
                ValidadorPrecioMedicamento.convertir(
                        "  1250.50  ")
        );
    }

    @Test
    void rechazaPreciosNegativos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidadorPrecioMedicamento.convertir(
                        "-100.00")
        );
    }

    @Test
    void rechazaMasDeDosDecimales() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidadorPrecioMedicamento.convertir(
                        "100.999")
        );
    }

    @Test
    void rechazaMasDeDiezDigitosEnteros() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidadorPrecioMedicamento.convertir(
                        "10000000000")
        );
    }

    @Test
    void rechazaTextoNoNumerico() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ValidadorPrecioMedicamento.convertir(
                        "precio invalido")
        );
        assertTrue(error.getMessage().contains("Precio invalido"));
    }

    @Test
    void rechazaFormatoConComa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidadorPrecioMedicamento.convertir(
                        "100,50")
        );
    }
}
