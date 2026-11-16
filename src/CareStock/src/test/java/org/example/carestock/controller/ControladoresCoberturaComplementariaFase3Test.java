package org.example.carestock.controller;

import org.example.carestock.DataTransferObject.MovimientoHistorial;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladoresCoberturaComplementariaFase3Test {

    @Test
    void historialRechazaRolesSinPermisos() {
        assertFalse(
                ControladorHistorialMovimientos
                        .rolPermitido(null));

        assertFalse(
                ControladorHistorialMovimientos
                        .rolPermitido("AUXILIAR"));

        assertTrue(
                ControladorHistorialMovimientos
                        .rolPermitido(" farmaceutico "));

        assertFalse(
                ControladorHistorialMovimientos
                        .coincideFiltros(
                                null, "Todos", "Todos",
                                null, null));
    }

    @Test
    void historialValidaFechasOpcionales() {
        MovimientoHistorial movimiento =
                mock(MovimientoHistorial.class);

        when(movimiento.categoria())
                .thenReturn("ASEO");

        when(movimiento.tipoMovimiento())
                .thenReturn("ENTRADA");

        LocalDate fecha = LocalDate.of(2026, 11, 16);

        assertFalse(
                ControladorHistorialMovimientos
                        .coincideFiltros(
                                movimiento,
                                "ASEO", "ENTRADA",
                                fecha, null));

        when(movimiento.fechaMovimiento())
                .thenReturn(
                        OffsetDateTime.parse(
                                "2026-11-16T12:00:00-05:00"));

        assertTrue(
                ControladorHistorialMovimientos
                        .coincideFiltros(
                                movimiento,
                                "ASEO", "ENTRADA",
                                fecha, fecha));

        assertFalse(
                ControladorHistorialMovimientos
                        .coincideFiltros(
                                movimiento,
                                "ASEO", "ENTRADA",
                                fecha.plusDays(1), null));

        assertFalse(
                ControladorHistorialMovimientos
                        .coincideFiltros(
                                movimiento,
                                "ASEO", "ENTRADA",
                                null, fecha.minusDays(1)));
    }

    @Test
    void ajustesDistingueRolesAdministrativos()
            throws Exception {

        Method metodo = ControladorAjustes.class
                .getDeclaredMethod(
                        "administrador", String.class);

        metodo.setAccessible(true);

        assertTrue((boolean)
                metodo.invoke(null, "ADMINISTRADOR"));

        assertTrue((boolean)
                metodo.invoke(null, " super_admin "));

        assertFalse((boolean)
                metodo.invoke(null, "FARMACEUTICO"));

        assertFalse((boolean)
                metodo.invoke(null, (Object) null));
    }

    @Test
    void movimientosRechazaNavegacionSinDestino() {
        ControladorMovimientos controlador =
                new ControladorMovimientos();

        assertThrows(
                SecurityException.class,
                () -> controlador.abrirDesdeAlerta(null));
    }

    @Test
    void preferenciasSmsFormateaHorarios()
            throws Exception {

        Method metodo = ControladorPreferenciasSms.class
                .getDeclaredMethod(
                        "hora", java.time.LocalTime.class);

        metodo.setAccessible(true);

        assertEquals(
                "", metodo.invoke(null, (Object) null));

        assertEquals(
                "07:05",
                metodo.invoke(
                        null,
                        java.time.LocalTime.of(7, 5)));

        assertEquals(
                "23:59",
                metodo.invoke(
                        null,
                        java.time.LocalTime.of(23, 59)));
    }
}
