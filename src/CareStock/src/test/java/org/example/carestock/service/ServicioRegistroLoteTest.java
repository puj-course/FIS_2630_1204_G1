package org.example.carestock.service;

import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioRegistroLoteTest {

    private final ServicioRegistroLote servicio = new ServicioRegistroLote();

    private static Lote lote(int cantidad, Integer ubicacion) {
        return new Lote("L-001", 1, cantidad, LocalDate.now().plusDays(90),
                ubicacion, "DISPONIBLE", 1);
    }

    @Test
    void rechazaLoteNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarMedicamentoLote(null, 1, 1));
    }

    @Test
    void rechazaCantidadCero() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMedicamentoLote(lote(0, 1), 1, 1));
        assertEquals("El lote debe ingresar con cantidad positiva", error.getMessage());
    }

    @Test
    void rechazaCantidadNegativa() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMedicamentoLote(lote(-5, 1), 1, 1));
    }

    @Test
    void exigeUbicacion() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMedicamentoLote(lote(5, null), 1, 1));
        assertEquals("La ubicacion del lote es obligatoria", error.getMessage());
    }

    @Test
    void exigeUbicacionPositiva() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMedicamentoLote(lote(5, 0), 1, 1));
    }
}
