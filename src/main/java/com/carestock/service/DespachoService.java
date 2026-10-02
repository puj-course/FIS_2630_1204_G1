package com.carestock.service;

import com.carestock.config.ConnectionProvider;
import com.carestock.config.DatabaseConfig;
import com.carestock.dao.LoteDAO;
import com.carestock.dao.LoteDAOContract;
import com.carestock.model.DespachoLote;
import com.carestock.session.SessionContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servicio de dominio para movimientos de salida de inventario.
 *
 * Controla la frontera transaccional JDBC:
 *
 * - obtiene una conexión;
 * - deshabilita auto-commit;
 * - ejecuta las operaciones DAO;
 * - confirma mediante commit;
 * - revierte mediante rollback ante errores;
 * - restaura el estado original de auto-commit.
 */
public class DespachoService
        implements DespachoServiceContract {

    private final LoteDAOContract loteDAO;
    private final SessionContext sessionContext;
    private final ConnectionProvider connectionProvider;

    /**
     * Constructor utilizado por la aplicación.
     */
    public DespachoService() {
        this(
                new LoteDAO(),
                new SessionContext(),
                DatabaseConfig::getConnection
        );
    }

    /**
     * Constructor de compatibilidad.
     */
    public DespachoService(
            LoteDAOContract loteDAO,
            SessionContext sessionContext
    ) {
        this(
                loteDAO,
                sessionContext,
                DatabaseConfig::getConnection
        );
    }

    /**
     * Constructor con inyección completa de dependencias.
     *
     * Se utiliza principalmente para pruebas unitarias y permite
     * sustituir tanto el DAO como el proveedor de conexiones.
     */
    public DespachoService(
            LoteDAOContract loteDAO,
            SessionContext sessionContext,
            ConnectionProvider connectionProvider
    ) {

        if (loteDAO == null) {
            throw new IllegalArgumentException(
                    "LoteDAO no puede ser nulo."
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

        this.loteDAO = loteDAO;
        this.sessionContext = sessionContext;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void despachar(
            int idLote,
            int cantidad
    ) throws SQLException {

        /*
         * Se valida la sesión antes de abrir cualquier conexión
         * o realizar cualquier operación de persistencia.
         */
        int idUsuario =
                sessionContext.requireAuthenticatedUserId();

        DespachoLote despacho =
                new DespachoLote(
                        idLote,
                        cantidad,
                        idUsuario
                );

        try (
                Connection connection =
                        connectionProvider.getConnection()
        ) {

            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {

                /*
                 * Inicio explícito de la transacción.
                 */
                connection.setAutoCommit(false);

                loteDAO.despacharLote(
                        connection,
                        despacho
                );

                /*
                 * La operación completa fue exitosa.
                 */
                connection.commit();

            } catch (SQLException | RuntimeException e) {

                try {

                    connection.rollback();

                } catch (SQLException rollbackException) {

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

                } catch (SQLException restoreException) {

                    System.err.println(
                            "No fue posible restaurar autoCommit: "
                                    + restoreException.getMessage()
                    );
                }
            }
        }
    }
}
