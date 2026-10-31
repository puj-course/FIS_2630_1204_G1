package org.example.carestock.dto;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertaInventarioTest {

    private static AlertaInventario stock(int existencias, int umbral) {
        return new AlertaInventario(TipoAlerta.STOCK_MINIMO, Categoria.ASEO, 1,
                "AS-1", "Jabon", null, existencias, umbral, null, null,
                Severidad.ADVERTENCIA, null, null, null);
    }

    private static AlertaInventario lote(int dias) {
        return new AlertaInventario(TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO, 7,
                "INV-1", "Acetaminofen", "L-001", 5, null, LocalDate.of(2026, 12, 1),
                dias, Severidad.CRITICA, null, null, null);
    }

    @Test
    void mensajeSinExistencias() {
        assertEquals("Sin existencias (mínimo 5)", stock(0, 5).mensaje());
    }

    @Test
    void mensajeExistenciasBajas() {
        assertEquals("Existencias 3 (mínimo 5)", stock(3, 5).mensaje());
    }

    @Test
    void mensajeVenceEnVariosDias() {
        assertEquals("Vence en 12 días (01/12/2026)", lote(12).mensaje());
    }

    @Test
    void mensajeVenceEnUnDia() {
        assertEquals("Vence en 1 día (01/12/2026)", lote(1).mensaje());
    }

    @Test
    void mensajeVenceHoy() {
        assertEquals("El lote vence hoy", lote(0).mensaje());
    }

    @Test
    void mensajeVencidoHaceVariosDias() {
        assertEquals("Lote vencido hace 3 días, pendiente de deshabilitar", lote(-3).mensaje());
    }

    @Test
    void mensajeVencidoHaceUnDia() {
        assertEquals("Lote vencido hace 1 día, pendiente de deshabilitar", lote(-1).mensaje());
    }

    @Test
    void referenciaUsaElLoteOElCodigo() {
        assertEquals("Lote L-001", lote(5).referencia());
        assertEquals("AS-1", stock(1, 5).referencia());
    }

    @Test
    void rechazaAlertaDeStockSinUmbral() {
        assertThrows(IllegalArgumentException.class, () -> new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, Categoria.ASEO, 1, "AS-1", "Jabon", null,
                1, null, null, null, Severidad.ADVERTENCIA, null, null, null));
    }

    @Test
    void rechazaAlertaDeVencimientoSinFecha() {
        assertThrows(IllegalArgumentException.class, () -> new AlertaInventario(
                TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO, 1, "INV", "Med", "L-1",
                1, null, null, null, Severidad.CRITICA, null, null, null));
    }

    @Test
    void rechazaExistenciasNegativas() {
        assertThrows(IllegalArgumentException.class, () -> stock(-1, 5));
    }

    @Test
    void rechazaProductoVacio() {
        assertThrows(IllegalArgumentException.class, () -> new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, Categoria.ASEO, 1, "AS-1", " ", null,
                1, 5, null, null, Severidad.ADVERTENCIA, null, null, null));
    }

    @Test
    void rechazaReferenciaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, Categoria.ASEO, 0, "AS-1", "Jabon", null,
                1, 5, null, null, Severidad.ADVERTENCIA, null, null, null));
    }
}
