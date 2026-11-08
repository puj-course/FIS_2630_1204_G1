package org.example.carestock.utils;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

class FechaNegocioTest {
    @Test
    void zonaDeNegocioEsBogota() {
        assertEquals(ZoneId.of("America/Bogota"), FechaNegocio.ZONA);
    }

    @Test
    void deNocheUtcTodaviaEsElDiaAnteriorEnColombia() {
        Clock utc = Clock.fixed(Instant.parse("2026-11-08T02:00:00Z"), ZoneOffset.UTC);
        assertEquals(LocalDate.of(2026, 11, 7), FechaNegocio.hoy(utc));
    }

    @Test
    void madrugadaUtcYaEsNuevoDiaEnColombia() {
        Clock utc = Clock.fixed(Instant.parse("2026-11-08T06:00:00Z"), ZoneOffset.UTC);
        assertEquals(LocalDate.of(2026, 11, 8), FechaNegocio.hoy(utc));
    }

    @Test
    void relojNuloEsInvalido() {
        assertThrows(NullPointerException.class, () -> FechaNegocio.hoy(null));
    }
}
