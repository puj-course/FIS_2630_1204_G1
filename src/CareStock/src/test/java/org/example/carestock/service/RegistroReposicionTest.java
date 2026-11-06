package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistroReposicionTest {

    private RegistroReposicion nuevo(int existencia, int minimo, int faltante,
                                    Severidad severidad) {
        return new RegistroReposicion(Categoria.ASEO, 1, "A", "Alcohol",
                existencia, minimo, faltante, severidad);
    }

    @Test
    void creaRegistroValidoYNormalizaCodigoAusente() {
        var registro = new RegistroReposicion(Categoria.MEDICAMENTO, 10, null,
                "Producto", 0, 0, 0, Severidad.CRITICA);
        assertEquals("No registrado", registro.codigo());
        assertEquals(0, registro.cantidadFaltante());
    }

    @Test
    void codigoEnBlancoSeMarcaComoNoRegistrado() {
        var registro = new RegistroReposicion(Categoria.ASEO, 12, " ",
                "Jabon", 2, 2, 0, Severidad.ADVERTENCIA);
        assertEquals("No registrado", registro.codigo());
    }

    @Test
    void noPermiteUnFaltanteIncorrecto() {
        assertThrows(IllegalArgumentException.class,
                () -> nuevo(1, 5, 5, Severidad.ADVERTENCIA));
    }

    @Test
    void noPermiteExistenciasSuperioresAlMinimo() {
        assertThrows(IllegalArgumentException.class,
                () -> nuevo(6, 5, 0, Severidad.ADVERTENCIA));
    }

    @Test
    void noPermiteMinimosNiExistenciasNegativas() {
        assertThrows(IllegalArgumentException.class,
                () -> nuevo(-1, 5, 6, Severidad.ADVERTENCIA));
        assertThrows(IllegalArgumentException.class,
                () -> nuevo(0, -1, 0, Severidad.CRITICA));
    }

    @Test
    void laClasificacionDebeSerConsistente() {
        assertThrows(IllegalArgumentException.class,
                () -> nuevo(0, 5, 5, Severidad.ADVERTENCIA));
        assertThrows(IllegalArgumentException.class,
                () -> nuevo(1, 5, 4, Severidad.CRITICA));
    }

    @Test
    void noPermiteDatosDeIdentidadInvalidos() {
        assertThrows(NullPointerException.class, () -> new RegistroReposicion(
                null, 1, "A", "Alcohol", 0, 0, 0, Severidad.CRITICA));
        assertThrows(NullPointerException.class, () -> new RegistroReposicion(
                Categoria.ASEO, 1, "A", "Alcohol", 0, 0, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new RegistroReposicion(
                Categoria.ASEO, 0, "A", "Alcohol", 0, 0, 0, Severidad.CRITICA));
        assertThrows(IllegalArgumentException.class, () -> new RegistroReposicion(
                Categoria.ASEO, 1, "A", " ", 0, 0, 0, Severidad.CRITICA));
        assertThrows(IllegalArgumentException.class, () -> new RegistroReposicion(
                Categoria.ASEO, 1, "A", null, 0, 0, 0, Severidad.CRITICA));
    }
}
