package com.carestock.service;

import com.carestock.dao.UsuarioDAO;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordChangeServiceTest {

    private static final int ID_USUARIO = 501;

    private static final String PASSWORD_ACTUAL =
            "Actual123*";

    private static final String PASSWORD_NUEVA =
            "Nueva456*";


    @Test
    void cambiaPasswordCuandoActualEsCorrecta()
            throws SQLException {

        FakeUsuarioDAO dao =
                new FakeUsuarioDAO(
                        BCrypt.hashpw(
                                PASSWORD_ACTUAL,
                                BCrypt.gensalt()
                        )
                );

        PasswordChangeService service =
                new PasswordChangeService(
                        dao
                );

        PasswordChangeService.ResultadoCambio resultado =
                service.cambiarPassword(
                        ID_USUARIO,
                        PASSWORD_ACTUAL,
                        PASSWORD_NUEVA,
                        PASSWORD_NUEVA
                );

        assertEquals(
                PasswordChangeService
                        .ResultadoCambio
                        .EXITOSO,
                resultado
        );

        assertTrue(
                dao.updateCalled
        );

        assertNotEquals(
                PASSWORD_NUEVA,
                dao.nuevoHash
        );

        assertTrue(
                BCrypt.checkpw(
                        PASSWORD_NUEVA,
                        dao.nuevoHash
                )
        );
    }


    @Test
    void rechazaPasswordActualIncorrecta()
            throws SQLException {

        FakeUsuarioDAO dao =
                new FakeUsuarioDAO(
                        BCrypt.hashpw(
                                PASSWORD_ACTUAL,
                                BCrypt.gensalt()
                        )
                );

        PasswordChangeService service =
                new PasswordChangeService(
                        dao
                );

        PasswordChangeService.ResultadoCambio resultado =
                service.cambiarPassword(
                        ID_USUARIO,
                        "Incorrecta123*",
                        PASSWORD_NUEVA,
                        PASSWORD_NUEVA
                );

        assertEquals(
                PasswordChangeService
                        .ResultadoCambio
                        .ACTUAL_INCORRECTA,
                resultado
        );

        assertFalse(
                dao.updateCalled
        );
    }


    @Test
    void rechazaNuevasPasswordsDiferentes()
            throws SQLException {

        FakeUsuarioDAO dao =
                new FakeUsuarioDAO(
                        BCrypt.hashpw(
                                PASSWORD_ACTUAL,
                                BCrypt.gensalt()
                        )
                );

        PasswordChangeService service =
                new PasswordChangeService(
                        dao
                );

        PasswordChangeService.ResultadoCambio resultado =
                service.cambiarPassword(
                        ID_USUARIO,
                        PASSWORD_ACTUAL,
                        "Nueva123*",
                        "Otra123*"
                );

        assertEquals(
                PasswordChangeService
                        .ResultadoCambio
                        .NUEVAS_NO_COINCIDEN,
                resultado
        );

        assertFalse(
                dao.updateCalled
        );
    }


    @Test
    void rechazaPasswordNuevaDemasiadoCorta()
            throws SQLException {

        FakeUsuarioDAO dao =
                new FakeUsuarioDAO(
                        BCrypt.hashpw(
                                PASSWORD_ACTUAL,
                                BCrypt.gensalt()
                        )
                );

        PasswordChangeService service =
                new PasswordChangeService(
                        dao
                );

        PasswordChangeService.ResultadoCambio resultado =
                service.cambiarPassword(
                        ID_USUARIO,
                        PASSWORD_ACTUAL,
                        "123",
                        "123"
                );

        assertEquals(
                PasswordChangeService
                        .ResultadoCambio
                        .NUEVA_DEMASIADO_CORTA,
                resultado
        );

        assertFalse(
                dao.updateCalled
        );
    }


    @Test
    void nuncaEntregaPasswordNuevaEnTextoPlanoAlDAO()
            throws SQLException {

        FakeUsuarioDAO dao =
                new FakeUsuarioDAO(
                        BCrypt.hashpw(
                                PASSWORD_ACTUAL,
                                BCrypt.gensalt()
                        )
                );

        PasswordChangeService service =
                new PasswordChangeService(
                        dao
                );

        service.cambiarPassword(
                ID_USUARIO,
                PASSWORD_ACTUAL,
                PASSWORD_NUEVA,
                PASSWORD_NUEVA
        );

        assertNotEquals(
                PASSWORD_NUEVA,
                dao.nuevoHash
        );

        assertTrue(
                dao.nuevoHash.startsWith("$2")
        );
    }


    private static final class FakeUsuarioDAO
            extends UsuarioDAO {

        private final String hashActual;

        private boolean updateCalled;

        private String nuevoHash;


        private FakeUsuarioDAO(
                String hashActual
        ) {

            this.hashActual =
                    hashActual;
        }


        @Override
        public String obtenerPasswordHashActivoPorId(
                int idUsuario
        ) {

            return hashActual;
        }


        @Override
        public boolean actualizarPasswordHash(
                int idUsuario,
                String hashActualEsperado,
                String nuevoHash
        ) {

            this.updateCalled =
                    true;

            this.nuevoHash =
                    nuevoHash;

            return hashActual.equals(
                    hashActualEsperado
            );
        }
    }
}
