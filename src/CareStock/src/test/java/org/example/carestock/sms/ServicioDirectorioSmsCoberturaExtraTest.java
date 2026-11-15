package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ServicioDirectorioSmsCoberturaExtraTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    3, "Admin QA", "admin@qa.test",
                    "ADMINISTRADOR", 5, "Farmacia QA");

    @Test
    void deniegaAccesoSinAdministrador() {
        var auxiliar = new SesionUsuario.DatosUsuario(
                3, "Auxiliar", "aux@test.local",
                "AUXILIAR", 5, "Farmacia QA");

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::actual)
                    .thenReturn(auxiliar);

            assertThrows(SecurityException.class,
                    () -> new ServicioDirectorioSms().listar());

            db.verifyNoInteractions();
        }
    }

    @Test
    void deniegaAccesoSinFarmacia() {
        var usuario = new SesionUsuario.DatosUsuario(
                3, "QA", "qa@test.local",
                "ADMINISTRADOR", 0, "Sin farmacia");

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::actual)
                    .thenReturn(usuario);

            assertThrows(SecurityException.class,
                    () -> new ServicioDirectorioSms().listar());
        }
    }

    @Test
    void listarSinRegistrosDevuelveListaVacia() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
            db.when(ConexionBD::getConexion)
                    .thenReturn(con);

            var resultado = new ServicioDirectorioSms().listar();

            assertTrue(resultado.isEmpty());
            verify(ps).setInt(1, 3);
            verify(ps).setInt(2, 5);
            verify(rs).close();
            verify(ps).close();
        }
    }

    @Test
    void consentimientoSinFechaNoAutoriza() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject("id_destinatario")).thenReturn(99);
        when(rs.getBoolean("autorizado")).thenReturn(true);
        when(rs.getTimestamp("autorizado_en")).thenReturn(null);

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
            db.when(ConexionBD::getConexion)
                    .thenReturn(con);

            var destinatario =
                    new ServicioDirectorioSms().listar().get(0);

            assertTrue(destinatario.configurado());
            assertFalse(destinatario.autorizado());
        }
    }

    @Test
    void deshabilitarExigeRegistroExistente() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1, 0);

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class)) {

            sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
            db.when(ConexionBD::getConexion)
                    .thenReturn(con);

            var directorio = new ServicioDirectorioSms();

            assertDoesNotThrow(
                    () -> directorio.deshabilitar(11));

            verify(ps).setInt(1, 11);
            verify(ps).setInt(2, 5);
            verify(ps).setInt(3, 3);

            assertThrows(SecurityException.class,
                    () -> directorio.deshabilitar(11));

            verify(ps, times(2)).executeUpdate();
        }
    }
}
