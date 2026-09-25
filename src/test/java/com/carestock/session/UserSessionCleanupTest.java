package com.carestock.session;

import com.carestock.model.Usuario;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSessionCleanupTest {

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
    void clearSessionEliminaCompletamenteElUsuarioActivo() {

        Usuario usuario =
                new Usuario(
                        101,
                        "Usuario Anterior",
                        "anterior@carestock.com",
                        "hash-no-almacenado-en-sesion",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuario
        );

        UserSession.CurrentUser usuarioSesion =
                userSession.getCurrentUser();

        assertTrue(
                userSession.isLoggedIn()
        );

        assertEquals(
                101,
                usuarioSesion.getId()
        );

        assertEquals(
                "Usuario Anterior",
                usuarioSesion.getNombre()
        );

        assertEquals(
                "anterior@carestock.com",
                usuarioSesion.getEmail()
        );

        assertEquals(
                "ADMINISTRADOR",
                usuarioSesion.getRol()
        );

        userSession.clearSession();

        assertFalse(
                userSession.isLoggedIn()
        );

        assertNull(
                userSession.getCurrentUser()
        );
    }

    @Test
    void clearSessionEsIdempotente() {

        assertFalse(
                userSession.isLoggedIn()
        );

        userSession.clearSession();
        userSession.clearSession();

        assertFalse(
                userSession.isLoggedIn()
        );

        assertNull(
                userSession.getCurrentUser()
        );
    }

    @Test
    void contextoQuedaInvalidadoDespuesDelLogout() {

        Usuario usuario =
                new Usuario(
                        101,
                        "Usuario Prueba",
                        "usuario@carestock.com",
                        "hash-no-almacenado-en-sesion",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuario
        );

        SessionContext sessionContext =
                new SessionContext(
                        userSession
                );

        assertEquals(
                101,
                sessionContext
                        .requireAuthenticatedUserId()
        );

        userSession.clearSession();

        assertThrows(
                IllegalStateException.class,
                sessionContext::
                        requireAuthenticatedUserId
        );
    }

    @Test
    void nuevaSesionNoConservaDatosDeLaSesionAnterior() {

        Usuario usuarioAnterior =
                new Usuario(
                        101,
                        "Usuario Anterior",
                        "anterior@carestock.com",
                        "hash-anterior",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuarioAnterior
        );

        userSession.clearSession();

        assertNull(
                userSession.getCurrentUser()
        );

        Usuario usuarioNuevo =
                new Usuario(
                        202,
                        "Usuario Nuevo",
                        "nuevo@carestock.com",
                        "hash-nuevo",
                        2,
                        "FARMACEUTICO",
                        "ACTIVO"
                );

        userSession.setCurrentUser(
                usuarioNuevo
        );

        UserSession.CurrentUser actual =
                userSession.getCurrentUser();

        assertEquals(
                202,
                actual.getId()
        );

        assertEquals(
                "Usuario Nuevo",
                actual.getNombre()
        );

        assertEquals(
                "nuevo@carestock.com",
                actual.getEmail()
        );

        assertEquals(
                "FARMACEUTICO",
                actual.getRol()
        );
    }
}
