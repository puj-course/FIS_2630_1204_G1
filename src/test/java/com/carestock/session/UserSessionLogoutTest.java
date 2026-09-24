package com.carestock.session;

import com.carestock.controller.SessionController;
import com.carestock.model.Usuario;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSessionLogoutTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private final SessionController sessionController =
            new SessionController();

    @BeforeEach
    void setUp() {

        userSession.clearSession();
    }

    @AfterEach
    void tearDown() {

        userSession.clearSession();
    }

    @Test
    void logoutLimpiaCompletamenteLaSesionActiva() {

        Usuario usuario =
                crearUsuarioPrueba();

        userSession.setCurrentUser(
                usuario
        );

        assertTrue(
                userSession.isLoggedIn()
        );

        assertEquals(
                usuario.getIdUsuario(),
                userSession
                        .getCurrentUser()
                        .getId()
        );

        assertEquals(
                usuario.getEmail(),
                userSession
                        .getCurrentUser()
                        .getEmail()
        );

        assertEquals(
                usuario.getNombreRol(),
                userSession
                        .getCurrentUser()
                        .getRol()
        );

        /*
         * El logout debe pasar por el controlador oficial
         * de sesión y terminar invocando clearSession().
         */
        sessionController.cerrarSesion();

        assertFalse(
                userSession.isLoggedIn()
        );

        assertNull(
                userSession.getCurrentUser()
        );
    }

    @Test
    void clearSessionEsIdempotente() {

        userSession.setCurrentUser(
                crearUsuarioPrueba()
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
    void singletonPermanecePeroSinUsuarioDespuesDelLogout() {

        UserSession primeraReferencia =
                UserSession.getInstance();

        primeraReferencia.setCurrentUser(
                crearUsuarioPrueba()
        );

        sessionController.cerrarSesion();

        UserSession segundaReferencia =
                UserSession.getInstance();

        /*
         * UserSession es Singleton: la instancia permanece.
         * Lo que debe destruirse es el contexto autenticado.
         */
        assertTrue(
                primeraReferencia
                == segundaReferencia
        );

        assertNull(
                segundaReferencia.getCurrentUser()
        );

        assertFalse(
                segundaReferencia.isLoggedIn()
        );
    }

    private Usuario crearUsuarioPrueba() {

        return new Usuario(
                501,
                "Usuario Prueba Sesión",
                "sesion@carestock.test",
                "hash-no-almacenado-en-user-session",
                1,
                "ADMINISTRADOR",
                "ACTIVO"
        );
    }
}
