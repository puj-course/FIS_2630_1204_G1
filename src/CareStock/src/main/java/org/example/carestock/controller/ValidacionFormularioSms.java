package org.example.carestock.controller;

import org.example.carestock.session.SesionUsuario;
import org.example.carestock.sms.ServicioPreferenciasSms;
import org.example.carestock.sms.ValidacionSms;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class ValidacionFormularioSms {

    private ValidacionFormularioSms() {}

    public static boolean puedeAcceder(
            SesionUsuario.DatosUsuario sesion) {

        return sesion != null
                && sesion.esAdministrador()
                && sesion.idFarmacia() != null
                && sesion.idFarmacia() > 0;
    }

    public static ServicioPreferenciasSms.Solicitud crear(
            String telefono,
            boolean autorizado,
            boolean stock,
            boolean vencimiento,
            String inicio,
            String fin,
            String zona,
            boolean consentimientoConfirmado) {

        ValidacionSms.exigirNumeroValido(telefono);

        if (autorizado && !consentimientoConfirmado) {
            throw new IllegalArgumentException(
                    "Debe confirmar el consentimiento del destinatario.");
        }

        String desde = inicio == null ? "" : inicio.trim();
        String hasta = fin == null ? "" : fin.trim();

        if (desde.isEmpty() != hasta.isEmpty()) {
            throw new IllegalArgumentException(
                    "Indique ambos horarios o deje ambos vacios.");
        }

        LocalTime horaInicio = null;
        LocalTime horaFin = null;

        try {
            if (!desde.isEmpty()) {
                if (!desde.matches("\\d{2}:\\d{2}")
                        || !hasta.matches("\\d{2}:\\d{2}")) {
                    throw new IllegalArgumentException(
                            "Use formato HH:mm.");
                }

                horaInicio = LocalTime.parse(desde);
                horaFin = LocalTime.parse(hasta);

                if (horaInicio.equals(horaFin)) {
                    throw new IllegalArgumentException(
                            "El horario inicial y final deben diferir.");
                }
            }

            ZoneId.of(zona);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Horario invalido. Use HH:mm.", e);
        } catch (DateTimeException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Zona horaria invalida.", e);
        }

        return new ServicioPreferenciasSms.Solicitud(
                telefono.trim(),
                autorizado,
                stock,
                vencimiento,
                horaInicio,
                horaFin,
                zona);
    }
}
