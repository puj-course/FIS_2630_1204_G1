package com.carestock.security;

import com.carestock.exception.AccesoDenegadoException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MedicamentoAccessPolicyTest {

    @Test
    void superAdminPuedeGestionarInventario() {

        assertTrue(
                MedicamentoAccessPolicy
                        .puedeGestionarInventario(
                                "SUPER_ADMIN"
                        )
        );


        assertDoesNotThrow(
                () ->
                        MedicamentoAccessPolicy
                                .requireGestionarInventario(
                                        "SUPER_ADMIN"
                                )
        );
    }


    @Test
    void administradorPuedeGestionarInventario() {

        assertTrue(
                MedicamentoAccessPolicy
                        .puedeGestionarInventario(
                                "ADMINISTRADOR"
                        )
        );


        assertDoesNotThrow(
                () ->
                        MedicamentoAccessPolicy
                                .requireGestionarInventario(
                                        "ADMINISTRADOR"
                                )
        );
    }


    @Test
    void farmaceuticoNoPuedeGestionarInventario() {

        assertFalse(
                MedicamentoAccessPolicy
                        .puedeGestionarInventario(
                                "FARMACEUTICO"
                        )
        );


        assertThrows(
                AccesoDenegadoException.class,
                () ->
                        MedicamentoAccessPolicy
                                .requireGestionarInventario(
                                        "FARMACEUTICO"
                                )
        );
    }


    @Test
    void rolNuloEsRechazado() {

        assertFalse(
                MedicamentoAccessPolicy
                        .puedeGestionarInventario(
                                null
                        )
        );
    }
}
