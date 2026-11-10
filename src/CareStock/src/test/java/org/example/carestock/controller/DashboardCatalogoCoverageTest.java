package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardCatalogoCoverageTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    1, "Admin QA", "qa@test.local",
                    "ADMINISTRADOR", 7, "Farmacia QA");

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);

        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException iniciado) {
            listo.countDown();
        }

        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Callable<T> accion) throws Exception {
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
    private static <T> T obtener(Object objeto, String nombre)
            throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    @SuppressWarnings("unchecked")
    private static <T> T ejecutar(
            Object objeto, String nombre,
            Class<?>[] tipos, Object... argumentos)
            throws Exception {

        Method metodo = objeto.getClass()
                .getDeclaredMethod(nombre, tipos);
        metodo.setAccessible(true);
        return (T) metodo.invoke(objeto, argumentos);
    }

    private static ControladorInventario preparar(
            ControladorCatalogo catalogo) throws Exception {

        var controlador = new ControladorInventario();

        campo(controlador, "dashboardRoot", new BorderPane());
        campo(controlador, "vistaCatalogo", new BorderPane());
        campo(controlador, "vistaLotes", new ScrollPane());
        campo(controlador, "controladorCatalogo", catalogo);
        campo(controlador, "btnCatalogo", new Button("Catálogo"));

        campo(controlador, "tituloTotalCatalogo", new Label());
        campo(controlador, "tituloIntermedioCatalogo", new Label());
        campo(controlador, "tituloAgotadosCatalogo", new Label());

        campo(controlador, "totalMedicamentosCatalogo", new Label());
        campo(controlador, "vencidosCatalogo", new Label());
        campo(controlador, "agotadosCatalogo", new Label());

        return controlador;
    }

    @Test
    void bloqueaCatalogoSinSesion() throws Exception {
        fx(() -> {
            var controlador = preparar(mock(ControladorCatalogo.class));

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(false);

                assertThrows(SecurityException.class,
                        () -> controlador.abrirCatalogoIntegrado(
                                null, null));
            }

            return null;
        });
    }

    @Test
    void bloqueaCatalogoSinFarmacia() throws Exception {
        fx(() -> {
            var controlador = preparar(mock(ControladorCatalogo.class));

            var sinFarmacia = new SesionUsuario.DatosUsuario(
                    1, "Admin", "admin@test.local",
                    "ADMINISTRADOR", null, null);

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(sinFarmacia);

                assertThrows(SecurityException.class,
                        () -> controlador.abrirCatalogoIntegrado(
                                null, null));
            }

            return null;
        });
    }

    @Test
    void reutilizaPanelYCambiaIndicadoresPorCategoria()
            throws Exception {

        fx(() -> {
            var catalogo = mock(ControladorCatalogo.class);
            when(catalogo.categoriaActual())
                    .thenReturn("Aseo", "Maternidad");
            when(catalogo.registrosDeCategoria())
                    .thenReturn(List.of());

            var controlador = preparar(catalogo);
            BorderPane root = obtener(controlador, "dashboardRoot");
            BorderPane vista = obtener(controlador, "vistaCatalogo");

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                controlador.abrirCatalogoIntegrado("Aseo", null);

                assertSame(vista, root.getCenter());
                verify(catalogo).abrirDesdeConsulta("Aseo", null);

                Label total = obtener(
                        controlador, "totalMedicamentosCatalogo");
                Label titulo = obtener(
                        controlador, "tituloTotalCatalogo");

                assertEquals("0", total.getText());
                assertEquals("TOTAL DE ASEO", titulo.getText());

                controlador.abrirCatalogoIntegrado(
                        "Maternidad", 12);

                verify(catalogo).abrirDesdeConsulta(
                        "Maternidad", 12);

                assertEquals("TOTAL DE MATERNIDAD",
                        titulo.getText());
                assertSame(vista, root.getCenter());
            }

            return null;
        });
    }

    @Test
    void errorDeCategoriaNoMuestraMetricasInventadas()
            throws Exception {

        fx(() -> {
            var catalogo = mock(ControladorCatalogo.class);
            when(catalogo.categoriaActual())
                    .thenReturn("Desconocida");

            var controlador = preparar(catalogo);

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                controlador.abrirCatalogoIntegrado(null, null);

                Label total = obtener(
                        controlador, "totalMedicamentosCatalogo");

                assertEquals("N/D", total.getText());
            }

            return null;
        });
    }

    @Test
    void cabeceraIncluyeRegresoYTarjetas()
            throws Exception {

        fx(() -> {
            var controlador = new ControladorInventario();

            VBox cabecera = ejecutar(
                    controlador, "encabezadoCatalogo",
                    new Class<?>[]{});

            assertNotNull(cabecera);
            assertEquals(2, cabecera.getChildren().size());

            HBox fila = (HBox) cabecera.getChildren().get(0);
            Button volver = (Button) fila.getChildren().get(1);

            assertTrue(volver.getText()
                    .contains("Inventario de lotes"));

            HBox tarjetas = (HBox) cabecera.getChildren().get(1);
            assertEquals(3, tarjetas.getChildren().size());

            return null;
        });
    }

    @Test
    void indicadoresSoloSonValidosEnLaSesionYVistaActual()
            throws Exception {

        fx(() -> {
            var controlador = preparar(mock(ControladorCatalogo.class));

            BorderPane root = obtener(controlador, "dashboardRoot");
            BorderPane vista = obtener(controlador, "vistaCatalogo");

            root.setCenter(vista);
            campo(controlador, "versionIndicadores", 5);

            Class<?>[] tipos = {
                    int.class, SesionUsuario.DatosUsuario.class
            };

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                assertEquals(Boolean.TRUE, ejecutar(
                        controlador, "indicadoresVigentes",
                        tipos, 5, ADMIN));

                assertEquals(Boolean.FALSE, ejecutar(
                        controlador, "indicadoresVigentes",
                        tipos, 4, ADMIN));

                root.setCenter(new ScrollPane());

                assertEquals(Boolean.FALSE, ejecutar(
                        controlador, "indicadoresVigentes",
                        tipos, 5, ADMIN));
            }

            return null;
        });
    }
}
