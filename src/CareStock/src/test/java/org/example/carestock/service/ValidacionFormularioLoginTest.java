package org.example.carestock.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionFormularioLoginTest {

    @Test
    void correoVacioRechazado() {
        assertFalse(ValidacionFormularioLogin
                .errorCorreo(null).isEmpty());

        assertFalse(ValidacionFormularioLogin
                .errorCorreo(" ").isEmpty());
    }

    @Test
    void correoMalFormadoRechazado() {
        assertFalse(ValidacionFormularioLogin
                .errorCorreo("usuario").isEmpty());

        assertFalse(ValidacionFormularioLogin
                .errorCorreo("usuario@dominio").isEmpty());

        assertFalse(ValidacionFormularioLogin
                .errorCorreo("usuario @dominio.com").isEmpty());
    }

    @Test
    void correoCorrectoAceptado() {
        assertEquals("",
                ValidacionFormularioLogin
                        .errorCorreo("ana@carestock.com"));

        assertEquals("",
                ValidacionFormularioLogin
                        .errorCorreo("  ana@carestock.com  "));
    }

    @Test
    void contrasenaObligatoria() {
        assertFalse(ValidacionFormularioLogin
                .errorContrasena(null).isEmpty());

        assertFalse(ValidacionFormularioLogin
                .errorContrasena("").isEmpty());

        assertFalse(ValidacionFormularioLogin
                .errorContrasena("  ").isEmpty());

        assertEquals("",
                ValidacionFormularioLogin
                        .errorContrasena("cualquier-clave"));
    }
}
