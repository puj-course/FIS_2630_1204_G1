package org.example.carestock.sms;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsSimpleAndroidCoberturaExtraTest {

    private static DestinatariosPermitidosSms autorizados() {
        return new DestinatariosPermitidosSms(
                Set.of("+573001234567"));
    }

    @Test
    void construyeRutaDeEnvioLocal() throws Exception {
        var servicio = new ServicioSmsSimpleAndroid(
                "http://192.168.10.2:8080/", autorizados());

        var campo = ServicioSmsSimpleAndroid.class
                .getDeclaredField("endpoint");
        campo.setAccessible(true);

        assertEquals(
                URI.create("http://192.168.10.2:8080/send-sms"),
                campo.get(servicio));
    }

    @Test
    void rechazaDireccionesInseguras() {
        String[] direcciones = {
                "https://192.168.10.2:8080",
                "http://8.8.8.8:8080",
                "http://127.0.0.1:8080",
                "http://192.168.10.2",
                "http://usuario@192.168.10.2:8080",
                "http://192.168.10.2:8080/?q=a",
                "http://192.168.10.2:8080/#frag",
                "http://192.168.10.2:8080/admin",
                "ftp://192.168.10.2:8080",
                "http://[::1]:8080"
        };

        for (String direccion : direcciones) {
            assertThrows(IllegalArgumentException.class,
                    () -> new ServicioSmsSimpleAndroid(
                            direccion, autorizados()));
        }
    }

    @Test
    void rechazaConfiguracionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsSimpleAndroid(
                        null, autorizados()));

        assertThrows(NullPointerException.class,
                () -> new ServicioSmsSimpleAndroid(
                        "http://192.168.1.2:8080", null));
    }

    @Test
    void escapaCaracteresEspecialesJson() throws Exception {
        Method metodo = ServicioSmsSimpleAndroid.class
                .getDeclaredMethod("escapar", String.class);
        metodo.setAccessible(true);

        String original = "\"\\\n\r\t";
        String esperado = "\\\"\\\\\\n\\r\\t";

        assertEquals(esperado, metodo.invoke(null, original));
        assertEquals("texto", metodo.invoke(null, "texto"));
    }

    @Test
    void rechazaTelefonoNoAutorizado() {
        var servicio = new ServicioSmsSimpleAndroid(
                "http://192.168.1.2:8080", autorizados());

        assertThrows(SecurityException.class,
                () -> servicio.enviar(
                        "+573001234568", "Alerta de prueba"));
    }

    @Test
    void rechazaMensajeVacio() {
        var servicio = new ServicioSmsSimpleAndroid(
                "http://192.168.1.2:8080", autorizados());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.enviar(
                        "+573001234567", ""));
    }
}
