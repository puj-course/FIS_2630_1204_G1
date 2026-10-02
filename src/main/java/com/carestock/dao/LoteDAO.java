package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.DespachoLote;
import com.carestock.model.Lote;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementación JDBC de las operaciones de persistencia asociadas a lotes.
 *
 * Las operaciones de escritura utilizadas dentro de movimientos de stock
 * pueden recibir una Connection externa. Esto permite que la capa Service
 * controle el ciclo completo de la transacción:
 *
 * setAutoCommit(false) -> operaciones DAO -> commit / rollback.
 *
 * El DAO no realiza commit ni rollback cuando recibe una Connection externa.
 */
public class LoteDAO implements LoteDAOContract {

    /**
     * Método de compatibilidad para registrar un lote utilizando
     * una conexión propia.
     *
     * Las operaciones transaccionales coordinadas por los servicios
     * deben utilizar registrarNuevoLote(Connection, Lote).
     */
    public void registrarNuevoLote(
            Lote lote
    ) throws SQLException {

        try (
                Connection connection =
                        DatabaseConfig.getConnection()
        ) {

            registrarNuevoLote(
                    connection,
                    lote
            );
        }
    }

    /**
     * Registra un lote utilizando una conexión proporcionada
     * por la capa Service.
     *
     * La conexión NO se cierra dentro de este método porque pertenece
     * al contexto transaccional controlado por el servicio.
     */
    @Override
    public void registrarNuevoLote(
            Connection connection,
            Lote lote
    ) throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "La conexión no puede ser nula."
            );
        }

        if (lote == null) {
            throw new IllegalArgumentException(
                    "El lote no puede ser nulo."
            );
        }

        String sql =
                "CALL sp_registrar_nuevo_lote(?, ?, ?, ?, ?, ?)";

        try (
                CallableStatement stmt =
                        connection.prepareCall(sql)
        ) {

            stmt.setString(
                    1,
                    lote.getNumeroLote()
            );

            stmt.setInt(
                    2,
                    lote.getIdMedicamento()
            );

            stmt.setInt(
                    3,
                    lote.getCantidadActual()
            );

            stmt.setDate(
                    4,
                    Date.valueOf(
                            lote.getFechaVencimiento()
                    )
            );

            stmt.setInt(
                    5,
                    lote.getIdUbicacion()
            );

            /*
             * El usuario responsable ya fue obtenido
             * desde SessionContext en la capa Service.
             */
            stmt.setInt(
                    6,
                    lote.getIdUsuario()
            );

            stmt.execute();
        }
    }

    /**
     * Método de compatibilidad para realizar un despacho utilizando
     * una conexión propia.
     *
     * Las operaciones transaccionales coordinadas por los servicios
     * deben utilizar despacharLote(Connection, DespachoLote).
     */
    public void despacharLote(
            DespachoLote despacho
    ) throws SQLException {

        try (
                Connection connection =
                        DatabaseConfig.getConnection()
        ) {

            despacharLote(
                    connection,
                    despacho
            );
        }
    }

    /**
     * Ejecuta un despacho utilizando la misma conexión controlada
     * por la capa Service.
     *
     * Este método no ejecuta commit, rollback ni cierra la conexión.
     */
    @Override
    public void despacharLote(
            Connection connection,
            DespachoLote despacho
    ) throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "La conexión no puede ser nula."
            );
        }

        if (despacho == null) {
            throw new IllegalArgumentException(
                    "El despacho no puede ser nulo."
            );
        }

        String sql =
                "SELECT fn_despachar_lote(?, ?, ?)";

        try (
                PreparedStatement stmt =
                        connection.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    despacho.getIdLote()
            );

            stmt.setInt(
                    2,
                    despacho.getCantidad()
            );

            stmt.setInt(
                    3,
                    despacho.getIdUsuario()
            );

            stmt.execute();
        }
    }

    /**
     * Cuenta los lotes próximos a vencer en todas las farmacias.
     */
    public int contarProximosAVencer(
            int dias
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM LOTES " +
                "WHERE estado_lote = 'DISPONIBLE' " +
                "AND fecha_vencimiento > CURRENT_DATE " +
                "AND fecha_vencimiento <= CURRENT_DATE + " +
                "(? * INTERVAL '1 day')";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    dias
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                return rs.next()
                        ? rs.getInt(1)
                        : 0;
            }
        }
    }

    /**
     * Cuenta los lotes próximos a vencer pertenecientes
     * a una farmacia específica.
     */
    public int contarProximosAVencerPorFarmacia(
            int dias,
            int idFarmacia
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) "
                        + "FROM LOTES l "
                        + "INNER JOIN MEDICAMENTOS m "
                        + "ON m.id_medicamento = l.id_medicamento "
                        + "WHERE m.id_farmacia = ? "
                        + "AND l.estado_lote = 'DISPONIBLE' "
                        + "AND l.fecha_vencimiento > CURRENT_DATE "
                        + "AND l.fecha_vencimiento <= CURRENT_DATE + "
                        + "(? * INTERVAL '1 day')";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idFarmacia
            );

            stmt.setInt(
                    2,
                    dias
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                return rs.next()
                        ? rs.getInt(1)
                        : 0;
            }
        }
    }
}
