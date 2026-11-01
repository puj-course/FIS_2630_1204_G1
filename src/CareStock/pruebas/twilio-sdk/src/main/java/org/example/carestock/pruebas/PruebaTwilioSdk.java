package org.example.carestock.pruebas;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public final class PruebaTwilioSdk {

    private PruebaTwilioSdk() {
    }

    private static String requerida(String nombre) {
        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Falta la variable: " + nombre);
        }

        return valor.trim();
    }

    public static void main(String[] args) {

        String destino = requerida("TWILIO_TEST_TO");
        String remitente = requerida("TWILIO_FROM_NUMBER");

        boolean trial = !"NO".equalsIgnoreCase(
                System.getenv("TWILIO_PRUEBA_TRIAL"));

        String contenido = trial
                ? "sms_internal_alerts"
                : requerida("TWILIO_MESSAGE_BODY");

        System.out.println("CARESTOCK | Prueba SDK Twilio");
        System.out.println("Modo: " +
                (trial ? "TRIAL" : "PERSONALIZADO"));

        if (!"SI".equalsIgnoreCase(
                System.getenv("TWILIO_CONFIRMAR_ENVIO"))) {

            System.out.println(
                    "Envio no realizado. Falta confirmacion.");
            return;
        }

        if (!destino.matches("\\+[1-9][0-9]{7,14}")
                || !remitente.matches("\\+[1-9][0-9]{7,14}")) {
            throw new IllegalArgumentException(
                    "Los telefonos deben usar formato E.164.");
        }

        String sid = requerida("TWILIO_ACCOUNT_SID");
        String token = requerida("TWILIO_AUTH_TOKEN");

        try {
            Twilio.init(sid, token);

            Message mensaje = Message.creator(
                    new PhoneNumber(destino),
                    new PhoneNumber(remitente),
                    contenido
            ).create();

            System.out.println("Solicitud aceptada por Twilio.");
            System.out.println("SID: " + mensaje.getSid());
            System.out.println("Estado: " + mensaje.getStatus());
            System.out.println(
                    "La entrega debe verificarse en Twilio Logs.");

        } catch (ApiException e) {
            System.err.println("Error API Twilio");
            System.err.println("HTTP: " + e.getStatusCode());
            System.err.println("Codigo: " + e.getCode());
            throw new IllegalStateException(
                    "Twilio rechazo la solicitud SMS.", e);
        }
    }
}
