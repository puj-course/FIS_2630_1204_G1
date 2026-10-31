package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DirectorioSmsCoberturaTest {

    private SesionUsuario.DatosUsuario sesion(String rol, int farmacia) {
        return new SesionUsuario.DatosUsuario(
                9, "Usuario QA", "qa@test.com",
                rol, farmacia, "Farmacia QA");
    }

    @Test
    void administradorListaSoloSuFarmacia() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getObject("id_destinatario")).thenReturn(21);
        when(rs.getInt("id_usuario")).thenReturn(12);
        when(rs.getString("nombre_completo")).thenReturn("Responsable QA");
        when(rs.getString("email")).thenReturn("responsable@test.com");
        when(rs.getString("telefono")).thenReturn("+573001234567");
        when(rs.getBoolean("autorizado")).thenReturn(true);
        when(rs.getTimestamp("autorizado_en"))
                .thenReturn(Timestamp.valueOf("2026-10-30 10:00:00"));
        when(rs.getBoolean("notifica_stock_critico")).thenReturn(true);
        when(rs.getBoolean("notifica_vencimiento_critico"))
                .thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", 7));

            var lista = new ServicioDirectorioSms().listar();

            assertEquals(1, lista.size());
            assertEquals(12, lista.get(0).idUsuario());
            assertTrue(lista.get(0).autorizado());
            assertTrue(lista.get(0).configurado());
            assertTrue(lista.get(0).stock());
            assertFalse(lista.get(0).vencimientos());

            verify(ps).setInt(1, 9);
            verify(ps).setInt(2, 7);
        }
    }

    @Test
    void administradorDeshabilitaDestinatario() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", 7));

            assertDoesNotThrow(() ->
                    new ServicioDirectorioSms().deshabilitar(12));

            verify(ps).setInt(1, 12);
            verify(ps).setInt(2, 7);
            verify(ps).setInt(3, 9);
        }
    }

    @Test
    void deshabilitacionNoAutorizadaEsRechazada() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", 7));

            assertThrows(SecurityException.class, () ->
                    new ServicioDirectorioSms().deshabilitar(12));
        }
    }

    @Test
    void farmaceuticoNoPuedeAdministrarDirectorio() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::actual)
                    .thenReturn(sesion("FARMACEUTICO", 7));

            ServicioDirectorioSms directorio =
                    new ServicioDirectorioSms();

            assertThrows(SecurityException.class,
                    directorio::listar);
            assertThrows(SecurityException.class,
                    () -> directorio.deshabilitar(12));

            bd.verifyNoInteractions();
        }
    }

    @Test
    void administradorSinFarmaciaEsRechazado() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", 0));

            assertThrows(SecurityException.class, () ->
                    new ServicioDirectorioSms().listar());

            bd.verifyNoInteractions();
        }
    }
}
