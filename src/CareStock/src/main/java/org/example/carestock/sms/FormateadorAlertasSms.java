package org.example.carestock.sms;

import java.util.Map;

public final class FormateadorAlertasSms {

    private static final String STOCK =
            "{marca} | STOCK CRITICO | Farmacia: {farmacia} | "
          + "{categoria}: {producto} | "
          + "Existencias: {unidades}. {accion}";

    private static final String VENCE =
            "{marca} | VENCIMIENTO | Farmacia: {farmacia} | "
          + "Producto: {producto} | Lote: {lote} | "
          + "{plazo} | Unidades: {unidades}. {accion}";

    public String generar(
            EventoSms evento,
            String farmacia,
            Map<String, String> config
    ) {
        boolean vencimiento =
                "VENCIMIENTO".equals(evento.tipo());

        String plantilla = config.getOrDefault(
                vencimiento
                        ? "SMS_VENCE_TEMPLATE"
                        : "SMS_STOCK_TEMPLATE",
                vencimiento ? VENCE : STOCK
        );

        String plazo = evento.dias() < 0
                ? "Vencido hace " + (-evento.dias()) + " dias"
                : evento.dias() == 0
                        ? "Vence hoy"
                        : "Vence en " + evento.dias() + " dias";

        Map<String, String> datos = Map.of(
                "{marca}", limpio(config.getOrDefault(
                        "SMS_MARCA", "CareStock")),
                "{farmacia}", limpio(farmacia),
                "{categoria}", limpio(evento.categoria()),
                "{producto}", limpio(evento.producto()),
                "{lote}", limpio(evento.lote()),
                "{unidades}", Integer.toString(evento.unidades()),
                "{dias}", Integer.toString(evento.dias()),
                "{plazo}", plazo,
                "{accion}", limpio(config.getOrDefault(
                        vencimiento
                                ? "SMS_ACCION_VENCE"
                                : "SMS_ACCION_STOCK",
                        vencimiento
                                ? "Revisar y priorizar rotacion."
                                : "Gestionar reposicion."
                ))
        );

        for (var dato : datos.entrySet()) {
            plantilla = plantilla.replace(
                    dato.getKey(), dato.getValue()
            );
        }

        ValidacionSms.exigirMensajeValido(plantilla);

        return plantilla;
    }

    private static String limpio(String texto) {
        return texto == null
                ? ""
                : texto.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
