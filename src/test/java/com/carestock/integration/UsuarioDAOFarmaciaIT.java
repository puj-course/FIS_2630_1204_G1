package com.carestock.integration;

import com.carestock.config.DatabaseConfig;
import com.carestock.dao.UsuarioDAO;
import com.carestock.model.Usuario;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioDAOFarmaciaIT {

    private final UserSession userSession =
            UserSession.getInstance();

    @AfterEach
    void tearDown() {

        userSession.clearSession();
    }

    @Test
    void cargaFarmaciaDesdePostgresYLaConservaEnSesion()
            throws Exception {

        String email;
        int idFarmaciaEsperada;

        String sql =
                "SELECT email, id_farmacia "
                + "FROM USUARIOS "
                + "WHERE estado = 'ACTIVO' "
                + "AND id_farmacia IS NOT NULL "
                + "ORDER BY id_usuario "
                + "LIMIT 1";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()
        ) {

            assertTrue(
                    rs.next(),
                    "Debe existir al menos un usuario "
                    + "activo asociado a una farmacia."
            );

            email =
                    rs.getString(
                            "email"
                    );

            idFarmaciaEsperada =
                    rs.getInt(
                            "id_farmacia"
                    );
        }

        Usuario usuario =
                new UsuarioDAO()
                        .buscarPorEmail(
                                email
                        );

        assertNotNull(
                usuario
        );

        assertEquals(
                idFarmaciaEsperada,
                usuario.getIdFarmacia()
        );

        userSession.setCurrentUser(
                usuario
        );

        assertEquals(
                idFarmaciaEsperada,
                userSession
                        .getCurrentUser()
                        .getIdFarmacia()
        );

        assertEquals(
                idFarmaciaEsperada,
                new SessionContext()
                        .requireAuthenticatedPharmacyId()
        );
    }
}
