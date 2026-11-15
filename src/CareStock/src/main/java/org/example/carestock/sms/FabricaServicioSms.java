package org.example.carestock.sms;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class FabricaServicioSms {

    private FabricaServicioSms() { }

    public static ServicioSms desdeEntorno() {
        return desdeConfiguracion(System.getenv());
    }

    // Permite pruebas con configuración controlada.
    static ServicioSms desdeConfiguracion(Map<String, String> entorno) {
        Objects.requireNonNull(entorno, "entorno");

        String modo = entorno.getOrDefault("SMS_PROVIDER", "simulado")
                .trim()
                .toLowerCase(Locale.ROOT);

        DestinatariosPermitidosSms destinatarios =
                DestinatariosPermitidosSms.desdeCsv(
                        entorno.get("SMS_ALLOWED_TO"));

        return switch (modo) {
            case "simulado" ->
                    new ServicioSmsSimulado(destinatarios);

            case "twilio" -> {
                exigirEnvioRealConfirmado(entorno);
                yield new ServicioSmsTwilio(
                        entorno.get("TWILIO_ACCOUNT_SID"),
                        entorno.get("TWILIO_AUTH_TOKEN"),
                        entorno.get("TWILIO_FROM_NUMBER"),
                        destinatarios);
            }

            case "twilio-curl" -> {
                exigirEnvioRealConfirmado(entorno);
                yield new ServicioSmsTwilioCurl(
                        entorno.get("TWILIO_ACCOUNT_SID"),
                        entorno.get("TWILIO_AUTH_TOKEN"),
                        entorno.get("TWILIO_FROM_NUMBER"),
                        destinatarios);
            }

            case "android" -> {
                exigirEnvioRealConfirmado(entorno);
                yield new ServicioSmsAndroid(
                        entorno.get("SMS_GATEWAY_URL"),
                        entorno.get("SMS_GATEWAY_USERNAME"),
                        entorno.get("SMS_GATEWAY_PASSWORD"),
                        destinatarios);
            }

            case "simple-android" -> {
                exigirEnvioRealConfirmado(entorno);
                yield new ServicioSmsSimpleAndroid(
                        entorno.get("SMS_GATEWAY_URL"),
                        destinatarios);
            }

            default -> throw new IllegalArgumentException(
                    "SMS_PROVIDER debe ser simulado, twilio, twilio-curl, "
                    + "android o simple-android.");
        };
    }

    private static void exigirEnvioRealConfirmado(
            Map<String, String> entorno) {
        if (!"SI".equalsIgnoreCase(
                entorno.get("SMS_CONFIRM_REAL_SEND"))) {
            throw new IllegalStateException(
                    "Confirme SMS_CONFIRM_REAL_SEND=SI.");
        }
    }
}
