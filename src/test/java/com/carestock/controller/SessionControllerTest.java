package com.carestock.controller;

import com.carestock.model.Usuario;
import com.carestock.session.UserSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionControllerTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private SessionController sessionController;

    @BeforeEach
    void setUp() {

        userSession.clearSession();

        sessionController =
                new SessionController(userSession);
    }

    @AfterEach
    void tearDown() {
        userSession.clearSession();
    }

    @Test
    void cerrarSesionEliminaUsuarioActivo() {

        Usuario usuario =
                new Usuario(
                        99,
                        "Usuario Prueba",
                        "usuario@carestock.com",
                        "hash-no-utilizado",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(usuario);

        assertTrue(userSession.isLoggedIn());

        sessionController.cerrarSesion();

        assertFalse(userSession.isLoggedIn());
    }
}
