package org.example.carestock.proxy;

import org.example.carestock.DataTransferObject.DatosCatalogo;
import org.example.carestock.DataTransferObject.DatosLotes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VistasCacheTest {

    @AfterEach
    void limpiar() {
        VistasCache.invalidarTodo();
    }

    @Test
    void catalogoSeReutilizaHastaInvalidacion() throws Exception {
        VistasCache.invalidarTodo();
        AtomicInteger consultas = new AtomicInteger();

        var consulta = (java.util.concurrent.Callable<DatosCatalogo>) () ->
                new DatosCatalogo(List.of(), Map.of(),
                        consultas.incrementAndGet());

        DatosCatalogo primero = VistasCache.catalogo(1, consulta, () -> true);
        DatosCatalogo segundo = VistasCache.catalogo(1, consulta, () -> true);

        assertSame(primero, segundo);
        assertEquals(1, consultas.get());

        VistasCache.invalidarFarmacia(1);
        VistasCache.catalogo(1, consulta, () -> true);

        assertEquals(2, consultas.get());
    }

    @Test
    void lotesSeReutilizanPorFarmacia() throws Exception {
        VistasCache.invalidarTodo();
        AtomicInteger consultas = new AtomicInteger();

        var consulta = (java.util.concurrent.Callable<DatosLotes>) () -> {
            consultas.incrementAndGet();
            return new DatosLotes(
                    List.of(), Map.of(), Map.of(), Map.of());
        };

        DatosLotes primero = VistasCache.lotes(2, consulta, () -> true);
        DatosLotes segundo = VistasCache.lotes(2, consulta, () -> true);

        assertSame(primero, segundo);
        assertEquals(1, consultas.get());

        VistasCache.invalidarTodo();
        VistasCache.lotes(2, consulta, () -> true);

        assertEquals(2, consultas.get());
    }

    @Test
    void ofreceMetricasYControlDeAcceso() {
        assertTrue(VistasCache.ttlCatalogoNanos() > 0);
        assertTrue(VistasCache.metricas().contains("Catalogo"));
        assertTrue(VistasCache.metricas().contains("Lotes"));

        assertThrows(SecurityException.class,
                () -> VistasCache.catalogo(
                        3,
                        () -> new DatosCatalogo(List.of(), Map.of(), 0),
                        () -> false));
    }
}
