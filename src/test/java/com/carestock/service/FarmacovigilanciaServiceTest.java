package com.carestock.service;

import com.carestock.exception.ReglaNegocioException;
import com.carestock.model.Lote;
import com.carestock.model.Medicamento;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FarmacovigilanciaServiceTest {

    private final FarmacovigilanciaService service =
            new FarmacovigilanciaService();

    private Medicamento medicamento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setIdMedicamento(1L);
        medicamento.setNombreComercial("Medicamento de prueba");
        medicamento.setCodigoInvima("REGISTRO-PRUEBA");
        medicamento.setStockMinimo(1);
        medicamento.setStockTotal(20);
        medicamento.setEstado("ACTIVO");
        return medicamento;
    }

    private Lote lote(int idMedicamento, LocalDate vencimiento) {
        return new Lote(
                "LOTE-PRUEBA", idMedicamento, 10,
                vencimiento, 1, 1);
    }

    @Test
    void permiteCantidadDisponibleSinModificarStock() {
        Medicamento medicamento = medicamento();
        Lote lote = lote(1, LocalDate.now().plusDays(30));

        assertDoesNotThrow(() ->
                service.validarDispensacion(medicamento, lote, 10));

        assertEquals(10, lote.getCantidadActual());
        assertEquals(Integer.valueOf(20), medicamento.getStockTotal());
    }

    @Test
    void rechazaCantidadCeroNegativaYSuperiorAlStock() {
        for (int cantidad : new int[]{0, -1, 11}) {
            assertThrows(ReglaNegocioException.class, () ->
                    service.validarDispensacion(
                            medicamento(),
                            lote(1, LocalDate.now().plusDays(30)),
                            cantidad));
        }
    }

    @Test
    void rechazaLoteVencidoYConVencimientoHoy() {
        for (LocalDate fecha : new LocalDate[]{
                LocalDate.now().minusDays(1), LocalDate.now()}) {
            assertThrows(ReglaNegocioException.class, () ->
                    service.validarDispensacion(
                            medicamento(), lote(1, fecha), 1));
        }
    }

    @Test
    void rechazaLoteDeOtroMedicamento() {
        assertThrows(ReglaNegocioException.class, () ->
                service.validarDispensacion(
                        medicamento(),
                        lote(2, LocalDate.now().plusDays(30)), 1));
    }

    @Test
    void rechazaMedicamentoBloqueado() {
        Medicamento medicamento = medicamento();
        medicamento.setEstado("BLOQUEADO");

        assertThrows(ReglaNegocioException.class, () ->
                service.validarDispensacion(
                        medicamento,
                        lote(1, LocalDate.now().plusDays(30)), 1));
    }

    @Test
    void rechazaLoteEnCuarentena() {
        Lote lote = lote(1, LocalDate.now().plusDays(30));
        lote.setEstadoLote("CUARENTENA");

        assertThrows(ReglaNegocioException.class, () ->
                service.validarDispensacion(medicamento(), lote, 1));
    }

    @Test
    void rechazaStockTotalInsuficiente() {
        Medicamento medicamento = medicamento();
        medicamento.setStockTotal(2);

        assertThrows(ReglaNegocioException.class, () ->
                service.validarDispensacion(
                        medicamento,
                        lote(1, LocalDate.now().plusDays(30)), 3));
    }
}
