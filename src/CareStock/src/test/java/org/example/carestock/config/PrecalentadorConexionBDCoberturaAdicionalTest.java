package org.example.carestock.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrecalentadorConexionBDCoberturaAdicionalTest {

    private AtomicBoolean iniciado;
    private boolean original;
    private AtomicReference<HikariDataSource> pool;
    private HikariDataSource poolOriginal;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() throws Exception {
        Field estado = PrecalentadorConexionBD.class
                .getDeclaredField("INICIADO");
        estado.setAccessible(true);

        iniciado = (AtomicBoolean) estado.get(null);
        original = iniciado.getAndSet(false);

        Field campo = ConexionBD.class
                .getDeclaredField("POOL");
        campo.setAccessible(true);

        pool = (AtomicReference<HikariDataSource>)
                campo.get(null);

        poolOriginal = pool.get();
    }

    @AfterEach
    void restaurar() {
        iniciado.set(original);
        pool.set(poolOriginal);
    }

    @Test
    void precalientaConexionValida() throws Exception {
        HikariDataSource ds = mock(HikariDataSource.class);
        Connection conexion = mock(Connection.class);
        CountDownLatch cierre = new CountDownLatch(1);

        when(ds.getConnection()).thenReturn(conexion);
        when(conexion.isValid(3)).thenReturn(true);

        doAnswer(invocacion -> {
            cierre.countDown();
            return null;
        }).when(conexion).close();

        pool.set(ds);

        PrecalentadorConexionBD.iniciarAsync();

        assertTrue(cierre.await(10, TimeUnit.SECONDS));

        verify(conexion).isValid(3);
        verify(conexion).close();
        assertTrue(iniciado.get());

        PrecalentadorConexionBD.iniciarAsync();

        verify(ds, times(1)).getConnection();
    }

    @Test
    void manejaConexionInvalida() throws Exception {
        HikariDataSource ds = mock(HikariDataSource.class);
        Connection conexion = mock(Connection.class);
        CountDownLatch cierre = new CountDownLatch(1);

        when(ds.getConnection()).thenReturn(conexion);
        when(conexion.isValid(3)).thenReturn(false);

        doAnswer(invocacion -> {
            cierre.countDown();
            return null;
        }).when(conexion).close();

        pool.set(ds);

        PrecalentadorConexionBD.iniciarAsync();

        assertTrue(cierre.await(10, TimeUnit.SECONDS));

        verify(conexion).isValid(3);
        verify(conexion).close();
    }

    @Test
    void capturaErrorSqlSinPropagarlo() throws Exception {
        HikariDataSource ds = mock(HikariDataSource.class);
        CountDownLatch intento = new CountDownLatch(1);

        when(ds.getConnection()).thenAnswer(invocacion -> {
            intento.countDown();
            throw new SQLException("Conexion QA fallida");
        });

        pool.set(ds);

        PrecalentadorConexionBD.iniciarAsync();

        assertTrue(intento.await(10, TimeUnit.SECONDS));
        assertTrue(iniciado.get());

        PrecalentadorConexionBD.iniciarAsync();

        verify(ds, times(1)).getConnection();
    }
}
