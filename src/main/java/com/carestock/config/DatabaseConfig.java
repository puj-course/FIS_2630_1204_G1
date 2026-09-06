package com.carestock.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL = "jdbc:postgresql://ep-late-thunder-ayd67owm-pooler.c-5.us-east-2.aws.neon.tech:5432/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_ZNWs6Ff9kmMJ";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC de PostgreSQL no encontrado en el classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
