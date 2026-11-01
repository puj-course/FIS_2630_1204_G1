package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsAndroidTest {

    private static final String URL =
            "http://192.168.1.50:8080";

    private static final String DESTINO =
            "+573001234567";

    private final DestinatariosPermitidosSms permitidos =
            new DestinatariosPermitidosSms(Set.of(DESTINO));

    @Test
    void aceptaMensajePersonalizado() {

        ServicioSmsAndroid sms = new ServicioSmsAndroid(
                URL,
                "usuario-prueba",
                "clave-prueba",
                permitidos,
                solicitud -> {
                    assertEquals("POST", solicitud.method());
                    assertEquals(
                            URL + "/message",
                            solicitud.uri().toString()
                    );
                    assertTrue(
                            solicitud.headers()
                                    .firstValue("Authorization")
                                    .orElse("")
                                    .startsWith("Basic ")
                    );

                    return new ServicioSmsAndroid.Respuesta(
                            202,
                            "{\"id\":\"mensaje-123\"}"
                    );
                }
        );

        ResultadoSms resultado = sms.enviar(
                DESTINO,
                "CARESTOCK: Farmacia Principal, "
                + "producto agotado, existencias 0."
        );

        assertTrue(resultado.aceptado());
        assertFalse(resultado.simulado());
        assertEquals(202, resultado.codigoHttp());
        assertEquals("mensaje-123", resultado.referencia());
    }

    @Test
    void rechazaDestinatariosNoAutorizados() {

        ServicioSmsAndroid sms = new ServicioSmsAndroid(
                URL, "usuario", "clave", permitidos,
                solicitud -> {
                    fail("No debe llamar al proveedor.");
                    return null;
                }
        );

        assertThrows(
                SecurityException.class,
                () -> sms.enviar(
                        "+573009999999", "Alerta CareStock"
                )
        );
    }

    @Test
    void rechazaErroresDelProveedor() {

        ServicioSmsAndroid sms = new ServicioSmsAndroid(
                URL, "usuario", "clave", permitidos,
                solicitud -> new ServicioSmsAndroid.Respuesta(
                        401, "No autorizado"
                )
        );

        assertThrows(
                ExcepcionSms.class,
                () -> sms.enviar(DESTINO, "Alerta CareStock")
        );
    }

    @Test
    void impideConexionesFueraDeLaRedPrivada() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ServicioSmsAndroid(
                        "http://8.8.8.8:8080",
                        "usuario",
                        "clave",
                        permitidos
                )
        );
    }
}
