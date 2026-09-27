package com.carestock.security;

import com.carestock.exception.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicamentoAccessPolicyTest {

    @Test
    void administradorPuedeRegistrarMedicamentos() {

        assertTrue(
                MedicamentoAccessPolicy
                        .puedeRegistrarMedicamento(
                                "ADMINISTRADOR"
                        )
        );

        assertDoesNotThrow(
                () -> MedicamentoAccessPolicy
                        .requireRegistrarMedicamento(
                                "ADMINISTRADOR"
                        )
        );
    }

    @Test
    void farmaceuticoPuedeRegistrarMedicamentos() {

        assertTrue(
                MedicamentoAccessPolicy
                        .puedeRegistrarMedicamento(
                                "FARMACEUTICO"
                        )
        );

        assertDoesNotThrow(
                () -> MedicamentoAccessPolicy
                        .requireRegistrarMedicamento(
                                "FARMACEUTICO"
                        )
        );
    }

    @Test
    void rolNoAutorizadoEsRechazado() {

        assertFalse(
                MedicamentoAccessPolicy
                        .puedeRegistrarMedicamento(
                                "CONSULTA"
                        )
        );

        assertThrows(
                AccesoDenegadoException.class,
                () -> MedicamentoAccessPolicy
                        .requireRegistrarMedicamento(
                                "CONSULTA"
                        )
        );
    }

    @Test
    void rolNuloEsRechazado() {

        assertFalse(
                MedicamentoAccessPolicy
                        .puedeRegistrarMedicamento(
                                null
                        )
        );
    }
}
