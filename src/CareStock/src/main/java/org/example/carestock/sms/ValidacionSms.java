package org.example.carestock.sms;

import java.util.regex.Pattern;

public final class ValidacionSms {

    private static final Pattern E164 =
            Pattern.compile("\\+[1-9][0-9]{7,14}");

    private ValidacionSms() {
    }

    public static void exigirNumeroValido(String numero) {
        if (numero == null || !E164.matcher(numero).matches()) {
            throw new IllegalArgumentException(
                    "El numero SMS debe usar formato E.164 (+paisnumero)."
            );
        }
    }

    public static void exigirMensajeValido(String mensaje) {
        if (mensaje == null || mensaje.isBlank()
                || mensaje.length() > 1600) {
            throw new IllegalArgumentException(
                    "El mensaje debe tener entre 1 y 1600 caracteres."
            );
        }
    }
}
