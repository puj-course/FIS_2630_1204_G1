package org.example.carestock.facade;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.config.ConexionBD;
import org.example.carestock.dao.*;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.*;
import org.example.carestock.proxy.ConsultaInventarioProxy;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.service.ServicioAlertasInventario;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventarioFachadaCoberturaTest {

    private Connection con;
    private PreparedStatement psSesion;
    private ResultSet rsSesion;
    private ConsultaInventarioProxy consulta;

    private MockedStatic<ConexionBD> bd;
    private MockedStatic<SesionUsuario> sesion;
    private MockedStatic<InventarioConsultas> cache;

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setIdUsuario(9);
        return u;
    }

    private Inventario inventario() {
        return new Inventario(usuario());
    }

    @BeforeEach
    void preparar() throws Exception {
        con = mock(Connection.class);
        psSesion = mock(PreparedStatement.class);
        rsSesion = mock(ResultSet.class);
        consulta = mock(ConsultaInventarioProxy.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(psSesion);

        when(psSesion.executeQuery())
                .thenReturn(rsSesion);

        when(rsSesion.next()).thenReturn(true, false);
        when(rsSesion.getInt("id_farmacia"))
                .thenReturn(7);

        bd = mockStatic(ConexionBD.class);
        bd.when(ConexionBD::getConexion)
                .thenReturn(con);

        sesion = mockStatic(SesionUsuario.class);
        sesion.when(SesionUsuario::estaActiva).thenReturn(true);

        sesion.when(SesionUsuario::actual)
                .thenReturn(new SesionUsuario.DatosUsuario(
                        9,
                        "Usuario de pruebas",
                        "pruebas@carestock.test",
                        "ADMINISTRADOR",
                        7,
                        "Farmacia Central"
                ));

        cache = mockStatic(InventarioConsultas.class);

        cache.when(InventarioConsultas::compartida)
                .thenReturn(consulta);
    }

    @AfterEach
    void liberar() {
        cache.close();
        sesion.close();
        bd.close();
    }

    @Test
    void rechazaUsuarioNoAutenticado() {
        Usuario invalido = new Usuario();

        assertAll(
            () -> assertThrows(
                    IllegalArgumentException.class,
                    () -> new Inventario(null)),
            () -> assertThrows(
                    IllegalArgumentException.class,
                    () -> new Inventario(invalido))
        );
    }

    @Test
    void rechazaCambioDeSesion() {
        sesion.when(SesionUsuario::actual)
                .thenReturn(new SesionUsuario.DatosUsuario(
                        20, "Otro usuario", "otro@test.com",
                        "ADMINISTRADOR", 7, "Farmacia Central"
                ));

        Inventario fachada = inventario();

        assertThrows(
                ReglaNegocioException.class,
                fachada::listarAseo
        );

        bd.verify(ConexionBD::getConexion, never());
    }

    @Test
    void rechazaUsuarioSinFarmaciaActiva()
            throws Exception {

        when(rsSesion.next()).thenReturn(false);

        Inventario fachada = inventario();

        assertThrows(
                ReglaNegocioException.class,
                fachada::listarMaternidad
        );

        verify(psSesion).setInt(1, 9);
    }

    @Test
    void buscaAseoDeFarmaciaActiva() throws Exception {
        try (MockedConstruction<AseoDAO> creados =
                     mockConstruction(AseoDAO.class)) {

            Inventario fachada = inventario();
            Aseo producto = new Aseo();

            RegistroCatalogo<Aseo> registro =
                    new RegistroCatalogo<>(
                            40, producto, "ACTIVO");

            AseoDAO dao = creados.constructed().get(0);

            when(dao.buscarPorId(40, 7))
                    .thenReturn(registro);

            var resultado = fachada.buscarAseo(40);

            assertSame(registro, resultado);
            verify(dao).buscarPorId(40, 7);
        }
    }

    @Test
    void registraAseoEInvalidaCache()
            throws Exception {

        try (MockedConstruction<AseoDAO> creados =
                     mockConstruction(AseoDAO.class)) {

            Inventario fachada = inventario();
            Aseo producto = new Aseo();

            AseoDAO dao = creados.constructed().get(0);

            when(dao.guardar(producto, 7, 9))
                    .thenReturn(51);

            int id = fachada.registrarAseo(producto);

            assertEquals(51, id);

            verify(dao).guardar(producto, 7, 9);

            cache.verify(() ->
                    InventarioConsultas.invalidarFarmacia(7));
        }
    }

    @Test
    void noInvalidaCacheSiRegistroFalla()
            throws Exception {

        try (MockedConstruction<AseoDAO> creados =
                     mockConstruction(AseoDAO.class)) {

            Inventario fachada = inventario();
            Aseo producto = new Aseo();

            AseoDAO dao = creados.constructed().get(0);

            when(dao.guardar(producto, 7, 9))
                    .thenThrow(new SQLException("Fallo JDBC"));

            assertThrows(
                    SQLException.class,
                    () -> fachada.registrarAseo(producto)
            );

            cache.verify(
                    () -> InventarioConsultas.invalidarFarmacia(7),
                    never()
            );
        }
    }

    @Test
    void impideDesactivarAseoConExistencias()
            throws Exception {

        try (MockedConstruction<AseoDAO> creados =
                     mockConstruction(AseoDAO.class)) {

            Inventario fachada = inventario();

            Aseo producto = new Aseo();
            producto.setStock(8);

            AseoDAO dao = creados.constructed().get(0);

            when(dao.buscarPorId(40, 7))
                    .thenReturn(new RegistroCatalogo<>(
                            40, producto, "ACTIVO"));

            assertThrows(
                    ReglaNegocioException.class,
                    () -> fachada.desactivarAseo(40)
            );

            verify(dao, never()).desactivar(
                    anyInt(), anyInt(), anyInt());

            cache.verify(
                    () -> InventarioConsultas.invalidarFarmacia(7),
                    never()
            );
        }
    }

    @Test
    void impideDesactivarMaternidadConExistencias()
            throws Exception {

        try (MockedConstruction<MaternidadDAO> creados =
                     mockConstruction(MaternidadDAO.class)) {

            Inventario fachada = inventario();

            Maternidad producto = new Maternidad();
            producto.setStock(5);

            MaternidadDAO dao =
                    creados.constructed().get(0);

            when(dao.buscarPorId(55, 7))
                    .thenReturn(new RegistroCatalogo<>(
                            55, producto, "ACTIVO"));

            assertThrows(
                    ReglaNegocioException.class,
                    () -> fachada.desactivarMaternidad(55)
            );

            verify(dao, never()).desactivar(
                    anyInt(), anyInt(), anyInt());
        }
    }

    @Test
    void listaMedicamentosConSuEstado()
            throws Exception {

        try (MockedConstruction<MedicamentoCatalogoDAO> creados =
                     mockConstruction(
                             MedicamentoCatalogoDAO.class)) {

            Inventario fachada = inventario();

            Medicamento m = new Medicamento();
            m.setIdMedicamento(15);
            m.setNombreComercial("Acetaminofen");
            m.setEstado("ACTIVO");

            MedicamentoCatalogoDAO dao =
                    creados.constructed().get(0);

            when(dao.listarPorFarmacia(7))
                    .thenReturn(List.of(m));

            var lista = fachada.listarMedicamentos();

            assertEquals(1, lista.size());
            assertEquals(15, lista.get(0).id());
            assertSame(m, lista.get(0).producto());
            assertEquals("ACTIVO", lista.get(0).estado());

            verify(dao).listarPorFarmacia(7);
        }
    }

    @Test
    void rechazaMedicamentoSinCategoria()
            throws Exception {

        Inventario fachada = inventario();

        Medicamento m = new Medicamento();
        m.setNombreComercial("Acetaminofen");
        m.setCodigoInvima("INV-100");
        m.setPrincipioActivo("Paracetamol");

        assertThrows(
                ReglaNegocioException.class,
                () -> fachada.registrarMedicamento(m)
        );

        cache.verify(
                () -> InventarioConsultas.invalidarFarmacia(7),
                never()
        );
    }

    @Test
    void cuentaProductosPorCategoria()
            throws Exception {

        when(consulta.listarPorFarmacia(7))
                .thenReturn(List.of(
                        new ProductoInventario(
                                1, "Medicamento", "M1",
                                "Producto 1", "Caja", 8, "ACTIVO"),
                        new ProductoInventario(
                                2, "Medicamento", "M2",
                                "Producto 2", "Caja", 0, "ACTIVO"),
                        new ProductoInventario(
                                3, "Aseo", "A1",
                                "Jabon", "Unidad", 5, "ACTIVO")
                ));

        Map<String, Integer> resultado =
                inventario().contarPorCategoria();

        assertEquals(2, resultado.get("Medicamento"));
        assertEquals(1, resultado.get("Aseo"));
        assertEquals(0, resultado.get("Maternidad"));

        verify(consulta).listarPorFarmacia(7);
    }

    @Test
    void consultaInventarioSinInvalidarCache()
            throws Exception {

        var datos = List.of(new ProductoInventario(
                1, "Aseo", "A1", "Jabon",
                "Unidad", 4, "ACTIVO"
        ));

        when(consulta.listarPorFarmacia(7))
                .thenReturn(datos);

        assertSame(
                datos,
                inventario().listarInventarioUnificado()
        );

        cache.verify(
                () -> InventarioConsultas.invalidarFarmacia(7),
                never()
        );
    }

    @Test
    void refrescaInventarioInvalidandoCache()
            throws Exception {

        when(consulta.listarPorFarmacia(7))
                .thenReturn(List.of());

        var resultado = inventario()
                .actualizarInventarioUnificado();

        assertTrue(resultado.isEmpty());

        cache.verify(() ->
                InventarioConsultas.invalidarFarmacia(7));

        verify(consulta).listarPorFarmacia(7);
    }

    private org.example.carestock.dto.AlertaInventario crearAlerta(
            org.example.carestock.dto.AlertaInventario.TipoAlerta tipo,
            int id) {

        var alerta = mock(
                org.example.carestock.dto.AlertaInventario.class);

        when(alerta.categoria()).thenReturn(
                org.example.carestock.dto.AlertaInventario
                        .Categoria.MEDICAMENTO);
        when(alerta.tipoAlerta()).thenReturn(tipo);
        when(alerta.idReferencia()).thenReturn(id);

        return alerta;
    }

    @Test
    void consultaMedicamentosConStockBajo() throws Exception {

        var tipo = org.example.carestock.dto.AlertaInventario
                .TipoAlerta.STOCK_MINIMO;

        var alertas = List.of(
                crearAlerta(tipo, 15),
                crearAlerta(tipo, 20)
        );

        try (MockedConstruction<ServicioAlertasInventario> mocks =
                     mockConstruction(
                             ServicioAlertasInventario.class,
                             (mock, context) ->
                                     when(mock.listar(7))
                                             .thenReturn(alertas))) {

            Set<Integer> ids = inventario()
                    .obtenerIdsMedicamentosStockBajo();

            assertEquals(Set.of(15, 20), ids);

            assertFalse(mocks.constructed().isEmpty());
            assertTrue(mocks.constructed().stream().anyMatch(servicio ->
                    mockingDetails(servicio).getInvocations().stream()
                            .anyMatch(invocacion ->
                                    invocacion.getMethod().getName().equals("listar")
                                    && invocacion.getArguments().length == 1
                                    && Integer.valueOf(7).equals(invocacion.getArgument(0))
                            )
            ));
        }
    }

    @Test
    void obtieneResumenDeAlertasPorFarmacia() throws Exception {

        var stock = org.example.carestock.dto.AlertaInventario
                .TipoAlerta.STOCK_MINIMO;

        var vencimiento = java.util.Arrays.stream(
                org.example.carestock.dto.AlertaInventario
                        .TipoAlerta.values())
                .filter(tipo -> tipo.name().contains("VENC"))
                .findFirst()
                .orElseThrow();

        var alertas = List.of(
                crearAlerta(stock, 15),
                crearAlerta(stock, 20),
                crearAlerta(stock, 25),
                crearAlerta(vencimiento, 30),
                crearAlerta(vencimiento, 31)
        );

        try (MockedConstruction<ServicioAlertasInventario> mocks =
                     mockConstruction(
                             ServicioAlertasInventario.class,
                             (mock, context) ->
                                     when(mock.listar(7))
                                             .thenReturn(alertas))) {

            Map<String, Integer> resumen =
                    inventario().obtenerResumenAlertas();

            assertEquals(3, resumen.get("stockBajo"));
            assertEquals(2, resumen.get("proximosVencer"));

            assertFalse(mocks.constructed().isEmpty());
            assertTrue(mocks.constructed().stream().anyMatch(servicio ->
                    mockingDetails(servicio).getInvocations().stream()
                            .anyMatch(invocacion ->
                                    invocacion.getMethod().getName().equals("listar")
                                    && invocacion.getArguments().length == 1
                                    && Integer.valueOf(7).equals(invocacion.getArgument(0))
                            )
            ));
        }
    }

    @Test
    void obtieneListaDeAlertasDeStockYVencimiento()
            throws Exception {

        PreparedStatement psStock =
                mock(PreparedStatement.class);

        PreparedStatement psVenc =
                mock(PreparedStatement.class);

        ResultSet rsStock = mock(ResultSet.class);
        ResultSet rsVenc = mock(ResultSet.class);

        when(con.prepareStatement(
                contains("SELECT nombre_comercial")))
                .thenReturn(psStock);

        when(con.prepareStatement(
                contains("SELECT m.nombre_comercial, l.fecha_vencimiento")))
                .thenReturn(psVenc);

        when(psStock.executeQuery()).thenReturn(rsStock);
        when(psVenc.executeQuery()).thenReturn(rsVenc);

        when(rsStock.next()).thenReturn(true, false);
        when(rsVenc.next()).thenReturn(true, false);

        when(rsStock.getString("nombre_comercial"))
                .thenReturn("Acetaminofen");

        when(rsVenc.getString("nombre_comercial"))
                .thenReturn("Ibuprofeno");

        when(rsVenc.getDate("fecha_vencimiento"))
                .thenReturn(Date.valueOf(
                        LocalDate.of(2026, 11, 10)));

        List<String> alertas =
                inventario().obtenerListaAlertas();

        assertEquals(2, alertas.size());
        assertTrue(alertas.get(0).contains("Acetaminofen"));
        assertTrue(alertas.get(1).contains("Ibuprofeno"));

        verify(psStock).setInt(1, 7);
        verify(psVenc).setInt(1, 7);
    }
}
