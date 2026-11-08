package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.example.carestock.CareStockApp;
import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaInventarioCoverage87Test {

    private static final SesionUsuario.DatosUsuario ADMIN =
        new SesionUsuario.DatosUsuario(
            7, "QA", "qa@test.local",
            "ADMINISTRADOR", 8, "Farmacia QA");

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        }
        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void poner(Object obj, String campo, Object valor)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(obj, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object obj, String campo)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    private static Object invocar(
            Object obj, String nombre, Class<?>[] tipos,
            Object... valores) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(nombre, tipos);
        m.setAccessible(true);
        return m.invoke(obj, valores);
    }

    private static Object invocar(Object obj, String nombre)
            throws Exception {
        return invocar(obj, nombre, new Class<?>[0]);
    }

    private static ProductoInventario producto(
            int id, String categoria, int stock, String estado) {
        return new ProductoInventario(
            id, categoria, "COD-" + id,
            "Producto " + id, "Unidad", stock, estado);
    }

    private static ConsultaInventarioController vista()
            throws Exception {
        var c = new ConsultaInventarioController();

        poner(c, "cmbCategoria", new ComboBox<String>());
        poner(c, "txtBuscar", new TextField());
        poner(c, "btnActualizar", new Button());
        poner(c, "btnRegistrar", new Button());
        poner(c, "btnEditar", new Button());
        poner(c, "lblFarmacia", new Label());
        poner(c, "lblEstado", new Label());
        poner(c, "lblTotal", new Label());
        poner(c, "sesionVista", ADMIN);

        TableView<ProductoInventario> tabla = new TableView<>();
        poner(c, "tablaInventario", tabla);

        for (String campo : new String[]{
                "colCodigo", "colNombre", "colCategoria",
                "colPresentacion", "colExistencia", "colEstado"}) {
            TableColumn<ProductoInventario, String> columna =
                new TableColumn<>(campo);
            poner(c, campo, columna);
            tabla.getColumns().add(columna);
        }

        invocar(c, "configurarColumnas");
        invocar(c, "configurarTabla");
        invocar(c, "configurarFiltros");

        return c;
    }

    private static void consultar(
            ConsultaInventarioController c,
            ProductoInventario... productos) throws Exception {
        ObservableList<ProductoInventario> lista = leer(c, "productos");
        lista.setAll(productos);
        poner(c, "consultaExitosa", true);
        invocar(c, "aplicarFiltros");
    }

    private static String estado(ConsultaInventarioController c)
            throws Exception {
        Label label = leer(c, "lblEstado");
        return label.getText();
    }

    private static Destino alerta(int id, int farmacia) {
        return new Destino(
            TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
            id, "COD-" + id, "Producto " + id, null, farmacia);
    }

    @Test
    void convierteCeldasNulasYNumericas() throws Exception {
        fx(() -> {
            var c = vista();
            ReadOnlyStringWrapper vacia = (ReadOnlyStringWrapper)
                invocar(c, "celda", new Class<?>[]{Object.class},
                    (Object) null);
            ReadOnlyStringWrapper numero = (ReadOnlyStringWrapper)
                invocar(c, "celda", new Class<?>[]{Object.class}, 42);

            assertEquals("", vacia.get());
            assertEquals("42", numero.get());
            return null;
        });
    }

    @Test
    void muestraColumnasYEstadoDeMedicamentoSinStock()
            throws Exception {
        fx(() -> {
            var c = vista();
            ProductoInventario p = producto(
                1, "Medicamento", 0, "ACTIVO");

            TableColumn<ProductoInventario, String> codigo =
                leer(c, "colCodigo");
            TableColumn<ProductoInventario, String> nombre =
                leer(c, "colNombre");
            TableColumn<ProductoInventario, String> categoria =
                leer(c, "colCategoria");
            TableColumn<ProductoInventario, String> presentacion =
                leer(c, "colPresentacion");
            TableColumn<ProductoInventario, String> existencia =
                leer(c, "colExistencia");
            TableColumn<ProductoInventario, String> estado =
                leer(c, "colEstado");

            assertAll(
                () -> assertEquals("COD-1",
                    codigo.getCellObservableValue(p).getValue()),
                () -> assertEquals("Producto 1",
                    nombre.getCellObservableValue(p).getValue()),
                () -> assertEquals("Medicamento",
                    categoria.getCellObservableValue(p).getValue()),
                () -> assertEquals("Unidad",
                    presentacion.getCellObservableValue(p).getValue()),
                () -> assertEquals("0",
                    existencia.getCellObservableValue(p).getValue()),
                () -> assertEquals("DESHABILITADO",
                    estado.getCellObservableValue(p).getValue())
            );
            return null;
        });
    }

    @Test
    void respetaEstadoActivoDeAseoYMedicamentoConStock()
            throws Exception {
        fx(() -> {
            var c = vista();
            TableColumn<ProductoInventario, String> col =
                leer(c, "colEstado");

            assertEquals("ACTIVO",
                col.getCellObservableValue(
                    producto(2, "Aseo", 0, "ACTIVO")).getValue());

            assertEquals("ACTIVO",
                col.getCellObservableValue(
                    producto(3, "Medicamento", 8, "ACTIVO"))
                    .getValue());

            assertEquals("INACTIVO",
                col.getCellObservableValue(
                    producto(4, "Medicamento", 0, "INACTIVO"))
                    .getValue());
            return null;
        });
    }

    @Test
    void muestraMensajeCuandoFarmaciaNoTieneProductos()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c);

            assertTrue(estado(c).contains("No hay productos"));
            Label total = leer(c, "lblTotal");
            assertEquals("Productos encontrados: 0", total.getText());
            return null;
        });
    }

    @Test
    void informaTotalDeProductosVisibles()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c,
                producto(1, "Medicamento", 5, "ACTIVO"),
                producto(2, "Aseo", 7, "ACTIVO"));

            assertTrue(estado(c).contains("Mostrando 2 de 2"));
            Label total = leer(c, "lblTotal");
            assertEquals("Productos encontrados: 2",
                total.getText());
            return null;
        });
    }

    @Test
    void filtraPorCategoriaYBusqueda() throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c,
                producto(1, "Medicamento", 5, "ACTIVO"),
                producto(2, "Aseo", 7, "ACTIVO"),
                producto(3, "Aseo", 4, "ACTIVO"));

            ComboBox<String> categoria = leer(c, "cmbCategoria");
            TextField buscar = leer(c, "txtBuscar");
            TableView<ProductoInventario> tabla =
                leer(c, "tablaInventario");

            categoria.setValue("Aseo");
            assertEquals(2, tabla.getItems().size());

            buscar.setText("COD-2");
            assertEquals(1, tabla.getItems().size());
            assertEquals(2, tabla.getItems().get(0).id());

            buscar.setText("NO-EXISTE");
            assertTrue(tabla.getItems().isEmpty());
            assertTrue(estado(c).contains("No hay coincidencias"));
            return null;
        });
    }

    @Test
    void noAplicaFiltrosAntesDeUnaConsultaExitosa()
            throws Exception {
        fx(() -> {
            var c = vista();
            TextField buscar = leer(c, "txtBuscar");
            buscar.setText("inexistente");

            invocar(c, "aplicarFiltros");

            assertEquals("", estado(c));
            return null;
        });
    }

    @Test
    void errorDeConsultaLimpiaDatosYEstado()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c, producto(2, "Aseo", 5, "ACTIVO"));

            invocar(c, "mostrarErrorConsulta");

            TableView<ProductoInventario> tabla =
                leer(c, "tablaInventario");
            Label total = leer(c, "lblTotal");

            assertTrue(tabla.getItems().isEmpty());
            assertEquals("Productos encontrados: --",
                total.getText());
            assertTrue(estado(c).contains("No se pudo cargar"));
            assertFalse((Boolean) leer(c, "consultaExitosa"));
            return null;
        });
    }

    @Test
    void validaSesionPorIdentidadNoSoloPorUsuario()
            throws Exception {
        fx(() -> {
            var c = vista();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);

                assertTrue((Boolean) invocar(c, "mismaSesionYVista"));

                var otraSesion = new SesionUsuario.DatosUsuario(
                    7, "QA", "qa@test.local",
                    "ADMINISTRADOR", 8, "Farmacia QA");

                sesion.when(SesionUsuario::actual)
                    .thenReturn(otraSesion);

                assertFalse((Boolean) invocar(c, "mismaSesionYVista"));

                poner(c, "vistaActiva", false);
                assertFalse((Boolean) invocar(c, "mismaSesionYVista"));
            }
            return null;
        });
    }

    @Test
    void sesionCerradaInvalidaLaVista() throws Exception {
        fx(() -> {
            var c = vista();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenThrow(new IllegalStateException("Sin sesion"));

                assertFalse((Boolean) invocar(c, "mismaSesionYVista"));
            }
            return null;
        });
    }

    @Test
    void rechazaAlertaDeOtraFarmacia() throws Exception {
        fx(() -> {
            var c = vista();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);

                c.abrirDesdeAlerta(alerta(2, 99));
                assertTrue(estado(c).contains("farmacia activa"));

                c.abrirDesdeAlerta(null);
                assertTrue(estado(c).contains("farmacia activa"));
            }
            return null;
        });
    }

    @Test
    void seleccionaProductoDesdeAlertaValida() throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c,
                producto(1, "Medicamento", 5, "ACTIVO"),
                producto(2, "Aseo", 7, "ACTIVO"));

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);

                c.abrirDesdeAlerta(alerta(2, 8));

                TableView<ProductoInventario> tabla =
                    leer(c, "tablaInventario");
                TextField buscar = leer(c, "txtBuscar");

                assertEquals(2,
                    tabla.getSelectionModel().getSelectedItem().id());
                assertEquals("COD-2", buscar.getText());
                assertTrue(estado(c).contains("Producto seleccionado"));
            }
            return null;
        });
    }

    @Test
    void alertaSinProductoCoincidenteNoSeleccionaNada()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c, producto(2, "Aseo", 5, "ACTIVO"));

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);

                c.abrirDesdeAlerta(alerta(999, 8));

                assertTrue(estado(c).contains("ya no existe"));
                TableView<ProductoInventario> tabla =
                    leer(c, "tablaInventario");
                assertNull(tabla.getSelectionModel().getSelectedItem());
            }
            return null;
        });
    }

    @Test
    void permisosDeAccionDependenDeSesionYSeleccion()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c, producto(2, "Aseo", 5, "ACTIVO"));

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);

                invocar(c, "configurarAcciones");

                Button registrar = leer(c, "btnRegistrar");
                Button editar = leer(c, "btnEditar");
                TableView<ProductoInventario> tabla =
                    leer(c, "tablaInventario");

                assertFalse(registrar.isDisabled());
                assertTrue(editar.isDisabled());

                tabla.getSelectionModel().selectFirst();
                assertFalse(editar.isDisabled());

                poner(c, "cargando", true);
                invocar(c, "actualizarAcciones");
                assertTrue(registrar.isDisabled());
                assertTrue(editar.isDisabled());

                poner(c, "cargando", false);
                poner(c, "vistaActiva", false);
                invocar(c, "actualizarAcciones");
                assertTrue(registrar.isDisabled());
            }
            return null;
        });
    }

    @Test
    void auxiliarNoPuedeRegistrarNiEditar() throws Exception {
        fx(() -> {
            var c = vista();
            var auxiliar = new SesionUsuario.DatosUsuario(
                7, "Auxiliar", "aux@test.local",
                "AUXILIAR", 8, "Farmacia QA");
            poner(c, "sesionVista", auxiliar);

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(auxiliar);

                invocar(c, "configurarAcciones");

                Button registrar = leer(c, "btnRegistrar");
                Button editar = leer(c, "btnEditar");

                assertFalse(registrar.isVisible());
                assertFalse(editar.isVisible());
                assertTrue(registrar.isDisabled());
                assertTrue(editar.isDisabled());
            }
            return null;
        });
    }

    @Test
    void registrarSinCategoriaUsaMedicamento()
            throws Exception {
        fx(() -> {
            var c = vista();
            CareStockApp app = mock(CareStockApp.class);

            try (var sesion = mockStatic(SesionUsuario.class);
                 var aplicacion = mockStatic(CareStockApp.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                invocar(c, "onRegistrar");

                verify(app).mostrarCatalogoParaRegistro("Medicamento");
                assertFalse((Boolean) leer(c, "vistaActiva"));
            }
            return null;
        });
    }

    @Test
    void registrarProductoSeleccionadoConservaCategoria()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c, producto(2, "Aseo", 5, "ACTIVO"));

            TableView<ProductoInventario> tabla =
                leer(c, "tablaInventario");
            tabla.getSelectionModel().selectFirst();

            CareStockApp app = mock(CareStockApp.class);

            try (var sesion = mockStatic(SesionUsuario.class);
                 var aplicacion = mockStatic(CareStockApp.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                invocar(c, "onRegistrar");

                verify(app).mostrarCatalogoParaRegistro("Aseo");
            }
            return null;
        });
    }

    @Test
    void editarNecesitaProductoSeleccionado()
            throws Exception {
        fx(() -> {
            var c = vista();
            CareStockApp app = mock(CareStockApp.class);

            try (var sesion = mockStatic(SesionUsuario.class);
                 var aplicacion = mockStatic(CareStockApp.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                invocar(c, "onEditar");

                verifyNoInteractions(app);
                assertTrue((Boolean) leer(c, "vistaActiva"));
            }
            return null;
        });
    }

    @Test
    void editarSeleccionadoEnviaIdentificadorCorrecto()
            throws Exception {
        fx(() -> {
            var c = vista();
            consultar(c, producto(31, "Aseo", 5, "ACTIVO"));

            TableView<ProductoInventario> tabla =
                leer(c, "tablaInventario");
            tabla.getSelectionModel().selectFirst();

            CareStockApp app = mock(CareStockApp.class);

            try (var sesion = mockStatic(SesionUsuario.class);
                 var aplicacion = mockStatic(CareStockApp.class)) {
                sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                invocar(c, "onEditar");

                verify(app).mostrarCatalogoParaEditar("Aseo", 31);
                assertFalse((Boolean) leer(c, "vistaActiva"));
            }
            return null;
        });
    }

    @Test
    void cerrarSesionDesactivaVista() throws Exception {
        fx(() -> {
            var c = vista();
            CareStockApp app = mock(CareStockApp.class);

            try (var aplicacion = mockStatic(CareStockApp.class)) {
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                invocar(c, "onCerrarSesion");

                verify(app).cerrarSesion();
                assertFalse((Boolean) leer(c, "vistaActiva"));
            }
            return null;
        });
    }

    @Test
    void volverAlCatalogoDesactivaVista()
            throws Exception {
        fx(() -> {
            var c = vista();
            CareStockApp app = mock(CareStockApp.class);

            try (var aplicacion = mockStatic(CareStockApp.class)) {
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                invocar(c, "onVolver");

                verify(app).mostrarCatalogo();
                assertFalse((Boolean) leer(c, "vistaActiva"));
            }
            return null;
        });
    }
}
