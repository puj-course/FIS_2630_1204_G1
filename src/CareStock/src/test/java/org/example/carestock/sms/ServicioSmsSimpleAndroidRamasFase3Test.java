package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsSimpleAndroidRamasFase3Test {

    @Test
    void escapaCaracteresEspecialesJson() throws Exception {
        Method metodo = ServicioSmsSimpleAndroid.class
                .getDeclaredMethod("escapar", String.class);

        metodo.setAccessible(true);

        assertEquals(
                "a\\u0001b",
                metodo.invoke(null, "a\u0001b"));

        assertEquals(
                "\\\"\\\\\\n\\r\\t",
                metodo.invoke(null, "\"\\\n\r\t"));

        assertEquals(
                "Texto",
                metodo.invoke(null, "Texto"));
    }

    @Test
    void rechazaDireccionesInsegurasSinEnviarSms() {
        var autorizados = new DestinatariosPermitidosSms(
                Set.of("+573001234567"));

        String[] direcciones = {
                "https://192.168.1.20:8080",
                "http://8.8.8.8:8080",
                "http://127.0.0.1:8080",
                "http://192.168.1.20:8080/admin",
                "http://usuario@192.168.1.20:8080"
        };

        for (String direccion : direcciones) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ServicioSmsSimpleAndroid(
                            direccion, autorizados));
        }
    }
}
