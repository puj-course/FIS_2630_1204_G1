package com.carestock.service;

import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Usuario;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class InventarioCrudServiceScopeTest {

    private final UserSession session =
            UserSession.getInstance();

    private final InventarioCrudService service =
            new InventarioCrudService();


    @AfterEach
    void limpiar() {

        session.clearSession();
    }


    @Test
    void superAdminDebeSeleccionarFarmacia() {

        session.setCurrentUser(
                usuario(
                        1,
                        "SUPER_ADMIN",
                        null
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service
                                .resolverFarmacia(
                                        null
                                )
        );


        assertEquals(
                7,
                service
                        .resolverFarmacia(
                                7
                        )
        );
    }


    @Test
    void administradorQuedaLimitadoASuFarmacia() {

        session.setCurrentUser(
                usuario(
                        2,
                        "ADMINISTRADOR",
                        5
                )
        );


        assertEquals(
                5,
                service
                        .resolverFarmacia(
                                null
                        )
        );


        assertEquals(
                5,
                service
                        .resolverFarmacia(
                                5
                        )
        );


        assertThrows(
                AccesoDenegadoException.class,
                () ->
                        service
                                .resolverFarmacia(
                                        6
                                )
        );
    }


    @Test
    void farmaceuticoNoPuedeGestionarInventario() {

        session.setCurrentUser(
                usuario(
                        3,
                        "FARMACEUTICO",
                        5
                )
        );


        assertThrows(
                AccesoDenegadoException.class,
                () ->
                        service
                                .resolverFarmacia(
                                        5
                                )
        );
    }


    private Usuario usuario(
            int id,
            String rol,
            Integer idFarmacia
    ) {

        return new Usuario(
                id,
                "Usuario Test",
                "test"
                + id
                + "@carestock.local",
                "hash",
                id,
                rol,
                "ACTIVO",
                idFarmacia
        );
    }
}
