package com.carestock.integration;

import com.carestock.controller.SessionController;
import com.carestock.dao.AccesoDAO;
import com.carestock.dao.UsuarioDAO;
import com.carestock.model.Usuario;
import com.carestock.security.AccessControl;
import com.carestock.service.AuthenticationService;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integración del ciclo completo de seguridad de sesión:
 *
 * Login -> Uso autorizado -> Logout ->
 * Intento de reingreso sin autenticación.
 *
 * Los DAOs utilizados son dobles controlados en memoria,
 * por lo que esta prueba no modifica la base de datos real.
 */
class SessionLifecycleIT {

    private static final String EMAIL =
            "integracion@carestock.test";

    private static final String PASSWORD =
            "ClaveSegura123*";

    private static final int ID_USUARIO =
            9001;

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
    void loginUsoLogoutYReingresoNoAutorizado()
            throws SQLException {

        // =====================================================
        // 1. LOGIN
        // =====================================================

        Usuario usuarioPersistido =
                crearUsuarioPersistido();

        FakeUsuarioDAO usuarioDAO =
                new FakeUsuarioDAO(
                        usuarioPersistido
                );

        FakeAccesoDAO accesoDAO =
                new FakeAccesoDAO();

        AuthenticationService authenticationService =
                new AuthenticationService(
                        usuarioDAO,
                        accesoDAO
                );

        Usuario autenticado =
                authenticationService.autenticar(
                        EMAIL,
                        PASSWORD
                );

        /*
         * LoginFX realiza conceptualmente este paso:
         * después de autenticar, registra al usuario en
         * el contexto global de sesión.
         */
        userSession.setCurrentUser(
                autenticado
        );

        assertTrue(
                userSession.isLoggedIn()
        );

        assertTrue(
                AccessControl.hasValidSession()
        );

        assertEquals(
                "EXITOSO",
                accesoDAO.getUltimoResultado()
        );


        // =====================================================
        // 2. USO DE UN RECURSO PROTEGIDO
        // =====================================================

        SessionContext sessionContext =
                new SessionContext();

        int usuarioEnOperacion =
                sessionContext
                        .requireAuthenticatedUserId();

        assertEquals(
                ID_USUARIO,
                usuarioEnOperacion
        );

        /*
         * El usuario autenticado puede superar el guard
         * central de acceso.
         */
        AccessControl.requireAuthenticated();


        // =====================================================
        // 3. LOGOUT
        // =====================================================

        SessionController sessionController =
                new SessionController();

        sessionController.cerrarSesion();

        assertFalse(
                userSession.isLoggedIn()
        );

        assertNull(
                userSession.getCurrentUser()
        );

        assertFalse(
                AccessControl.hasValidSession()
        );


        // =====================================================
        // 4. INTENTO DE REINGRESO NO AUTORIZADO
        // =====================================================

        /*
         * Sin una nueva autenticación, el antiguo contexto
         * no debe poder acceder nuevamente al sistema.
         */
        assertThrows(
                IllegalStateException.class,
                AccessControl::requireAuthenticated
        );

        /*
         * Las operaciones protegidas tampoco deben recuperar
         * el ID del usuario de la sesión destruida.
         */
        assertThrows(
                IllegalStateException.class,
                sessionContext::
                        requireAuthenticatedUserId
        );
    }


    /**
     * Usuario almacenado en el DAO simulado.
     *
     * Se genera un hash BCrypt real para que la integración
     * también atraviese AuthenticationService.
     */
    private Usuario crearUsuarioPersistido() {

        String passwordHash =
                BCrypt.hashpw(
                        PASSWORD,
                        BCrypt.gensalt()
                );

        return new Usuario(
                ID_USUARIO,
                "Usuario Integración",
                EMAIL,
                passwordHash,
                1,
                "ADMINISTRADOR",
                "ACTIVO"
        );
    }


    /**
     * DAO controlado que reemplaza únicamente el acceso
     * físico a PostgreSQL durante esta prueba.
     */
    private static final class FakeUsuarioDAO
            extends UsuarioDAO {

        private final Usuario usuario;

        private FakeUsuarioDAO(
                Usuario usuario
        ) {

            this.usuario =
                    usuario;
        }

        @Override
        public Usuario buscarPorEmail(
                String email
        ) {

            if (
                    email != null
                    && usuario
                            .getEmail()
                            .equalsIgnoreCase(
                                    email.trim()
                            )
            ) {

                return usuario;
            }

            return null;
        }
    }


    /**
     * Auditoría en memoria para evitar escrituras
     * en la base de datos durante la integración.
     */
    private static final class FakeAccesoDAO
            extends AccesoDAO {

        private String ultimoResultado;

        @Override
        public void registrarAcceso(
                int idUsuario,
                String resultado
        ) {

            this.ultimoResultado =
                    resultado;
        }

        private String getUltimoResultado() {

            return ultimoResultado;
        }
    }
}
