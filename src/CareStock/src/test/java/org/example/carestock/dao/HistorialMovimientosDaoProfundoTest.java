package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.DataTransferObject.MovimientoHistorial;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class HistorialMovimientosDaoProfundoTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
        new SesionUsuario.DatosUsuario(
            7, "Administrador", "qa@test.local",
            "ADMINISTRADOR", 8, "Farmacia QA");

    private static class Jdbc {
        final Connection con = mock(Connection.class);
        final PreparedStatement ps = mock(PreparedStatement.class);
        final ResultSet rs = mock(ResultSet.class);

        Jdbc() throws SQLException {
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
        }
    }

    @Test
    void permitePaginacionMinima() {
        assertDoesNotThrow(() ->
            HistorialMovimientosDao.validarPaginacion(1, 0));
    }

    @Test
    void permitePaginacionMaxima() {
        assertDoesNotThrow(() ->
            HistorialMovimientosDao.validarPaginacion(500, 10000));
    }

    @Test
    void rechazaLimitesInvalidos() {
        for (int limite : new int[]{-1, 0, 501, 1000}) {
            assertThrows(IllegalArgumentException.class,
                () -> HistorialMovimientosDao.validarPaginacion(
                    limite, 0));
        }
    }

    @Test
    void rechazaDesplazamientoNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> HistorialMovimientosDao.validarPaginacion(
                20, -1));
    }

    @Test
    void rechazaConexionNula() {
        assertThrows(NullPointerException.class,
            () -> HistorialMovimientosDao.consultar(
                null, 8, 20, 0));
    }

    @Test
    void rechazaFarmaciaInvalidaSinEjecutarSql() {
        Connection con = mock(Connection.class);

        assertThrows(IllegalArgumentException.class,
            () -> HistorialMovimientosDao.consultar(
                con, 0, 20, 0));

        verifyNoInteractions(con);
    }

    @Test
    void consultaVaciaRetornaListaVacia() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(false);

        List<MovimientoHistorial> resultado =
            HistorialMovimientosDao.consultar(
                jdbc.con, 8, 20, 0);

        assertTrue(resultado.isEmpty());
        verify(jdbc.ps).setInt(1, 8);
        verify(jdbc.ps).setInt(2, 20);
        verify(jdbc.ps).setInt(3, 0);
        verify(jdbc.rs).close();
        verify(jdbc.ps).close();
    }

    @Test
    void respetaLimiteYDesplazamiento() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(false);

        HistorialMovimientosDao.consultar(
            jdbc.con, 15, 500, 1000);

        verify(jdbc.ps).setInt(1, 15);
        verify(jdbc.ps).setInt(2, 500);
        verify(jdbc.ps).setInt(3, 1000);
    }

    @Test
    void convierteResultadoJdbcCorrectamente() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(true, false);
        when(jdbc.rs.getString("origen"))
            .thenReturn("MOVIMIENTOS_PRODUCTOS");
        when(jdbc.rs.getLong("id_origen")).thenReturn(51L);
        when(jdbc.rs.getString("categoria")).thenReturn("ASEO");
        when(jdbc.rs.getString("tipo_movimiento"))
            .thenReturn("SALIDA");
        when(jdbc.rs.getInt("id_referencia")).thenReturn(5);
        when(jdbc.rs.getString("producto")).thenReturn("Jabon");
        when(jdbc.rs.getString("referencia")).thenReturn("ASE-05");
        when(jdbc.rs.getLong("cantidad")).thenReturn(3L);
        when(jdbc.rs.getString("motivo")).thenReturn("Venta");
        when(jdbc.rs.getObject("stock_anterior")).thenReturn(10);
        when(jdbc.rs.getObject("stock_posterior")).thenReturn(7);
        when(jdbc.rs.getObject("diferencia")).thenReturn(-3L);
        when(jdbc.rs.getInt("id_farmacia")).thenReturn(8);
        when(jdbc.rs.getInt("id_usuario")).thenReturn(7);
        when(jdbc.rs.getString("usuario")).thenReturn("QA");

        OffsetDateTime fecha =
            OffsetDateTime.parse("2026-11-08T12:00:00-05:00");

        when(jdbc.rs.getObject(
            "fecha_movimiento", OffsetDateTime.class))
            .thenReturn(fecha);

        var filas = HistorialMovimientosDao.consultar(
            jdbc.con, 8, 20, 0);

        assertEquals(1, filas.size());
        MovimientoHistorial m = filas.get(0);

        assertAll(
            () -> assertEquals(
                "MOVIMIENTOS_PRODUCTOS:51", m.claveUnica()),
            () -> assertEquals("ASEO", m.categoria()),
            () -> assertEquals("SALIDA", m.tipoMovimiento()),
            () -> assertEquals("Jabon", m.producto()),
            () -> assertEquals("ASE-05", m.referencia()),
            () -> assertEquals(3L, m.cantidad()),
            () -> assertEquals(10, m.stockAnterior()),
            () -> assertEquals(7, m.stockPosterior()),
            () -> assertEquals(-3L, m.diferencia()),
            () -> assertEquals(8, m.idFarmacia()),
            () -> assertEquals(fecha, m.fechaMovimiento())
        );
    }

    @Test
    void permiteCamposDeStockNulos() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(true, false);
        when(jdbc.rs.getString("origen"))
            .thenReturn("LOG_MOVIMIENTOS");
        when(jdbc.rs.getLong("id_origen")).thenReturn(7L);

        var filas = HistorialMovimientosDao.consultar(
            jdbc.con, 8, 10, 0);

        assertEquals(1, filas.size());
        assertNull(filas.get(0).stockAnterior());
        assertNull(filas.get(0).stockPosterior());
        assertNull(filas.get(0).diferencia());
        assertEquals("LOG_MOVIMIENTOS:7",
            filas.get(0).claveUnica());
    }

    @Test
    void retornaListaInmutable() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(false);

        var resultado = HistorialMovimientosDao.consultar(
            jdbc.con, 8, 10, 0);

        assertThrows(UnsupportedOperationException.class,
            () -> resultado.add(null));
    }

    @Test
    void propagaErrorSqlYCierraRecursos() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.ps.executeQuery())
            .thenThrow(new SQLException("Fallo de consulta"));

        SQLException error = assertThrows(
            SQLException.class,
            () -> HistorialMovimientosDao.consultar(
                jdbc.con, 8, 10, 0));

        assertEquals("Fallo de consulta", error.getMessage());
        verify(jdbc.ps).close();
    }

    @Test
    void rechazaConsultaSinSesion() {
        try (MockedStatic<SesionUsuario> sesion =
                 mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> bd =
                 mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(false);

            assertThrows(IllegalStateException.class,
                () -> new HistorialMovimientosDao().listar(20, 0));

            bd.verifyNoInteractions();
        }
    }

    @Test
    void rechazaUsuarioSinFarmacia() {
        var usuario = new SesionUsuario.DatosUsuario(
            7, "QA", "qa@test.local",
            "ADMINISTRADOR", null, null);

        try (MockedStatic<SesionUsuario> sesion =
                 mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                .thenReturn(usuario);

            assertThrows(IllegalStateException.class,
                () -> new HistorialMovimientosDao().listar(10, 0));
        }
    }

    @Test
    void consultaRespetaFarmaciaDeSesion() throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(false);

        try (MockedStatic<SesionUsuario> sesion =
                 mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> bd =
                 mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                .thenReturn(ADMIN);
            bd.when(ConexionBD::getConexion)
                .thenReturn(jdbc.con);

            var resultado = new HistorialMovimientosDao()
                .listar(25, 50);

            assertTrue(resultado.isEmpty());
            verify(jdbc.ps).setInt(1, 8);
            verify(jdbc.ps).setInt(2, 25);
            verify(jdbc.ps).setInt(3, 50);
            verify(jdbc.con).close();
        }
    }

    @Test
    void detectaCierreDeSesionDuranteConsulta()
            throws Exception {
        Jdbc jdbc = new Jdbc();
        when(jdbc.rs.next()).thenReturn(false);

        try (MockedStatic<SesionUsuario> sesion =
                 mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> bd =
                 mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true, false);
            sesion.when(SesionUsuario::actual)
                .thenReturn(ADMIN);
            bd.when(ConexionBD::getConexion)
                .thenReturn(jdbc.con);

            assertThrows(IllegalStateException.class,
                () -> new HistorialMovimientosDao().listar(10, 0));

            verify(jdbc.ps).executeQuery();
            verify(jdbc.con).close();
        }
    }
}
