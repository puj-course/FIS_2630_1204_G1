package org.example.carestock.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AjusteExistenciasTest {
    private AjusteExistencias crear(String motivo, int anterior, int nueva) {
        return new AjusteExistencias(AjusteExistencias.Categoria.ASEO,
                1, 2, 3, motivo, anterior, nueva);
    }

    @Test
    void calculaDiferenciaYNormalizaMotivo() {
        AjusteExistencias a = crear("  Conteo fisico  ", 10, 15);
        assertEquals(5, a.diferencia());
        assertEquals("Conteo fisico", a.motivo());
        assertEquals(-3, crear("Correccion", 8, 5).diferencia());
        assertEquals(3, AjusteExistencias.Categoria.values().length);
    }

    @Test
    void rechazaCategoriaEIdentificadoresInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new AjusteExistencias(null, 1, 2, 3, "Motivo", 1, 2));
        for (int[] ids : new int[][]{{0, 2, 3}, {1, 0, 3}, {1, 2, 0}}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new AjusteExistencias(AjusteExistencias.Categoria.MEDICAMENTO,
                            ids[0], ids[1], ids[2], "Motivo", 1, 2));
        }
    }

    @Test
    void rechazaCantidadesInvalidasYSinCambio() {
        assertThrows(IllegalArgumentException.class, () -> crear("A", -1, 3));
        assertThrows(IllegalArgumentException.class, () -> crear("A", 1, -1));
        assertThrows(IllegalArgumentException.class, () -> crear("A", 4, 4));
    }

    @Test
    void rechazaMotivoAusenteODemasiadoLargo() {
        assertThrows(IllegalArgumentException.class, () -> crear(null, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> crear("  ", 1, 2));
        assertThrows(IllegalArgumentException.class, () -> crear("x".repeat(251), 1, 2));
    }
}
