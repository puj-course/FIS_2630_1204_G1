package com.carestock.integration;

import com.carestock.config.DatabaseConfig;
import com.carestock.dao.UsuarioDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Usuario;
import com.carestock.service.UsuarioService;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioJerarquiaIT {

    private final UserSession userSession =
            UserSession.getInstance();

    private Integer idFarmaciaPrueba;

    private String emailAdministrador;
    private String emailFarmaceutico;

    @AfterEach
    void limpiar() throws Exception {

        userSession.clearSession();

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            if (emailFarmaceutico != null) {

                eliminarUsuario(
                        conn,
                        emailFarmaceutico
                );
            }

            if (emailAdministrador != null) {

                eliminarUsuario(
                        conn,
                        emailAdministrador
                );
            }

            if (idFarmaciaPrueba != null) {

                try (
                        PreparedStatement stmt =
                                conn.prepareStatement(
                                        "DELETE FROM FARMACIAS "
                                        + "WHERE id_farmacia = ?"
                                )
                ) {

                    stmt.setInt(
                            1,
                            idFarmaciaPrueba
                    );

                    stmt.executeUpdate();
                }
            }
        }
    }

    @Test
    void superAdminCreaAdministradorYAdministradorCreaFarmaceutico()
            throws Exception {

        String sufijo =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8);

        emailAdministrador =
                "admin457." + sufijo
                + "@carestock.test";

        emailFarmaceutico =
                "farmaceutico457." + sufijo
                + "@carestock.test";

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            idFarmaciaPrueba =
                    crearFarmacia(
                            conn,
                            sufijo
                    );
        }


        // =====================================================
        // SUPER_ADMIN
        // =====================================================

        Usuario superAdmin =
                new UsuarioDAO()
                        .buscarPorEmail(
                                "admin@carestock.com"
                        );

        assertTrue(
                superAdmin != null
                && superAdmin.esSuperAdmin()
        );

        userSession.setCurrentUser(
                superAdmin
        );


        int idAdministrador =
                new UsuarioService()
                        .crearUsuario(
                                "Administrador IT",
                                emailAdministrador,
                                "Admin123*",
                                idFarmaciaPrueba
                        );


        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            assertUsuario(
                    conn,
                    idAdministrador,
                    "ADMINISTRADOR",
                    idFarmaciaPrueba,
                    superAdmin.getIdUsuario()
            );
        }


        // =====================================================
        // ADMINISTRADOR DE FARMACIA
        // =====================================================

        Usuario administrador =
                new UsuarioDAO()
                        .buscarPorEmail(
                                emailAdministrador
                        );

        userSession.setCurrentUser(
                administrador
        );


        int idFarmaceutico =
                new UsuarioService()
                        .crearUsuario(
                                "Farmacéutico IT",
                                emailFarmaceutico,
                                "Farm123*",
                                null
                        );


        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            assertUsuario(
                    conn,
                    idFarmaceutico,
                    "FARMACEUTICO",
                    idFarmaciaPrueba,
                    idAdministrador
            );
        }


        // =====================================================
        // FARMACEUTICO NO PUEDE CREAR USUARIOS
        // =====================================================

        Usuario farmaceutico =
                new UsuarioDAO()
                        .buscarPorEmail(
                                emailFarmaceutico
                        );

        userSession.setCurrentUser(
                farmaceutico
        );


        assertThrows(
                AccesoDenegadoException.class,
                () ->
                        new UsuarioService()
                                .crearUsuario(
                                        "Usuario Prohibido",
                                        "prohibido."
                                        + sufijo
                                        + "@carestock.test",
                                        "Password123*",
                                        null
                                )
        );
    }

    private int crearFarmacia(
            Connection conn,
            String sufijo
    ) throws Exception {

        String sql =
                "INSERT INTO FARMACIAS "
                + "(codigo, nombre, estado) "
                + "VALUES (?, ?, 'ACTIVA') "
                + "RETURNING id_farmacia";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    "IT-456-" + sufijo
            );

            stmt.setString(
                    2,
                    "Farmacia IT " + sufijo
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                rs.next();

                return rs.getInt(1);
            }
        }
    }

    private void assertUsuario(
            Connection conn,
            int idUsuario,
            String rolEsperado,
            int farmaciaEsperada,
            int creadorEsperado
    ) throws Exception {

        String sql =
                "SELECT "
                + "r.nombre_rol, "
                + "u.id_farmacia, "
                + "u.id_usuario_creacion "
                + "FROM USUARIOS u "
                + "INNER JOIN ROLES r "
                + "ON r.id_rol = u.id_rol "
                + "WHERE u.id_usuario = ?";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                assertTrue(
                        rs.next()
                );

                assertEquals(
                        rolEsperado,
                        rs.getString(
                                "nombre_rol"
                        )
                );

                assertEquals(
                        farmaciaEsperada,
                        rs.getInt(
                                "id_farmacia"
                        )
                );

                assertEquals(
                        creadorEsperado,
                        rs.getInt(
                                "id_usuario_creacion"
                        )
                );
            }
        }
    }

    private void eliminarUsuario(
            Connection conn,
            String email
    ) throws Exception {

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(
                                "DELETE FROM USUARIOS "
                                + "WHERE LOWER(email) = LOWER(?)"
                        )
        ) {

            stmt.setString(
                    1,
                    email
            );

            stmt.executeUpdate();
        }
    }
}
