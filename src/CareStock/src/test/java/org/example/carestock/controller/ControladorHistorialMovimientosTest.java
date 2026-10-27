package org.example.carestock.controller;

import org.example.carestock.DataTransferObject.MovimientoHistorial;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ControladorHistorialMovimientosTest {

    private MovimientoHistorial movimiento(
            String categoria,
            String tipo,
            LocalDate fecha
    ) {

        return new MovimientoHistorial(
                "LOG_MOVIMIENTOS",
                1L,
                categoria,
                tipo,
                10,
                "Producto de prueba",
                "REF-001",
                5,
                "Inventario de prueba",
                null,
                null,
                null,
                2,
                3,
                "Usuario de prueba",
                LocalDateTime.of(
                        fecha.getYear(),
                        fecha.getMonthValue(),
                        fecha.getDayOfMonth(),
                        12, 0
                ).atZone(ZoneId.systemDefault())
                 .toOffsetDateTime()
        );
    }

    @Test
    void administradorPuedeConsultar() {

        assertTrue(
                ControladorHistorialMovimientos
                        .rolPermitido("ADMINISTRADOR"));
    }

    @Test
    void farmaceuticoPuedeConsultar() {

        assertTrue(
                ControladorHistorialMovimientos
                        .rolPermitido("FARMACEUTICO"));
    }

    @Test
    void otroRolNoPuedeConsultar() {

        assertFalse(
                ControladorHistorialMovimientos
                        .rolPermitido("USUARIO"));

        assertFalse(
                ControladorHistorialMovimientos
                        .rolPermitido(null));
    }

    @Test
    void filtraPorCategoria() {

        var m = movimiento(
                "ASEO", "ENTRADA",
                LocalDate.of(2026, 10, 24));

        assertTrue(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "ASEO", "Todos", null, null));

        assertFalse(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "MATERNIDAD", "Todos", null, null));
    }

    @Test
    void filtraPorTipoDeMovimiento() {

        var m = movimiento(
                "MEDICAMENTO", "SALIDA",
                LocalDate.of(2026, 10, 24));

        assertTrue(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "Todos", "SALIDA", null, null));

        assertFalse(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "Todos", "ENTRADA", null, null));
    }

    @Test
    void filtraPorRangoDeFechasInclusivo() {

        LocalDate fecha = LocalDate.of(2026, 10, 24);

        var m = movimiento("ASEO", "AJUSTE", fecha);

        assertTrue(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "Todos", "Todos",
                        fecha, fecha));

        assertFalse(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "Todos", "Todos",
                        LocalDate.of(2026, 10, 25),
                        LocalDate.of(2026, 10, 30)));
    }

    @Test
    void permiteCombinarTodosLosFiltros() {

        var m = movimiento(
                "MATERNIDAD",
                "ENTRADA",
                LocalDate.of(2026, 10, 20));

        assertTrue(
                ControladorHistorialMovimientos.coincideFiltros(
                        m,
                        "MATERNIDAD",
                        "ENTRADA",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)));

        assertFalse(
                ControladorHistorialMovimientos.coincideFiltros(
                        m,
                        "ASEO",
                        "ENTRADA",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)));
    }

    @Test
    void permiteConsultarTodasLasCategorias() {

        var m = movimiento(
                "MEDICAMENTO",
                "AJUSTE",
                LocalDate.of(2026, 10, 24));

        assertTrue(
                ControladorHistorialMovimientos.coincideFiltros(
                        m, "Todos", "Todos", null, null));
    }
}
