package com.carestock.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConfig {

    private static volatile HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {

        HikariDataSource local = dataSource;

        if (local == null) {

            synchronized (DatabaseConfig.class) {

                local = dataSource;

                if (local == null) {
                    local = createDataSource();
                    dataSource = local;
                }
            }
        }

        return local.getConnection();
    }

    private static HikariDataSource createDataSource()
            throws SQLException {

        String url =
                readRequired(
                        "carestock.db.url",
                        "DB_URL"
                );

        String user =
                readRequired(
                        "carestock.db.user",
                        "DB_USER"
                );

        String password =
                readRequired(
                        "carestock.db.password",
                        "DB_PASSWORD"
                );


        HikariConfig config =
                new HikariConfig();


        config.setJdbcUrl(
                url
        );

        config.setUsername(
                user
        );

        config.setPassword(
                password
        );


        config.setPoolName(
                "CareStockPool"
        );


        int maxPool =
                readInt(
                        "carestock.db.pool.max",
                        "DB_POOL_MAX_SIZE",
                        4
                );

        int minIdle =
                readInt(
                        "carestock.db.pool.minIdle",
                        "DB_POOL_MIN_IDLE",
                        1
                );


        config.setMaximumPoolSize(
                maxPool
        );

        config.setMinimumIdle(
                Math.min(
                        minIdle,
                        maxPool
                )
        );


        /*
         * La aplicación es de escritorio.
         * No necesitamos decenas de conexiones.
         */
        config.setConnectionTimeout(
                10_000
        );

        config.setIdleTimeout(
                120_000
        );

        config.setMaxLifetime(
                900_000
        );


        config.setAutoCommit(
                true
        );


        config.addDataSourceProperty(
                "ApplicationName",
                "CareStock"
        );

        config.addDataSourceProperty(
                "tcpKeepAlive",
                "true"
        );


        try {

            return new HikariDataSource(
                    config
            );

        } catch (RuntimeException e) {

            throw new SQLException(
                    "No fue posible inicializar el pool de conexiones.",
                    e
            );
        }
    }


    public static void closePool() {

        HikariDataSource local =
                dataSource;

        if (
                local != null
                && !local.isClosed()
        ) {

            local.close();
        }
    }


    private static int readInt(
            String propertyName,
            String environmentName,
            int defaultValue
    ) {

        String value =
                System.getProperty(
                        propertyName
                );

        if (
                value == null
                || value.isBlank()
        ) {

            value =
                    System.getenv(
                            environmentName
                    );
        }

        if (
                value == null
                || value.isBlank()
        ) {

            return defaultValue;
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return defaultValue;
        }
    }


    private static String readRequired(
            String propertyName,
            String environmentName
    ) throws SQLException {

        String value =
                System.getProperty(
                        propertyName
                );

        if (
                value == null
                || value.isBlank()
        ) {

            value =
                    System.getenv(
                            environmentName
                    );
        }

        if (
                value == null
                || value.isBlank()
        ) {

            throw new SQLException(
                    "Falta configuración de base de datos: "
                    + environmentName
                    + ". Consulte .env.example y "
                    + "docs/user_guide/DEPLOY.md."
            );
        }

        return value.trim();
    }
}
