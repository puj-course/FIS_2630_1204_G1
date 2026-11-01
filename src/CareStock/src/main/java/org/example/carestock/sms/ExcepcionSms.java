package org.example.carestock.sms;

public class ExcepcionSms extends RuntimeException {

    public ExcepcionSms(String mensaje) {
        super(mensaje);
    }

    public ExcepcionSms(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
