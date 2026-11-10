package org.example.carestock.service;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumenCategoriaCatalogoTest {

    private static RegistroCatalogo<?> aseo(
            int id, int stock, int minimo) {

        Aseo a = new Aseo();
        a.setStock(stock);
        a.setStockMinimo(minimo);

        return new RegistroCatalogo<>(id, a, "ACTIVO");
    }

    private static RegistroCatalogo<?> maternidad(
            int id, int stock, int minimo) {

        Maternidad m = new Maternidad();
        m.setStock(stock);
        m.setStockMinimo(minimo);

        return new RegistroCatalogo<>(id, m, "ACTIVO");
    }

    @Test
    void aseoDistingueAgotadosYBajoMinimo() {
        assertEquals(
                new ResumenCategoriaCatalogo(4, 2, 1),
                ResumenCategoriaCatalogo.calcular(
                        "Aseo",
                        List.of(
                                aseo(1, 0, 5),
                                aseo(2, 2, 5),
                                aseo(3, 5, 5),
                                aseo(4, 8, 5))));
    }

    @Test
    void maternidadCalculaSusPropiosIndicadores() {
        assertEquals(
                new ResumenCategoriaCatalogo(3, 1, 1),
                ResumenCategoriaCatalogo.calcular(
                        "Maternidad",
                        List.of(
                                maternidad(1, 0, 3),
                                maternidad(2, 2, 3),
                                maternidad(3, 7, 3))));
    }

    @Test
    void minimoCeroNoCreaAdvertenciaConStockPositivo() {
        assertEquals(
                new ResumenCategoriaCatalogo(2, 0, 1),
                ResumenCategoriaCatalogo.calcular(
                        "Aseo",
                        List.of(
                                aseo(1, 6, 0),
                                aseo(2, 0, 0))));
    }

    @Test
    void duplicadosYElementosNulosNoAumentanIndicadores() {
        assertEquals(
                new ResumenCategoriaCatalogo(1, 0, 1),
                ResumenCategoriaCatalogo.calcular(
                        "Aseo",
                        Arrays.asList(
                                null,
                                aseo(4, 0, 3),
                                aseo(4, 0, 3),
                                aseo(0, 0, 3),
                                maternidad(5, 0, 3))));
    }

    @Test
    void categoriasDiferentesNoSeMezclan() {
        assertEquals(
                new ResumenCategoriaCatalogo(1, 0, 1),
                ResumenCategoriaCatalogo.calcular(
                        "Maternidad",
                        List.of(
                                aseo(2, 0, 4),
                                maternidad(2, 0, 4))));
    }

    @Test
    void entradasInvalidasSonRechazadas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ResumenCategoriaCatalogo.calcular(
                        "Medicamentos", List.of()));

        assertThrows(
                IllegalArgumentException.class,
                () -> ResumenCategoriaCatalogo.calcular(
                        "Aseo", null));
    }
}
