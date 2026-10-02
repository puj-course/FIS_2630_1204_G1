package com.carestock.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contrato para obtener conexiones JDBC.
 *
 * Permite desacoplar los servicios de DatabaseConfig y facilita
 * la ejecución de pruebas unitarias sin acceder a una base de datos real.
 */
@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
