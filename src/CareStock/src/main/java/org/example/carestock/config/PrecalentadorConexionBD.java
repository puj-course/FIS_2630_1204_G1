package org.example.carestock.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Prepara la primera conexion JDBC sin bloquear JavaFX.
 */
public final class PrecalentadorConexionBD {
    private static final System.Logger LOGGER =
            System.getLogger(PrecalentadorConexionBD.class.getName());

    private static final AtomicBoolean INICIADO =
            new AtomicBoolean(false);

    private PrecalentadorConexionBD() { }

    public static void iniciarAsync() {

        if (!INICIADO.compareAndSet(false, true)) {
            return;
        }

        Thread.ofVirtual()
                .name("carestock-jdbc-warmup")
                .start(() -> {

                    long inicio = System.nanoTime();

                    try (Connection conexion =
                                 ConexionBD.getConexion()) {

                        if (!conexion.isValid(3)) {
                            throw new SQLException(
                                    "La conexion inicial no es valida"
                            );
                        }

                        long duracionMs =
                                (System.nanoTime() - inicio)
                                        / 1_000_000;

                        LOGGER.log(System.Logger.Level.INFO,
                                "[CareStock] Pool JDBC precalentado en "
                                        + duracionMs + " ms"
                        );

                    } catch (SQLException e) {

                        LOGGER.log(System.Logger.Level.WARNING,
                                "[CareStock] No se pudo precalentar JDBC: "
                                        + e.getMessage()
                        );
                    }
                });
    }
}
