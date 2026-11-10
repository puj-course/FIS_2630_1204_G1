package org.example.carestock.service;

import org.example.carestock.dao.UsuarioDAO;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceCoberturaAdicionalTest {

    private AuthenticationService servicio(UsuarioDAO dao)
            throws Exception {

        AuthenticationService autenticacion =
                new AuthenticationService();

        Field campo = AuthenticationService.class
                .getDeclaredField("usuarioDAO");

        campo.setAccessible(true);
        campo.set(autenticacion, dao);

        return autenticacion;
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "99"})
    void rechazaHashConCostoBcryptInvalido(String costo)
            throws Exception {

        UsuarioDAO dao = mock(UsuarioDAO.class);

        String hash = "$2a$" + costo + "$" + "A".repeat(53);
        assertEquals(60, hash.length());

        Usuario usuario = new Usuario(
                7, "Ana", "ana@test.local",
                hash, "FARMACEUTICO");

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(usuario);

        AuthenticationService autenticacion = servicio(dao);

        assertThrows(ReglaNegocioException.class,
                () -> autenticacion.autenticar(
                        "ana@test.local", "Clave#2026"));

        assertEquals(hash, usuario.getPassword(),
                "Una autenticacion fallida no debe modificar el hash");
    }
}
