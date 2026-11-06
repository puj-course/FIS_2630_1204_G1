package org.example.carestock.service;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Lote;
import org.example.carestock.model.Medicamento;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndicadoresCatalogoTest {

    private static final LocalDate HOY =
            LocalDate.of(2026, 11, 6);

    private RegistroCatalogo<Medicamento> medicamento(
            int id, int stock) {

        Medicamento m = new Medicamento();
        m.setIdMedicamento(id);
        m.setStockTotal(stock);

        return new RegistroCatalogo<>(id, m, "ACTIVO");
    }

    private Lote lote(int id, LocalDate fecha) {
        Lote l = new Lote();
        l.setIdMedicamento(id);
        l.setFechaVencimiento(fecha);
        return l;
    }

    @Test
    void noCuentaLotesComoMedicamentos() {
        var resultado = IndicadoresCatalogo.calcular(
                List.of(
                        medicamento(1, 8),
                        medicamento(1, 8),
                        medicamento(2, 0)),
                List.of(
                        lote(1, HOY.minusDays(3)),
                        lote(1, HOY.minusDays(1)),
                        lote(2, HOY.plusDays(5))),
                HOY);

        assertEquals(
                new IndicadoresCatalogo(2, 1, 1),
                resultado);
    }

    @Test
    void vencimientoDelDiaCuentaComoVencido() {
        var resultado = IndicadoresCatalogo.calcular(
                List.of(medicamento(3, 5)),
                List.of(lote(3, HOY)),
                HOY);

        assertEquals(1, resultado.conLotesVencidos());
    }

    @Test
    void ignoraLotesDeOtrosMedicamentos() {
        var resultado = IndicadoresCatalogo.calcular(
                List.of(medicamento(3, 5)),
                List.of(lote(999, HOY.minusDays(1))),
                HOY);

        assertEquals(0, resultado.conLotesVencidos());
    }

    @Test
    void ignoraDatosNulosOInvalidos() {
        var resultado = IndicadoresCatalogo.calcular(
                Arrays.asList(
                        null,
                        new RegistroCatalogo<Medicamento>(
                                0, null, "ACTIVO"),
                        medicamento(4, 0)),
                Arrays.asList(
                        null,
                        lote(4, null)),
                HOY);

        assertEquals(
                new IndicadoresCatalogo(1, 0, 1),
                resultado);
    }

    @Test
    void listasVaciasGeneranCeros() {
        assertEquals(
                new IndicadoresCatalogo(0, 0, 0),
                IndicadoresCatalogo.calcular(
                        List.of(), List.of(), HOY));
    }

    @Test
    void rechazaArgumentosNulos() {
        assertThrows(NullPointerException.class,
                () -> IndicadoresCatalogo.calcular(
                        null, List.of(), HOY));

        assertThrows(NullPointerException.class,
                () -> IndicadoresCatalogo.calcular(
                        List.of(), null, HOY));

        assertThrows(NullPointerException.class,
                () -> IndicadoresCatalogo.calcular(
                        List.of(), List.of(), null));
    }
}
