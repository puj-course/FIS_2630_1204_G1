package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SugerenciaReposicionTest {

    private static AlertaInventario stock(int existencias, int minimo) {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                1, "ASE-001", "Jabon", null,
                existencias, minimo, null, null,
                existencias == 0 ? Severidad.CRITICA : Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario vencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO,
                7, "MED-007", "Acetaminofen", "L-77",
                12, null, LocalDate.of(2026, 11, 10), 8,
                Severidad.CRITICA, null, null, null);
    }

    @Test
    void sugiereElDobleDelMinimoMenosLasExistencias() {
        assertEquals(OptionalInt.of(8), SugerenciaReposicion.cantidadSugerida(stock(2, 5)));
    }

    @Test
    void sinExistenciasSugiereElDobleDelMinimo() {
        assertEquals(OptionalInt.of(10), SugerenciaReposicion.cantidadSugerida(stock(0, 5)));
    }

    @Test
    void conExistenciasIgualesAlMinimoSugiereElMinimo() {
        assertEquals(OptionalInt.of(5), SugerenciaReposicion.cantidadSugerida(stock(5, 5)));
    }

    @Test
    void nuncaSugiereUnNumeroNegativo() {
        assertEquals(OptionalInt.of(0), SugerenciaReposicion.cantidadSugerida(stock(20, 5)));
    }

    @Test
    void conMinimoCeroYSinExistenciasSugiereCero() {
        assertEquals(OptionalInt.of(0), SugerenciaReposicion.cantidadSugerida(stock(0, 0)));
    }

    @Test
    void lasAlertasDeVencimientoNoAplican() {
        assertEquals(OptionalInt.empty(), SugerenciaReposicion.cantidadSugerida(vencimiento()));
        assertEquals("—", SugerenciaReposicion.texto(vencimiento()));
    }

    @Test
    void textoMuestraLaCantidad() {
        assertEquals("8", SugerenciaReposicion.texto(stock(2, 5)));
        assertEquals("10", SugerenciaReposicion.texto(stock(0, 5)));
    }

    @Test
    void alertaNulaSeRechaza() {
        assertThrows(NullPointerException.class, () -> SugerenciaReposicion.cantidadSugerida(null));
    }
}
