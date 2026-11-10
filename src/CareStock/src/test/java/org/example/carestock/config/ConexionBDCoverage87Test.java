package org.example.carestock.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConexionBDCoverage87Test {

    private AtomicReference<HikariDataSource> referencia;
    private HikariDataSource original;

    @BeforeEach
    void preparar() throws Exception {
        Field campoPool = ConexionBD.class.getDeclaredField("POOL");
        campoPool.setAccessible(true);

        referencia = (AtomicReference<HikariDataSource>) campoPool.get(null);
        original = referencia.get();
        referencia.set(null);
    }

    @AfterEach
    void restaurar() throws Exception {
        referencia.set(original);
    }

    private void establecer(HikariDataSource pool) throws Exception {
        referencia.set(pool);
    }

    private Object privado(
            String nombre, Class<?>[] tipos, Object... valores)
            throws Exception {
        Method metodo = ConexionBD.class
            .getDeclaredMethod(nombre, tipos);
        metodo.setAccessible(true);
        return metodo.invoke(null, valores);
    }

    private String obligatorio(Properties p, String clave)
            throws Exception {
        return (String) privado("obligatorio",
            new Class<?>[]{Properties.class, String.class},
            p, clave);
    }

    private int entero(Properties p, String clave, int defecto)
            throws Exception {
        return (Integer) privado("entero",
            new Class<?>[]{
                Properties.class, String.class, int.class
            }, p, clave, defecto);
    }

    @Test
    void poolNoInicializadoTieneEstadoExplicito() {
        assertEquals("Pool no inicializado",
            ConexionBD.estadoPool());
    }

    @Test
    void poolCerradoTieneEstadoNoInicializado()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);
        when(pool.isClosed()).thenReturn(true);
        establecer(pool);

        assertEquals("Pool no inicializado",
            ConexionBD.estadoPool());
    }

    @Test
    void poolSinMetricasIndicaInicializacion()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);
        when(pool.isClosed()).thenReturn(false);
        when(pool.getHikariPoolMXBean()).thenReturn(null);
        establecer(pool);

        assertEquals("Pool inicializando",
            ConexionBD.estadoPool());
    }

    @Test
    void estadoIncluyeTodasLasMetricasDelPool()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);
        HikariPoolMXBean mx = mock(HikariPoolMXBean.class);

        when(pool.isClosed()).thenReturn(false);
        when(pool.getHikariPoolMXBean()).thenReturn(mx);

        when(mx.getTotalConnections()).thenReturn(7);
        when(mx.getActiveConnections()).thenReturn(2);
        when(mx.getIdleConnections()).thenReturn(5);
        when(mx.getThreadsAwaitingConnection()).thenReturn(1);

        establecer(pool);

        assertEquals(
            "total=7, activas=2, libres=5, esperando=1",
            ConexionBD.estadoPool());
    }

    @Test
    void cerrarPoolSinInicializarEsSeguro() {
        assertDoesNotThrow(ConexionBD::cerrarPool);
        assertEquals("Pool no inicializado",
            ConexionBD.estadoPool());
    }

    @Test
    void cerrarPoolLiberaYEliminaReferencia()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);
        establecer(pool);

        ConexionBD.cerrarPool();

        verify(pool).close();
        assertNull(referencia.get());
        assertEquals("Pool no inicializado",
            ConexionBD.estadoPool());
    }

    @Test
    void cerrarPoolDosVecesNoDuplicaCierre()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);
        establecer(pool);

        ConexionBD.cerrarPool();
        ConexionBD.cerrarPool();

        verify(pool, times(1)).close();
    }

    @Test
    void obtenerConexionReutilizaPoolExistente()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);
        Connection con = mock(Connection.class);

        when(pool.getConnection()).thenReturn(con);
        establecer(pool);

        assertSame(con, ConexionBD.getConexion());
        assertSame(con, ConexionBD.getConexion());
        verify(pool, times(2)).getConnection();
        assertSame(pool, referencia.get());
    }

    @Test
    void errorAlObtenerConexionSePropaga()
            throws Exception {
        HikariDataSource pool = mock(HikariDataSource.class);

        when(pool.getConnection())
            .thenThrow(new SQLException("Pool agotado QA"));

        establecer(pool);

        SQLException error = assertThrows(
            SQLException.class, ConexionBD::getConexion);

        assertEquals("Pool agotado QA", error.getMessage());
        assertSame(pool, referencia.get());
    }

    @Test
    void parametroObligatorioEliminaEspacios()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.user", "  usuario_qa  ");

        assertEquals("usuario_qa",
            obligatorio(p, "db.user"));
    }

    @Test
    void parametroObligatorioAusenteLanzaError()
            throws Exception {
        Properties p = new Properties();

        InvocationTargetException error = assertThrows(
            InvocationTargetException.class,
            () -> obligatorio(p, "db.url"));

        assertInstanceOf(SQLException.class, error.getCause());
        assertTrue(error.getCause().getMessage()
            .contains("db.url"));
    }

    @Test
    void parametroObligatorioEnBlancoLanzaError()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.password", "  ");

        InvocationTargetException error = assertThrows(
            InvocationTargetException.class,
            () -> obligatorio(p, "db.password"));

        assertInstanceOf(SQLException.class, error.getCause());
    }

    @Test
    void enteroAusenteUsaValorPorDefecto()
            throws Exception {
        assertEquals(4,
            entero(new Properties(), "db.pool.max", 4));
    }

    @Test
    void enteroEnBlancoUsaValorPorDefecto()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.pool.min", "  ");

        assertEquals(1,
            entero(p, "db.pool.min", 1));
    }

    @Test
    void enteroValidoSeConvierteCorrectamente()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.pool.max", " 8 ");

        assertEquals(8,
            entero(p, "db.pool.max", 4));
    }

    @Test
    void enteroNegativoSeConvierteSinValidarRango()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.pool.min", "-1");

        assertEquals(-1,
            entero(p, "db.pool.min", 1));
    }

    @Test
    void enteroNoNumericoProduceSQLException()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.pool.max", "incorrecto");

        InvocationTargetException error = assertThrows(
            InvocationTargetException.class,
            () -> entero(p, "db.pool.max", 4));

        assertInstanceOf(SQLException.class, error.getCause());
        assertInstanceOf(NumberFormatException.class,
            error.getCause().getCause());
    }

    @Test
    void enteroFueraDeRangoNumericoProduceSQLException()
            throws Exception {
        Properties p = new Properties();
        p.setProperty("db.pool.max", "99999999999999999999");

        InvocationTargetException error = assertThrows(
            InvocationTargetException.class,
            () -> entero(p, "db.pool.max", 4));

        assertInstanceOf(SQLException.class, error.getCause());
    }
}
