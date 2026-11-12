package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistroInicioSesionSmsTest {
    private static final Instant INSTANTE =
            Instant.parse("2026-11-11T19:50:00Z");

    private static SesionUsuario.DatosUsuario sesion(Integer farmacia) {
        return new SesionUsuario.DatosUsuario(10, "Ana", "ana@test",
                "ADMINISTRADOR", farmacia, "Central");
    }

    @Test
    void encolaSoloParaDestinatariosAutorizadosAdministradores() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);
            int cantidad = new RegistroInicioSesionSms().registrar(
                    sesion(1), INSTANTE);
            assertEquals(1, cantidad);
            verify(ps).setString(eq(1), startsWith("ACCESO:"));
            verify(ps).setString(eq(2), contains("11/11/2026 14:50"));
            verify(ps).setInt(3, 10);
            verify(ps).setInt(4, 1);
            verify(con).close();
            verify(ps).close();
        }
    }

    @Test
    void sinFarmaciaNoEncolaNiAbreConexion() throws Exception {
        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            assertEquals(0, new RegistroInicioSesionSms().registrar(
                    sesion(null), INSTANTE));
            bd.verifyNoInteractions();
        }
    }

    @Test
    void errorSqlSePropagaAlLlamadorParaTratamientoNoBloqueante()
            throws Exception {
        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenThrow(
                new SQLException("Sin conexion"));
        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);
            assertThrows(SQLException.class,
                    () -> new RegistroInicioSesionSms().registrar(
                            sesion(1), INSTANTE));
            verify(con).close();
        }
    }

    @Test
    void requiereSesionEInstante() {
        var servicio = new RegistroInicioSesionSms();
        assertThrows(NullPointerException.class,
                () -> servicio.registrar(null, INSTANTE));
        assertThrows(NullPointerException.class,
                () -> servicio.registrar(sesion(1), null));
    }
}
