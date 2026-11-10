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
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioPlanificacionReposicionTest {

    private static final int FARMACIA = 7;

    private static SesionUsuario.DatosUsuario sesion(int idFarmacia) {
        return new SesionUsuario.DatosUsuario(1, "Administrador", "admin@prueba.local",
                "ADMINISTRADOR", idFarmacia, "Farmacia");
    }

    private static MockedStatic<SesionUsuario> conSesion(int idFarmacia) {
        MockedStatic<SesionUsuario> mock = mockStatic(SesionUsuario.class);
        mock.when(SesionUsuario::estaActiva).thenReturn(true);
        mock.when(SesionUsuario::actual).thenReturn(sesion(idFarmacia));
        return mock;
    }

    private static AlertaInventario alerta(Categoria categoria, int id, String codigo,
                                            int existencias, Integer minimo) {
        return new AlertaInventario(TipoAlerta.STOCK_MINIMO, categoria, id,
                codigo, "Producto " + id, null, existencias, minimo,
                null, null, existencias == 0 ? Severidad.CRITICA : Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static ProductoInventario producto(Categoria categoria, int id, String codigo,
                                                int existencias) {
        return new ProductoInventario(id, categoria.etiqueta(), codigo, "Producto " + id,
                "Presentacion", existencias, "ACTIVO");
    }

    private static ServicioPlanificacionReposicion preparar(
            ServicioAlertasInventario alertas, ConsultaInventario inventario,
            List<AlertaInventario> listaAlertas, List<ProductoInventario> listaInventario)
            throws Exception {
        when(alertas.listar(FARMACIA)).thenReturn(listaAlertas);
        when(inventario.listarPorFarmacia(FARMACIA)).thenReturn(listaInventario);
        return new ServicioPlanificacionReposicion(alertas, inventario);
    }

    @Test
    void incluyeTresCategoriasYCalculaFaltantesConSeveridadCorrecta() throws Exception {
        ServicioAlertasInventario alertas = mock(ServicioAlertasInventario.class);
        ConsultaInventario inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario, List.of(
                alerta(Categoria.MATERNIDAD, 5, "MISMO", 3, 7),
                alerta(Categoria.ASEO, 5, "MISMO", 2, 2),
                alerta(Categoria.MEDICAMENTO, 5, "MISMO", 0, 8)
        ), List.of(
                producto(Categoria.MATERNIDAD, 5, "MISMO", 3),
                producto(Categoria.ASEO, 5, "MISMO", 2),
                producto(Categoria.MEDICAMENTO, 5, "MISMO", 0)
        ));

        try (var sesion = conSesion(FARMACIA)) {
            List<RegistroReposicion> resultado = servicio.listarPorFarmacia(FARMACIA);
            assertEquals(3, resultado.size());
            assertEquals(Categoria.MEDICAMENTO, resultado.get(0).categoria());
            assertEquals(8, resultado.get(0).cantidadFaltante());
            assertEquals(Severidad.CRITICA, resultado.get(0).severidad());
            assertEquals(0, resultado.stream().filter(r -> r.categoria() == Categoria.ASEO)
                    .findFirst().orElseThrow().cantidadFaltante());
            assertEquals(4, resultado.stream().filter(r -> r.categoria() == Categoria.MATERNIDAD)
                    .findFirst().orElseThrow().cantidadFaltante());
            assertTrue(resultado.stream().filter(r -> r.existenciasActuales() > 0)
                    .allMatch(r -> r.severidad() == Severidad.ADVERTENCIA));
        }
        verify(alertas).listar(FARMACIA);
        verify(inventario).listarPorFarmacia(FARMACIA);
        verifyNoMoreInteractions(alertas, inventario);
    }

    @Test
    void distingueProductosDeMismaCategoriaConMismoCodigoPeroDiferenteId() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.ASEO, 1, "A-1", 1, 4),
                        alerta(Categoria.ASEO, 2, "A-1", 0, 4)),
                List.of(producto(Categoria.ASEO, 1, "A-1", 1),
                        producto(Categoria.ASEO, 2, "A-1", 0)));

        try (var sesion = conSesion(FARMACIA)) {
            var resultado = servicio.listarPorFarmacia(FARMACIA);
            assertEquals(2, resultado.size());
            assertNotEquals(resultado.get(0).idProducto(), resultado.get(1).idProducto());
            assertEquals("A-1", resultado.get(0).codigo());
        }
    }

    @Test
    void excluyeExistenciasSuficientesYAlertasDeVencimiento() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        AlertaInventario vencimiento = new AlertaInventario(TipoAlerta.VENCIMIENTO,
                Categoria.MEDICAMENTO, 34, "MED-1", "Medicamento", "LOTE-34", 2,
                null, LocalDate.now().plusDays(5), 5, Severidad.CRITICA,
                null, null, null);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.ASEO, 1, "A1", 8, 5), vencimiento),
                List.of(producto(Categoria.ASEO, 1, "A1", 8)));

        try (var sesion = conSesion(FARMACIA)) {
            assertTrue(servicio.listarPorFarmacia(FARMACIA).isEmpty());
        }
    }

    @Test
    void minimoCeroNoInventaFaltantesYConservaLaAlertaDeAgotado() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.ASEO, 1, "C", 0, 0),
                        alerta(Categoria.MATERNIDAD, 2, "D", 1, 0)),
                List.of(producto(Categoria.ASEO, 1, "C", 0),
                        producto(Categoria.MATERNIDAD, 2, "D", 1)));

        try (var sesion = conSesion(FARMACIA)) {
            var resultado = servicio.listarPorFarmacia(FARMACIA);
            assertEquals(1, resultado.size());
            assertEquals(0, resultado.get(0).stockMinimo());
            assertEquals(0, resultado.get(0).cantidadFaltante());
            assertEquals(Severidad.CRITICA, resultado.get(0).severidad());
        }
    }

    @Test
    void usaCodigoDelInventarioCuandoLaVistaNoLoTiene() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.MEDICAMENTO, 10, null, 1, 3)),
                List.of(producto(Categoria.MEDICAMENTO, 10, "INV-10", 3)));
        try (var sesion = conSesion(FARMACIA)) {
            var registro = servicio.listarPorFarmacia(FARMACIA).get(0);
            assertEquals("INV-10", registro.codigo());
            assertEquals(1, registro.existenciasActuales());
            assertEquals(2, registro.cantidadFaltante());
        }
    }

    @Test
    void siElProxyEstaDesactualizadoUsaLaAlertaComoFuenteDeVerdad() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.MATERNIDAD, 33, "MAT-33", 0, 9)),
                List.of(producto(Categoria.MATERNIDAD, 33, "MAT-33", 100)));
        try (var sesion = conSesion(FARMACIA)) {
            var registro = servicio.listarPorFarmacia(FARMACIA).get(0);
            assertEquals(0, registro.existenciasActuales());
            assertEquals(9, registro.cantidadFaltante());
        }
    }

    @Test
    void evitaDuplicarUnaAlertaParaLaMismaIdentidad() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        AlertaInventario a = alerta(Categoria.ASEO, 3, "COD", 0, 2);
        var servicio = preparar(alertas, inventario, List.of(a, a),
                List.of(producto(Categoria.ASEO, 3, "COD", 0)));
        try (var sesion = conSesion(FARMACIA)) {
            assertEquals(1, servicio.listarPorFarmacia(FARMACIA).size());
        }
    }

    @Test
    void farmaciaInvalidaSeRechazaAntesDeConsultarRepositorios() {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = new ServicioPlanificacionReposicion(alertas, inventario);
        assertThrows(IllegalArgumentException.class, () -> servicio.listarPorFarmacia(0));
        verifyNoInteractions(alertas, inventario);
    }

    @Test
    void sinSesionNoConsultaDatos() {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = new ServicioPlanificacionReposicion(alertas, inventario);
        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva).thenReturn(false);
            assertThrows(ReglaNegocioException.class,
                    () -> servicio.listarPorFarmacia(FARMACIA));
        }
        verifyNoInteractions(alertas, inventario);
    }

    @Test
    void farmaciaDiferenteALaSesionNoConsultaDatos() {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = new ServicioPlanificacionReposicion(alertas, inventario);
        try (var sesion = conSesion(99)) {
            assertThrows(ReglaNegocioException.class,
                    () -> servicio.listarPorFarmacia(FARMACIA));
        }
        verifyNoInteractions(alertas, inventario);
    }

    @Test
    void cambioDeFarmaciaDuranteConsultaInvalidaElResultado() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.ASEO, 1, "A", 0, 3)),
                List.of(producto(Categoria.ASEO, 1, "A", 0)));
        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva).thenReturn(true);
            sesion.when(SesionUsuario::actual).thenReturn(sesion(FARMACIA), sesion(99));
            assertThrows(ReglaNegocioException.class,
                    () -> servicio.listarPorFarmacia(FARMACIA));
        }
    }

    @Test
    void errorDeLecturaSePropagaSinModificarInventario() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        when(alertas.listar(FARMACIA)).thenReturn(List.of());
        when(inventario.listarPorFarmacia(FARMACIA))
                .thenThrow(new SQLException("Fallo lectura"));
        var servicio = new ServicioPlanificacionReposicion(alertas, inventario);
        try (var sesion = conSesion(FARMACIA)) {
            assertThrows(SQLException.class, () -> servicio.listarPorFarmacia(FARMACIA));
        }
        verify(alertas).listar(FARMACIA);
        verify(inventario).listarPorFarmacia(FARMACIA);
        verifyNoMoreInteractions(alertas, inventario);
    }

    @Test
    void minimoNegativoSeRechazaSinInventarUnValor() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.ASEO, 1, "A", 0, -1)), List.of());
        try (var sesion = conSesion(FARMACIA)) {
            assertThrows(IllegalStateException.class,
                    () -> servicio.listarPorFarmacia(FARMACIA));
        }
    }

    @Test
    void umbralRealmenteDesconocidoSeOmiteSinAsignarleCero() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        AlertaInventario umbralDesconocido = mock(AlertaInventario.class);
        when(umbralDesconocido.tipoAlerta()).thenReturn(TipoAlerta.STOCK_MINIMO);
        when(umbralDesconocido.umbral()).thenReturn(null);
        var servicio = preparar(alertas, inventario,
                List.of(umbralDesconocido), List.of());
        try (var sesion = conSesion(FARMACIA)) {
            assertTrue(servicio.listarPorFarmacia(FARMACIA).isEmpty());
        }
    }

    @Test
    void codigoInexistenteEnAmbasFuentesSeIndicaSinInventarUnCodigo() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.MATERNIDAD, 77, null, 0, 4)), List.of());
        try (var sesion = conSesion(FARMACIA)) {
            assertEquals("No registrado", servicio.listarPorFarmacia(FARMACIA).get(0).codigo());
        }
    }

    @Test
    void ignoraCategoriasNoIncluidasEnInventarioSinPerderAlertasValidas() throws Exception {
        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);
        var otro = new ProductoInventario(4, "OTRO", "X", "Otro", "P", 4, "ACTIVO");
        var servicio = preparar(alertas, inventario,
                List.of(alerta(Categoria.ASEO, 4, "Y", 1, 2)), List.of(otro));
        try (var sesion = conSesion(FARMACIA)) {
            assertEquals("Y", servicio.listarPorFarmacia(FARMACIA).get(0).codigo());
        }
    }

    @Test
    void exigeAmbasDependencias() {
        assertThrows(NullPointerException.class,
                () -> new ServicioPlanificacionReposicion(null, mock(ConsultaInventario.class)));
        assertThrows(NullPointerException.class,
                () -> new ServicioPlanificacionReposicion(mock(ServicioAlertasInventario.class), null));
    }
}
