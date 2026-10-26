package org.example.carestock.sms;

/** Aceptado no significa entregado al telefono. */
public record ResultadoSms(
        boolean aceptado,
        boolean simulado,
        int codigoHttp,
        String referencia
) {
}
