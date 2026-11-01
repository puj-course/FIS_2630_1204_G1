package org.example.carestock.sms;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

public final class DespachadorProtegidoSms {

    private final ServicioSms proveedor;

    public DespachadorProtegidoSms(ServicioSms proveedor) {
        this.proveedor = Objects.requireNonNull(proveedor);
    }

    // El llamador debe revalidar la BD bajo bloqueo
    // antes de invocar este metodo.
    public Optional<ResultadoSms> enviarSiPermitido(
            boolean elegible,
            boolean critica,
            LocalTime inicio,
            LocalTime fin,
            String zona,
            Instant instante,
            String telefono,
            String mensaje) {

        if (!ReglasNotificacionSms.puedeEnviar(
                elegible, critica, inicio, fin, zona, instante)) {
            return Optional.empty();
        }

        ValidacionSms.exigirNumeroValido(telefono);
        ValidacionSms.exigirMensajeValido(mensaje);

        return Optional.of(
                Objects.requireNonNull(
                        proveedor.enviar(telefono, mensaje)
                )
        );
    }
}
