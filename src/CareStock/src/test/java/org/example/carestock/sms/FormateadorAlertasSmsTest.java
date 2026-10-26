package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormateadorAlertasSmsTest {

    private final FormateadorAlertasSms formateador =
            new FormateadorAlertasSms();

    @Test
    void generaMensajeStockCritico() {

        EventoSms evento = new EventoSms(
                1,
                "STOCK:MED:10",
                "STOCK",
                "Medicamento",
                "Acetaminofen 500 mg",
                "",
                0,
                0
        );

        String mensaje = formateador.generar(
                evento,
                "Farmacia Principal",
                Map.of(
                        "SMS_MARCA", "CareStock",
                        "SMS_ACCION_STOCK",
                        "Solicitar reposicion."
                )
        );

        assertTrue(mensaje.contains("CareStock"));
        assertTrue(mensaje.contains("Farmacia Principal"));
        assertTrue(mensaje.contains("Acetaminofen"));
        assertTrue(mensaje.contains("Existencias: 0"));
        assertTrue(mensaje.contains("Solicitar reposicion"));
    }

    @Test
    void generaMensajeVencimientoCritico() {

        EventoSms evento = new EventoSms(
                2,
                "LOTE:20",
                "VENCIMIENTO",
                "Medicamento",
                "Ibuprofeno 400 mg",
                "IB-2026-042",
                25,
                8
        );

        String mensaje = formateador.generar(
                evento,
                "Farmacia Norte",
                Map.of()
        );

        assertTrue(mensaje.contains("Farmacia Norte"));
        assertTrue(mensaje.contains("IB-2026-042"));
        assertTrue(mensaje.contains("Vence en 8 dias"));
        assertTrue(mensaje.contains("Unidades: 25"));
    }

    @Test
    void identificaLoteVencido() {

        EventoSms evento = new EventoSms(
                1,
                "LOTE:30",
                "VENCIMIENTO",
                "Medicamento",
                "Medicamento de prueba",
                "LOTE-VENCIDO",
                10,
                -3
        );

        String mensaje = formateador.generar(
                evento,
                "Farmacia Principal",
                Map.of()
        );

        assertTrue(mensaje.contains("Vencido hace 3 dias"));
    }

    @Test
    void permitePersonalizarLaPlantilla() {

        EventoSms evento = new EventoSms(
                1,
                "STOCK:ASEO:12",
                "STOCK",
                "Aseo",
                "Jabon antibacterial",
                "",
                0,
                0
        );

        String mensaje = formateador.generar(
                evento,
                "Farmacia Sur",
                Map.of(
                        "SMS_STOCK_TEMPLATE",
                        "{marca}: reponer {producto} en {farmacia}",
                        "SMS_MARCA",
                        "CareStock"
                )
        );

        assertEquals(
                "CareStock: reponer Jabon antibacterial "
                + "en Farmacia Sur",
                mensaje
        );
    }
}
