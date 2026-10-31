package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockMinimoCatalogoTest {

    private static Aseo aseo(int stockMinimo) {
        Aseo producto = new Aseo();
        producto.setCodigo("AS-1");
        producto.setNombre("Jabon");
        producto.setDescripcion("Jabon liquido");
        producto.setPrecio(1500);
        producto.setStock(10);
        producto.setStockMinimo(stockMinimo);
        return producto;
    }

    private static Maternidad maternidad(int stockMinimo) {
        Maternidad producto = new Maternidad();
        producto.setCodigo("MA-1");
        producto.setNombre("Crema");
        producto.setDescripcion("Crema hidratante");
        producto.setPrecio(2500);
        producto.setStock(10);
        producto.setStockMinimo(stockMinimo);
        return producto;
    }

    @Test
    void elStockMinimoDeAseoPorDefectoEsCero() {
        assertEquals(0, new Aseo().getStockMinimo());
    }

    @Test
    void aseoAceptaUnStockMinimoNoNegativo() {
        assertDoesNotThrow(() -> aseo(0).validar());
        assertDoesNotThrow(() -> aseo(8).validar());
        assertEquals(8, aseo(8).getStockMinimo());
    }

    @Test
    void aseoRechazaUnStockMinimoNegativo() {
        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> aseo(-1).validar());
        assertEquals("El stock mínimo del producto de aseo no puede ser negativo.",
                error.getMessage());
    }

    @Test
    void elStockMinimoDeMaternidadPorDefectoEsCero() {
        assertEquals(0, new Maternidad().getStockMinimo());
    }

    @Test
    void maternidadAceptaUnStockMinimoNoNegativo() {
        assertDoesNotThrow(() -> maternidad(0).validar());
        assertDoesNotThrow(() -> maternidad(6).validar());
        assertEquals(6, maternidad(6).getStockMinimo());
    }

    @Test
    void maternidadRechazaUnStockMinimoNegativo() {
        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> maternidad(-2).validar());
        assertEquals("El stock mínimo del producto de maternidad no puede ser negativo.",
                error.getMessage());
    }
}
