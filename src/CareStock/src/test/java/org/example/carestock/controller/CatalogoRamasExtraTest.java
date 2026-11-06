package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.facade.Inventario;
import org.example.carestock.model.Aseo;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogoRamasExtraTest {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch preparado = new CountDownLatch(1);

        try {
            Platform.startup(preparado::countDown);
        } catch (IllegalStateException yaIniciado) {
            preparado.countDown();
        }

        assertTrue(preparado.await(20, TimeUnit.SECONDS));
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

    private static void asignar(
            Object objeto, String nombre, Object valor)
            throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String nombre)
            throws Exception {

        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(Object objeto, String nombre)
            throws Exception {

        Method metodo = objeto.getClass().getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(objeto);
    }

    private static ControladorCatalogo conNavegacion()
            throws Exception {

        var controlador = new ControladorCatalogo();

        asignar(controlador, "cmbCategoria",
                new ComboBox<String>());

        asignar(controlador, "tablaProductos",
                new TableView<RegistroCatalogo<?>>());

        asignar(controlador, "lblEstado", new Label());

        return controlador;
    }

    @Test
    void usuarioSinPermisoNoPuedeAbrirEdicionDirecta()
            throws Exception {

        enFx(() -> {
            var catalogo = conNavegacion();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(false);

                catalogo.abrirDesdeConsulta("Aseo", 9);
            }

            Label estado = leer(catalogo, "lblEstado");

            assertTrue(estado.getText().contains("Sin permisos"));
            return null;
        });
    }

    @Test
    void edicionDirectaInvalidaLaCategoriaDesconocida()
            throws Exception {

        enFx(() -> {
            var catalogo = conNavegacion();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                assertThrows(IllegalArgumentException.class,
                        () -> catalogo.abrirDesdeConsulta("Otra", 9));
            }

            return null;
        });
    }

    @Test
    void edicionDirectaNoPermiteProductoDeOtraFarmacia()
            throws Exception {

        enFx(() -> {
            var catalogo = conNavegacion();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                catalogo.abrirDesdeConsulta("Maternidad", 999);
            }

            Label estado = leer(catalogo, "lblEstado");
            assertTrue(estado.getText().contains("no disponible"));

            ComboBox<String> categoria =
                    leer(catalogo, "cmbCategoria");

            assertEquals("Maternidad", categoria.getValue());
            return null;
        });
    }

    @Test
    void edicionDirectaEncuentraFilaYAbreFormulario()
            throws Exception {

        enFx(() -> {
            var catalogo = conNavegacion();

            TableView<RegistroCatalogo<?>> tabla =
                    leer(catalogo, "tablaProductos");

            tabla.getItems().add(
                    new RegistroCatalogo<>(
                            35, mock(Aseo.class), "ACTIVO"));

            var capa = new StackPane();

            asignar(catalogo, "capaFormulario", capa);
            asignar(catalogo, "txtCodigo", new TextField());

            asignar(catalogo, "btnRegistrarFormulario",
                    new Button());

            asignar(catalogo, "btnActualizarFormulario",
                    new Button());

            asignar(catalogo, "btnDesactivarFormulario",
                    new Button());

            asignar(catalogo, "btnSimularFormulario",
                    new Button());

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                catalogo.abrirDesdeConsulta("Aseo", 35);
            }

            assertEquals(
                    35,
                    tabla.getSelectionModel()
                            .getSelectedItem().id());

            assertTrue(capa.isVisible());

            Button editar = leer(
                    catalogo, "btnActualizarFormulario");

            Button registrar = leer(
                    catalogo, "btnRegistrarFormulario");

            assertTrue(editar.isVisible());
            assertFalse(registrar.isVisible());

            return null;
        });
    }

    @Test
    void cargaCatalogoPorCadaCategoriaSinBaseDeDatos()
            throws Exception {

        enFx(() -> {
            var inventario = mock(Inventario.class);

            when(inventario.listarAseo()).thenReturn(List.of());
            when(inventario.listarMaternidad()).thenReturn(List.of());
            when(inventario.listarMedicamentos()).thenReturn(List.of());

            for (String categoria : List.of(
                    "Aseo", "Maternidad", "Medicamentos")) {

                var catalogo = conNavegacion();

                asignar(catalogo, "inventario", inventario);
                asignar(catalogo, "txtBuscar", new TextField());

                AtomicInteger notificaciones = new AtomicInteger();

                catalogo.alActualizarIndicadores(
                        notificaciones::incrementAndGet);

                ComboBox<String> selector =
                        leer(catalogo, "cmbCategoria");

                selector.setValue(categoria);

                ejecutar(catalogo, "cargarTabla");

                assertEquals(1, notificaciones.get());
                assertEquals(
                        0,
                        catalogo.registrosDeCategoria().size());

                Label estado = leer(catalogo, "lblEstado");

                assertTrue(
                        estado.getText().contains("Se cargaron 0"));
            }

            verify(inventario).listarAseo();
            verify(inventario).listarMaternidad();
            verify(inventario).listarMedicamentos();

            return null;
        });
    }

    @Test
    void errorAlConsultarNoDejaDatosNiIndicadoresEnganosos()
            throws Exception {

        enFx(() -> {
            var catalogo = conNavegacion();
            var inventario = mock(Inventario.class);

            asignar(catalogo, "inventario", inventario);
            asignar(catalogo, "txtBuscar", new TextField());

            ComboBox<String> selector =
                    leer(catalogo, "cmbCategoria");

            selector.setValue("Aseo");

            when(inventario.listarAseo()).thenThrow(
                    new IllegalStateException("Consulta fallida"));

            AtomicInteger avisos = new AtomicInteger();

            catalogo.alActualizarIndicadores(
                    avisos::incrementAndGet);

            try (MockedConstruction<Alert> alertas =
                         mockConstruction(Alert.class)) {

                ejecutar(catalogo, "cargarTabla");

                assertEquals(1, alertas.constructed().size());

                verify(alertas.constructed().get(0))
                        .showAndWait();
            }

            assertEquals(1, avisos.get());

            assertThrows(IllegalStateException.class,
                    catalogo::registrosDeCategoria);

            TableView<?> tabla =
                    leer(catalogo, "tablaProductos");

            assertTrue(tabla.getItems().isEmpty());

            return null;
        });
    }

    @Test
    void consultaSinCategoriaNoAccedeInventario()
            throws Exception {

        enFx(() -> {
            var catalogo = conNavegacion();
            var inventario = mock(Inventario.class);

            asignar(catalogo, "inventario", inventario);

            ejecutar(catalogo, "cargarTabla");

            verifyNoInteractions(inventario);
            return null;
        });
    }

    @Test
    void busquedaIncompletaNoModificaLaTabla()
            throws Exception {

        enFx(() -> {
            var catalogo = new ControladorCatalogo();

            ejecutar(catalogo, "actualizarBusqueda");

            asignar(catalogo, "txtBuscar",
                    new TextField("Aseo"));

            ejecutar(catalogo, "actualizarBusqueda");

            return null;
        });
    }

    @Test
    void botonEditarSeleccionadoAbreModoActualizacion()
            throws Exception {

        enFx(() -> {
            var catalogo = new ControladorCatalogo();
            var capa = new StackPane();

            asignar(catalogo, "seleccionado",
                    new RegistroCatalogo<>(
                            18, mock(Aseo.class), "ACTIVO"));

            asignar(catalogo, "capaFormulario", capa);
            asignar(catalogo, "txtCodigo", new TextField());

            var registrar = new Button();
            var actualizar = new Button();

            asignar(catalogo, "btnRegistrarFormulario",
                    registrar);

            asignar(catalogo, "btnActualizarFormulario",
                    actualizar);

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                ejecutar(catalogo, "onEditarSeleccionado");
            }

            assertTrue(capa.isVisible());
            assertTrue(actualizar.isVisible());
            assertFalse(registrar.isVisible());

            return null;
        });
    }

    @Test
    void botonEditarSinSeleccionNoAbreFormulario()
            throws Exception {

        enFx(() -> {
            var catalogo = new ControladorCatalogo();
            var capa = new StackPane();

            capa.setVisible(false);
            asignar(catalogo, "capaFormulario", capa);

            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> alertas =
                         mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                ejecutar(catalogo, "onEditarSeleccionado");

                assertEquals(1, alertas.constructed().size());

                verify(alertas.constructed().get(0))
                        .showAndWait();
            }

            assertFalse(capa.isVisible());

            return null;
        });
    }

    @Test
    void botonEditarDeniegaUsuarioSinPermisos()
            throws Exception {

        enFx(() -> {
            var catalogo = new ControladorCatalogo();

            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> alertas =
                         mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(false);

                ejecutar(catalogo, "onEditarSeleccionado");

                assertEquals(1, alertas.constructed().size());

                verify(alertas.constructed().get(0))
                        .showAndWait();
            }

            return null;
        });
    }

    @Test
    void modalRechazaContenedorQueNoEsPane()
            throws Exception {

        enFx(() -> {
            var catalogo = new ControladorCatalogo();
            var capa = new StackPane();

            new Scene(new Group(capa), 750, 550);
            asignar(catalogo, "capaFormulario", capa);

            Method metodo = catalogo.getClass()
                    .getDeclaredMethod("instalarCapaEnVentana");

            metodo.setAccessible(true);

            InvocationTargetException error = assertThrows(
                    InvocationTargetException.class,
                    () -> metodo.invoke(catalogo));

            assertInstanceOf(
                    IllegalStateException.class,
                    error.getCause());

            return null;
        });
    }

    @Test
    void modalAdmiteFondoSinRegionYContenidoNoRedimensionable()
            throws Exception {

        enFx(() -> {
            var catalogo = new ControladorCatalogo();

            var capa = new StackPane(
                    new Rectangle(20, 20));

            var contenedor = new StackPane(capa);
            Scene escena = new Scene(
                    new Group(contenedor), 750, 550);

            asignar(catalogo, "capaFormulario", capa);

            ejecutar(catalogo, "instalarCapaEnVentana");

            assertInstanceOf(
                    StackPane.class, escena.getRoot());

            StackPane root = (StackPane) escena.getRoot();

            assertEquals(2, root.getChildren().size());
            assertSame(capa, root.getChildren().get(1));

            assertEquals(
                    Boolean.TRUE,
                    leer(catalogo, "capaFormularioEnVentana"));

            return null;
        });
    }
}
