package com.carestock.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuración central de PostgreSQL.
 *
 * Las credenciales NO se almacenan en el repositorio. Se leen de propiedades
 * de la JVM o de variables de entorno:
 *   - carestock.db.url / DB_URL
 *   - carestock.db.user / DB_USER
 *   - carestock.db.password / DB_PASSWORD
 */
public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        String url = readRequired("carestock.db.url", "DB_URL");
        String user = readRequired("carestock.db.user", "DB_USER");
        String password = readRequired("carestock.db.password", "DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }

    private static String readRequired(String propertyName, String environmentName) throws SQLException {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName);
        }
        if (value == null || value.isBlank()) {
            throw new SQLException(
                "Falta configuración de base de datos: " + environmentName
                + ". Consulte .env.example y docs/user_guide/DEPLOY.md."
            );
        }
        return value.trim();
    }
}
