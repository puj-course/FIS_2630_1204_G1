package org.example.carestock.service;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioFarmacovigilanciaTest {

    private final ServicioFarmacovigilancia servicio = new ServicioFarmacovigilancia();

    private static final String MENSAJE_DATOS =
            "Lote, cantidad, usuario y farmacia deben ser validos";

    @Test
    void laVersionSinFarmaciaEstaBloqueada() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(1, 1));
        assertTrue(error.getMessage().contains("fachada Inventario"));
    }

    @Test
    void rechazaLoteInvalido() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(0, 1, 1, 1));
        assertEquals(MENSAJE_DATOS, error.getMessage());
    }

    @Test
    void rechazaCantidadInvalida() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(1, 0, 1, 1));
    }

    @Test
    void rechazaCantidadNegativa() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(1, -3, 1, 1));
    }

    @Test
    void rechazaFarmaciaInvalida() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(1, 1, 0, 1));
    }

    @Test
    void rechazaUsuarioInvalido() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(1, 1, 1, 0));
    }
}
