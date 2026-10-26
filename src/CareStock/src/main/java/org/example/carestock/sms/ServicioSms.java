package org.example.carestock.sms;

public interface ServicioSms {
    ResultadoSms enviar(String destino, String mensaje);
}
