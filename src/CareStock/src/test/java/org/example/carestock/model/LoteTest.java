package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class LoteTest {
    private Lote valido() {
        return new Lote("LOT-123", 1, 20, LocalDate.now().plusDays(30),
                2, "DISPONIBLE", 7);
    }

    @Test
    void aceptaLoteVigente() {
        assertDoesNotThrow(() -> valido().validar());
        assertTrue(valido().esAptoParaDispensar());
    }

    @Test
    void rechazaNumeroIdYCantidadInvalidos() {
        Lote l = valido();
        l.setNumeroLote(" ");
        assertThrows(ReglaNegocioException.class, l::validar);
        l = valido();
        l.setIdMedicamento(0);
        assertThrows(ReglaNegocioException.class, l::validar);
        l = valido();
        l.setCantidadActual(-1);
        assertThrows(ReglaNegocioException.class, l::validar);
    }

    @Test
    void rechazaFechaAusenteOVencida() {
        Lote l = valido();
        l.setFechaVencimiento(null);
        assertThrows(ReglaNegocioException.class, l::validar);
        l.setFechaVencimiento(LocalDate.now().minusDays(1));
        assertThrows(ReglaNegocioException.class, l::validar);
    }

    @Test
    void rechazaCuarentenaYBloqueo() {
        for (String estado : new String[]{"CUARENTENA", "bloqueado"}) {
            Lote l = valido();
            l.setEstadoLote(estado);
            assertThrows(ReglaNegocioException.class, l::validar);
            assertFalse(l.esAptoParaDispensar());
        }
    }

    @Test
    void noDispensaSinStockNiVigencia() {
        Lote l = valido();
        l.setCantidadActual(0);
        assertFalse(l.esAptoParaDispensar());
        l.setCantidadActual(1);
        l.setFechaVencimiento(LocalDate.now());
        assertFalse(l.esAptoParaDispensar());
        l.setFechaVencimiento(null);
        assertFalse(l.esAptoParaDispensar());
    }

    @Test
    void guardaDatosDeTrazabilidad() {
        Lote l = valido();
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 1, 9, 0);
        l.setIdLote(12);
        l.setFechaIngreso(fecha);
        l.setIdUsuarioModificacion(9);
        l.setFechaModificacion(fecha.plusHours(1));
        assertAll(
            () -> assertEquals(12, l.getIdLote()),
            () -> assertEquals("LOT-123", l.getNumeroLote()),
            () -> assertEquals(1, l.getIdMedicamento()),
            () -> assertEquals(20, l.getCantidadActual()),
            () -> assertEquals(Integer.valueOf(2), l.getIdUbicacion()),
            () -> assertEquals(Integer.valueOf(7), l.getIdUsuario()),
            () -> assertEquals("DISPONIBLE", l.getEstadoLote()),
            () -> assertEquals(fecha, l.getFechaIngreso()),
            () -> assertEquals(Integer.valueOf(9), l.getIdUsuarioModificacion()),
            () -> assertEquals(fecha.plusHours(1), l.getFechaModificacion())
        );
    }
}
