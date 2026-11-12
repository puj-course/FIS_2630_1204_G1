package org.example.carestock.sms;

import org.example.carestock.session.SesionUsuario;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Texto de acceso: siempre utiliza el instante del evento, no el del envio. */
public final class FormateadorInicioSesionSms {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withZone(ZONA);

    public String generar(SesionUsuario.DatosUsuario sesion, Instant acceso) {
        Objects.requireNonNull(sesion, "Sesion requerida");
        Objects.requireNonNull(acceso, "Instante requerido");
        if (sesion.id() <= 0 || sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0) {
            throw new IllegalArgumentException("Sesion sin farmacia valida");
        }

        String mensaje = "CareStock | INICIO DE SESION | Usuario: "
                + limpiar(sesion.nombre())
                + " | Rol: " + limpiar(sesion.rol())
                + " | Farmacia: " + limpiar(sesion.farmacia())
                + " | Fecha y hora: " + FORMATO.format(acceso)
                + " (America/Bogota). Acceso exitoso.";
        ValidacionSms.exigirMensajeValido(mensaje);
        return mensaje;
    }

    private static String limpiar(String valor) {
        if (valor == null) return "No disponible";
        String limpio = valor.replaceAll("[\\r\\n\\t]+", " ")
                .replace('|', ' ')
                .trim();
        if (limpio.isEmpty()) return "No disponible";
        return limpio.length() > 90 ? limpio.substring(0, 90) : limpio;
    }
}
