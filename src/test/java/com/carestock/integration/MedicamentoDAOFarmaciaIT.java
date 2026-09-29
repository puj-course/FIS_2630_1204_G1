package com.carestock.integration;

import com.carestock.config.DatabaseConfig;
import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicamentoDAOFarmaciaIT {

    private final MedicamentoDAO dao =
            new MedicamentoDAO();

    private String sufijo;
    private String codigoInvima;
    private String categoria;

    private int idFarmaciaA;
    private int idFarmaciaB;
    private int idCategoria;

    private Integer idUsuarioExistente;

    @BeforeEach
    void setUp() throws Exception {

        sufijo =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8);

        codigoInvima =
                "INVIMA-457-" + sufijo;

        categoria =
                "CAT-457-" + sufijo;

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            idFarmaciaA =
                    insertarFarmacia(
                            conn,
                            "IT-FAR-A-" + sufijo,
                            "Farmacia IT A"
                    );

            idFarmaciaB =
                    insertarFarmacia(
                            conn,
                            "IT-FAR-B-" + sufijo,
                            "Farmacia IT B"
                    );

            idCategoria =
                    insertarCategoria(
                            conn,
                            categoria
                    );

            idUsuarioExistente =
                    obtenerUsuarioExistente(
                            conn
                    );

            insertarMedicamento(
                    conn,
                    idFarmaciaA,
                    11,
                    5
            );

            insertarMedicamento(
                    conn,
                    idFarmaciaB,
                    2,
                    5
            );
        }
    }

    @AfterEach
    void tearDown() throws Exception {

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    "DELETE FROM MEDICAMENTOS "
                                    + "WHERE id_farmacia IN (?, ?)"
                            )
            ) {

                stmt.setInt(
                        1,
                        idFarmaciaA
                );

                stmt.setInt(
                        2,
                        idFarmaciaB
                );

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
                                    + "WHERE id_farmacia IN (?, ?)"
                            )
            ) {

                stmt.setInt(
                        1,
                        idFarmaciaA
                );

                stmt.setInt(
                        2,
                        idFarmaciaB
                );

                stmt.executeUpdate();
            }
        }
    }

    @Test
    void filtraInventarioPorFarmaciaEnPostgreSQL()
            throws Exception {

        List<Medicamento> farmaciaA =
                dao.obtenerPorFarmacia(
                        idFarmaciaA
                );

        List<Medicamento> farmaciaB =
                dao.obtenerPorFarmacia(
                        idFarmaciaB
                );

        assertEquals(
                1,
                farmaciaA.size()
        );

        assertEquals(
                1,
                farmaciaB.size()
        );

        assertEquals(
                codigoInvima,
                farmaciaA
                        .get(0)
                        .getCodigoInvima()
        );

        assertEquals(
                codigoInvima,
                farmaciaB
                        .get(0)
                        .getCodigoInvima()
        );

        assertEquals(
                11,
                farmaciaA
                        .get(0)
                        .getStockTotal()
        );

        assertEquals(
                2,
                farmaciaB
                        .get(0)
                        .getStockTotal()
        );

        assertNotEquals(
                farmaciaA
                        .get(0)
                        .getIdMedicamento(),
                farmaciaB
                        .get(0)
                        .getIdMedicamento()
        );
    }

    @Test
    void calculaMetricasPorFarmacia()
            throws Exception {

        assertEquals(
                11,
                dao.obtenerTotalUnidadesStockPorFarmacia(
                        idFarmaciaA
                )
        );

        assertEquals(
                2,
                dao.obtenerTotalUnidadesStockPorFarmacia(
                        idFarmaciaB
                )
        );

        assertEquals(
                0,
                dao.obtenerAlertasCriticasPorFarmacia(
                        idFarmaciaA
                )
        );

        assertEquals(
                1,
                dao.obtenerAlertasCriticasPorFarmacia(
                        idFarmaciaB
                )
        );
    }

    @Test
    void rechazaIdFarmaciaInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.obtenerPorFarmacia(0)
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
                    "Categoría temporal issue #457"
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
            int stockTotal,
            int stockMinimo
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
                    + "pero no existe ningún usuario."
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
                    "Medicamento Issue 457"
            );

            stmt.setString(
                    i++,
                    "Principio activo Issue 457"
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
                    stockTotal
            );

            stmt.setInt(
                    i++,
                    stockMinimo
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

        DatabaseMetaData metaData =
                conn.getMetaData();

        try (
                ResultSet rs =
                        metaData.getColumns(
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
                        metaData.getColumns(
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
