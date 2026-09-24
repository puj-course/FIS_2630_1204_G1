package com.carestock.utils;

import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IngresoLoteValidatorTest {

    private final Medicamento medicamento = new Medicamento(
        1L, "INVIMA-001", "Acetaminofén", "Paracetamol", "500 mg",
        "ANALGESICOS", 0, 10
    );
    private final Ubicacion ubicacion = new Ubicacion(1, "A", "1", "Bodega");

    @Test
    void aceptaDatosValidos() {
        String error = IngresoLoteValidator.validar(
            medicamento, "LOT-2026-001", "100", LocalDate.now().plusMonths(6), ubicacion
        );
        assertNull(error);
    }

    @Test
    void rechazaMedicamentoNulo() {
        assertEquals("Debe seleccionar un medicamento activo.",
            IngresoLoteValidator.validar(null, "LOT-001", "10", LocalDate.now().plusDays(1), ubicacion));
    }

    @Test
    void rechazaLoteConCaracteresInvalidos() {
        assertEquals("El número de lote solo debe contener letras, números y guiones (3 a 50 caracteres).",
            IngresoLoteValidator.validar(medicamento, "LOT 001", "10", LocalDate.now().plusDays(1), ubicacion));
    }

    @Test
    void rechazaCantidadCero() {
        assertEquals("La cantidad debe ser un número entero mayor a cero.",
            IngresoLoteValidator.validar(medicamento, "LOT-001", "0", LocalDate.now().plusDays(1), ubicacion));
    }

    @Test
    void rechazaCantidadNoNumerica() {
        assertEquals("La cantidad ingresada debe ser un número entero válido.",
            IngresoLoteValidator.validar(medicamento, "LOT-001", "abc", LocalDate.now().plusDays(1), ubicacion));
    }

    @Test
    void rechazaFechaVencida() {
        assertEquals("La fecha de vencimiento debe ser posterior a la fecha actual (" + LocalDate.now() + ").",
            IngresoLoteValidator.validar(medicamento, "LOT-001", "10", LocalDate.now(), ubicacion));
    }

    @Test
    void rechazaUbicacionNula() {
        assertEquals("Debe seleccionar una ubicación física disponible.",
            IngresoLoteValidator.validar(medicamento, "LOT-001", "10", LocalDate.now().plusDays(1), null));
    }
}
