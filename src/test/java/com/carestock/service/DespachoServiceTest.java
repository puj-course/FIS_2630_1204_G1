package com.carestock.service;

import com.carestock.dao.LoteDAOContract;
import com.carestock.model.DespachoLote;
import com.carestock.model.Lote;
import com.carestock.model.Usuario;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;
import com.carestock.testutil.RecordingConnectionProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DespachoServiceTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private FakeLoteDAO loteDAO;

    private RecordingConnectionProvider
            connectionProvider;

    private DespachoService service;

    @BeforeEach
    void setUp() {

        userSession.clearSession();

        loteDAO =
                new FakeLoteDAO();

        connectionProvider =
                new RecordingConnectionProvider();

        service =
                new DespachoService(
                        loteDAO,
                        new SessionContext(),
                        connectionProvider
                );
    }

    @AfterEach
    void tearDown() {

        userSession.clearSession();
    }

    @Test
    void inyectaUsuarioDeSesionEnDespacho()
            throws Exception {

        userSession.setCurrentUser(
                new Usuario(
                        91,
                        "Usuario Despacho",
                        "despacho@carestock.com",
                        "hash-no-utilizado",
                        2,
                        "FARMACEUTICO",
                        "ACTIVO"
                )
        );

        service.despachar(
                15,
                4
        );

        assertTrue(
                loteDAO.fueInvocado
        );

        assertEquals(
                15,
                loteDAO
                        .despachoPersistido
                        .getIdLote()
        );

        assertEquals(
                4,
                loteDAO
                        .despachoPersistido
                        .getCantidad()
        );

        assertEquals(
                91,
                loteDAO
                        .despachoPersistido
                        .getIdUsuario()
        );

        /*
         * La operación exitosa debe confirmar exactamente
         * una transacción.
         */
        assertEquals(
                1,
                connectionProvider.getCommitCount()
        );

        assertEquals(
                0,
                connectionProvider.getRollbackCount()
        );

        /*
         * El auto-commit debe quedar restaurado.
         */
        assertTrue(
                connectionProvider.isAutoCommit()
        );

        assertTrue(
                connectionProvider.isClosed()
        );
    }

    @Test
    void ejecutaRollbackCuandoFallaDespacho()
            throws Exception {

        userSession.setCurrentUser(
                new Usuario(
                        91,
                        "Usuario Despacho",
                        "despacho@carestock.com",
                        "hash-no-utilizado",
                        2,
                        "FARMACEUTICO",
                        "ACTIVO"
                )
        );

        loteDAO.fallarDespacho = true;

        assertThrows(
                SQLException.class,
                () -> service.despachar(
                        15,
                        4
                )
        );

        assertEquals(
                0,
                connectionProvider.getCommitCount()
        );

        assertEquals(
                1,
                connectionProvider.getRollbackCount()
        );

        assertTrue(
                connectionProvider.isAutoCommit()
        );

        assertTrue(
                connectionProvider.isClosed()
        );
    }

    @Test
    void bloqueaDespachoSinSesion() {

        assertThrows(
                IllegalStateException.class,
                () -> service.despachar(
                        15,
                        4
                )
        );

        assertFalse(
                loteDAO.fueInvocado
        );

        /*
         * Sin sesión ni siquiera debe solicitarse
         * una conexión JDBC.
         */
        assertEquals(
                0,
                connectionProvider.getConnectionCount()
        );

        assertEquals(
                0,
                connectionProvider.getCommitCount()
        );

        assertEquals(
                0,
                connectionProvider.getRollbackCount()
        );
    }

    private static final class FakeLoteDAO
            implements LoteDAOContract {

        private boolean fueInvocado;

        private boolean fallarDespacho;

        private DespachoLote despachoPersistido;

        @Override
        public void despacharLote(
                Connection connection,
                DespachoLote despacho
        ) throws SQLException {

            fueInvocado = true;

            if (fallarDespacho) {
                throw new SQLException(
                        "Error simulado durante el despacho."
                );
            }

            despachoPersistido =
                    despacho;
        }

        @Override
        public void registrarNuevoLote(
                Connection connection,
                Lote lote
        ) {
            throw new UnsupportedOperationException(
                    "Esta prueba solo utiliza operaciones de despacho."
            );
        }
    }
}
