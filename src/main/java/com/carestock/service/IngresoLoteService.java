package com.carestock.service;

import com.carestock.config.ConnectionProvider;
import com.carestock.config.DatabaseConfig;
import com.carestock.dao.LoteDAO;
import com.carestock.dao.LoteDAOContract;
import com.carestock.model.Lote;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.session.SessionContext;
import com.carestock.utils.IngresoLoteValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Servicio de dominio para movimientos de entrada de inventario.
 *
 * Controla la frontera transaccional JDBC para el registro
 * de lotes y movimientos de entrada.
 */
public class IngresoLoteService
        implements IngresoLoteServiceContract {

    private final LoteDAOContract loteDAO;
    private final SessionContext sessionContext;
    private final ConnectionProvider connectionProvider;

    /**
     * Constructor utilizado por la aplicación.
     */
    public IngresoLoteService() {
        this(
                new LoteDAO(),
                new SessionContext(),
                DatabaseConfig::getConnection
        );
    }

    /**
     * Constructor de compatibilidad.
     */
    public IngresoLoteService(
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
     */
    public IngresoLoteService(
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
    public void registrar(
            Medicamento medicamento,
            String numeroLote,
            String cantidadStr,
            LocalDate fechaVencimiento,
            Ubicacion ubicacion
    ) throws SQLException {

        /*
         * La sesión se valida antes de abrir la conexión.
         */
        int idUsuario =
                sessionContext.requireAuthenticatedUserId();

        String error =
                IngresoLoteValidator.validar(
                        medicamento,
                        numeroLote,
                        cantidadStr,
                        fechaVencimiento,
                        ubicacion
                );

        if (error != null) {
            throw new IllegalArgumentException(
                    error
            );
        }

        int cantidad =
                Integer.parseInt(
                        cantidadStr.trim()
                );

        Lote lote =
                new Lote(
                        numeroLote.trim(),
                        Math.toIntExact(
                                medicamento.getIdMedicamento()
                        ),
                        cantidad,
                        fechaVencimiento,
                        ubicacion.getIdUbicacion(),
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

                loteDAO.registrarNuevoLote(
                        connection,
                        lote
                );

                /*
                 * Persistencia completada correctamente.
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
