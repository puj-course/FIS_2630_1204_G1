package org.example.carestock.sms;

public final class PruebaSmsConsola {

    private PruebaSmsConsola() {
    }

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            System.err.println(
                "Uso: PruebaSmsConsola +573001234567 [mensaje]"
            );
            System.exit(2);
        }

        String mensaje = args.length == 2
                ? args[1]
                : "[PRUEBA CareStock] Integracion SMS; "
                  + "no es una alerta real.";

        try {
            ResultadoSms resultado =
                    FabricaServicioSms.desdeEntorno()
                            .enviar(args[0], mensaje);

            System.out.println(
                    resultado.simulado()
                    ? "SIMULACION CORRECTA: no se envio ningun SMS."
                    : "ACEPTADO POR TWILIO: pendiente de confirmar entrega."
            );

            System.out.println(
                    "Referencia: " + resultado.referencia()
            );

        } catch (IllegalArgumentException
                 | SecurityException
                 | ExcepcionSms
                 | IllegalStateException e) {

            System.err.println(
                    "Fallo de prueba SMS: " + e.getMessage()
            );
            System.exit(1);
        }
    }
}
