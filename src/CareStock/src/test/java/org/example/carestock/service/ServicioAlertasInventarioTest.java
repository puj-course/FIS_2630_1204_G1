package org.example.carestock.service;

import org.example.carestock.dao.AlertasInventarioDao;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioAlertasInventarioTest {

    private static AlertaInventario stock(Categoria categoria, Severidad severidad) {
        return new AlertaInventario(TipoAlerta.STOCK_MINIMO, categoria, 1, "C-1",
                "Producto", null, 2, 5, null, null, severidad, null, null, null);
    }

    private static AlertaInventario vencimiento(Severidad severidad) {
        return new AlertaInventario(TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO, 1,
                "INV", "Medicamento", "L-1", 4, null, LocalDate.now().plusDays(10), 10,
                severidad, null, null, null);
    }

    private static List<AlertaInventario> muestra() {
        return List.of(
                stock(Categoria.ASEO, Severidad.CRITICA),
                stock(Categoria.MATERNIDAD, Severidad.ADVERTENCIA),
                stock(Categoria.MEDICAMENTO, Severidad.ADVERTENCIA),
                vencimiento(Severidad.CRITICA),
                vencimiento(Severidad.ADVERTENCIA));
    }

    @Test
    void resumeLasAlertasPorSeveridadYPorTipo() {
        ServicioAlertasInventario.Resumen resumen =
                ServicioAlertasInventario.resumir(muestra());

        assertEquals(2, resumen.criticas());
        assertEquals(3, resumen.advertencias());
        assertEquals(3, resumen.stockMinimo());
        assertEquals(2, resumen.vencimiento());
        assertEquals(5, resumen.total());
    }

    @Test
    void resumenDeUnaListaVaciaEsCero() {
        ServicioAlertasInventario.Resumen resumen =
                ServicioAlertasInventario.resumir(List.of());

        assertEquals(0, resumen.total());
        assertEquals(0, resumen.criticas());
    }

    @Test
    void filtraPorTipo() {
        List<AlertaInventario> resultado = ServicioAlertasInventario.filtrar(
                muestra(), TipoAlerta.VENCIMIENTO, null, null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(a -> a.tipoAlerta() == TipoAlerta.VENCIMIENTO));
    }

    @Test
    void filtraPorCategoria() {
        List<AlertaInventario> resultado = ServicioAlertasInventario.filtrar(
                muestra(), null, Categoria.ASEO, null);

        assertEquals(1, resultado.size());
    }

    @Test
    void filtraPorSeveridad() {
        List<AlertaInventario> resultado = ServicioAlertasInventario.filtrar(
                muestra(), null, null, Severidad.CRITICA);

        assertEquals(2, resultado.size());
    }

    @Test
    void combinaTodosLosFiltros() {
        List<AlertaInventario> resultado = ServicioAlertasInventario.filtrar(
                muestra(), TipoAlerta.STOCK_MINIMO, Categoria.MEDICAMENTO, Severidad.ADVERTENCIA);

        assertEquals(1, resultado.size());
    }

    @Test
    void sinFiltrosDevuelveTodo() {
        assertEquals(5, ServicioAlertasInventario.filtrar(muestra(), null, null, null).size());
    }

    @Test
    void rechazaUnaFarmaciaInvalida() {
        ServicioAlertasInventario servicio = new ServicioAlertasInventario(new AlertasInventarioDao());

        assertThrows(IllegalArgumentException.class, () -> servicio.listar(0));
    }

    @Test
    void exigeUnaSesionActivaParaConsultar() {
        ServicioAlertasInventario servicio = new ServicioAlertasInventario(new AlertasInventarioDao());

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> servicio.listar(1));
        assertEquals("Debe iniciar sesión.", error.getMessage());
    }

    @Test
    void exigeUnDaoValido() {
        assertThrows(NullPointerException.class, () -> new ServicioAlertasInventario(null));
    }
}
