package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Medicamento;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogoCoverageFocalizadoTest {

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        try { Platform.startup(listo::countDown); }
        catch (IllegalStateException iniciado) { listo.countDown(); }
        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Callable<T> tarea) throws Exception {
        if (Platform.isFxApplicationThread()) return tarea.call();
        FutureTask<T> pendiente = new FutureTask<>(tarea);
        Platform.runLater(pendiente);
        return pendiente.get(20, TimeUnit.SECONDS);
    }

    private static void campo(Object objeto, String nombre, Object valor)
            throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    private static void privado(Object objeto, String nombre)
            throws Exception {
        Method metodo = objeto.getClass().getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(objeto);
    }

    private static Medicamento med(int stock, int minimo) {
        Medicamento medicamento = new Medicamento();
        medicamento.setStockTotal(stock);
        medicamento.setStockMinimo(minimo);
        return medicamento;
    }

    private static RegistroCatalogo<Medicamento> reg(
            String estado, int stock, int minimo) {
        return new RegistroCatalogo<>(12, med(stock, minimo), estado);
    }

    @SuppressWarnings("unchecked")
    private static TableView<RegistroCatalogo<?>> tablaConfigurada(
            ControladorCatalogo controlador) throws Exception {
        TableView<RegistroCatalogo<?>> tabla = new TableView<>();
        campo(controlador, "tablaProductos", tabla);
        for (String nombre : new String[]{
                "colId", "colCodigo", "colNombre", "colDescripcion",
                "colPrecio", "colStock", "colEstado"}) {
            TableColumn<RegistroCatalogo<?>, String> columna =
                    new TableColumn<>(nombre);
            campo(controlador, nombre, columna);
            tabla.getColumns().add(columna);
        }
        privado(controlador, "configurarTabla");
        return tabla;
    }

    private static void comprobarFila(
            TableRow<RegistroCatalogo<?>> fila,
            RegistroCatalogo<?> producto, String esperado) throws Exception {
        Method pintar = fila.getClass().getDeclaredMethod(
                "updateItem", RegistroCatalogo.class, boolean.class);
        pintar.setAccessible(true);
        pintar.invoke(fila, producto, false);
        assertTrue(fila.getStyle().contains(esperado),
                "Estado=" + producto.estado() + ", estilo=" + fila.getStyle());
    }

    @Test
    void columnaStockMuestraExistenciasRealesDelModelo() throws Exception {
        fx(() -> {
            ControladorCatalogo controlador = new ControladorCatalogo();
            TableView<RegistroCatalogo<?>> tabla = tablaConfigurada(controlador);
            TableColumn<RegistroCatalogo<?>, String> columna =
                    (TableColumn<RegistroCatalogo<?>, String>) tabla.getColumns().get(5);
            assertEquals("0", columna.getCellObservableValue(reg("ACTIVO", 0, 10)).getValue());
            assertEquals("7", columna.getCellObservableValue(reg("ACTIVO", 7, 10)).getValue());
            assertEquals("25", columna.getCellObservableValue(reg("INACTIVO", 25, 10)).getValue());
            return null;
        });
    }

    @Test
    void filasDistinguenCeroBajoMinimoVerdeEHistorico() throws Exception {
        fx(() -> {
            ControladorCatalogo controlador = new ControladorCatalogo();
            TableView<RegistroCatalogo<?>> tabla = tablaConfigurada(controlador);
            TableRow<RegistroCatalogo<?>> fila = tabla.getRowFactory().call(tabla);
            comprobarFila(fila, reg("ACTIVO", 0, 10), "#F0A8A8");
            comprobarFila(fila, reg("ACTIVO", 4, 10), "#F6CE8E");
            comprobarFila(fila, reg("ACTIVO", 10, 10), "#D8F2DF");
            comprobarFila(fila, reg("ACTIVO", 25, 10), "#D8F2DF");
            comprobarFila(fila, reg("INACTIVO", 25, 10), "#ECEFF2");
            comprobarFila(fila, reg("INACTIVO", 0, 10), "#F0A8A8");
            comprobarFila(fila, reg("ACTIVO", 1, 0), "#D8F2DF");
            Method pintar = fila.getClass().getDeclaredMethod(
                    "updateItem", RegistroCatalogo.class, boolean.class);
            pintar.setAccessible(true);
            pintar.invoke(fila, null, true);
            assertEquals("", fila.getStyle());
            return null;
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void tooltipDeStockSeMuestraSoloConAlertaActiva() throws Exception {
        fx(() -> {
            ControladorCatalogo controlador = new ControladorCatalogo();
            TableView<RegistroCatalogo<?>> tabla = tablaConfigurada(controlador);
            TableColumn<RegistroCatalogo<?>, String> columna =
                    (TableColumn<RegistroCatalogo<?>, String>) tabla.getColumns().get(5);
            TableRow<RegistroCatalogo<?>> fila = tabla.getRowFactory().call(tabla);
            TableCell<RegistroCatalogo<?>, String> celda =
                    columna.getCellFactory().call(columna);
            celda.updateTableRow(fila);

            Method pintarFila = fila.getClass().getDeclaredMethod(
                    "updateItem", RegistroCatalogo.class, boolean.class);
            pintarFila.setAccessible(true);
            Method pintarCelda = celda.getClass().getDeclaredMethod(
                    "updateItem", String.class, boolean.class);
            pintarCelda.setAccessible(true);

            pintarFila.invoke(fila, reg("ACTIVO", 5, 10), false);
            pintarCelda.invoke(celda, "5", false);
            assertNotNull(celda.getTooltip());

            pintarFila.invoke(fila, reg("ACTIVO", 11, 10), false);
            pintarCelda.invoke(celda, "11", false);
            assertNull(celda.getTooltip());

            pintarFila.invoke(fila, reg("INACTIVO", 5, 10), false);
            pintarCelda.invoke(celda, "5", false);
            assertNull(celda.getTooltip());

            pintarFila.invoke(fila, null, true);
            pintarCelda.invoke(celda, "5", false);
            assertNull(celda.getTooltip());
            pintarCelda.invoke(celda, null, true);
            assertNull(celda.getTooltip());
            assertNull(celda.getText());
            return null;
        });
    }

    @Test
    void botonDesactivarRespetaStockEstadoYSeleccion() throws Exception {
        fx(() -> {
            ControladorCatalogo controlador = new ControladorCatalogo();
            privado(controlador, "actualizarDisponibilidadDesactivar"); // boton nulo
            Button boton = new Button();
            campo(controlador, "btnDesactivarFormulario", boton);
            privado(controlador, "actualizarDisponibilidadDesactivar");
            assertTrue(boton.isDisabled());

            campo(controlador, "seleccionado", reg("ACTIVO", 5, 10));
            privado(controlador, "actualizarDisponibilidadDesactivar");
            assertTrue(boton.isDisabled());

            campo(controlador, "seleccionado", reg("INACTIVO", 0, 10));
            privado(controlador, "actualizarDisponibilidadDesactivar");
            assertTrue(boton.isDisabled());

            campo(controlador, "seleccionado", reg("ACTIVO", 0, 10));
            privado(controlador, "actualizarDisponibilidadDesactivar");
            assertFalse(boton.isDisabled());
            assertNull(boton.getTooltip());
            return null;
        });
    }

    @Test
    void desactivacionRechazaProductoSinCumplirRegla() throws Exception {
        fx(() -> {
            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos = mockConstruction(Alert.class)) {
                sesion.when(SesionUsuario::esAdministrador).thenReturn(true);
                ControladorCatalogo controlador = new ControladorCatalogo();
                campo(controlador, "seleccionado", reg("ACTIVO", 5, 10));
                privado(controlador, "onDesactivar");
                campo(controlador, "seleccionado", reg("INACTIVO", 0, 10));
                privado(controlador, "onDesactivar");
                assertEquals(2, dialogos.constructed().size());
                for (Alert alerta : dialogos.constructed()) {
                    verify(alerta).showAndWait();
                }
            }
            return null;
        });
    }

    @Test
    void aperturaCatalogoRechazaScrollInvalido() throws Exception {
        fx(() -> {
            ControladorInventario dashboard = new ControladorInventario();
            campo(dashboard, "dashboardRoot", new BorderPane());
            campo(dashboard, "btnCatalogo", new Button());
            var datos = new SesionUsuario.DatosUsuario(
                    32, "Administrador QA", "qa@example.test",
                    "ADMINISTRADOR", 1, "Farmacia Principal");
            for (BorderPane panel : new BorderPane[]{
                    new BorderPane(),
                    new BorderPane(new Label("No es ScrollPane")),
                    new BorderPane(new ScrollPane())}) {
                try (var sesion = mockStatic(SesionUsuario.class);
                     MockedConstruction<FXMLLoader> cargas = mockConstruction(
                             FXMLLoader.class, (loader, contexto) -> {
                                 when(loader.load()).thenReturn(panel);
                                 when(loader.getController())
                                         .thenReturn(mock(ControladorCatalogo.class));
                             })) {
                    sesion.when(SesionUsuario::estaActiva).thenReturn(true);
                    sesion.when(SesionUsuario::actual).thenReturn(datos);
                    IllegalStateException error = assertThrows(
                            IllegalStateException.class,
                            () -> dashboard.abrirCatalogoIntegrado(null, null));
                    assertTrue(error.getMessage().contains("ScrollPane"));
                }
            }
            return null;
        });
    }
}
