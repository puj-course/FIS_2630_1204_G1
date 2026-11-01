package org.example.carestock.sms;

import java.util.Objects;
import java.util.UUID;

public final class ServicioSmsSimulado implements ServicioSms {

    private final DestinatariosPermitidosSms autorizados;

    public ServicioSmsSimulado(
            DestinatariosPermitidosSms autorizados) {
        this.autorizados = Objects.requireNonNull(autorizados);
    }

    @Override
    public ResultadoSms enviar(String destino, String mensaje) {
        autorizados.exigirAutorizado(destino);
        ValidacionSms.exigirMensajeValido(mensaje);

        return new ResultadoSms(
                true,
                true,
                0,
                "SIM-" + UUID.randomUUID()
        );
    }
}
