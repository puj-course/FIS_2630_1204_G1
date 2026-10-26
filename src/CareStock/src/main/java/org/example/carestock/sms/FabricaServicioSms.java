package org.example.carestock.sms;

import java.util.Locale;

public final class FabricaServicioSms {

    private FabricaServicioSms() {
    }

    public static ServicioSms desdeEntorno() {

        String modo = System.getenv()
                .getOrDefault("SMS_PROVIDER", "simulado")
                .trim()
                .toLowerCase(Locale.ROOT);

        DestinatariosPermitidosSms destinatarios =
                DestinatariosPermitidosSms.desdeCsv(
                        System.getenv("SMS_ALLOWED_TO")
                );

        return switch (modo) {

            case "simulado" ->
                    new ServicioSmsSimulado(destinatarios);

            case "twilio" -> {
                exigirEnvioRealConfirmado();

                yield new ServicioSmsTwilio(
                        System.getenv("TWILIO_ACCOUNT_SID"),
                        System.getenv("TWILIO_AUTH_TOKEN"),
                        System.getenv("TWILIO_FROM_NUMBER"),
                        destinatarios
                );
            }

            case "android" -> {
                exigirEnvioRealConfirmado();

                yield new ServicioSmsAndroid(
                        System.getenv("SMS_GATEWAY_URL"),
                        System.getenv("SMS_GATEWAY_USERNAME"),
                        System.getenv("SMS_GATEWAY_PASSWORD"),
                        destinatarios
                );
            }

            case "simple-android" -> {
                if (!"SI".equalsIgnoreCase(System.getenv("SMS_CONFIRM_REAL_SEND"))) {
                    throw new IllegalStateException("Confirme SMS_CONFIRM_REAL_SEND=SI.");
                }
                yield new ServicioSmsSimpleAndroid(
                        System.getenv("SMS_GATEWAY_URL"), destinatarios);
            }

            default -> throw new IllegalArgumentException(
                    "SMS_PROVIDER debe ser "
                    + "simulado, twilio o android."
            );
        };
    }

    private static void exigirEnvioRealConfirmado() {

        if (!"SI".equalsIgnoreCase(
                System.getenv("SMS_CONFIRM_REAL_SEND"))) {

            throw new IllegalStateException(
                    "Confirme SMS_CONFIRM_REAL_SEND=SI."
            );
        }
    }
}
