package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GatewaySmsSeguridadCoberturaTest {

    private DestinatariosPermitidosSms autorizados() {
        return new DestinatariosPermitidosSms(
                Set.of("+573001234567"));
    }

    @Test
    void rechazaEndpointsInseguros() {
        String[] endpoints = {
                "https://192.168.1.10:8080",
                "http://google.com:8080",
                "http://127.0.0.1:8080",
                "http://8.8.8.8:8080",
                "http://192.168.1.10:8080/otro",
                "http://192.168.1.10:8080?token=123",
                "http://usuario@192.168.1.10:8080",
                "http://192.168.1.10:8080#fragmento"
        };

        for (String endpoint : endpoints) {
            assertThrows(IllegalArgumentException.class,
                    () -> new ServicioSmsSimpleAndroid(
                            endpoint, autorizados()));
        }
    }

    @Test
    void aceptaServidorPrivadoConDestinatariosValidos() {
        assertDoesNotThrow(() ->
                new ServicioSmsSimpleAndroid(
                        "http://192.168.1.10:8080",
                        autorizados()));
    }

    @Test
    void impideMensajesADestinatariosNoAutorizados() {
        ServicioSmsSimpleAndroid servicio =
                new ServicioSmsSimpleAndroid(
                        "http://192.168.1.10:8080",
                        autorizados());

        assertThrows(SecurityException.class,
                () -> servicio.enviar("+573009999999", "Prueba"));
    }

    @Test
    void noAceptaMensajesVacios() {
        ServicioSmsSimpleAndroid servicio =
                new ServicioSmsSimpleAndroid(
                        "http://192.168.1.10:8080",
                        autorizados());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.enviar("+573001234567", "  "));
    }

    @Test
    void exigeDestinatariosConfigurados() {
        assertThrows(NullPointerException.class,
                () -> new ServicioSmsSimpleAndroid(
                        "http://192.168.1.10:8080", null));
    }
}
