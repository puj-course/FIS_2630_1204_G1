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
public final class ConexionBD {

    private static volatile HikariDataSource pool;

    private ConexionBD() {
    }

    public static Connection getConexion() throws SQLException {

        HikariDataSource ds = pool;

        if (ds == null) {
            synchronized (ConexionBD.class) {
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

        // Prioridad 1: variables permanentes de Windows.
        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        String clave = System.getenv("DB_PASSWORD");

        boolean configuracionExterna =
                url != null || usuario != null || clave != null;

        if (configuracionExterna) {
            if (url == null || url.isBlank()
                    || usuario == null || usuario.isBlank()
                    || clave == null || clave.isBlank()) {
                throw new SQLException(
                    "Las variables DB_URL, DB_USER y DB_PASSWORD "
                    + "deben estar completas."
                );
            }

            Properties p = new Properties();

            p.setProperty("db.url", url.trim());
            p.setProperty("db.user", usuario.trim());
            p.setProperty("db.password", clave);

            return p;
        }



        Path ruta = null;

        // Configuracion externa para Windows instalado.
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isBlank()) {
            Path externa = Path.of(
                    appData,
                    "CareStock",
                    "conexion-local.properties"
            );

            if (Files.isRegularFile(externa)) {
                ruta = externa;
            }
        }

        // Compatibilidad con ejecucion desde Maven.
        if (ruta == null) {
            Path local = Path.of(
                    "config",
                    "conexion-local.properties"
            );

            if (Files.isRegularFile(local)) {
                ruta = local;
            }
        }

        // Compatibilidad con la raiz del repositorio.
        if (ruta == null) {
            Path repositorio = Path.of(
                    "src",
                    "CareStock",
                    "config",
                    "conexion-local.properties"
            );

            if (Files.isRegularFile(repositorio)) {
                ruta = repositorio;
            }
        }

        if (ruta == null) {
            throw new SQLException(
                    "No se encontro la configuracion de CareStock. "
                    + "Configure conexion-local.properties "
                    + "en APPDATA/CareStock."
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
