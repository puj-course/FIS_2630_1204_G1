package com.carestock.service;

import com.carestock.dao.LoteDAOContract;
import com.carestock.model.DespachoLote;
import com.carestock.model.Lote;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.model.Usuario;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;
import com.carestock.testutil.RecordingConnectionProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngresoLoteServiceTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private FakeLoteDAO loteDAO;

    private RecordingConnectionProvider
            connectionProvider;

    private IngresoLoteService service;

    private final Medicamento medicamento =
            new Medicamento(
                    7L,
                    "INVIMA-TEST",
                    "Medicamento prueba",
                    "Principio activo",
                    "500 mg",
                    "ANALGESICOS",
                    0,
                    10
            );

    private final Ubicacion ubicacion =
            new Ubicacion(
                    3,
                    "A",
                    "1",
                    "Bodega"
            );

    @BeforeEach
    void setUp() {

        userSession.clearSession();

        loteDAO =
                new FakeLoteDAO();

        connectionProvider =
                new RecordingConnectionProvider();

        service =
                new IngresoLoteService(
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
    void inyectaUsuarioDeSesionAntesDePersistir()
            throws SQLException {

        userSession.setCurrentUser(
                new Usuario(
                        84,
                        "Usuario Prueba",
                        "prueba@carestock.com",
                        "hash-no-utilizado",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                )
        );

        service.registrar(
                medicamento,
                "LOT-2026-001",
                "25",
                LocalDate.now()
                        .plusMonths(6),
                ubicacion
        );

        assertTrue(
                loteDAO.fueInvocado
        );

        assertEquals(
                84,
                loteDAO
                        .lotePersistido
                        .getIdUsuario()
        );

        assertEquals(
                "LOT-2026-001",
                loteDAO
                        .lotePersistido
                        .getNumeroLote()
        );

        assertEquals(
                1,
                connectionProvider.getCommitCount()
        );

        assertEquals(
                0,
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
    void ejecutaRollbackCuandoFallaRegistroDeLote()
            throws SQLException {

        userSession.setCurrentUser(
                new Usuario(
                        84,
                        "Usuario Prueba",
                        "prueba@carestock.com",
                        "hash-no-utilizado",
                        1,
                        "ADMINISTRADOR",
                        "ACTIVO"
                )
        );

        loteDAO.fallarRegistro = true;

        assertThrows(
                SQLException.class,
                () -> service.registrar(
                        medicamento,
                        "LOT-2026-ERROR",
                        "25",
                        LocalDate.now()
                                .plusMonths(6),
                        ubicacion
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
    void noInvocaPersistenciaSinSesionAutenticada() {

        assertThrows(
                IllegalStateException.class,
                () -> service.registrar(
                        medicamento,
                        "LOT-2026-002",
                        "10",
                        LocalDate.now()
                                .plusMonths(6),
                        ubicacion
                )
        );

        assertFalse(
                loteDAO.fueInvocado
        );

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

        private boolean fallarRegistro;

        private Lote lotePersistido;

        @Override
        public void registrarNuevoLote(
                Connection connection,
                Lote lote
        ) throws SQLException {

            fueInvocado = true;

            if (fallarRegistro) {
                throw new SQLException(
                        "Error simulado durante el registro del lote."
                );
            }

            lotePersistido =
                    lote;
        }

        @Override
        public void despacharLote(
                Connection connection,
                DespachoLote despacho
        ) {
            throw new UnsupportedOperationException(
                    "Esta prueba solo utiliza operaciones de ingreso."
            );
        }
    }
}
