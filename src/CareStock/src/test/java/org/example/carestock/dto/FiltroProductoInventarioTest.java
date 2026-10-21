package org.example.carestock.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiltroProductoInventarioTest {

    private ProductoInventario producto(
            int id,
            String categoria,
            String codigo,
            String nombre,
            int stock
    ) {
        return new ProductoInventario(
                id,
                categoria,
                codigo,
                nombre,
                "No registrada",
                stock,
                "ACTIVO"
        );
    }

    @Test
    void consultaTodasLasCategorias() {
        var medicamento = producto(
                1, "Medicamento", "INV-1", "Acetaminofen", 5
        );

        var aseo = producto(
                1, "Aseo", "AS-1", "Jabon", 10
        );

        assertTrue(FiltroProductoInventario.coincide(
                medicamento, "Todas", ""
        ));

        assertTrue(FiltroProductoInventario.coincide(
                aseo, "Todas", ""
        ));
    }

    @Test
    void combinaCategoriaYBusqueda() {
        var medicamento = producto(
                1, "Medicamento", "INV-123", "Acetaminofen", 5
        );

        assertTrue(FiltroProductoInventario.coincide(
                medicamento, "Medicamento", "ACETA"
        ));

        assertTrue(FiltroProductoInventario.coincide(
                medicamento, "MEDICAMENTO", "inv-12"
        ));

        assertFalse(FiltroProductoInventario.coincide(
                medicamento, "Aseo", "ACETA"
        ));
    }

    @Test
    void diferenciaIdentificadoresPorCategoria() {
        var aseo = producto(
                3, "Aseo", "AS-3", "Jabon", 5
        );

        var maternidad = producto(
                3, "Maternidad", "MAT-3", "Biberon", 2
        );

        assertNotEquals(aseo, maternidad);

        assertTrue(FiltroProductoInventario.coincide(
                aseo, "Aseo", ""
        ));

        assertFalse(FiltroProductoInventario.coincide(
                maternidad, "Aseo", ""
        ));
    }

    @Test
    void muestraProductosSinStock() {
        var producto = producto(
                4, "Maternidad", "MAT-4", "Panal", 0
        );

        assertEquals(0, producto.existencia());

        assertTrue(FiltroProductoInventario.coincide(
                producto, "Todas", "PANAL"
        ));
    }
}
