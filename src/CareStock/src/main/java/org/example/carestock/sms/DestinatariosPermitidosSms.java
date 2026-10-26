package org.example.carestock.sms;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class DestinatariosPermitidosSms {

    private final Set<String> numeros;

    public DestinatariosPermitidosSms(Set<String> numeros) {
        if (numeros == null || numeros.isEmpty()) {
            throw new IllegalArgumentException(
                    "Configure al menos un destinatario autorizado."
            );
        }

        numeros.forEach(ValidacionSms::exigirNumeroValido);
        this.numeros = Set.copyOf(numeros);
    }

    public static DestinatariosPermitidosSms desdeCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException(
                    "SMS_ALLOWED_TO no esta configurada."
            );
        }

        Set<String> numeros = Arrays.stream(csv.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        return new DestinatariosPermitidosSms(numeros);
    }

    public void exigirAutorizado(String numero) {
        ValidacionSms.exigirNumeroValido(numero);

        if (!numeros.contains(numero)) {
            throw new SecurityException(
                    "El destinatario no esta autorizado para SMS."
            );
        }
    }
}
