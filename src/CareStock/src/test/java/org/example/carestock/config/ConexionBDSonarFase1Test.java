package org.example.carestock.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConexionBDSonarFase1Test {
    @SuppressWarnings("unchecked")
    private static AtomicReference<HikariDataSource> referencia() throws Exception {
        Field campo = ConexionBD.class.getDeclaredField("POOL");
        campo.setAccessible(true);
        return (AtomicReference<HikariDataSource>) campo.get(null);
    }

    @Test
    void devuelveConexionDesdeElPoolYaInicializado() throws Exception {
        AtomicReference<HikariDataSource> referencia = referencia();
        HikariDataSource anterior = referencia.get();
        HikariDataSource pool = mock(HikariDataSource.class);
        Connection conexion = mock(Connection.class);
        when(pool.getConnection()).thenReturn(conexion);
        try {
            referencia.set(pool);
            assertSame(conexion, ConexionBD.getConexion());
            verify(pool).getConnection();
        } finally {
            referencia.set(anterior);
        }
    }

    @Test
    void estadoDelPoolDistingueInicializacionYEstadisticas() throws Exception {
        AtomicReference<HikariDataSource> referencia = referencia();
        HikariDataSource anterior = referencia.get();
        HikariDataSource pool = mock(HikariDataSource.class);
        try {
            referencia.set(null);
            assertEquals("Pool no inicializado", ConexionBD.estadoPool());

            referencia.set(pool);
            when(pool.isClosed()).thenReturn(true, false, false);
            assertEquals("Pool no inicializado", ConexionBD.estadoPool());
            when(pool.getHikariPoolMXBean()).thenReturn(null);
            assertEquals("Pool inicializando", ConexionBD.estadoPool());

            HikariPoolMXBean estadisticas = mock(HikariPoolMXBean.class);
            when(pool.getHikariPoolMXBean()).thenReturn(estadisticas);
            when(estadisticas.getTotalConnections()).thenReturn(5);
            when(estadisticas.getActiveConnections()).thenReturn(2);
            when(estadisticas.getIdleConnections()).thenReturn(3);
            when(estadisticas.getThreadsAwaitingConnection()).thenReturn(1);
            assertEquals("total=5, activas=2, libres=3, esperando=1", ConexionBD.estadoPool());
        } finally {
            referencia.set(anterior);
        }
    }

    @Test
    void cerrarPoolLiberaLaReferenciaUnaSolaVez() throws Exception {
        AtomicReference<HikariDataSource> referencia = referencia();
        HikariDataSource anterior = referencia.get();
        HikariDataSource pool = mock(HikariDataSource.class);
        try {
            referencia.set(pool);
            ConexionBD.cerrarPool();
            assertNull(referencia.get());
            verify(pool).close();
            ConexionBD.cerrarPool();
            verify(pool, times(1)).close();
        } finally {
            referencia.set(anterior);
        }
    }
}
