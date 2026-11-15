package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.example.carestock.facade.Inventario;
import org.example.carestock.model.Lote;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogoRutasCoverageTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    1, "Administrador QA", "qa@test.local",
                    "ADMINISTRADOR", 7, "Farmacia QA");

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);

        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException iniciado) {
            listo.countDown();
        }

        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Callable<T> accion)
            throws Exception {

        if (Platform.isFxApplicationThread()) {
            return accion.call();
        }

        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);

        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void campo(
            Object objeto, String nombre, Object valor)
            throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T obtener(
            Object objeto, String nombre) throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void privado(
            Object objeto, String nombre) throws Exception {

        Method metodo = objeto.getClass()
                .getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(objeto);
    }

    private static ControladorInventario preparar()
            throws Exception {

        var dashboard = new ControladorInventario();

        campo(dashboard, "dashboardRoot", new BorderPane());
        campo(dashboard, "vistaLotes", new ScrollPane());
        campo(dashboard, "btnCatalogo", new Button("Catálogo"));

        return dashboard;
    }

    @Test
    void primeraAperturaCargaCatalogoYReutilizaPanel()
            throws Exception {

        fx(() -> {
            var dashboard = preparar();
            var catalogo = mock(ControladorCatalogo.class);
            var panel = new BorderPane();
            var scrollCatalogo = new ScrollPane(new StackPane());
            panel.setCenter(scrollCatalogo);

            when(catalogo.categoriaActual())
                    .thenReturn("Aseo", "Maternidad");

            when(catalogo.registrosDeCategoria())
                    .thenReturn(List.of());

            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<FXMLLoader> cargadores =
                         mockConstruction(
                                 FXMLLoader.class,
                                 (loader, contexto) -> {
                                     when(loader.load())
                                             .thenReturn(panel);
                                     when(loader.getController())
                                             .thenReturn(catalogo);
                                 })) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                dashboard.abrirCatalogoIntegrado(null, null);

                BorderPane raiz = obtener(
                        dashboard, "dashboardRoot");

                assertSame(panel, raiz.getCenter());
                assertNull(panel.getTop());
                assertSame(scrollCatalogo, panel.getCenter());

                VBox contenido = assertInstanceOf(
                        VBox.class, scrollCatalogo.getContent());

                assertEquals(2, contenido.getChildren().size());
                assertTrue(contenido.getChildren().get(0) instanceof VBox);
                assertTrue(contenido.getChildren().get(1) instanceof StackPane);

                verify(catalogo).alActualizarIndicadores(any());

                Label titulo = obtener(
                        dashboard, "tituloTotalCatalogo");

                assertEquals("TOTAL DE ASEO", titulo.getText());

                // Una segunda apertura reutiliza el mismo FXML.
                dashboard.abrirCatalogoIntegrado(
                        "Maternidad", 25);

                assertSame(panel, raiz.getCenter());
                assertEquals(1, cargadores.constructed().size());

                verify(catalogo).abrirDesdeConsulta(
                        "Maternidad", 25);

                assertEquals(
                        "TOTAL DE MATERNIDAD",
                        titulo.getText());
            }

            return null;
        });
    }

    @Test
    void rechazaVistaQueNoEsBorderPane()
            throws Exception {

        fx(() -> {
            var dashboard = preparar();

            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<FXMLLoader> cargadores =
                         mockConstruction(
                                 FXMLLoader.class,
                                 (loader, contexto) ->
                                         when(loader.load())
                                                 .thenReturn(new VBox()))) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                IllegalStateException error = assertThrows(
                        IllegalStateException.class,
                        () -> dashboard.abrirCatalogoIntegrado(
                                null, null));

                assertTrue(
                        error.getMessage().contains("BorderPane"));
            }

            return null;
        });
    }

    @Test
    void errorDeLecturaFXMLSeManejaSinAbrirCatalogo()
            throws Exception {

        fx(() -> {
            var dashboard = preparar();

            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<FXMLLoader> cargadores =
                         mockConstruction(
                                 FXMLLoader.class,
                                 (loader, contexto) ->
                                         when(loader.load()).thenThrow(
                                                 new IOException(
                                                         "FXML no disponible")))) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                IllegalStateException error = assertThrows(
                        IllegalStateException.class,
                        () -> dashboard.abrirCatalogoIntegrado(
                                null, null));

                assertInstanceOf(
                        IOException.class, error.getCause());
            }

            return null;
        });
    }

    @Test
    void indicadoresNoConsultanSiCatalogoNoEstaVisible() throws Exception {
        fx(() -> {
            var dashboard = preparar();
            privado(dashboard, "refrescarIndicadoresCatalogo");
            Integer versionInicial = obtener(dashboard, "versionIndicadores");
            assertEquals(0, versionInicial.intValue());

            BorderPane oculto = new BorderPane();
            campo(dashboard, "vistaCatalogo", oculto);
            privado(dashboard, "refrescarIndicadoresCatalogo");
            Integer versionFinal = obtener(dashboard, "versionIndicadores");
            BorderPane root = obtener(dashboard, "dashboardRoot");
            assertEquals(0, versionFinal.intValue());
            assertNotSame(oculto, root.getCenter());
            return null;
        });
    }

    @Test
    void volverALotesRestableceLaVistaYActualizaDatos()
            throws Exception {

        fx(() -> {
            var dashboard = preparar();
            var inventario = mock(Inventario.class);

            when(inventario.listarLotes())
                    .thenReturn(List.of());

            when(inventario.categoriaPorMedicamento())
                    .thenReturn(Map.of());

            when(inventario.contarPorCategoria())
                    .thenReturn(Map.of(
                            "Medicamento", 2,
                            "Aseo", 3,
                            "Maternidad", 4));

            campo(dashboard, "inventario", inventario);

            BorderPane raiz = obtener(
                    dashboard, "dashboardRoot");

            ScrollPane lotes = obtener(
                    dashboard, "vistaLotes");

            raiz.setCenter(new BorderPane());

            Button boton = obtener(
                    dashboard, "btnCatalogo");

            boton.setStyle("-fx-background-color: purple;");

            campo(
                    dashboard,
                    "estiloCatalogoOriginal",
                    "-fx-background-color: white;");

            campo(dashboard, "versionIndicadores", 5);

            var tabla = new TableView<Lote>();

            campo(dashboard, "tablaLotes", tabla);

            ObservableList<Lote> lista = obtener(
                    dashboard, "listaLotes");

            campo(
                    dashboard,
                    "datosFiltrados",
                    new FilteredList<>(lista));

            campo(dashboard, "dateInicio", new DatePicker());
            campo(dashboard, "dateFin", new DatePicker());

            campo(dashboard, "lblTotalLotes", new Label());
            campo(dashboard, "lblLotesVencidos", new Label());

            campo(dashboard, "lblTotalMedicamentos", new Label());
            campo(dashboard, "lblTotalAseo", new Label());
            campo(dashboard, "lblTotalMaternidad", new Label());

            privado(dashboard, "volverALotes");

            assertSame(lotes, raiz.getCenter());

            assertEquals(
                    "-fx-background-color: white;",
                    boton.getStyle());

            assertEquals(
                    Integer.valueOf(6),
                    obtener(dashboard, "versionIndicadores"));

            Label total = obtener(
                    dashboard, "lblTotalMedicamentos");

            assertEquals("2", total.getText());

            verify(inventario).listarLotes();
            verify(inventario).contarPorCategoria();

            return null;
        });
    }

    @Test
    void callbackDeIndicadoresSeRegistraYValida()
            throws Exception {

        var catalogo = new ControladorCatalogo();
        var contador = new AtomicInteger();

        catalogo.alActualizarIndicadores(
                contador::incrementAndGet);

        Field campo = ControladorCatalogo.class
                .getDeclaredField("alActualizarIndicadores");

        campo.setAccessible(true);

        Runnable accion = (Runnable) campo.get(catalogo);

        accion.run();
        accion.run();

        assertEquals(2, contador.get());

        assertThrows(
                NullPointerException.class,
                () -> catalogo.alActualizarIndicadores(null));
    }
}
