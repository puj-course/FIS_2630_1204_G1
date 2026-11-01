package org.example.carestock.sms;

public record EventoSms(
        int farmaciaId,
        String clave,
        String tipo,
        String categoria,
        String producto,
        String lote,
        int unidades,
        int dias
) {
}
