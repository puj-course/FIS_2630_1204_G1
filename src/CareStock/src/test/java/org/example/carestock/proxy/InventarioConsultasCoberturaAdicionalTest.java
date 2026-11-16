package org.example.carestock.proxy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class InventarioConsultasCoberturaAdicionalTest {

    @Test
    void mantieneUnaInstanciaCompartida() {
        var primera = InventarioConsultas.compartida();
        var segunda = InventarioConsultas.compartida();

        assertNotNull(primera);
        assertSame(primera, segunda);
    }

    @Test
    void permiteInvalidarCacheSinConsultarBD() {
        assertDoesNotThrow(
                () -> InventarioConsultas.invalidarFarmacia(1));

        assertDoesNotThrow(
                InventarioConsultas::invalidar);
    }

    @Test
    void usaTtlPredeterminado() throws Exception {
        String configurado = System.getenv(
                "CARESTOCK_INVENTARIO_CACHE_TTL_SECONDS");

        assumeTrue(configurado == null
                || configurado.isBlank());

        Method metodo = InventarioConsultas.class
                .getDeclaredMethod("obtenerTtl");

        metodo.setAccessible(true);

        assertEquals(
                Duration.ofSeconds(30),
                metodo.invoke(null));
    }
}
