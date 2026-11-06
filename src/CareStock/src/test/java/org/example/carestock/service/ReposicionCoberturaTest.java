package org.example.carestock.service;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.proxy.ConsultaInventario;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReposicionCoberturaTest {

    private static final int FARMACIA = 7;

    private static SesionUsuario.DatosUsuario sesion(int farmacia) {
        return new SesionUsuario.DatosUsuario(
                1, "Administrador", "admin@prueba.local",
                "ADMINISTRADOR", farmacia, "Farmacia");
    }

    private static AlertaInventario alerta(
            int id, String codigo, String nombre) {

        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                Categoria.ASEO,
                id,
                codigo,
                nombre,
                null,
                1,
                3,
                null,
                null,
                Severidad.ADVERTENCIA,
                null,
                null,
                null);
    }

    private static ProductoInventario producto(
            int id, String categoria, String codigo) {

        return new ProductoInventario(
                id,
                categoria,
                codigo,
                "Producto " + id,
                "Unidad",
                1,
                "ACTIVO");
    }

    private static List<RegistroReposicion> ejecutar(
            List<AlertaInventario> registros,
            List<ProductoInventario> productos) throws Exception {

        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);

        when(alertas.listar(FARMACIA)).thenReturn(registros);
        when(inventario.listarPorFarmacia(FARMACIA))
                .thenReturn(productos);

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(sesion(FARMACIA));

            return new ServicioPlanificacionReposicion(
                    alertas, inventario)
                    .listarPorFarmacia(FARMACIA);
        }
    }

    @Test
    void constructorPorDefectoInicializaDependencias() {
        assertNotNull(new ServicioPlanificacionReposicion());
    }

    @Test
    void codigoEnBlancoRecuperaCodigoDelCatalogo()
            throws Exception {

        var resultado = ejecutar(
                List.of(alerta(10, "   ", "Alcohol")),
                List.of(producto(10, "  aSeo  ", "ASEO-10")));

        assertEquals(1, resultado.size());
        assertEquals("ASEO-10", resultado.get(0).codigo());
        assertEquals(2, resultado.get(0).cantidadFaltante());
    }

    @Test
    void categoriaNulaSimuladaNoInterrumpeConsulta()
            throws Exception {

        // Simula una dependencia con categoria nula.
        var productoInvalido = mock(ProductoInventario.class);
        when(productoInvalido.categoria()).thenReturn(null);

        var resultado = ejecutar(
                List.of(alerta(11, "A-11", "Jabon")),
                List.of(productoInvalido));

        assertEquals(1, resultado.size());
        assertEquals("A-11", resultado.get(0).codigo());
    }

    @Test
    void catalogoDuplicadoConservaPrimerCodigoPorCategoriaEId()
            throws Exception {

        var resultado = ejecutar(
                List.of(alerta(12, null, "Jabon")),
                List.of(
                        producto(12, "Aseo", "PRIMERO"),
                        producto(12, "Aseo", "SEGUNDO")));

        assertEquals(1, resultado.size());
        assertEquals("PRIMERO", resultado.get(0).codigo());
    }

    @Test
    void consultaSinAlertasNiProductosDevuelveListaVacia()
            throws Exception {

        assertTrue(ejecutar(List.of(), List.of()).isEmpty());
    }

    @Test
    void falloEnAlertasImpideConsultarInventario()
            throws Exception {

        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);

        when(alertas.listar(FARMACIA))
                .thenThrow(new SQLException("Sin conexion"));

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(sesion(FARMACIA));

            assertThrows(SQLException.class, () ->
                    new ServicioPlanificacionReposicion(
                            alertas, inventario)
                            .listarPorFarmacia(FARMACIA));
        }

        verifyNoInteractions(inventario);
    }

    @Test
    void mismoNombreSeOrdenaPorIdentificador()
            throws Exception {

        var resultado = ejecutar(
                List.of(
                        alerta(20, "A-20", "Jabon"),
                        alerta(3, "A-3", "Jabon")),
                List.of());

        assertEquals(3, resultado.get(0).idProducto());
        assertEquals(20, resultado.get(1).idProducto());
    }

    @Test
    void cambioDeFarmaciaAntesDeRetornarBloqueaLosResultados()
            throws Exception {

        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);

        when(alertas.listar(FARMACIA))
                .thenReturn(List.of(
                        alerta(1, "A-1", "Jabon")));

        when(inventario.listarPorFarmacia(FARMACIA))
                .thenReturn(List.of());

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);

            sesion.when(SesionUsuario::actual)
                    .thenReturn(
                            sesion(FARMACIA),
                            sesion(FARMACIA),
                            sesion(99));

            assertThrows(ReglaNegocioException.class, () ->
                    new ServicioPlanificacionReposicion(
                            alertas, inventario)
                            .listarPorFarmacia(FARMACIA));
        }

        verify(alertas).listar(FARMACIA);
        verify(inventario).listarPorFarmacia(FARMACIA);
    }
}
