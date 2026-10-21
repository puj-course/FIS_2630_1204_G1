package org.example.carestock.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexion PostgreSQL / Neon mediante HikariCP.
 *
 * Lee los datos desde config/conexion-local.properties.
 */
public final class ConexionBaseDatos {

    private static volatile HikariDataSource pool;

    private ConexionBaseDatos() {
    }

    public static Connection getConexion() throws SQLException {

        HikariDataSource ds = pool;

        if (ds == null) {
            synchronized (ConexionBaseDatos.class) {
                ds = pool;

                if (ds == null) {
                    ds = crearPool();
                    pool = ds;
                }
            }
        }

        return ds.getConnection();
    }

    private static Properties cargarConfiguracion()
            throws SQLException {

        Path ruta = Path.of(
                "config", "conexion-local.properties"
        );

        // Compatibilidad si se ejecuta desde
        // la raiz del repositorio.
        if (!Files.exists(ruta)) {
            ruta = Path.of(
                    "src", "CareStock",
                    "config", "conexion-local.properties"
            );
        }

        if (!Files.isRegularFile(ruta)) {
            throw new SQLException(
                    "No se encuentra conexion-local.properties"
            );
        }

        Properties propiedades = new Properties();

        try (InputStream entrada = Files.newInputStream(ruta)) {
            propiedades.load(entrada);
        } catch (IOException e) {
            throw new SQLException(
                    "Error leyendo configuracion de Neon", e
            );
        }

        return propiedades;
    }

    private static String obligatorio(
            Properties p, String clave
    ) throws SQLException {

        String valor = p.getProperty(clave);

        if (valor == null || valor.isBlank()) {
            throw new SQLException(
                    "Falta configurar: " + clave
            );
        }

        return valor.trim();
    }

    private static int entero(
            Properties p,
            String clave,
            int defecto
    ) throws SQLException {

        String valor = p.getProperty(clave);

        if (valor == null || valor.isBlank()) {
            return defecto;
        }

        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            throw new SQLException(
                    "Valor numerico invalido: " + clave, ex
            );
        }
    }

    private static HikariDataSource crearPool()
            throws SQLException {

        Properties p = cargarConfiguracion();

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(
                obligatorio(p, "db.url")
        );

        config.setUsername(
                obligatorio(p, "db.user")
        );

        config.setPassword(
                obligatorio(p, "db.password")
        );

        config.setDriverClassName(
                "org.postgresql.Driver"
        );

        config.setPoolName("CareStock-Neon");

        int max = entero(p, "db.pool.max", 4);
        int min = entero(p, "db.pool.min", 0);

        if (max < 1 || max > 10 ||
                min < 0 || min > max) {
            throw new SQLException(
                    "Configuracion de pool invalida"
            );
        }

        config.setMaximumPoolSize(max);
        config.setMinimumIdle(min);

        config.setConnectionTimeout(30000);
        config.setValidationTimeout(5000);
        config.setIdleTimeout(120000);
        config.setMaxLifetime(1500000);

        config.setInitializationFailTimeout(-1);

        try {
            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new SQLException(
                    "Error inicializando HikariCP", e
            );
        }
    }

    public static String estadoPool() {

        HikariDataSource ds = pool;

        if (ds == null || ds.isClosed()) {
            return "Pool no inicializado";
        }

        var mx = ds.getHikariPoolMXBean();

        if (mx == null) {
            return "Pool inicializando";
        }

        return "total=" + mx.getTotalConnections()
                + ", activas=" + mx.getActiveConnections()
                + ", libres=" + mx.getIdleConnections()
                + ", esperando="
                + mx.getThreadsAwaitingConnection();
    }

    public static synchronized void cerrarPool() {

        if (pool != null) {
            pool.close();
            pool = null;
        }
    }
}
