package org.example.carestock.sms;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UtilidadesSmsCoberturaAdicionalTest {

    @Test
    void migracionRequiereConfirmacionExplicita() {
        assumeFalse("SI".equalsIgnoreCase(
                System.getenv("SMS_CONFIRM_MIGRATION")));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> MigracionSms.main(new String[0]));

        assertTrue(error.getMessage()
                .contains("SMS_CONFIRM_MIGRATION"));
    }

    @Test
    void procesoSmsRequiereHabilitacion() {
        assumeFalse("SI".equalsIgnoreCase(
                System.getenv("SMS_WORKER_ENABLED")));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ProcesoSms.main(new String[0]));

        assertTrue(error.getMessage()
                .contains("SMS_WORKER_ENABLED"));
    }

    @Test
    void registroDestinatarioValidaParametros() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RegistrarDestinatarioSms.main(
                        new String[0]));

        assertThrows(
                IllegalArgumentException.class,
                () -> RegistrarDestinatarioSms.main(
                        new String[]{"correo"}));

        assertThrows(
                IllegalArgumentException.class,
                () -> RegistrarDestinatarioSms.main(
                        new String[]{"a", "b", "c"}));
    }

    @Test
    void pruebaSmsUsaProveedorSimulado() {
        ServicioSms proveedor = mock(ServicioSms.class);

        when(proveedor.enviar(
                eq("+573001234567"),
                contains("PRUEBA CareStock")))
                .thenReturn(new ResultadoSms(
                        true, true, 200, "SIM-QA"));

        try (MockedStatic<FabricaServicioSms> fabrica =
                     mockStatic(FabricaServicioSms.class)) {

            fabrica.when(FabricaServicioSms::desdeEntorno)
                    .thenReturn(proveedor);

            assertDoesNotThrow(
                    () -> PruebaSmsConsola.main(
                            new String[]{"+573001234567"}));

            verify(proveedor).enviar(
                    eq("+573001234567"),
                    contains("PRUEBA CareStock"));
        }
    }

    @Test
    void pruebaSmsAceptaMensajePersonalizado() {
        ServicioSms proveedor = mock(ServicioSms.class);

        when(proveedor.enviar(
                "+573001234567", "Mensaje QA"))
                .thenReturn(new ResultadoSms(
                        true, false, 202, "QA-123"));

        try (MockedStatic<FabricaServicioSms> fabrica =
                     mockStatic(FabricaServicioSms.class)) {

            fabrica.when(FabricaServicioSms::desdeEntorno)
                    .thenReturn(proveedor);

            assertDoesNotThrow(
                    () -> PruebaSmsConsola.main(
                            new String[]{
                                "+573001234567",
                                "Mensaje QA"
                            }));

            verify(proveedor).enviar(
                    "+573001234567", "Mensaje QA");
        }
    }
}
