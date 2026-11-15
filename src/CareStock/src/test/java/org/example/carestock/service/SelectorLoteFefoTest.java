package org.example.carestock.service;

import org.example.carestock.model.Lote;
import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectorLoteFefoTest {

    private static Lote lote(int id, int idMedicamento, int unidades, String estado, int diasParaVencer) {
        Lote lote = new Lote();
        lote.setIdLote(id);
        lote.setNumeroLote("L-" + id);
        lote.setIdMedicamento(idMedicamento);
        lote.setCantidadActual(unidades);
        lote.setEstadoLote(estado);
        lote.setFechaVencimiento(FechaNegocio.hoy().plusDays(diasParaVencer));
        return lote;
    }

    @Test
    void sugiereElLoteQueVenceAntes() {
        Lote lejano = lote(1, 10, 5, "DISPONIBLE", 90);
        Lote cercano = lote(2, 10, 5, "DISPONIBLE", 20);
        Lote medio = lote(3, 10, 5, "DISPONIBLE", 45);

        assertEquals(cercano, SelectorLoteFefo.sugerir(List.of(lejano, cercano, medio), 10).orElseThrow());
    }

    @Test
    void ignoraLotesDeOtrosMedicamentos() {
        Lote otro = lote(1, 99, 5, "DISPONIBLE", 5);
        Lote propio = lote(2, 10, 5, "DISPONIBLE", 60);

        assertEquals(propio, SelectorLoteFefo.sugerir(List.of(otro, propio), 10).orElseThrow());
    }

    @Test
    void ignoraLotesVencidosRetenidosYAgotados() {
        Lote vencido = lote(1, 10, 5, "DISPONIBLE", -3);
        Lote venceHoy = lote(2, 10, 5, "DISPONIBLE", 0);
        Lote retenido = lote(3, 10, 5, "RETENIDO", 10);
        Lote agotado = lote(4, 10, 0, "DISPONIBLE", 10);
        Lote valido = lote(5, 10, 5, "DISPONIBLE", 40);

        assertEquals(valido, SelectorLoteFefo.sugerir(
                List.of(vencido, venceHoy, retenido, agotado, valido), 10).orElseThrow());
    }

    @Test
    void sinLotesAptosDevuelveVacio() {
        Lote retenido = lote(1, 10, 5, "RETENIDO", 10);

        assertTrue(SelectorLoteFefo.sugerir(List.of(retenido), 10).isEmpty());
        assertTrue(SelectorLoteFefo.sugerir(List.of(), 10).isEmpty());
    }

    @Test
    void conLaMismaFechaGanaElDeMenorIdentificador() {
        Lote segundo = lote(8, 10, 5, "DISPONIBLE", 30);
        Lote primero = lote(3, 10, 5, "DISPONIBLE", 30);

        assertEquals(primero, SelectorLoteFefo.sugerir(List.of(segundo, primero), 10).orElseThrow());
    }

    @Test
    void listaNulaSeRechaza() {
        assertThrows(NullPointerException.class, () -> SelectorLoteFefo.sugerir(null, 10));
    }
}
