package org.example.carestock.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexiones JDBC para PostgreSQL en Neon DB.
 */
public class ConexionBD {

    private static final String URL = "jdbc:postgresql://ep-late-thunder-ayd67owm-pooler.c-5.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require";
    private static final String USUARIO = "neondb_owner";
    private static final String CLAVE = "npg_dNyPinh6pR7S";

    /**
     * Obtiene una nueva conexión a la base de datos Neon.
     * @return Connection objeto de conexión JDBC activo.
     * @throws SQLException si falla la autenticación o la red.
     */
    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC de PostgreSQL no encontrado.", e);
        }
    }
}