package com.carestock.service;

import com.carestock.config.ConnectionProvider;
import com.carestock.config.DatabaseConfig;
import com.carestock.dao.LoteDAO;
import com.carestock.dao.LoteDAOContract;
import com.carestock.dao.MovimientoStockDAO;
import com.carestock.dao.MovimientoStockDAOContract;
import com.carestock.model.DespachoLote;
import com.carestock.session.SessionContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servicio de dominio para movimientos de salida de inventario.
 *
 * Garantiza atomicidad entre:
 *
 * 1. Actualización del stock.
 * 2. Registro del movimiento en kardex.
 *
 * Ambas operaciones utilizan la misma Connection JDBC y forman
 * parte de una única transacción.
 */
public class DespachoService
        implements DespachoServiceContract {

    private final LoteDAOContract loteDAO;

    private final MovimientoStockDAOContract
            movimientoStockDAO;

    private final SessionContext sessionContext;

    private final ConnectionProvider
            connectionProvider;

    /**
     * Constructor utilizado por la aplicación.
     */
    public DespachoService() {
        this(
                new LoteDAO(),
                new MovimientoStockDAO(),
                new SessionContext(),
                DatabaseConfig::getConnection
        );
    }

    /**
     * Constructor conservado por compatibilidad.
     */
    public DespachoService(
            LoteDAOContract loteDAO,
            SessionContext sessionContext
    ) {
        this(
                loteDAO,
                new MovimientoStockDAO(),
                sessionContext,
                DatabaseConfig::getConnection
        );
    }

    /**
     * Constructor conservado por compatibilidad con la infraestructura
     * transaccional implementada en la issue #546.
     */
    public DespachoService(
            LoteDAOContract loteDAO,
            SessionContext sessionContext,
            ConnectionProvider connectionProvider
    ) {
        this(
                loteDAO,
                new MovimientoStockDAO(),
                sessionContext,
                connectionProvider
        );
    }

    /**
     * Constructor con inyección completa de dependencias.
     */
    public DespachoService(
            LoteDAOContract loteDAO,
            MovimientoStockDAOContract movimientoStockDAO,
            SessionContext sessionContext,
            ConnectionProvider connectionProvider
    ) {

        if (loteDAO == null) {
            throw new IllegalArgumentException(
                    "LoteDAO no puede ser nulo."
            );
        }

        if (movimientoStockDAO == null) {
            throw new IllegalArgumentException(
                    "MovimientoStockDAO no puede ser nulo."
            );
        }

        if (sessionContext == null) {
            throw new IllegalArgumentException(
                    "SessionContext no puede ser nulo."
            );
        }

        if (connectionProvider == null) {
            throw new IllegalArgumentException(
                    "ConnectionProvider no puede ser nulo."
            );
        }

        this.loteDAO =
                loteDAO;

        this.movimientoStockDAO =
                movimientoStockDAO;

        this.sessionContext =
                sessionContext;

        this.connectionProvider =
                connectionProvider;
    }

    @Override
    public void despachar(
            int idLote,
            int cantidad
    ) throws SQLException {

        /*
         * La sesión debe validarse antes de abrir
         * cualquier conexión de base de datos.
         */
        int idUsuario =
                sessionContext
                        .requireAuthenticatedUserId();

        DespachoLote despacho =
                new DespachoLote(
                        idLote,
                        cantidad,
                        idUsuario
                );

        try (
                Connection connection =
                        connectionProvider
                                .getConnection()
        ) {

            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {

                /*
                 * Inicio de una única transacción.
                 */
                connection.setAutoCommit(false);

                /*
                 * PASO 1:
                 * actualizar el inventario.
                 *
                 * fn_despachar_lote realiza la operación
                 * actualmente utilizada por CareStock.
                 */
                loteDAO.despacharLote(
                        connection,
                        despacho
                );

                /*
                 * PASO 2:
                 * registrar la salida en el kardex.
                 *
                 * Se utiliza EXACTAMENTE la misma Connection.
                 */
                movimientoStockDAO.registrarSalida(
                        connection,
                        despacho
                );

                /*
                 * Solamente cuando AMBAS operaciones fueron
                 * exitosas se confirma la transacción.
                 */
                connection.commit();

            } catch (
                    SQLException
                    | RuntimeException e
            ) {

                /*
                 * Si falla cualquiera de las dos operaciones:
                 *
                 * - se revierte el cambio de inventario;
                 * - se revierte el registro de kardex.
                 */
                try {

                    connection.rollback();

                } catch (
                        SQLException rollbackException
                ) {

                    e.addSuppressed(
                            rollbackException
                    );
                }

                throw e;

            } finally {

                try {

                    connection.setAutoCommit(
                            autoCommitOriginal
                    );

                } catch (
                        SQLException restoreException
                ) {

                    System.err.println(
                            "No fue posible restaurar autoCommit: "
                                    + restoreException
                                            .getMessage()
                    );
                }
            }
        }
    }
}
