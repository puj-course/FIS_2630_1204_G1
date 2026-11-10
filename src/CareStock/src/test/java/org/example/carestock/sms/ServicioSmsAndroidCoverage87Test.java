package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsAndroidCoverage87Test {

    private static final String URL =
            "http://192.168.1.50:8080";
    private static final String DESTINO =
            "+573001234567";

    private static final DestinatariosPermitidosSms PERMITIDOS =
            new DestinatariosPermitidosSms(Set.of(DESTINO));

    private ServicioSmsAndroid crear(
            ServicioSmsAndroid.Transporte transporte) {
        return new ServicioSmsAndroid(
                URL, "usuario", "clave",
                PERMITIDOS, transporte);
    }

    @Test
    void aceptaHttp200ConIdentificador() {
        var sms = crear(solicitud ->
                new ServicioSmsAndroid.Respuesta(
                        200, "{\"id\":\"SMS-123\"}"));

        var resultado = sms.enviar(DESTINO, "Alerta QA");

        assertEquals("SMS-123", resultado.referencia());
        assertEquals(200, resultado.codigoHttp());
        assertFalse(resultado.simulado());
    }

    @Test
    void respuestaSinIdentificadorUsaReferenciaLocal() {
        var sms = crear(solicitud ->
                new ServicioSmsAndroid.Respuesta(204, null));

        assertEquals("ANDROID-HTTP-204",
                sms.enviar(DESTINO, "Alerta QA").referencia());
    }

    @Test
    void respuestaConJsonSinIdUsaReferenciaLocal() {
        var sms = crear(solicitud ->
                new ServicioSmsAndroid.Respuesta(
                        202, "{\"estado\":\"aceptado\"}"));

        assertEquals("ANDROID-HTTP-202",
                sms.enviar(DESTINO, "Alerta QA").referencia());
    }

    @Test
    void rechazaCodigoHttpInferiorA200() {
        var sms = crear(solicitud ->
                new ServicioSmsAndroid.Respuesta(199, ""));

        assertThrows(ExcepcionSms.class,
                () -> sms.enviar(DESTINO, "Alerta QA"));
    }

    @Test
    void rechazaCodigoHttpMayorOIgualA300() {
        var sms = crear(solicitud ->
                new ServicioSmsAndroid.Respuesta(300, ""));

        assertThrows(ExcepcionSms.class,
                () -> sms.enviar(DESTINO, "Alerta QA"));
    }

    @Test
    void encapsulaErrorDeComunicacion() {
        var sms = crear(solicitud -> {
            throw new IOException("Fallo de prueba");
        });

        var error = assertThrows(ExcepcionSms.class,
                () -> sms.enviar(DESTINO, "Alerta QA"));

        assertInstanceOf(IOException.class, error.getCause());
    }

    @Test
    void conservaInterrupcionDelHilo() {
        var sms = crear(solicitud -> {
            throw new InterruptedException("Interrumpido");
        });

        try {
            assertThrows(ExcepcionSms.class,
                    () -> sms.enviar(DESTINO, "Alerta QA"));

            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void generaAutenticacionBasicCorrecta() {
        var sms = crear(solicitud -> {
            String esperado = "Basic " +
                    Base64.getEncoder().encodeToString(
                            "usuario:clave".getBytes(
                                    StandardCharsets.UTF_8));

            assertEquals(esperado,
                    solicitud.headers()
                            .firstValue("Authorization")
                            .orElseThrow());

            assertEquals("POST", solicitud.method());
            assertEquals(URL + "/message",
                    solicitud.uri().toString());

            return new ServicioSmsAndroid.Respuesta(
                    200, "{\"id\":\"SMS-123\"}");
        });

        assertTrue(sms.enviar(DESTINO, "Alerta QA").aceptado());
    }

    @Test
    void noContactaProveedorConDestinoNoAutorizado() {
        var sms = crear(solicitud -> {
            fail("No debe invocar el transporte HTTP");
            return null;
        });

        assertThrows(SecurityException.class,
                () -> sms.enviar(
                        "+573009999999", "Alerta QA"));
    }

    @Test
    void noContactaProveedorConMensajeVacio() {
        var sms = crear(solicitud -> {
            fail("No debe invocar el transporte HTTP");
            return null;
        });

        assertThrows(IllegalArgumentException.class,
                () -> sms.enviar(DESTINO, ""));
    }

    @Test
    void rechazaCredencialesVacias() {
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsAndroid(
                        URL, " ", "clave",
                        PERMITIDOS, solicitud -> null));

        assertThrows(IllegalArgumentException.class,
                () -> new ServicioSmsAndroid(
                        URL, "usuario", "",
                        PERMITIDOS, solicitud -> null));
    }

    @Test
    void rechazaDependenciasNulas() {
        assertThrows(NullPointerException.class,
                () -> new ServicioSmsAndroid(
                        URL, "usuario", "clave",
                        null, solicitud -> null));

        assertThrows(NullPointerException.class,
                () -> new ServicioSmsAndroid(
                        URL, "usuario", "clave",
                        PERMITIDOS, null));
    }

    @Test
    void rechazaRutasYServidoresNoAutorizados() {
        for (String direccion : new String[]{
                "https://192.168.1.50:8080",
                "http://8.8.8.8:8080",
                "http://127.0.0.1:8080",
                "http://192.168.1.50:8080/admin",
                "http://usuario@192.168.1.50:8080",
                "http://192.168.1.50:8080?q=1",
                "http://192.168.1.50:8080#seccion"
        }) {
            assertThrows(IllegalArgumentException.class,
                    () -> new ServicioSmsAndroid(
                            direccion, "usuario", "clave",
                            PERMITIDOS, solicitud -> null),
                    direccion);
        }
    }

    @Test
    void escapaCaracteresDeControlJson() throws Exception {
        Method metodo = ServicioSmsAndroid.class
                .getDeclaredMethod("escaparJson", String.class);
        metodo.setAccessible(true);

        String original = "\"\\\n\r\t" + '\u0001';
        String esperado = "\\\"\\\\\\n\\r\\t\\u0001";

        assertEquals(esperado, metodo.invoke(null, original));
        assertEquals("Mensaje normal",
                metodo.invoke(null, "Mensaje normal"));
    }
}
