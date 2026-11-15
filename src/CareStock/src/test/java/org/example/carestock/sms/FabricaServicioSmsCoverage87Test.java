package org.example.carestock.sms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FabricaServicioSmsCoverage87Test {

    private static final String DESTINO = "+573001234567";

    private static Map<String, String> configuracion() {
        Map<String, String> env = new HashMap<>();
        env.put("SMS_ALLOWED_TO", DESTINO);
        return env;
    }

    private static void confirmar(Map<String, String> env) {
        env.put("SMS_CONFIRM_REAL_SEND", "SI");
    }

    @Test
    void proveedorPorDefectoEsSimulado() {
        ServicioSms sms = FabricaServicioSms
                .desdeConfiguracion(configuracion());

        assertInstanceOf(ServicioSmsSimulado.class, sms);

        ResultadoSms respuesta = sms.enviar(
                DESTINO, "Prueba de CareStock");

        assertTrue(respuesta.simulado());
        assertTrue(respuesta.aceptado());
    }

    @Test
    void aceptaNombreDeProveedorConEspaciosYMayusculas() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", " SIMULADO ");

        assertInstanceOf(ServicioSmsSimulado.class,
                FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void rechazaConfiguracionNula() {
        assertThrows(NullPointerException.class,
                () -> FabricaServicioSms.desdeConfiguracion(null));
    }

    @Test
    void rechazaListaDeDestinatariosAusente() {
        assertThrows(IllegalArgumentException.class,
                () -> FabricaServicioSms.desdeConfiguracion(
                        Map.of("SMS_PROVIDER", "simulado")));
    }

    @Test
    void rechazaDestinatarioInvalido() {
        Map<String, String> env = configuracion();
        env.put("SMS_ALLOWED_TO", "telefono-invalido");

        assertThrows(IllegalArgumentException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void rechazaProveedorDesconocido() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "desconocido");

        assertThrows(IllegalArgumentException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "twilio", "android", "simple-android"
    })
    void proveedoresRealesExigenConfirmacion(String proveedor) {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", proveedor);

        assertThrows(IllegalStateException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NO", "", "FALSE"})
    void confirmacionesInvalidasNoAutorizanEnvioReal(
            String confirmacion) {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "twilio");
        env.put("SMS_CONFIRM_REAL_SEND", confirmacion);

        assertThrows(IllegalStateException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void creaTwilioConConfiguracionValidaSinEnviar() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "twilio");
        env.put("TWILIO_ACCOUNT_SID", "AC" + "1".repeat(32));
        env.put("TWILIO_AUTH_TOKEN", "token-prueba");
        env.put("TWILIO_FROM_NUMBER", "+15005550006");
        confirmar(env);

        assertInstanceOf(ServicioSmsTwilio.class,
                FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void rechazaTwilioSinCredenciales() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "twilio");
        confirmar(env);

        assertThrows(IllegalArgumentException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void creaAndroidConConfiguracionValidaSinEnviar() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "android");
        env.put("SMS_GATEWAY_URL", "http://192.168.1.50:8080");
        env.put("SMS_GATEWAY_USERNAME", "usuario-prueba");
        env.put("SMS_GATEWAY_PASSWORD", "clave-prueba");
        confirmar(env);

        assertInstanceOf(ServicioSmsAndroid.class,
                FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void rechazaAndroidSinCredenciales() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "android");
        env.put("SMS_GATEWAY_URL", "http://192.168.1.50:8080");
        confirmar(env);

        assertThrows(IllegalArgumentException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void creaSimpleAndroidSinEnviar() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "simple-android");
        env.put("SMS_GATEWAY_URL", "http://192.168.1.50:8080");
        confirmar(env);

        assertInstanceOf(ServicioSmsSimpleAndroid.class,
                FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void simpleAndroidRechazaDireccionPublica() {
        Map<String, String> env = configuracion();
        env.put("SMS_PROVIDER", "simple-android");
        env.put("SMS_GATEWAY_URL", "http://8.8.8.8:8080");
        confirmar(env);

        assertThrows(IllegalArgumentException.class,
                () -> FabricaServicioSms.desdeConfiguracion(env));
    }

    @Test
    void proveedorSimuladoRespetaListaAutorizada() {
        ServicioSms sms = FabricaServicioSms
                .desdeConfiguracion(configuracion());

        assertThrows(SecurityException.class,
                () -> sms.enviar(
                        "+573009999999", "Mensaje de prueba"));
    }
}
