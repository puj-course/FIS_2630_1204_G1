package org.example.carestock.service;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioAjustesInventarioTest {

    @Test
    void aceptaAdministrador() {
        assertTrue(
            ServicioAjustesInventario.rolAdministrador(
                "ADMINISTRADOR"
            )
        );
    }

    @Test
    void aceptaSuperAdministrador() {
        assertTrue(
            ServicioAjustesInventario.rolAdministrador(
                "SUPER_ADMIN"
            )
        );
    }

    @Test
    void rechazaFarmaceutico() {
        assertFalse(
            ServicioAjustesInventario.rolAdministrador(
                "FARMACEUTICO"
            )
        );
    }

    @Test
    void rechazaRolNulo() {
        assertFalse(
            ServicioAjustesInventario.rolAdministrador(null)
        );
    }

    @Test
    void permiteIncrementarStock() {
        assertDoesNotThrow(
            () -> ServicioAjustesInventario.comprobarSaldo(
                10, 10, 15
            )
        );
    }

    @Test
    void permiteReducirStockACero() {
        assertDoesNotThrow(
            () -> ServicioAjustesInventario.comprobarSaldo(
                10, 10, 0
            )
        );
    }

    @Test
    void rechazaStockNegativo() {
        assertThrows(
            ReglaNegocioException.class,
            () -> ServicioAjustesInventario.comprobarSaldo(
                10, 10, -5
            )
        );
    }

    @Test
    void detectaModificacionConcurrente() {
        assertThrows(
            ReglaNegocioException.class,
            () -> ServicioAjustesInventario.comprobarSaldo(
                8, 10, 15
            )
        );
    }

    @Test
    void rechazaAjustesSinCambio() {
        assertThrows(
            ReglaNegocioException.class,
            () -> ServicioAjustesInventario.comprobarSaldo(
                10, 10, 10
            )
        );
    }

    @Test
    void rechazaStockRealNegativo() {
        assertThrows(
            ReglaNegocioException.class,
            () -> ServicioAjustesInventario.comprobarSaldo(
                -1, 10, 15
            )
        );
    }
}
