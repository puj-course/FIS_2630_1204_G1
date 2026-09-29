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
                new SessionContext(
                        userSession
                );
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
                        "ACTIVO",
                        7
                );

        userSession.setCurrentUser(
                usuario
        );

        assertEquals(
                42,
                sessionContext
                        .requireAuthenticatedUserId()
        );
    }

    @Test
    void conservaYExponeIdFarmaciaDelUsuarioAutenticado() {

        Usuario usuario =
                new Usuario(
                        42,
                        "Usuario Prueba",
                        "usuario@carestock.com",
                        "hash-no-utilizado",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO",
                        7
                );

        userSession.setCurrentUser(
                usuario
        );

        assertEquals(
                7,
                userSession
                        .getCurrentUser()
                        .getIdFarmacia()
        );

        assertEquals(
                7,
                sessionContext
                        .requireAuthenticatedPharmacyId()
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

    @Test
    void bloqueaAccesoAFarmaciaCuandoNoExisteSesion() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        sessionContext::
                                requireAuthenticatedPharmacyId
                );

        assertEquals(
                SessionContext.ERROR_SESION_REQUERIDA,
                exception.getMessage()
        );
    }

    @Test
    void bloqueaAccesoCuandoUsuarioNoTieneFarmacia() {

        Usuario usuario =
                new Usuario(
                        42,
                        "Usuario Sin Farmacia",
                        "sin.farmacia@carestock.com",
                        "hash-no-utilizado",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuario
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        sessionContext::
                                requireAuthenticatedPharmacyId
                );

        assertEquals(
                SessionContext.ERROR_FARMACIA_REQUERIDA,
                exception.getMessage()
        );
    }
}
