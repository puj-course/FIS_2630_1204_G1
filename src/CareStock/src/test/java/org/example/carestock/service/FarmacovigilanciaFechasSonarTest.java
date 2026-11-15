package org.example.carestock.service;

import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FarmacovigilanciaFechasSonarTest {
    @Test
    void loteSinFechaNoEsDispensable() {
        assertFalse(FarmacovigilanciaService.loteConVencimientoVigente(null));
    }

    @Test
    void loteQueVenceHoyNoEsDispensable() {
        assertFalse(FarmacovigilanciaService.loteConVencimientoVigente(FechaNegocio.hoy()));
    }

    @Test
    void loteVencidoNoEsDispensable() {
        assertFalse(FarmacovigilanciaService.loteConVencimientoVigente(
                FechaNegocio.hoy().minusDays(1)));
    }

    @Test
    void soloLoteConFechaFuturaEsDispensable() {
        assertTrue(FarmacovigilanciaService.loteConVencimientoVigente(
                FechaNegocio.hoy().plusDays(1)));
    }
}
