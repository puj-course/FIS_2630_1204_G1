package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;

public final class ProcesoSms {
    private static final System.Logger LOGGER =
            System.getLogger(ProcesoSms.class.getName());

    private ProcesoSms() {
    }

    public static void main(String[] args) throws Exception {

        if (!"SI".equalsIgnoreCase(
                System.getenv("SMS_WORKER_ENABLED")
        )) {
            throw new IllegalStateException(
                    "Configure SMS_WORKER_ENABLED=SI."
            );
        }

        int intervalo = Integer.parseInt(
                System.getenv().getOrDefault(
                        "SMS_INTERVALO_SEG", "60"
                )
        );

        if (intervalo < 15 || intervalo > 3600) {
            throw new IllegalArgumentException(
                    "Intervalo permitido: 15 a 3600 segundos."
            );
        }

        ServicioSms proveedor =
                FabricaServicioSms.desdeEntorno();

        var servicio = new ServicioAlertasSms(
                proveedor,
                System.getenv()
        );

        Runtime.getRuntime().addShutdownHook(
                new Thread(ConexionBD::cerrarPool)
        );

        LOGGER.log(System.Logger.Level.INFO,
                "Worker SMS iniciado. Modo: "
                + System.getenv().getOrDefault(
                        "SMS_PROVIDER", "simulado"
                )
        );

        boolean unaVez = "SI".equalsIgnoreCase(
                System.getenv("SMS_RUN_ONCE")
        );

        while (!Thread.currentThread().isInterrupted()) {

            try {
                servicio.ejecutarCiclo();

            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Ciclo SMS fallido: "
                        + e.getClass().getSimpleName()
                );
            }

            if (unaVez) {
                break;
            }

            try {
                Thread.sleep(intervalo * 1000L);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ConexionBD.cerrarPool();
    }
}
