package com.carestock.integration;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.model.Usuario;
import com.carestock.service.DespachoService;
import com.carestock.service.IngresoLoteService;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirmaInmutableIT {

    private final UserSession userSession =
            UserSession.getInstance();

    /*
     * Cada prueba utiliza datos únicos para evitar colisiones
     * con datos reales o con otras ejecuciones del test.
     */
    private String sufijo;

    private String nombreCategoria;
    private String estante;

    private String emailUsuarioA;
    private String emailUsuarioB;

    private String codigoInvima;
    private String numeroLote;

    private int idRolAdministrador;
    private int idRolFarmaceutico;
    private int idCategoria;
    private int idUbicacion;
    private int idUsuarioA;
    private int idUsuarioB;
    private int idMedicamento;

    private Usuario usuarioA;
    private Usuario usuarioB;

    private Medicamento medicamento;
    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() throws Exception {

        userSession.clearSession();

        /*
         * Se genera un identificador único para que la prueba
         * pueda ejecutarse varias veces sin generar conflictos.
         */
        sufijo =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8);

        nombreCategoria =
                "CAT_IT_" + sufijo;

        estante =
                "IT" + sufijo;

        emailUsuarioA =
                "usuario_a_" + sufijo
                        + "@integration.carestock";

        emailUsuarioB =
                "usuario_b_" + sufijo
                        + "@integration.carestock";

        codigoInvima =
                "INVIMA-IT-" + sufijo;

        numeroLote =
                "LOT-IT-" + sufijo;

        crearDatosPrueba();

        usuarioA =
                new Usuario(
                        idUsuarioA,
                        "Usuario A Integración",
                        emailUsuarioA,
                        "hash-integration-test",
                        idRolAdministrador,
                        "ADMINISTRADOR",
                        "ACTIVO"
                );

        usuarioB =
                new Usuario(
                        idUsuarioB,
                        "Usuario B Integración",
                        emailUsuarioB,
                        "hash-integration-test",
                        idRolFarmaceutico,
                        "FARMACEUTICO",
                        "ACTIVO"
                );

        medicamento =
                new Medicamento(
                        (long) idMedicamento,
                        codigoInvima,
                        "Medicamento Integración",
                        "Principio activo IT",
                        "100 mg",
                        nombreCategoria,
                        0,
                        5
                );

        ubicacion =
                new Ubicacion(
                        idUbicacion,
                        estante,
                        "1",
                        "Ubicación prueba de integración"
                );
    }

    @AfterEach
    void tearDown() throws Exception {

        userSession.clearSession();

        limpiarDatosPrueba();
    }

    /**
     * Escenario E2E:
     *
     * UserSession
     *     -> SessionContext
     *     -> IngresoLoteService
     *     -> LoteDAO
     *     -> sp_registrar_nuevo_lote
     *     -> PostgreSQL
     *
     * Comprueba que el usuario que tenía la sesión al momento
     * de ejecutar la operación queda almacenado en la BD.
     */
    @Test
    void registroLoteQuedaFirmadoPorUsuarioActivo()
            throws Exception {

        userSession.setCurrentUser(
                usuarioA
        );

        IngresoLoteService service =
                new IngresoLoteService();

        service.registrar(
                medicamento,
                numeroLote,
                "25",
                LocalDate.now()
                        .plusMonths(6),
                ubicacion
        );

        int idLote =
                obtenerIdLote();

        assertEquals(
                idUsuarioA,
                obtenerUsuarioFirmanteLote(idLote),
                "El lote debe quedar asociado al Usuario_A autenticado."
        );

        assertEquals(
                idUsuarioA,
                obtenerUsuarioMovimiento(
                        idLote,
                        "ENTRADA"
                ),
                "El movimiento ENTRADA debe quedar firmado por Usuario_A."
        );

        /*
         * Se cambia la sesión DESPUÉS de persistir.
         *
         * Esto demuestra que la firma registrada previamente
         * no cambia cuando cambia el usuario en memoria.
         */
        userSession.setCurrentUser(
                usuarioB
        );

        assertEquals(
                idUsuarioA,
                obtenerUsuarioFirmanteLote(idLote),
                "Cambiar la sesión no debe modificar la firma histórica del lote."
        );

        assertEquals(
                idUsuarioA,
                obtenerUsuarioMovimiento(
                        idLote,
                        "ENTRADA"
                ),
                "La trazabilidad histórica debe conservar Usuario_A."
        );
    }

    /**
     * Comprueba que la firma corresponde a la sesión existente
     * exactamente en el momento en que ocurre el despacho.
     */
    @Test
    void despachoQuedaFirmadoPorUsuarioActivo()
            throws Exception {

        /*
         * Primero registramos el lote como Usuario_A.
         */
        userSession.setCurrentUser(
                usuarioA
        );

        IngresoLoteService ingresoService =
                new IngresoLoteService();

        ingresoService.registrar(
                medicamento,
                numeroLote,
                "25",
                LocalDate.now()
                        .plusMonths(6),
                ubicacion
        );

        int idLote =
                obtenerIdLote();

        /*
         * Ahora la sesión cambia a Usuario_B.
         *
         * El despacho debe quedar firmado por B,
         * pero el ingreso original debe continuar firmado por A.
         */
        userSession.setCurrentUser(
                usuarioB
        );

        DespachoService despachoService =
                new DespachoService();

        despachoService.despachar(
                idLote,
                5
        );

        assertEquals(
                idUsuarioA,
                obtenerUsuarioFirmanteLote(idLote),
                "La firma original del lote no debe modificarse al despachar."
        );

        assertEquals(
                idUsuarioA,
                obtenerUsuarioMovimiento(
                        idLote,
                        "ENTRADA"
                ),
                "La entrada debe conservar la firma de Usuario_A."
        );

        assertEquals(
                idUsuarioB,
                obtenerUsuarioMovimiento(
                        idLote,
                        "SALIDA"
                ),
                "El despacho debe quedar firmado por el usuario activo Usuario_B."
        );

        assertEquals(
                20,
                obtenerCantidadActual(idLote),
                "El despacho debe descontar cinco unidades del lote."
        );
    }

    /**
     * Comprueba simultáneamente:
     *
     * 1. La operación sin sesión genera excepción.
     * 2. No ocurre ninguna escritura parcial en la BD.
     */
    @Test
    void operacionSinSesionEsBloqueadaYNoPersiste()
            throws Exception {

        userSession.clearSession();

        IngresoLoteService service =
                new IngresoLoteService();

        assertThrows(
                IllegalStateException.class,
                () -> service.registrar(
                        medicamento,
                        numeroLote,
                        "25",
                        LocalDate.now()
                                .plusMonths(6),
                        ubicacion
                ),
                "La operación debe bloquearse cuando no existe sesión activa."
        );

        assertEquals(
                0,
                contarLotesPrueba(),
                "No debe persistirse ningún lote cuando no existe sesión activa."
        );

        assertEquals(
                0,
                contarMovimientosPrueba(),
                "No debe generarse trazabilidad parcial sin sesión."
        );
    }

    // =========================================================
    // DATOS DE PRUEBA
    // =========================================================

    private void crearDatosPrueba()
            throws SQLException {

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            conn.setAutoCommit(false);

            try {

                /*
                 * Se utilizan los roles reales definidos por el dominio.
                 * fn_despachar_lote solo autoriza ADMINISTRADOR
                 * y FARMACEUTICO.
                 */
                idRolAdministrador =
                        obtenerIdRol(
                                conn,
                                "ADMINISTRADOR"
                        );

                idRolFarmaceutico =
                        obtenerIdRol(
                                conn,
                                "FARMACEUTICO"
                        );

                idCategoria =
                        insertarYRetornarId(
                                conn,
                                """
                                INSERT INTO CATEGORIAS (
                                    nombre_categoria,
                                    descripcion
                                )
                                VALUES (?, ?)
                                RETURNING id_categoria
                                """,
                                nombreCategoria,
                                "Categoría temporal para integración"
                        );

                idUbicacion =
                        insertarYRetornarId(
                                conn,
                                """
                                INSERT INTO UBICACIONES (
                                    estante,
                                    nivel,
                                    descripcion
                                )
                                VALUES (?, ?, ?)
                                RETURNING id_ubicacion
                                """,
                                estante,
                                "1",
                                "Ubicación temporal de integración"
                        );

                idUsuarioA =
                        insertarUsuario(
                                conn,
                                emailUsuarioA,
                                "Usuario A Integración",
                                idRolAdministrador
                        );

                idUsuarioB =
                        insertarUsuario(
                                conn,
                                emailUsuarioB,
                                "Usuario B Integración",
                                idRolFarmaceutico
                        );

                idMedicamento =
                        insertarMedicamento(
                                conn
                        );

                conn.commit();

            } catch (Exception e) {

                conn.rollback();

                throw e;
            }
        }
    }

    /**
     * Recupera un rol real definido en la base de datos.
     *
     * Las pruebas de integración no deben inventar roles que
     * violen las reglas de autorización del dominio.
     */
    private int obtenerIdRol(
            Connection conn,
            String nombreRol
    ) throws SQLException {

        String sql =
                """
                SELECT id_rol
                FROM ROLES
                WHERE UPPER(nombre_rol) = UPPER(?)
                """;

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    nombreRol
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new IllegalStateException(
                            "No existe el rol requerido para la prueba: "
                            + nombreRol
                    );
                }

                return rs.getInt(
                        "id_rol"
                );
            }
        }
    }

    private int insertarUsuario(
            Connection conn,
            String email,
            String nombre,
            int idRolUsuario
    ) throws SQLException {

        String sql =
                """
                INSERT INTO USUARIOS (
                    nombre_completo,
                    email,
                    password_hash,
                    id_rol,
                    estado
                )
                VALUES (?, ?, ?, ?, 'ACTIVO')
                RETURNING id_usuario
                """;

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
                    email
            );

            stmt.setString(
                    3,
                    "hash-integration-test"
            );

            stmt.setInt(
                    4,
                    idRolUsuario
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                rs.next();

                return rs.getInt(
                        "id_usuario"
                );
            }
        }
    }

    private int insertarMedicamento(
            Connection conn
    ) throws SQLException {

        String sql =
                """
                INSERT INTO MEDICAMENTOS (
                    codigo_invima,
                    nombre_comercial,
                    principio_activo,
                    concentracion,
                    forma_farmaceutica,
                    id_categoria,
                    stock_minimo,
                    stock_total,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 0, 'ACTIVO')
                RETURNING id_medicamento
                """;

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    codigoInvima
            );

            stmt.setString(
                    2,
                    "Medicamento Integración"
            );

            stmt.setString(
                    3,
                    "Principio activo IT"
            );

            stmt.setString(
                    4,
                    "100 mg"
            );

            stmt.setString(
                    5,
                    "TABLETA"
            );

            stmt.setInt(
                    6,
                    idCategoria
            );

            stmt.setInt(
                    7,
                    5
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                rs.next();

                return rs.getInt(
                        "id_medicamento"
                );
            }
        }
    }

    private int insertarYRetornarId(
            Connection conn,
            String sql,
            Object... valores
    ) throws SQLException {

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            for (
                    int i = 0;
                    i < valores.length;
                    i++
            ) {

                stmt.setObject(
                        i + 1,
                        valores[i]
                );
            }

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                rs.next();

                return rs.getInt(1);
            }
        }
    }

    // =========================================================
    // CONSULTAS DE VERIFICACIÓN
    // =========================================================

    private int obtenerIdLote()
            throws SQLException {

        String sql =
                """
                SELECT id_lote
                FROM LOTES
                WHERE numero_lote = ?
                AND id_medicamento = ?
                """;

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    numeroLote
            );

            stmt.setInt(
                    2,
                    idMedicamento
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new AssertionError(
                            "El lote de integración no fue encontrado en la BD."
                    );
                }

                return rs.getInt(
                        "id_lote"
                );
            }
        }
    }

    private int obtenerUsuarioFirmanteLote(
            int idLote
    ) throws SQLException {

        String sql =
                """
                SELECT id_usuario
                FROM LOTES
                WHERE id_lote = ?
                """;

        return consultarEntero(
                sql,
                idLote
        );
    }

    private int obtenerUsuarioMovimiento(
            int idLote,
            String tipoMovimiento
    ) throws SQLException {

        String sql =
                """
                SELECT id_usuario
                FROM LOG_MOVIMIENTOS
                WHERE id_lote = ?
                AND tipo_movimiento = ?
                ORDER BY id_log DESC
                LIMIT 1
                """;

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idLote
            );

            stmt.setString(
                    2,
                    tipoMovimiento
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new AssertionError(
                            "No se encontró movimiento "
                            + tipoMovimiento
                            + " para el lote "
                            + idLote
                    );
                }

                return rs.getInt(
                        "id_usuario"
                );
            }
        }
    }

    private int obtenerCantidadActual(
            int idLote
    ) throws SQLException {

        String sql =
                """
                SELECT cantidad_actual
                FROM LOTES
                WHERE id_lote = ?
                """;

        return consultarEntero(
                sql,
                idLote
        );
    }

    private int contarLotesPrueba()
            throws SQLException {

        String sql =
                """
                SELECT COUNT(*)
                FROM LOTES
                WHERE numero_lote = ?
                AND id_medicamento = ?
                """;

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    numeroLote
            );

            stmt.setInt(
                    2,
                    idMedicamento
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

    private int contarMovimientosPrueba()
            throws SQLException {

        String sql =
                """
                SELECT COUNT(*)
                FROM LOG_MOVIMIENTOS lm
                INNER JOIN LOTES l
                    ON l.id_lote = lm.id_lote
                WHERE l.numero_lote = ?
                AND l.id_medicamento = ?
                """;

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    numeroLote
            );

            stmt.setInt(
                    2,
                    idMedicamento
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

    private int consultarEntero(
            String sql,
            int parametro
    ) throws SQLException {

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    parametro
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new AssertionError(
                            "La consulta de integración no devolvió resultados."
                    );
                }

                return rs.getInt(1);
            }
        }
    }

    // =========================================================
    // LIMPIEZA
    // =========================================================

    private void limpiarDatosPrueba()
            throws SQLException {

        /*
         * La limpieza se realiza en orden inverso a las
         * relaciones de claves foráneas.
         *
         * De esta forma los tests no dejan basura en Neon.
         */
        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            conn.setAutoCommit(false);

            try {

                ejecutarUpdate(
                        conn,
                        """
                        DELETE FROM LOG_MOVIMIENTOS
                        WHERE id_lote IN (
                            SELECT id_lote
                            FROM LOTES
                            WHERE numero_lote = ?
                        )
                        """,
                        numeroLote
                );

                ejecutarUpdate(
                        conn,
                        """
                        DELETE FROM LOTES
                        WHERE numero_lote = ?
                        """,
                        numeroLote
                );

                ejecutarUpdate(
                        conn,
                        """
                        DELETE FROM MEDICAMENTOS
                        WHERE codigo_invima = ?
                        """,
                        codigoInvima
                );

                ejecutarUpdate(
                        conn,
                        """
                        DELETE FROM USUARIOS
                        WHERE email IN (?, ?)
                        """,
                        emailUsuarioA,
                        emailUsuarioB
                );

                ejecutarUpdate(
                        conn,
                        """
                        DELETE FROM UBICACIONES
                        WHERE estante = ?
                        """,
                        estante
                );

                ejecutarUpdate(
                        conn,
                        """
                        DELETE FROM CATEGORIAS
                        WHERE nombre_categoria = ?
                        """,
                        nombreCategoria
                );

                conn.commit();

            } catch (Exception e) {

                conn.rollback();

                throw e;
            }
        }
    }

    private void ejecutarUpdate(
            Connection conn,
            String sql,
            Object... parametros
    ) throws SQLException {

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            for (
                    int i = 0;
                    i < parametros.length;
                    i++
            ) {

                stmt.setObject(
                        i + 1,
                        parametros[i]
                );
            }

            stmt.executeUpdate();
        }
    }
}
