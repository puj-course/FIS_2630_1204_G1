package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MedicamentoTest {
    private Medicamento valido() {
        Medicamento m = new Medicamento();
        m.setCodigoInvima("INV-123");
        m.setNombreComercial("Acetaminofen");
        m.setEstado("ACTIVO");
        m.setStockMinimo(2);
        m.setStockTotal(10);
        m.setPrecio(new BigDecimal("1500.50"));
        return m;
    }

    @Test
    void validaMedicamentoCorrecto() {
        assertDoesNotThrow(() -> valido().validar());
    }

    @Test
    void rechazaNombreYCodigoAusentes() {
        Medicamento m = valido();
        m.setNombreComercial("  ");
        assertThrows(ReglaNegocioException.class, m::validar);
        m = valido();
        m.setCodigoInvima(null);
        assertThrows(ReglaNegocioException.class, m::validar);
    }

    @Test
    void rechazaPreciosInvalidos() {
        Medicamento m = valido();
        for (String precio : new String[]{"-0.01", "2.999", "12345678901.00"}) {
            m.setPrecio(new BigDecimal(precio));
            assertThrows(ReglaNegocioException.class, m::validar);
        }
        m.setPrecio(null);
        assertDoesNotThrow(m::validar);
    }

    @Test
    void rechazaStockNegativoYBloqueo() {
        Medicamento m = valido();
        m.setStockMinimo(-1);
        assertThrows(ReglaNegocioException.class, m::validar);
        m.setStockMinimo(0);
        m.setStockTotal(-1);
        assertThrows(ReglaNegocioException.class, m::validar);
        m.setStockTotal(2);
        m.setEstado("bloqueado");
        assertThrows(ReglaNegocioException.class, m::validar);
    }

    @Test
    void dispensacionExigeEstadoStockYCodigo() {
        Medicamento m = valido();
        assertTrue(m.esAptoParaDispensar());
        m.setStockTotal(0);
        assertFalse(m.esAptoParaDispensar());
        m.setStockTotal(1);
        m.setEstado("INACTIVO");
        assertFalse(m.esAptoParaDispensar());
        m.setEstado("ACTIVO");
        m.setCodigoInvima(" ");
        assertFalse(m.esAptoParaDispensar());
    }

    @Test
    void conservaDatosDeCatalogoYAuditoria() {
        Medicamento m = valido();
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 10, 0);
        m.setIdMedicamento(7);
        m.setPrincipioActivo("Paracetamol");
        m.setConcentracion("500mg");
        m.setFormaFarmaceutica("Tableta");
        m.setIdCategoria(4);
        m.setIdFarmacia(3);
        m.setPresentacion("Caja");
        m.setIdUsuarioCreacion(10);
        m.setIdUsuarioModificacion(11);
        m.setFechaModificacion(fecha);
        assertAll(
            () -> assertEquals(7, m.getIdMedicamento()),
            () -> assertEquals("Paracetamol", m.getPrincipioActivo()),
            () -> assertEquals("500mg", m.getConcentracion()),
            () -> assertEquals("Tableta", m.getFormaFarmaceutica()),
            () -> assertEquals(Integer.valueOf(4), m.getIdCategoria()),
            () -> assertEquals(Integer.valueOf(3), m.getIdFarmacia()),
            () -> assertEquals("Caja", m.getPresentacion()),
            () -> assertEquals(Integer.valueOf(10), m.getIdUsuarioCreacion()),
            () -> assertEquals(Integer.valueOf(11), m.getIdUsuarioModificacion()),
            () -> assertEquals(fecha, m.getFechaModificacion()),
            () -> assertEquals(new BigDecimal("1500.50"), m.getPrecio())
        );
    }
}
