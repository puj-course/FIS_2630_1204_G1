package org.example.carestock.controller;

import org.example.carestock.sms.ServicioDirectorioSms;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cubre la clasificacion visible de destinatarios sin inicializar JavaFX. */
class ControladorPreferenciasSmsEstadoSonarTest {

    private static String estado(boolean configurado, boolean autorizado) throws Exception {
        ServicioDirectorioSms.Destinatario destinatario =
                new ServicioDirectorioSms.Destinatario(
                        12, "Analista", "analista@prueba.local", "+573001234567",
                        autorizado, configurado, true, false);
        Method metodo = ControladorPreferenciasSms.class.getDeclaredMethod(
                "estadoDestinatario", ServicioDirectorioSms.Destinatario.class);
        metodo.setAccessible(true);
        return (String) metodo.invoke(null, destinatario);
    }

    @Test
    void sinConfiguracionTienePrioridadSobreAutorizacion() throws Exception {
        assertEquals("Sin configurar", estado(false, true));
    }

    @Test
    void destinatarioConfiguradoYAutorizado() throws Exception {
        assertEquals("Autorizado", estado(true, true));
    }

    @Test
    void destinatarioConfiguradoSinAutorizacion() throws Exception {
        assertEquals("No autorizado", estado(true, false));
    }
}
