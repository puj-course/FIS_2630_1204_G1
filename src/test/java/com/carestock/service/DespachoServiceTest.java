package com.carestock.service;

import com.carestock.dao.LoteDAO;
import com.carestock.model.DespachoLote;
import com.carestock.model.Usuario;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DespachoServiceTest {

    private final UserSession userSession =
            UserSession.getInstance();

    private FakeLoteDAO loteDAO;

    private DespachoService service;

    @BeforeEach
    void setUp() {

        userSession.cleanUserSession();

        loteDAO =
                new FakeLoteDAO();

        service =
                new DespachoService(
                        loteDAO,
                        new SessionContext()
                );
    }

    @AfterEach
    void tearDown() {

        userSession.cleanUserSession();
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
    }

    private static final class FakeLoteDAO
            extends LoteDAO {

        private boolean fueInvocado;

        private DespachoLote despachoPersistido;

        @Override
        public void despacharLote(
                DespachoLote despacho
        ) {

            this.fueInvocado = true;
            this.despachoPersistido = despacho;
        }
    }
}
