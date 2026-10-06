package com.carestock.integration;

import com.carestock.config.DatabaseConfig;
import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;
import com.carestock.model.Usuario;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioSegregacionFarmaciasIT {

    private final MedicamentoDAO medicamentoDAO =
            new MedicamentoDAO();

    private final UserSession session =
            UserSession.getInstance();

    private String sufijo;

    private int idFarmaciaA;
    private int idFarmaciaB;
    private int idFarmaciaC;
    private int idCategoria;

    private Integer idUsuarioExistente;


    @BeforeEach
    void setUp() throws Exception {

        session.clearSession();

        sufijo =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8);

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            idFarmaciaA =
                    insertarFarmacia(
                            conn,
                            "IT-460-A-" + sufijo,
                            "Farmacia A Issue 460"
                    );

            idFarmaciaB =
                    insertarFarmacia(
                            conn,
                            "IT-460-B-" + sufijo,
                            "Farmacia B Issue 460"
                    );

            idFarmaciaC =
                    insertarFarmacia(
                            conn,
                            "IT-460-C-" + sufijo,
                            "Farmacia C Issue 460"
                    );

            idCategoria =
                    insertarCategoria(
                            conn,
                            "CAT-460-" + sufijo
                    );

            idUsuarioExistente =
                    obtenerUsuarioExistente(
                            conn
                    );

            insertarMedicamento(
                    conn,
                    idFarmaciaA,
                    "A1",
                    "INV-A1-" + sufijo
            );

            insertarMedicamento(
                    conn,
                    idFarmaciaA,
                    "A2",
                    "INV-A2-" + sufijo
            );

            insertarMedicamento(
                    conn,
                    idFarmaciaB,
                    "B1",
                    "INV-B1-" + sufijo
            );

            insertarMedicamento(
                    conn,
                    idFarmaciaB,
                    "B2",
                    "INV-B2-" + sufijo
            );

            /*
             * Farmacia C se deja intencionalmente
             * sin medicamentos.
             */
        }
    }


    @AfterEach
    void tearDown() throws Exception {

        session.clearSession();

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    "DELETE FROM MEDICAMENTOS "
                                    + "WHERE id_farmacia IN (?, ?, ?)"
                            )
            ) {

                stmt.setInt(1, idFarmaciaA);
                stmt.setInt(2, idFarmaciaB);
                stmt.setInt(3, idFarmaciaC);

                stmt.executeUpdate();
            }


            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    "DELETE FROM CATEGORIAS "
                                    + "WHERE id_categoria = ?"
                            )
            ) {

                stmt.setInt(
                        1,
                        idCategoria
                );

                stmt.executeUpdate();
            }


            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    "DELETE FROM FARMACIAS "
                                    + "WHERE id_farmacia IN (?, ?, ?)"
                            )
            ) {

                stmt.setInt(1, idFarmaciaA);
                stmt.setInt(2, idFarmaciaB);
                stmt.setInt(3, idFarmaciaC);

                stmt.executeUpdate();
            }
        }
    }


    @Test
    void usuarioASoloVisualizaInventarioFarmaciaA()
            throws Exception {

        session.setCurrentUser(
                usuario(
                        4601,
                        "Usuario A",
                        idFarmaciaA
                )
        );

        List<Medicamento> inventario =
                inventarioSesionActual();

        Set<String> nombres =
                nombres(inventario);

        assertEquals(
                2,
                inventario.size()
        );

        assertTrue(
                nombres.contains("A1")
        );

        assertTrue(
                nombres.contains("A2")
        );

        assertFalse(
                nombres.contains("B1")
        );

        assertFalse(
                nombres.contains("B2")
        );
    }


    @Test
    void usuarioBSoloVisualizaInventarioFarmaciaB()
            throws Exception {

        session.setCurrentUser(
                usuario(
                        4602,
                        "Usuario B",
                        idFarmaciaB
                )
        );

        List<Medicamento> inventario =
                inventarioSesionActual();

        Set<String> nombres =
                nombres(inventario);

        assertEquals(
                2,
                inventario.size()
        );

        assertTrue(
                nombres.contains("B1")
        );

        assertTrue(
                nombres.contains("B2")
        );

        assertFalse(
                nombres.contains("A1")
        );

        assertFalse(
                nombres.contains("A2")
        );
    }


    @Test
    void cambiarUsuarioCambiaAutomaticamenteInventario()
            throws Exception {

        session.setCurrentUser(
                usuario(
                        4601,
                        "Usuario A",
                        idFarmaciaA
                )
        );

        Set<String> inventarioA =
                nombres(
                        inventarioSesionActual()
                );

        assertEquals(
                Set.of(
                        "A1",
                        "A2"
                ),
                inventarioA
        );


        session.clearSession();


        session.setCurrentUser(
                usuario(
                        4602,
                        "Usuario B",
                        idFarmaciaB
                )
        );

        Set<String> inventarioB =
                nombres(
                        inventarioSesionActual()
                );

        assertEquals(
                Set.of(
                        "B1",
                        "B2"
                ),
                inventarioB
        );

        assertFalse(
                inventarioB.contains("A1")
        );

        assertFalse(
                inventarioB.contains("A2")
        );
    }


    @Test
    void farmaciaSinInventarioDevuelveListaVacia()
            throws Exception {

        session.setCurrentUser(
                usuario(
                        4603,
                        "Usuario C",
                        idFarmaciaC
                )
        );

        List<Medicamento> inventario =
                inventarioSesionActual();

        assertTrue(
                inventario.isEmpty()
        );
    }


    @Test
    void sinSesionNoSePuedeConsultarInventario() {

        session.clearSession();

        assertThrows(
                IllegalStateException.class,
                this::inventarioSesionActual
        );
    }


    @Test
    void usuarioSinFarmaciaNoPuedeRealizarConsultaGlobal() {

        session.setCurrentUser(
                usuario(
                        4604,
                        "Usuario Sin Farmacia",
                        null
                )
        );

        assertThrows(
                IllegalStateException.class,
                this::inventarioSesionActual
        );
    }


    private List<Medicamento> inventarioSesionActual()
            throws SQLException {

        /*
         * Primero se exige contexto autenticado.
         * Si no existe sesión o farmacia,
         * el DAO nunca llega a ejecutarse.
         */
        int idFarmacia =
                new SessionContext()
                        .requireAuthenticatedPharmacyId();

        return medicamentoDAO
                .obtenerPorFarmacia(
                        idFarmacia
                );
    }


    private Set<String> nombres(
            List<Medicamento> medicamentos
    ) {

        return medicamentos
                .stream()
                .map(
                        Medicamento::getNombreComercial
                )
                .collect(
                        Collectors.toSet()
                );
    }


    private Usuario usuario(
            int id,
            String nombre,
            Integer idFarmacia
    ) {

        return new Usuario(
                id,
                nombre,
                "issue460-"
                + id
                + "@carestock.local",
                "hash-test",
                2,
                "ADMINISTRADOR",
                "ACTIVO",
                idFarmacia
        );
    }


    private int insertarFarmacia(
            Connection conn,
            String codigo,
            String nombre
    ) throws SQLException {

        String sql =
                "INSERT INTO FARMACIAS "
                + "(codigo, nombre) "
                + "VALUES (?, ?) "
                + "RETURNING id_farmacia";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    codigo
            );

            stmt.setString(
                    2,
                    nombre
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


    private int insertarCategoria(
            Connection conn,
            String nombre
    ) throws SQLException {

        String sql =
                "INSERT INTO CATEGORIAS "
                + "(nombre_categoria, descripcion) "
                + "VALUES (?, ?) "
                + "RETURNING id_categoria";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    nombre
            );

            stmt.setString(
                    2,
                    "Categoría temporal Issue #460"
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


    private Integer obtenerUsuarioExistente(
            Connection conn
    ) throws SQLException {

        String sql =
                "SELECT id_usuario "
                + "FROM USUARIOS "
                + "ORDER BY id_usuario "
                + "LIMIT 1";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()
        ) {

            return rs.next()
                    ? rs.getInt(1)
                    : null;
        }
    }


    private void insertarMedicamento(
            Connection conn,
            int idFarmacia,
            String nombre,
            String codigoInvima
    ) throws SQLException {

        boolean tienePresentacion =
                columnaExiste(
                        conn,
                        "medicamentos",
                        "presentacion"
                );

        boolean tieneUsuarioCreacion =
                columnaExiste(
                        conn,
                        "medicamentos",
                        "id_usuario_creacion"
                );


        if (
                tieneUsuarioCreacion
                && idUsuarioExistente == null
        ) {

            throw new SQLException(
                    "La BD requiere id_usuario_creacion "
                    + "pero no existe un usuario disponible."
            );
        }


        StringBuilder columnas =
                new StringBuilder(
                        "codigo_invima, nombre_comercial, "
                        + "principio_activo, concentracion, "
                        + "forma_farmaceutica, id_categoria, "
                        + "stock_total, stock_minimo, estado, "
                        + "id_farmacia"
                );


        StringBuilder valores =
                new StringBuilder(
                        "?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO', ?"
                );


        if (tienePresentacion) {

            columnas.append(
                    ", presentacion"
            );

            valores.append(
                    ", ?"
            );
        }


        if (tieneUsuarioCreacion) {

            columnas.append(
                    ", id_usuario_creacion"
            );

            valores.append(
                    ", ?"
            );
        }


        String sql =
                "INSERT INTO MEDICAMENTOS ("
                + columnas
                + ") VALUES ("
                + valores
                + ")";


        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            int i = 1;

            stmt.setString(
                    i++,
                    codigoInvima
            );

            stmt.setString(
                    i++,
                    nombre
            );

            stmt.setString(
                    i++,
                    "Principio activo Issue 460"
            );

            stmt.setString(
                    i++,
                    "100 mg"
            );

            stmt.setString(
                    i++,
                    "TABLETA"
            );

            stmt.setInt(
                    i++,
                    idCategoria
            );

            stmt.setInt(
                    i++,
                    10
            );

            stmt.setInt(
                    i++,
                    2
            );

            stmt.setInt(
                    i++,
                    idFarmacia
            );


            if (tienePresentacion) {

                stmt.setString(
                        i++,
                        "Caja prueba integración"
                );
            }


            if (tieneUsuarioCreacion) {

                stmt.setInt(
                        i,
                        idUsuarioExistente
                );
            }


            stmt.executeUpdate();
        }
    }


    private boolean columnaExiste(
            Connection conn,
            String tabla,
            String columna
    ) throws SQLException {

        DatabaseMetaData meta =
                conn.getMetaData();

        try (
                ResultSet rs =
                        meta.getColumns(
                                null,
                                null,
                                tabla,
                                columna
                        )
        ) {

            if (rs.next()) {
                return true;
            }
        }


        try (
                ResultSet rs =
                        meta.getColumns(
                                null,
                                null,
                                tabla.toUpperCase(),
                                columna.toUpperCase()
                        )
        ) {

            return rs.next();
        }
    }
}
