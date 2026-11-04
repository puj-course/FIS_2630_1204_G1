package org.example.carestock.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionFormularioLoginCasosLimiteTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "usuario@dominio.co",
            "nombre.apellido@institucion.edu.co",
            "usuario+etiqueta@dominio.com",
            " Usuario@DOMINIO.COM "
    })
    void aceptaCorreosConFormatoValido(String correo) {
        assertEquals("",
                ValidacionFormularioLogin.errorCorreo(correo));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sin-arroba",
            "usuario@",
            "@dominio.com",
            "usuario@@dominio.com",
            "usuario@dominio",
            "usuario@dominio.",
            "usuario @dominio.com",
            "usuario@dominio .com",
            "dos\nlineas@dominio.com"
    })
    void rechazaCorreosMalFormados(String correo) {
        assertFalse(
                ValidacionFormularioLogin
                        .errorCorreo(correo).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void rechazaContrasenasVacias(String clave) {
        assertFalse(
                ValidacionFormularioLogin
                        .errorContrasena(clave).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "Clave#2026",
            " clave con espacios ",
            "ContrasenaConCaracteres123!"
    })
    void noAlteraContrasenasIngresadas(String clave) {
        assertEquals("",
                ValidacionFormularioLogin
                        .errorContrasena(clave));
    }
}
