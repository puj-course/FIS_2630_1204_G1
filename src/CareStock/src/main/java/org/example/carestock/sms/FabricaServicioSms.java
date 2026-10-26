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

                if (!"SI".equalsIgnoreCase(
                        System.getenv("SMS_CONFIRM_REAL_SEND"))) {
                    throw new IllegalStateException(
                        "Para usar Twilio confirme "
                        + "SMS_CONFIRM_REAL_SEND=SI."
                    );
                }

                yield new ServicioSmsTwilio(
                        System.getenv("TWILIO_ACCOUNT_SID"),
                        System.getenv("TWILIO_AUTH_TOKEN"),
                        System.getenv("TWILIO_FROM_NUMBER"),
                        destinatarios
                );
            }

            default -> throw new IllegalArgumentException(
                    "SMS_PROVIDER debe ser simulado o twilio."
            );
        };
    }
}
