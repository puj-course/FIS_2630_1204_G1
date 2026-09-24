package com.carestock.service;

import com.carestock.dao.LoteDAO;
import com.carestock.model.Lote;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.model.Usuario;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        userSession.cleanUserSession();

        loteDAO =
                new FakeLoteDAO();

        service =
                new IngresoLoteService(
                        loteDAO,
                        new SessionContext()
                );
    }

    @AfterEach
    void tearDown() {

        userSession.cleanUserSession();
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
    }

    private static final class FakeLoteDAO
            extends LoteDAO {

        private boolean fueInvocado;

        private Lote lotePersistido;

        @Override
        public void registrarNuevoLote(
                Lote lote
        ) {

            this.fueInvocado = true;
            this.lotePersistido = lote;
        }
    }
}
