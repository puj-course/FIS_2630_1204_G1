package com.carestock.session;

import com.carestock.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionContextTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private SessionContext sessionContext;

    @BeforeEach
    void setUp() {

        userSession.clearSession();

        sessionContext =
                new SessionContext(userSession);
    }

    @AfterEach
    void tearDown() {

        userSession.clearSession();
    }

    @Test
    void extraeIdDelUsuarioAutenticado() {

        Usuario usuario =
                new Usuario(
                        42,
                        "Usuario Prueba",
                        "usuario@carestock.com",
                        "hash-no-utilizado",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(usuario);

        assertEquals(
                42,
                sessionContext
                        .requireAuthenticatedUserId()
        );
    }

    @Test
    void bloqueaOperacionCuandoNoExisteUsuarioAutenticado() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        sessionContext::
                                requireAuthenticatedUserId
                );

        assertEquals(
                SessionContext.ERROR_SESION_REQUERIDA,
                exception.getMessage()
        );
    }
}
