package com.carestock.security;

import com.carestock.model.Usuario;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessControlTest {

    private final UserSession userSession =
            UserSession.getInstance();

    @BeforeEach
    void setUp() {

        userSession.clearSession();
    }

    @AfterEach
    void tearDown() {

        userSession.clearSession();
    }

    @Test
    void deniegaAccesoCuandoNoExisteSesion() {

        assertFalse(
                AccessControl.hasValidSession()
        );
    }

    @Test
    void permiteAccesoCuandoExisteSesionValida() {

        Usuario usuario =
                new Usuario(
                        101,
                        "Usuario Seguro",
                        "usuario@carestock.com",
                        "hash-no-almacenado-en-sesion",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuario
        );

        assertTrue(
                AccessControl.hasValidSession()
        );
    }

    @Test
    void requireAuthenticatedBloqueaSinSesion() {

        assertThrows(
                IllegalStateException.class,
                AccessControl::requireAuthenticated
        );
    }

    @Test
    void sesionIncompletaNoSeConsideraValida() {

        Usuario usuarioIncompleto =
                new Usuario(
                        101,
                        "Usuario Incompleto",
                        "",
                        "hash-no-almacenado-en-sesion",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuarioIncompleto
        );

        assertFalse(
                AccessControl.hasValidSession()
        );
    }

    @Test
    void cierreDeSesionRevocaAccesoProtegido() {

        Usuario usuario =
                new Usuario(
                        101,
                        "Usuario Seguro",
                        "usuario@carestock.com",
                        "hash-no-almacenado-en-sesion",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuario
        );

        assertTrue(
                AccessControl.hasValidSession()
        );

        userSession.clearSession();

        assertFalse(
                AccessControl.hasValidSession()
        );
    }
}
