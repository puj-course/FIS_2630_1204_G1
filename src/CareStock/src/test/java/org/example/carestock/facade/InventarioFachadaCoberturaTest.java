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

        when(rsSesion.next()).thenReturn(true);
        when(rsSesion.getInt("id_farmacia"))
                .thenReturn(7);

        bd = mockStatic(ConexionBD.class);
        bd.when(ConexionBD::getConexion)
                .thenReturn(con);

        sesion = mockStatic(SesionUsuario.class);

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

    @Test
    void consultaMedicamentosConStockBajo()
            throws Exception {

        PreparedStatement psAlertas =
                mock(PreparedStatement.class);

        ResultSet rsAlertas = mock(ResultSet.class);

        when(con.prepareStatement(
                contains("SELECT id_medicamento")))
                .thenReturn(psAlertas);

        when(psAlertas.executeQuery())
                .thenReturn(rsAlertas);

        when(rsAlertas.next())
                .thenReturn(true, true, false);

        when(rsAlertas.getInt("id_medicamento"))
                .thenReturn(15, 20);

        Set<Integer> ids = inventario()
                .obtenerIdsMedicamentosStockBajo();

        assertEquals(Set.of(15, 20), ids);
        verify(psAlertas).setInt(1, 7);
    }

    @Test
    void obtieneResumenDeAlertasPorFarmacia()
            throws Exception {

        PreparedStatement psStock =
                mock(PreparedStatement.class);

        PreparedStatement psVencimiento =
                mock(PreparedStatement.class);

        ResultSet rsStock = mock(ResultSet.class);
        ResultSet rsVencimiento = mock(ResultSet.class);

        when(con.prepareStatement(
                contains("SELECT COUNT(*) FROM medicamentos")))
                .thenReturn(psStock);

        when(con.prepareStatement(
                contains("SELECT COUNT(*) FROM lotes l")))
                .thenReturn(psVencimiento);

        when(psStock.executeQuery()).thenReturn(rsStock);
        when(psVencimiento.executeQuery())
                .thenReturn(rsVencimiento);

        when(rsStock.next()).thenReturn(true);
        when(rsVencimiento.next()).thenReturn(true);

        when(rsStock.getInt(1)).thenReturn(3);
        when(rsVencimiento.getInt(1)).thenReturn(2);

        Map<String, Integer> resumen =
                inventario().obtenerResumenAlertas();

        assertEquals(3, resumen.get("stockBajo"));
        assertEquals(2, resumen.get("proximosVencer"));

        verify(psStock).setInt(1, 7);
        verify(psVencimiento).setInt(1, 7);
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
                contains("SELECT nombre_comercial FROM medicamentos")))
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
