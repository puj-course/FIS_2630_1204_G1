package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SmsCurlCoberturaExtraTest {

    private static final String CUENTA = "AC" + "a".repeat(32);
    private static final String TOKEN = "abcdef0123456789abcdef0123456789";
    private static final String REMITENTE = "+15005550006";
    private static final String DESTINO = "+573001234567";
    private static final String MENSAJE = "sms_appointment_reminders";

    private DestinatariosPermitidosSms permitidos() {
        return new DestinatariosPermitidosSms(Set.of(DESTINO));
    }

    private ServicioSmsTwilioCurl crear(
            ServicioSmsTwilioCurl.Ejecutor ejecutor) {
        return new ServicioSmsTwilioCurl(
                CUENTA, TOKEN, REMITENTE, permitidos(), ejecutor);
    }

    private ServicioSmsTwilioCurl simular(int codigo, String texto) {
        return crear(config ->
                new ServicioSmsTwilioCurl.Salida(codigo, texto));
    }

    @Test
    void constructorPublicoSinEnvio() {
        assertNotNull(new ServicioSmsTwilioCurl(
                CUENTA, TOKEN, REMITENTE, permitidos()));
    }

    @Test
    void cuentaNulaOInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsTwilioCurl(
                        null, TOKEN, REMITENTE, permitidos(), c -> null));

        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsTwilioCurl(
                        "INVALIDA", TOKEN, REMITENTE, permitidos(), c -> null));
    }

    @Test
    void tokenNuloOInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsTwilioCurl(
                        CUENTA, null, REMITENTE, permitidos(), c -> null));

        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsTwilioCurl(
                        CUENTA, "INVALIDO", REMITENTE, permitidos(), c -> null));
    }

    @Test
    void respuestaSinMarcaHttp() {
        assertThrows(ExcepcionSms.class,
                () -> simular(0, "{\"sid\":\"invalido\"}")
                        .enviar(DESTINO, MENSAJE));
    }

    @Test
    void respuestaConEstadoInvalido() {
        assertThrows(ExcepcionSms.class,
                () -> simular(0, "{}__CARESTOCK_HTTP__:ABC")
                        .enviar(DESTINO, MENSAJE));
    }

    @Test
    void rechazoHttp400() {
        assertThrows(ExcepcionSms.class,
                () -> simular(0,
                        "{\"code\":21608,\"message\":\"Error\"}"
                        + "__CARESTOCK_HTTP__:400")
                        .enviar(DESTINO, MENSAJE));
    }

    @Test
    void respuestaExitosaSinSid() {
        assertThrows(ExcepcionSms.class,
                () -> simular(0, "{}__CARESTOCK_HTTP__:201")
                        .enviar(DESTINO, MENSAJE));
    }

    @Test
    void respuestaConTextoNulo() {
        assertThrows(ExcepcionSms.class,
                () -> simular(0, null)
                        .enviar(DESTINO, MENSAJE));
    }

    @Test
    void capturaFalloDeEntradaSalida() {
        ServicioSmsTwilioCurl sms = crear(config -> {
            throw new IOException("Fallo simulado");
        });

        assertThrows(ExcepcionSms.class,
                () -> sms.enviar(DESTINO, MENSAJE));
    }

    @Test
    void capturaInterrupcion() {
        ServicioSmsTwilioCurl sms = crear(config -> {
            throw new InterruptedException("Prueba");
        });

        try {
            assertThrows(ExcepcionSms.class,
                    () -> sms.enviar(DESTINO, MENSAJE));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void fabricaRechazaProveedorSinConfirmacion() {
        Map<String, String> entorno = new HashMap<>();
        entorno.put("SMS_PROVIDER", "twilio-curl");
        entorno.put("SMS_ALLOWED_TO", DESTINO);

        assertThrows(IllegalStateException.class,
                () -> FabricaServicioSms.desdeConfiguracion(entorno));
    }

    @Test
    void fabricaConstruyeProveedorSinEnviar() {
        Map<String, String> entorno = new HashMap<>();
        entorno.put("SMS_PROVIDER", "twilio-curl");
        entorno.put("SMS_ALLOWED_TO", DESTINO);
        entorno.put("SMS_CONFIRM_REAL_SEND", "SI");
        entorno.put("TWILIO_ACCOUNT_SID", CUENTA);
        entorno.put("TWILIO_AUTH_TOKEN", TOKEN);
        entorno.put("TWILIO_FROM_NUMBER", REMITENTE);

        assertInstanceOf(ServicioSmsTwilioCurl.class,
                FabricaServicioSms.desdeConfiguracion(entorno));
    }
}
