package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HistorialJdbcCoberturaTest {

    private SesionUsuario.DatosUsuario sesion(int farmacia) {
        return new SesionUsuario.DatosUsuario(
                9, "Administrador", "qa@test.com",
                "ADMINISTRADOR", farmacia, "Farmacia QA");
    }

    @Test
    void paginacionRechazaValoresIncorrectos() {
        for (int limite : new int[]{-1, 0, 501}) {
            assertThrows(IllegalArgumentException.class,
                    () -> HistorialMovimientosDao
                            .validarPaginacion(limite, 0));
        }

        assertThrows(IllegalArgumentException.class,
                () -> HistorialMovimientosDao
                        .validarPaginacion(50, -1));

        assertDoesNotThrow(() ->
                HistorialMovimientosDao.validarPaginacion(500, 0));
    }

    @Test
    void consultaJdbcFiltraPorFarmaciaYPagina() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getString("origen")).thenReturn("LOG_MOVIMIENTOS");
        when(rs.getLong("id_origen")).thenReturn(51L);
        when(rs.getString("categoria")).thenReturn("MEDICAMENTO");
        when(rs.getString("tipo_movimiento")).thenReturn("SALIDA");
        when(rs.getInt("id_referencia")).thenReturn(12);
        when(rs.getString("producto")).thenReturn("Acetaminofen");
        when(rs.getString("referencia")).thenReturn("LOTE-01");
        when(rs.getLong("cantidad")).thenReturn(2L);
        when(rs.getString("motivo")).thenReturn("Dispensacion");
        when(rs.getObject("stock_anterior")).thenReturn(5);
        when(rs.getObject("stock_posterior")).thenReturn(3);
        when(rs.getObject("diferencia")).thenReturn(-2L);
        when(rs.getInt("id_farmacia")).thenReturn(7);
        when(rs.getInt("id_usuario")).thenReturn(9);
        when(rs.getString("usuario")).thenReturn("Administrador");
        when(rs.getObject("fecha_movimiento", OffsetDateTime.class))
                .thenReturn(OffsetDateTime.parse(
                        "2026-10-30T12:00:00-05:00"));

        var resultado = HistorialMovimientosDao
                .consultar(con, 7, 20, 40);

        assertEquals(1, resultado.size());
        assertEquals("LOG_MOVIMIENTOS:51",
                resultado.get(0).claveUnica());
        assertEquals("MEDICAMENTO",
                resultado.get(0).categoria());
        assertEquals(7, resultado.get(0).idFarmacia());

        verify(ps).setInt(1, 7);
        verify(ps).setInt(2, 20);
        verify(ps).setInt(3, 40);
        verify(ps).close();
    }

    @Test
    void noPermiteConsultaSinSesion() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::estaActiva).thenReturn(false);

            assertThrows(IllegalStateException.class, () ->
                    new HistorialMovimientosDao().listar(20, 0));

            bd.verifyNoInteractions();
        }
    }

    @Test
    void noPermiteConsultaSinFarmaciaAsignada() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::estaActiva).thenReturn(true);
            usuario.when(SesionUsuario::actual)
                    .thenReturn(sesion(0));

            assertThrows(IllegalStateException.class, () ->
                    new HistorialMovimientosDao().listar(20, 0));

            bd.verifyNoInteractions();
        }
    }

    @Test
    void detectaCambioDeSesionDuranteConsulta() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::estaActiva).thenReturn(true);
            usuario.when(SesionUsuario::actual).thenReturn(
                    sesion(7), sesion(99));

            assertThrows(IllegalStateException.class, () ->
                    new HistorialMovimientosDao().listar(20, 0));
        }
    }

    @Test
    void conexionYFarmaciaSonObligatorias() {
        assertThrows(NullPointerException.class,
                () -> HistorialMovimientosDao
                        .consultar(null, 7, 20, 0));

        Connection con = mock(Connection.class);

        assertThrows(IllegalArgumentException.class,
                () -> HistorialMovimientosDao
                        .consultar(con, 0, 20, 0));
    }
}
