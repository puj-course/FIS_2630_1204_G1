package com.carestock.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreferenciaSesionTest {

    @Test
    void creaPreferenciaPredeterminadaSegura() {

        PreferenciaSesion preferencia =
                PreferenciaSesion.porDefecto(
                        10
                );

        assertEquals(
                10,
                preferencia.getIdUsuario()
        );

        assertTrue(
                preferencia.isTimeoutActivo()
        );

        assertEquals(
                15,
                preferencia.getTimeoutMinutos()
        );
    }

    @Test
    void permiteTiempoMinimo() {

        PreferenciaSesion preferencia =
                new PreferenciaSesion(
                        1,
                        true,
                        1
                );

        assertEquals(
                1,
                preferencia.getTimeoutMinutos()
        );
    }

    @Test
    void permiteTiempoMaximo() {

        PreferenciaSesion preferencia =
                new PreferenciaSesion(
                        1,
                        true,
                        120
                );

        assertEquals(
                120,
                preferencia.getTimeoutMinutos()
        );
    }

    @Test
    void rechazaTiempoFueraDelRango() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PreferenciaSesion(
                        1,
                        true,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PreferenciaSesion(
                        1,
                        true,
                        121
                )
        );
    }

    @Test
    void permiteDesactivarElTimeout() {

        PreferenciaSesion preferencia =
                new PreferenciaSesion(
                        1,
                        false,
                        15
                );

        assertEquals(
                false,
                preferencia.isTimeoutActivo()
        );
    }
}
