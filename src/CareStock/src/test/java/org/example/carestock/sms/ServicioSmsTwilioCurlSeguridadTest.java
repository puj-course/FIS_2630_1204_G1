package org.example.carestock.sms;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioSmsTwilioCurlSeguridadTest {

    @Test
    void utilizaRutaAbsolutaSinDependerDelPath() {

        ProcessBuilder proceso =
                ServicioSmsTwilioCurl.crearProcesoCurl();

        List<String> comando = proceso.command();

        assertEquals(
                "C:\\Windows\\System32\\curl.exe",
                comando.get(0)
        );

        assertEquals(
                List.of(
                    "C:\\Windows\\System32\\curl.exe",
                    "-q",
                    "--config",
                    "-"
                ),
                comando
        );

        assertTrue(proceso.redirectErrorStream());
    }

    @Test
    void noCargaConfiguracionExternaDeCurl() {

        ProcessBuilder proceso =
                ServicioSmsTwilioCurl.crearProcesoCurl();

        assertEquals("-q", proceso.command().get(1));
        assertEquals("--config", proceso.command().get(2));
        assertEquals("-", proceso.command().get(3));
    }
}
