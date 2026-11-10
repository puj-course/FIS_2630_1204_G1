package org.example.carestock.DataTransferObject;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiltroDisponibilidadTest {

    private static ProductoInventario producto(int existencia) {
        return new ProductoInventario(
                1, "Aseo", "ASE-001", "Jabon", "Barra 100 g", existencia, "ACTIVO");
    }

    @Test
    void conExistenciasSoloAceptaProductosConUnidades() {
        assertTrue(FiltroDisponibilidad.coincide(producto(5), FiltroDisponibilidad.CON_EXISTENCIAS));
        assertTrue(FiltroDisponibilidad.coincide(producto(1), FiltroDisponibilidad.CON_EXISTENCIAS));
        assertFalse(FiltroDisponibilidad.coincide(producto(0), FiltroDisponibilidad.CON_EXISTENCIAS));
    }

    @Test
    void agotadosSoloAceptaProductosSinUnidades() {
        assertTrue(FiltroDisponibilidad.coincide(producto(0), FiltroDisponibilidad.AGOTADOS));
        assertFalse(FiltroDisponibilidad.coincide(producto(1), FiltroDisponibilidad.AGOTADOS));
        assertFalse(FiltroDisponibilidad.coincide(producto(20), FiltroDisponibilidad.AGOTADOS));
    }

    @Test
    void todosAceptaCualquierProducto() {
        assertTrue(FiltroDisponibilidad.coincide(producto(0), FiltroDisponibilidad.TODOS));
        assertTrue(FiltroDisponibilidad.coincide(producto(7), FiltroDisponibilidad.TODOS));
    }

    @Test
    void opcionNulaODesconocidaSeTrataComoTodos() {
        assertTrue(FiltroDisponibilidad.coincide(producto(0), null));
        assertTrue(FiltroDisponibilidad.coincide(producto(3), null));
        assertTrue(FiltroDisponibilidad.coincide(producto(0), "otra cosa"));
    }

    @Test
    void productoNuloNoCoincide() {
        assertFalse(FiltroDisponibilidad.coincide(null, FiltroDisponibilidad.TODOS));
    }

    @Test
    void lasOpcionesVanEnElOrdenDelCombo() {
        assertEquals(List.of("Todos", "Con existencias", "Agotados"),
                FiltroDisponibilidad.opciones());
    }
}
