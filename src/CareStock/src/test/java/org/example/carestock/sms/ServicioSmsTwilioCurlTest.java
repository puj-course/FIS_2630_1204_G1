package org.example.carestock.sms;

import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsTwilioCurlTest {

    private static final String CUENTA =
            "AC" + "1".repeat(32);
    private static final String DESTINO =
            "+573001234567";
    private static final String SID =
            "SM" + "2".repeat(32);

    private final DestinatariosPermitidosSms autorizados =
            new DestinatariosPermitidosSms(Set.of(DESTINO));

    @Test
    void plantillaTrialGeneraSolicitudCurlYGuardaSid() {
        var sms = new ServicioSmsTwilioCurl(
                CUENTA, "tokenPrueba12345678",
                "+15005550006", autorizados,
                configuracion -> {
                    assertTrue(configuracion.contains(
                            "data-urlencode = \"Body=sms_appointment_reminders\""));
                    assertTrue(configuracion.contains(
                            "data-urlencode = \"To=" + DESTINO + "\""));
                    assertTrue(configuracion.contains(
                            "data-urlencode = \"From=+15005550006\""));
                    assertTrue(configuracion.contains(
                            "user = \"" + CUENTA + ":"));

                    return new ServicioSmsTwilioCurl.Salida(
                            0, "{\"sid\":\"" + SID
                            + "\"}__CARESTOCK_HTTP__:201");
                });

        ResultadoSms resultado = sms.enviar(
                DESTINO, "sms_appointment_reminders");

        assertTrue(resultado.aceptado());
        assertFalse(resultado.simulado());
        assertEquals(201, resultado.codigoHttp());
        assertEquals(SID, resultado.referencia());
    }

    @Test
    void rechazaMensajesNoPermitidosPorTrial() {
        var sms = crearSinEnvio();

        assertThrows(IllegalArgumentException.class,
                () -> sms.enviar(DESTINO, "Mensaje personalizado"));
    }

    @Test
    void rechazaDestinatarioNoAutorizado() {
        var sms = crearSinEnvio();

        assertThrows(SecurityException.class,
                () -> sms.enviar(
                        "+573009999999",
                        "sms_appointment_reminders"));
    }

    @Test
    void conservaCodigoHttpSinRevelarDatosSensibles() {
        var sms = new ServicioSmsTwilioCurl(
                CUENTA, "tokenPrueba12345678",
                "+15005550006", autorizados,
                configuracion ->
                    new ServicioSmsTwilioCurl.Salida(
                            0,
                            "{\"code\":572006,"
                            + "\"message\":\"SECRETO\"}"
                            + "__CARESTOCK_HTTP__:400"));

        ExcepcionSms error = assertThrows(
                ExcepcionSms.class,
                () -> sms.enviar(
                        DESTINO, "sms_appointment_reminders"));

        assertTrue(error.getMessage().contains("HTTP 400"));
        assertTrue(error.getMessage().contains("572006"));
        assertFalse(error.getMessage().contains("SECRETO"));
    }

    @Test
    void errorCurlNoSeMarcaComoAceptado() {
        var sms = new ServicioSmsTwilioCurl(
                CUENTA, "tokenPrueba12345678",
                "+15005550006", autorizados,
                configuracion ->
                    new ServicioSmsTwilioCurl.Salida(28, ""));

        assertThrows(ExcepcionSms.class,
                () -> sms.enviar(
                        DESTINO, "sms_appointment_reminders"));
    }

    private ServicioSmsTwilioCurl crearSinEnvio() {
        return new ServicioSmsTwilioCurl(
                CUENTA, "tokenPrueba12345678",
                "+15005550006", autorizados,
                configuracion -> {
                    fail("No debe ejecutarse curl");
                    return null;
                });
    }
}
