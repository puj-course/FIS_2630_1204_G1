package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsTest {

    private static final String TELEFONO = "+573001234567";
    private static final String CUENTA = "AC" + "1".repeat(32);
    private static final String SID = "SM" + "2".repeat(32);

    private final DestinatariosPermitidosSms destinatarios =
            new DestinatariosPermitidosSms(Set.of(TELEFONO));

    @Test
    void simuladoNoEnviaSmsYDevuelveReferencia() {

        ResultadoSms respuesta =
                new ServicioSmsSimulado(destinatarios)
                        .enviar(TELEFONO, "Aviso de prueba");

        assertTrue(respuesta.aceptado());
        assertTrue(respuesta.simulado());
        assertTrue(respuesta.referencia().startsWith("SIM-"));
    }

    @Test
    void rechazaTelefonoInvalidoYNoAutorizado() {

        ServicioSms servicio =
                new ServicioSmsSimulado(destinatarios);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.enviar("3001234567", "Aviso"));

        assertThrows(SecurityException.class,
                () -> servicio.enviar("+573009999999", "Aviso"));
    }

    @Test
    void rechazaMensajesVacios() {

        ServicioSms servicio =
                new ServicioSmsSimulado(destinatarios);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.enviar(TELEFONO, "  "));
    }

    @Test
    void twilioFormaSolicitudHttpYSuRespuesta() {

        ServicioSmsTwilio servicio = new ServicioSmsTwilio(
                CUENTA,
                "token-de-prueba",
                "+15005550006",
                destinatarios,
                solicitud -> {

                    assertEquals("POST", solicitud.method());

                    assertEquals(
                        "https://api.twilio.com/2010-04-01/Accounts/"
                        + CUENTA + "/Messages.json",
                        solicitud.uri().toString()
                    );

                    assertTrue(
                        solicitud.headers()
                            .firstValue("Authorization")
                            .orElse("")
                            .startsWith("Basic ")
                    );

                    assertTrue(
                        solicitud.headers()
                            .firstValue("Content-Type")
                            .orElse("")
                            .startsWith(
                                "application/x-www-form-urlencoded")
                    );

                    return new ServicioSmsTwilio.RespuestaHttp(
                            201, "{\"sid\":\"" + SID + "\"}"
                    );
                }
        );

        ResultadoSms resultado =
                servicio.enviar(TELEFONO, "Prueba CareStock");

        assertTrue(resultado.aceptado());
        assertFalse(resultado.simulado());
        assertEquals(201, resultado.codigoHttp());
        assertEquals(SID, resultado.referencia());
    }

    @Test
    void twilioNoRevelaDetallesDeRespuestaFallida() {

        ServicioSmsTwilio servicio = new ServicioSmsTwilio(
                CUENTA,
                "token-de-prueba",
                "+15005550006",
                destinatarios,
                solicitud -> new ServicioSmsTwilio.RespuestaHttp(
                        401, "DATO_SENSIBLE"
                )
        );

        ExcepcionSms error = assertThrows(
                ExcepcionSms.class,
                () -> servicio.enviar(TELEFONO, "Aviso")
        );

        assertTrue(error.getMessage().contains("HTTP 401"));
        assertFalse(error.getMessage().contains("DATO_SENSIBLE"));
    }

    @Test
    void twilioGestionaFallosDeRed() {

        ServicioSmsTwilio servicio = new ServicioSmsTwilio(
                CUENTA,
                "token-de-prueba",
                "+15005550006",
                destinatarios,
                solicitud -> {
                    throw new IOException("Fallo de conexion");
                }
        );

        assertThrows(
                ExcepcionSms.class,
                () -> servicio.enviar(TELEFONO, "Aviso")
        );
    }

    // ===== QUALITY GATE: RESPUESTAS TWILIO =====

    @Test
    void twilioRechazaRespuestasSinSidValido() {
        for (String contenido : new String[]{
                null, "{\"sid\":\"INVALIDO\"}"}) {

            ServicioSmsTwilio servicio = new ServicioSmsTwilio(
                    CUENTA, "token-de-prueba", "+15005550006",
                    destinatarios,
                    solicitud -> new ServicioSmsTwilio.RespuestaHttp(
                            201, contenido));

            assertThrows(
                    ExcepcionSms.class,
                    () -> servicio.enviar(TELEFONO, "Prueba"));
        }
    }

    @Test
    void twilioRechazaCodigosHttpFueraDelRangoExitoso() {
        for (int codigo : new int[]{199, 300}) {
            ServicioSmsTwilio servicio = new ServicioSmsTwilio(
                    CUENTA, "token-de-prueba", "+15005550006",
                    destinatarios,
                    solicitud -> new ServicioSmsTwilio.RespuestaHttp(
                            codigo, "Respuesta de prueba"));

            assertThrows(
                    ExcepcionSms.class,
                    () -> servicio.enviar(TELEFONO, "Prueba"));
        }
    }

    @Test
    void twilioConservaEstadoDeInterrupcion() {
        ServicioSmsTwilio servicio = new ServicioSmsTwilio(
                CUENTA, "token-de-prueba", "+15005550006",
                destinatarios,
                solicitud -> {
                    throw new InterruptedException(
                            "Interrupcion simulada");
                });

        try {
            assertThrows(
                    ExcepcionSms.class,
                    () -> servicio.enviar(TELEFONO, "Prueba"));

            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

}
