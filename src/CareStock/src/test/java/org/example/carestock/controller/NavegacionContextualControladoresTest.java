package org.example.carestock.controller;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.TableView;

import org.example.carestock.CareStockApp;
import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.facade.Inventario;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NavegacionContextualControladoresTest {

    private static final int FARMACIA = 8;

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

    private static <T> T enFx(Callable<T> accion) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return accion.call();
        }
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static SesionUsuario.DatosUsuario sesion(int farmacia) {
        return new SesionUsuario.DatosUsuario(
                7, "Usuario QA", "qa@local",
                "FARMACEUTICO", farmacia, "Farmacia QA");
    }

    private static <T> T conSesion(
            SesionUsuario.DatosUsuario datos,
            Callable<T> accion) throws Exception {

        return enFx(() -> {
            Field campo = SesionUsuario.class
                    .getDeclaredField("actual");
            campo.setAccessible(true);
            Object anterior = campo.get(null);

            try {
                campo.set(null, datos);
                return accion.call();
            } finally {
                campo.set(null, anterior);
            }
        });
    }

    private static void poner(
            Object objeto, String nombre, Object valor)
            throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T obtener(Object objeto, String nombre)
            throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(Object objeto, String metodo)
            throws Exception {

        Method accion = objeto.getClass().getDeclaredMethod(metodo);
        accion.setAccessible(true);
        accion.invoke(objeto);
    }

    private static Destino destino(
            Categoria categoria, String codigo, int farmacia) {

        return new Destino(
                TipoAlerta.STOCK_MINIMO,
                categoria, 17, codigo,
                "Producto 17", null, farmacia);
    }

    private static ProductoInventario producto(
            Categoria categoria, int id) {

        return new ProductoInventario(
                id, categoria.etiqueta(), "COD-17",
                "Producto 17", "Unidad", 2, "ACTIVO");
    }

    private static ConsultaInventarioController consulta(
            SesionUsuario.DatosUsuario datos) throws Exception {

        ConsultaInventarioController c =
                new ConsultaInventarioController();

        poner(c, "sesionVista", datos);
        poner(c, "cmbCategoria", new ComboBox<String>());
        poner(c, "txtBuscar", new TextField());
        poner(c, "tablaInventario",
                new TableView<ProductoInventario>());
        poner(c, "lblEstado", new Label());
        poner(c, "lblTotal", new Label());

        ejecutar(c, "configurarTabla");
        ejecutar(c, "configurarFiltros");

        poner(c, "consultaExitosa", true);
        return c;
    }

    private static AlertaInventario alertaStock(Categoria categoria) {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, categoria,
                17, "COD-17", "Producto 17", null,
                2, 5, null, null, Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario alertaVencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO,
                44, "MED-44", "Medicamento 44", "LOT-44",
                3, null, LocalDate.now().plusDays(5), 5,
                Severidad.CRITICA, null, null, null);
    }

    private static ControladorAlertas alertas(
            SesionUsuario.DatosUsuario datos) throws Exception {

        ControladorAlertas c = new ControladorAlertas();
        poner(c, "sesionVista", datos);
        poner(c, "tablaAlertas", new TableView<AlertaInventario>());
        poner(c, "lblEstado", new Label());
        poner(c, "btnVerDetalle", new Button());
        return c;
    }

    // =====================================
    // CONSULTA UNIFICADA DE INVENTARIO
    // =====================================

    @ParameterizedTest
    @EnumSource(Categoria.class)
    void seleccionaProductoEnSuCategoria(Categoria categoria)
            throws Exception {

        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = consulta(datos);
            var producto = producto(categoria, 17);

            ObservableList<ProductoInventario> lista =
                    obtener(c, "productos");
            lista.add(producto);

            c.abrirDesdeAlerta(
                    destino(categoria, "COD-17", FARMACIA));

            TableView<ProductoInventario> tabla =
                    obtener(c, "tablaInventario");
            ComboBox<String> combo =
                    obtener(c, "cmbCategoria");
            TextField busqueda = obtener(c, "txtBuscar");
            Label estado = obtener(c, "lblEstado");

            assertEquals(producto,
                    tabla.getSelectionModel().getSelectedItem());
            assertEquals(categoria.etiqueta(), combo.getValue());
            assertEquals("COD-17", busqueda.getText());
            assertTrue(estado.getText().contains(
                    "Producto seleccionado"));

            return null;
        });
    }

    @Test
    void informaProductoQueYaNoExiste() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = consulta(datos);

            c.abrirDesdeAlerta(
                    destino(Categoria.ASEO, "COD-17", FARMACIA));

            TableView<?> tabla = obtener(c, "tablaInventario");
            Label estado = obtener(c, "lblEstado");

            assertNull(tabla.getSelectionModel().getSelectedItem());
            assertTrue(estado.getText().contains(
                    "ya no existe"));

            return null;
        });
    }

    @Test
    void buscaPorNombreCuandoFaltaElCodigo() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = consulta(datos);
            var producto = producto(Categoria.ASEO, 17);

            ObservableList<ProductoInventario> lista =
                    obtener(c, "productos");
            lista.add(producto);

            c.abrirDesdeAlerta(
                    destino(Categoria.ASEO, null, FARMACIA));

            TextField busqueda = obtener(c, "txtBuscar");
            TableView<ProductoInventario> tabla =
                    obtener(c, "tablaInventario");

            assertEquals("Producto 17", busqueda.getText());
            assertEquals(producto,
                    tabla.getSelectionModel().getSelectedItem());

            return null;
        });
    }

    @Test
    void conservaDestinoHastaFinalizarConsulta() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = consulta(datos);
            var producto = producto(Categoria.MATERNIDAD, 17);

            ObservableList<ProductoInventario> lista =
                    obtener(c, "productos");
            lista.add(producto);

            poner(c, "consultaExitosa", false);

            c.abrirDesdeAlerta(
                    destino(Categoria.MATERNIDAD, "COD-17", FARMACIA));

            TableView<ProductoInventario> tabla =
                    obtener(c, "tablaInventario");
            Label estado = obtener(c, "lblEstado");

            assertNull(tabla.getSelectionModel().getSelectedItem());
            assertTrue(estado.getText().contains("Localizando"));

            poner(c, "consultaExitosa", true);
            ejecutar(c, "seleccionarProductoDeAlerta");

            assertEquals(producto,
                    tabla.getSelectionModel().getSelectedItem());

            assertNull(obtener(c, "destinoAlerta"));

            return null;
        });
    }

    @Test
    void rechazaDestinoDeOtraFarmacia() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = consulta(datos);

            c.abrirDesdeAlerta(
                    destino(Categoria.ASEO, "COD-17", 99));

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains(
                    "farmacia activa ha cambiado"));

            return null;
        });
    }

    @Test
    void rechazaDestinoNulo() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = consulta(datos);

            c.abrirDesdeAlerta(null);

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains(
                    "No es posible abrir"));

            return null;
        });
    }

    @Test
    void rechazaSesionReemplazada() throws Exception {
        var original = sesion(FARMACIA);
        var reemplazo = sesion(FARMACIA);

        conSesion(reemplazo, () -> {
            var c = consulta(original);

            c.abrirDesdeAlerta(
                    destino(Categoria.ASEO, "COD-17", FARMACIA));

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains(
                    "farmacia activa ha cambiado"));

            return null;
        });
    }

    // =====================================
    // CONTROLADOR DE ALERTAS
    // =====================================

    @Test
    void alertaSinSeleccionMuestraMensaje() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = alertas(datos);

            ejecutar(c, "onVerDetalle");

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains(
                    "Seleccione una alerta"));

            return null;
        });
    }

    @ParameterizedTest
    @EnumSource(Categoria.class)
    void alertaStockEnviaReferenciaCorrecta(Categoria categoria)
            throws Exception {

        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = alertas(datos);
            var alerta = alertaStock(categoria);

            TableView<AlertaInventario> tabla =
                    obtener(c, "tablaAlertas");
            tabla.getItems().add(alerta);
            tabla.getSelectionModel().select(alerta);

            CareStockApp app = mock(CareStockApp.class);

            try (MockedStatic<CareStockApp> estatico =
                         mockStatic(CareStockApp.class)) {

                estatico.when(CareStockApp::getInstance)
                        .thenReturn(app);

                ejecutar(c, "onVerDetalle");

                verify(app).mostrarDetalleAlerta(
                        alerta, FARMACIA);
            }

            return null;
        });
    }

    @Test
    void alertaVencimientoEnviaElLoteCorrecto()
            throws Exception {

        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = alertas(datos);
            var alerta = alertaVencimiento();

            TableView<AlertaInventario> tabla =
                    obtener(c, "tablaAlertas");
            tabla.getItems().add(alerta);
            tabla.getSelectionModel().select(alerta);

            CareStockApp app = mock(CareStockApp.class);

            try (MockedStatic<CareStockApp> estatico =
                         mockStatic(CareStockApp.class)) {

                estatico.when(CareStockApp::getInstance)
                        .thenReturn(app);

                ejecutar(c, "onVerDetalle");

                verify(app).mostrarDetalleAlerta(
                        alerta, FARMACIA);
            }

            return null;
        });
    }

    @Test
    void alertaRechazaSesionCambiada() throws Exception {
        var original = sesion(FARMACIA);
        var reemplazo = sesion(FARMACIA);

        conSesion(reemplazo, () -> {
            var c = alertas(original);
            var alerta = alertaStock(Categoria.ASEO);

            TableView<AlertaInventario> tabla =
                    obtener(c, "tablaAlertas");
            tabla.getItems().add(alerta);
            tabla.getSelectionModel().select(alerta);

            ejecutar(c, "onVerDetalle");

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains(
                    "La sesión ha cambiado"));

            return null;
        });
    }

    @Test
    void errorNavegacionNoRompeLaPantalla() throws Exception {
        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = alertas(datos);
            var alerta = alertaStock(Categoria.ASEO);

            TableView<AlertaInventario> tabla =
                    obtener(c, "tablaAlertas");
            tabla.getItems().add(alerta);
            tabla.getSelectionModel().select(alerta);

            CareStockApp app = mock(CareStockApp.class);

            doThrow(new IllegalStateException(
                    "Referencia no disponible"))
                    .when(app).mostrarDetalleAlerta(
                            alerta, FARMACIA);

            try (MockedStatic<CareStockApp> estatico =
                         mockStatic(CareStockApp.class)) {

                estatico.when(CareStockApp::getInstance)
                        .thenReturn(app);

                ejecutar(c, "onVerDetalle");

                Label estado = obtener(c, "lblEstado");
                assertTrue(estado.getText().contains(
                        "Referencia no disponible"));
            }

            return null;
        });
    }

    // =====================================
    // INICIALIZACION DE ALERTAS
    // =====================================

    @Test
    void seleccionHabilitaYDeshabilitaBotonDetalle()
            throws Exception {

        var datos = sesion(FARMACIA);

        conSesion(datos, () -> {
            var c = new ControladorAlertas();

            for (String nombre : List.of(
                    "lblFarmacia", "lblCriticas",
                    "lblAdvertencias", "lblTotal",
                    "lblActualizado", "lblEstado")) {
                poner(c, nombre, new Label());
            }

            for (String nombre : List.of(
                    "cmbTipo", "cmbCategoria", "cmbSeveridad")) {
                poner(c, nombre, new ComboBox<String>());
            }

            poner(c, "btnActualizar", new Button());
            poner(c, "btnExportarCsv", new Button());
            poner(c, "btnVerDetalle", new Button());
            poner(c, "btnGestionar", new Button());
            poner(c, "tablaAlertas",
                    new TableView<AlertaInventario>());

            for (String nombre : List.of(
                    "colSeveridad", "colTipo",
                    "colCategoria", "colProducto",
                    "colReferencia", "colExistencias",
                    "colReposicion", "colDetalle",
                    "colUltimoMovimiento")) {
                poner(c, nombre, new TableColumn<>());
            }

            try (MockedConstruction<Inventario> fachadas =
                         mockConstruction(
                                 Inventario.class,
                                 (mock, contexto) ->
                                         when(mock.listarAlertas())
                                                 .thenReturn(List.of()))) {

                c.initialize();

                Timeline refresco = obtener(c, "refresco");

                try {
                    Button boton = obtener(c, "btnVerDetalle");
                    TableView<AlertaInventario> tabla =
                            obtener(c, "tablaAlertas");

                    assertTrue(boton.isDisable());

                    var alerta = alertaStock(Categoria.ASEO);
                    tabla.getItems().add(alerta);
                    tabla.getSelectionModel().select(alerta);

                    assertFalse(boton.isDisable());

                    tabla.getSelectionModel().clearSelection();
                    assertTrue(boton.isDisable());

                    assertNotNull(
                            tabla.getRowFactory().call(tabla)
                                    .getOnMouseClicked());
                } finally {
                    refresco.stop();
                }
            }

            return null;
        });
    }
}
