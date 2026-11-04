package org.example.carestock.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionFormularioLoginSonarTest {

    @Test
    void admiteCorreosConSubdominios() {
        assertEquals("",
                ValidacionFormularioLogin.errorCorreo(
                        "usuario@laboratorio.edu.co"));
    }

    @Test
    void rechazaDominioConPuntosConsecutivos() {
        assertFalse(
                ValidacionFormularioLogin.errorCorreo(
                        "usuario@dominio..com").isEmpty());
    }

    @Test
    void rechazaCorreoMalformadoMuyLargo() {
        String correo = "usuario@"
                + "dominio.".repeat(10000)
                + ".com";

        assertFalse(
                ValidacionFormularioLogin.errorCorreo(
                        correo).isEmpty());
    }
}
