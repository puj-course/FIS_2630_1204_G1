package com.carestock.service;

import com.carestock.dao.LoteDAO;
import com.carestock.dao.MovimientoStockDAO;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DespachoServiceTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private FakeLoteDAO loteDAO;

    private FakeMovimientoStockDAO
            movimientoStockDAO;

    private RecordingConnectionProvider
            connectionProvider;

    private DespachoService service;

    @BeforeEach
    void setUp() {

        userSession.clearSession();

        loteDAO =
                new FakeLoteDAO();

        movimientoStockDAO =
                new FakeMovimientoStockDAO();

        connectionProvider =
                new RecordingConnectionProvider();

        service =
                new DespachoService(
                        loteDAO,
                        movimientoStockDAO,
                        new SessionContext(),
                        connectionProvider
                );
    }

    @AfterEach
    void tearDown() {

        userSession.clearSession();
    }

    @Test
    void actualizaStockYRegistraKardexEnMismaTransaccion()
            throws Exception {

        autenticarUsuario();

        service.despachar(
                15,
                4
        );

        assertTrue(
                loteDAO.fueInvocado
        );

        assertTrue(
                movimientoStockDAO.fueInvocado
        );

        /*
         * Los dos DAO deben recibir exactamente
         * la misma Connection JDBC.
         */
        assertSame(
                loteDAO.connectionRecibida,
                movimientoStockDAO.connectionRecibida
        );

        assertEquals(
                15,
                loteDAO
                        .despachoPersistido
                        .getIdLote()
        );

        assertEquals(
                4,
                movimientoStockDAO
                        .despachoPersistido
                        .getCantidad()
        );

        assertEquals(
                91,
                movimientoStockDAO
                        .despachoPersistido
                        .getIdUsuario()
        );

        assertEquals(
                1,
                connectionProvider
                        .getCommitCount()
        );

        assertEquals(
                0,
                connectionProvider
                        .getRollbackCount()
        );

        assertTrue(
                connectionProvider.isAutoCommit()
        );
    }

    @Test
    void rollbackCuandoFallaRegistroDeKardex()
            throws Exception {

        autenticarUsuario();

        /*
         * El cambio de stock se ejecutará correctamente,
         * pero el segundo paso fallará.
         */
        movimientoStockDAO.fallarRegistro =
                true;

        assertThrows(
                SQLException.class,
                () -> service.despachar(
                        15,
                        4
                )
        );

        /*
         * El UPDATE/operación de stock sí alcanzó
         * a ejecutarse.
         */
        assertTrue(
                loteDAO.fueInvocado
        );

        /*
         * También se intentó insertar el kardex.
         */
        assertTrue(
                movimientoStockDAO.fueInvocado
        );

        /*
         * Pero no puede existir COMMIT.
         */
        assertEquals(
                0,
                connectionProvider
                        .getCommitCount()
        );

        /*
         * Debe revertirse toda la transacción.
         */
        assertEquals(
                1,
                connectionProvider
                        .getRollbackCount()
        );

        assertTrue(
                connectionProvider.isAutoCommit()
        );
    }

    @Test
    void noRegistraKardexCuandoFallaActualizacionDeStock()
            throws Exception {

        autenticarUsuario();

        loteDAO.fallarDespacho =
                true;

        assertThrows(
                SQLException.class,
                () -> service.despachar(
                        15,
                        4
                )
        );

        assertTrue(
                loteDAO.fueInvocado
        );

        /*
         * Si falla el primer paso, no debe intentarse
         * registrar un movimiento inconsistente.
         */
        assertFalse(
                movimientoStockDAO.fueInvocado
        );

        assertEquals(
                0,
                connectionProvider
                        .getCommitCount()
        );

        assertEquals(
                1,
                connectionProvider
                        .getRollbackCount()
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

        assertFalse(
                movimientoStockDAO.fueInvocado
        );

        assertEquals(
                0,
                connectionProvider
                        .getConnectionCount()
        );
    }

    private void autenticarUsuario() {

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
    }

    private static final class FakeLoteDAO
            extends LoteDAO {

        private boolean fueInvocado;

        private boolean fallarDespacho;

        private Connection connectionRecibida;

        private DespachoLote despachoPersistido;

        @Override
        public void despacharLote(
                Connection connection,
                DespachoLote despacho
        ) throws SQLException {

            fueInvocado =
                    true;

            connectionRecibida =
                    connection;

            if (fallarDespacho) {

                throw new SQLException(
                        "Error simulado al actualizar stock."
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
                    "Esta prueba solo utiliza despachos."
            );
        }
    }

    private static final class FakeMovimientoStockDAO
            extends MovimientoStockDAO {

        private boolean fueInvocado;

        private boolean fallarRegistro;

        private Connection connectionRecibida;

        private DespachoLote despachoPersistido;

        @Override
        public void registrarSalida(
                Connection connection,
                DespachoLote despacho
        ) throws SQLException {

            fueInvocado =
                    true;

            connectionRecibida =
                    connection;

            if (fallarRegistro) {

                throw new SQLException(
                        "Error simulado al registrar kardex."
                );
            }

            despachoPersistido =
                    despacho;
        }
    }
}
