package com.carestock.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdleSessionManagerTest {

    @Test
    void aceptaLimiteMinimo() {

        assertDoesNotThrow(
                () -> IdleSessionManager
                        .validarTimeoutMinutes(
                                1
                        )
        );
    }

    @Test
    void aceptaLimiteMaximo() {

        assertDoesNotThrow(
                () -> IdleSessionManager
                        .validarTimeoutMinutes(
                                120
                        )
        );
    }

    @Test
    void rechazaCero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> IdleSessionManager
                        .validarTimeoutMinutes(
                                0
                        )
        );
    }

    @Test
    void rechazaValorSuperiorAlMaximo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> IdleSessionManager
                        .validarTimeoutMinutes(
                                121
                        )
        );
    }
}
