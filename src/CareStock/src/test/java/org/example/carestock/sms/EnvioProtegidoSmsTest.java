package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnvioProtegidoSmsTest {

    private record Escenario(
            Connection conexion,
            PreparedStatement bloqueo,
            PreparedStatement actualizar,
            ResultSet resultado) {}

    private Escenario escenario() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement bloqueo = mock(PreparedStatement.class);
        PreparedStatement actualizar = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            return sql.contains("SELECT a.estado")
                    ? bloqueo : actualizar;
        });

        when(bloqueo.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getBoolean("activa")).thenReturn(true);
        when(rs.getString("estado")).thenReturn("EN_PROCESO");
        when(rs.getString("clave")).thenReturn("STOCK:1");

        when(rs.getBoolean("destinatario_activo")).thenReturn(true);
        when(rs.getBoolean("autorizado")).thenReturn(true);
        when(rs.getBoolean("notifica_stock_critico")).thenReturn(true);
        when(rs.getString("usuario_estado")).thenReturn("ACTIVO");
        when(rs.getString("farmacia_estado")).thenReturn("ACTIVA");

        when(rs.getTimestamp("autorizado_en"))
                .thenReturn(Timestamp.valueOf("2026-10-01 10:00:00"));

        when(rs.getString("telefono")).thenReturn("+573001234567");
        when(rs.getString("mensaje")).thenReturn("Alerta de stock");

        return new Escenario(con, bloqueo, actualizar, rs);
    }

    @Test
    void rechazaConfiguracionNula() {
        ServicioSms proveedor = (numero, mensaje) ->
                new ResultadoSms(true, true, 0, "SIM-1");

        assertThrows(NullPointerException.class,
                () -> new EnvioProtegidoSms(proveedor, null));
    }

    @Test
    void alertaInexistenteNoProduceEnvio() throws Exception {
        Escenario e = escenario();
        when(e.resultado().next()).thenReturn(false);
        AtomicInteger envios = new AtomicInteger();

        ServicioSms proveedor = (numero, mensaje) -> {
            envios.incrementAndGet();
            return new ResultadoSms(true, true, 0, "SIM-1");
        };

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(e.conexion());

            new EnvioProtegidoSms(proveedor, Map.of()).procesar(10);

            assertEquals(0, envios.get());
            verify(e.conexion()).commit();
        }
    }

    @Test
    void alertaSinConsentimientoNoSeEnvia() throws Exception {
        Escenario e = escenario();
        when(e.resultado().getBoolean("autorizado")).thenReturn(false);

        AtomicInteger envios = new AtomicInteger();
        ServicioSms proveedor = (numero, mensaje) -> {
            envios.incrementAndGet();
            return new ResultadoSms(true, true, 0, "SIM-1");
        };

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(e.conexion());

            new EnvioProtegidoSms(proveedor, Map.of()).procesar(10);

            assertEquals(0, envios.get());
            verify(e.actualizar()).setString(1, "FALLIDA");
            verify(e.conexion()).commit();
        }
    }

    @Test
    void enviaAlertaCriticaSimulada() throws Exception {
        Escenario e = escenario();
        AtomicInteger envios = new AtomicInteger();

        ServicioSms proveedor = (numero, mensaje) -> {
            assertEquals("+573001234567", numero);
            assertEquals("sms_internal_alerts", mensaje);
            envios.incrementAndGet();
            return new ResultadoSms(true, true, 0, "SIM-100");
        };

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(e.conexion());

            new EnvioProtegidoSms(
                    proveedor, Map.of("SMS_MODO_TRIAL", "SI"))
                    .procesar(20);

            assertEquals(1, envios.get());
            verify(e.actualizar()).setString(1, "SIMULADA");
            verify(e.actualizar()).setString(2, "SIM-100");
            verify(e.conexion()).commit();
            verify(e.conexion(), never()).rollback();
        }
    }

    @Test
    void respuestaRechazadaQuedaComoFallida() throws Exception {
        Escenario e = escenario();

        ServicioSms proveedor = (numero, mensaje) ->
                new ResultadoSms(false, false, 400, "ERR-400");

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(e.conexion());

            new EnvioProtegidoSms(proveedor, Map.of()).procesar(30);

            verify(e.actualizar()).setString(1, "FALLIDA");
            verify(e.conexion()).commit();
        }
    }

    @Test
    void errorDelProveedorSeRegistraSinAbortarTransaccion() throws Exception {
        Escenario e = escenario();

        ServicioSms proveedor = (numero, mensaje) -> {
            throw new IllegalStateException("Proveedor no disponible");
        };

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(e.conexion());

            new EnvioProtegidoSms(proveedor, Map.of()).procesar(40);

            verify(e.actualizar()).setString(1, "FALLIDA");
            verify(e.conexion()).commit();
        }
    }

    @Test
    void errorSqlHaceRollback() throws Exception {
        Escenario e = escenario();
        when(e.bloqueo().executeQuery())
                .thenThrow(new SQLException("Fallo de consulta"));

        ServicioSms proveedor = (numero, mensaje) ->
                new ResultadoSms(true, true, 0, "SIM-1");

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(e.conexion());

            assertThrows(SQLException.class,
                    () -> new EnvioProtegidoSms(proveedor, Map.of())
                            .procesar(50));

            verify(e.conexion()).rollback();
        }
    }
}
