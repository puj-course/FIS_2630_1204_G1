package com.carestock.session;

import com.carestock.model.Usuario;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionContextFarmaciaTest {

    private final UserSession userSession =
            UserSession.getInstance();

    @AfterEach
    void limpiar() {
        userSession.clearSession();
    }

    @Test
    void administradorConservaFarmacia() {

        Usuario usuario =
                new Usuario(
                        10,
                        "Administrador Farmacia",
                        "admin.farmacia@test.com",
                        "hash",
                        2,
                        "ADMINISTRADOR",
                        "ACTIVO",
                        7
                );

        userSession.setCurrentUser(
                usuario
        );

        assertEquals(
                7,
                new SessionContext()
                        .requireAuthenticatedPharmacyId()
        );
    }

    @Test
    void superAdminPuedeExistirSinFarmacia() {

        Usuario usuario =
                new Usuario(
                        1,
                        "Admin CareStock",
                        "admin@carestock.com",
                        "hash",
                        1,
                        "SUPER_ADMIN",
                        "ACTIVO",
                        null
                );

        userSession.setCurrentUser(
                usuario
        );

        assertEquals(
                1,
                new SessionContext()
                        .requireAuthenticatedUserId()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        new SessionContext()
                                .requireAuthenticatedPharmacyId()
        );
    }
}
