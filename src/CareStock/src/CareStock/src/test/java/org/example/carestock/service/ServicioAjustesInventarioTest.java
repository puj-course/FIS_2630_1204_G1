package org.example.carestock.service;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioAjustesInventarioTest {

    @Test
    void aceptaAdministradorYSuperAdmin() {

        assertTrue(
                ServicioAjustesInventario.rolAdministrador(
                        " ADMINISTRADOR "
                )
        );

        assertTrue(
                ServicioAjustesInventario.rolAdministrador(
                        "super_admin"
                )
        );
    }

    @Test
    void rechazaRolesSinPermiso() {

        assertFalse(
                ServicioAjustesInventario.rolAdministrador(
                        "FARMACEUTICO"
                )
        );

        assertFalse(
                ServicioAjustesInventario.rolAdministrador(null)
        );
    }

    @Test
    void admiteAumento() {

        assertDoesNotThrow(
                () -> ServicioAjustesInventario.comprobarSaldo(
                        8, 8, 15
                )
        );
    }

    @Test
    void admiteDisminucionHastaCero() {

        assertDoesNotThrow(
                () -> ServicioAjustesInventario.comprobarSaldo(
                        8, 8, 0
                )
        );
    }

    @Test
    void rechazaCantidadFinalNegativa() {

        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioAjustesInventario.comprobarSaldo(
                        8, 8, -1
                )
        );
    }

    @Test
    void rechazaSaldoDesactualizadoPorConcurrencia() {

        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioAjustesInventario.comprobarSaldo(
                        7, 8, 10
                )
        );
    }

    @Test
    void rechazaAjusteSinCambios() {

        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioAjustesInventario.comprobarSaldo(
                        8, 8, 8
                )
        );
    }

    @Test
    void rechazaStockRealInvalido() {

        assertThrows(
                ReglaNegocioException.class,
                () -> ServicioAjustesInventario.comprobarSaldo(
                        -1, 0, 5
                )
        );
    }
}
