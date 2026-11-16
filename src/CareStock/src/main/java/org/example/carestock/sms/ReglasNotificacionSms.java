package org.example.carestock.sms;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ReglasNotificacionSms {

    private ReglasNotificacionSms() {}

    public record CondicionesSms(
            boolean destinatarioActivo,
            boolean autorizado,
            boolean fechaConsentimientoPresente,
            boolean usuarioActivo,
            boolean farmaciaActiva) { }

    public record PreferenciasEvento(
            String tipo,
            boolean stock,
            boolean vencimiento,
            boolean acceso) { }

    public static boolean puedeGenerar(
            CondicionesSms condiciones, PreferenciasEvento preferencias) {
        if (!condiciones.destinatarioActivo()
                || !condiciones.autorizado()
                || !condiciones.fechaConsentimientoPresente()
                || !condiciones.usuarioActivo()
                || !condiciones.farmaciaActiva()) {
            return false;
        }
        return switch (preferencias.tipo() == null ? "" : preferencias.tipo()) {
            case "STOCK" -> preferencias.stock();
            case "VENCIMIENTO" -> preferencias.vencimiento();
            case "ACCESO" -> preferencias.acceso();
            default -> false;
        };
    }

    public static boolean puedeEnviar(
            boolean elegible,
            boolean critica,
            LocalTime inicio,
            LocalTime fin,
            String zonaHoraria,
            Instant instante) {

        if (!elegible) return false;

        // Una alerta critica solo omite la restriccion horaria.
        if (critica) return true;

        if (inicio == null && fin == null) return true;

        if (inicio == null || fin == null
                || inicio.equals(fin)) return false;

        LocalTime ahora = instante
                .atZone(ZoneId.of(zonaHoraria))
                .toLocalTime();

        if (inicio.isBefore(fin)) {
            return !ahora.isBefore(inicio)
                    && ahora.isBefore(fin);
        }

        // Permite horarios nocturnos, por ejemplo 21:00-06:00.
        return !ahora.isBefore(inicio)
                || ahora.isBefore(fin);
    }

    public static String tipo(String clave) {
        if (clave == null) return "";

        if (clave.startsWith("STOCK:")
                || clave.startsWith("AVISO:STOCK:")) {
            return "STOCK";
        }

        if (clave.startsWith("LOTE:")
                || clave.startsWith("AVISO:VENCIMIENTO:")) {
            return "VENCIMIENTO";
        }

        if (clave.startsWith("ACCESO:")) {
            return "ACCESO";
        }

        return "";
    }

    public static boolean esCritica(String clave) {
        return clave != null
                && (clave.startsWith("STOCK:")
                    || clave.startsWith("LOTE:"));
    }
}
