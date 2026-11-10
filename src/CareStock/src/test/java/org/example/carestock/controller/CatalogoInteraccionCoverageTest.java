package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Aseo;
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

class CatalogoInteraccionCoverageTest {

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

    private static void ejecutar(
            Object objeto, String nombre,
            Class<?>[] tipos, Object... argumentos)
            throws Exception {

        Method metodo = objeto.getClass()
                .getDeclaredMethod(nombre, tipos);
        metodo.setAccessible(true);
        metodo.invoke(objeto, argumentos);
    }

    private static void ejecutar(Object objeto, String nombre)
            throws Exception {

        ejecutar(objeto, nombre, new Class<?>[]{});
    }

    private static RegistroCatalogo<Aseo> producto(
            int id, String codigo, String nombre) {

        Aseo aseo = mock(Aseo.class);

        when(aseo.getCodigo()).thenReturn(codigo);
        when(aseo.getNombre()).thenReturn(nombre);
        when(aseo.getDescripcion())
                .thenReturn("Producto de aseo");

        return new RegistroCatalogo<>(id, aseo, "ACTIVO");
    }

    @Test
    void busquedaFiltraTablaSinEliminarRegistrosOriginales()
            throws Exception {

        fx(() -> {
            var controlador = new ControladorCatalogo();

            var tabla = new TableView<RegistroCatalogo<?>>();
            var buscar = new TextField();

            campo(controlador, "tablaProductos", tabla);
            campo(controlador, "txtBuscar", buscar);

            campo(controlador, "registrosCompletos", List.of(
                    producto(1, "ASE-001", "Jabon"),
                    producto(2, "ASE-002", "Shampoo"),
                    producto(3, "ASE-003", "Alcohol")));

            ejecutar(controlador, "configurarBusqueda");
            ejecutar(controlador, "actualizarBusqueda");

            assertEquals(3, tabla.getItems().size());

            buscar.setText("jabon");
            assertEquals(1, tabla.getItems().size());
            assertEquals(1, tabla.getItems().get(0).id());

            buscar.setText("ASE-003");
            assertEquals(1, tabla.getItems().size());
            assertEquals(3, tabla.getItems().get(0).id());

            buscar.setText("inexistente");
            assertTrue(tabla.getItems().isEmpty());

            buscar.clear();
            assertEquals(3, tabla.getItems().size());

            return null;
        });
    }

    @Test
    void registroCompletoEsIndependienteDelFiltro()
            throws Exception {

        fx(() -> {
            var controlador = new ControladorCatalogo();
            var categorias = new ComboBox<String>();

            categorias.getItems().addAll(
                    "Medicamentos", "Aseo", "Maternidad");

            categorias.setValue("Aseo");

            campo(controlador, "cmbCategoria", categorias);
            campo(controlador, "cargaCategoriaExitosa", true);
            campo(controlador, "registrosCompletos", List.of(
                    producto(1, "ASE-001", "Jabon"),
                    producto(2, "ASE-002", "Alcohol")));

            assertEquals("Aseo", controlador.categoriaActual());
            assertEquals(
                    2, controlador.registrosDeCategoria().size());

            return null;
        });
    }

    @Test
    void consultaFallidaNoEntregaIndicadoresFalsos()
            throws Exception {

        var controlador = new ControladorCatalogo();

        assertThrows(IllegalStateException.class,
                controlador::registrosDeCategoria);
    }

    @Test
    void nuevoYEditarCambianAccionesDelFormulario()
            throws Exception {

        fx(() -> {
            var controlador = new ControladorCatalogo();
            var capa = new StackPane();

            Button registrar = new Button("Registrar");
            Button actualizar = new Button("Guardar cambios");
            Button desactivar = new Button("Desactivar");
            Button simular = new Button("Simular compra");

            campo(controlador, "capaFormulario", capa);
            campo(controlador, "txtCodigo", new TextField());
            campo(controlador, "btnRegistrarFormulario", registrar);
            campo(controlador, "btnActualizarFormulario", actualizar);
            campo(controlador, "btnDesactivarFormulario", desactivar);
            campo(controlador, "btnSimularFormulario", simular);

            ejecutar(
                    controlador, "mostrarFormulario",
                    new Class<?>[]{boolean.class}, true);

            assertTrue(capa.isVisible());
            assertTrue(capa.isManaged());
            assertTrue(registrar.isVisible());
            assertFalse(actualizar.isVisible());
            assertFalse(desactivar.isVisible());
            assertFalse(simular.isVisible());

            ejecutar(
                    controlador, "mostrarFormulario",
                    new Class<?>[]{boolean.class}, false);

            assertFalse(registrar.isVisible());
            assertTrue(actualizar.isVisible());
            assertTrue(desactivar.isVisible());
            assertTrue(simular.isVisible());

            ejecutar(controlador, "onCerrarFormulario");

            assertFalse(capa.isVisible());
            assertFalse(capa.isManaged());

            return null;
        });
    }

    @Test
    void modalSeSuperponeSinReemplazarElDashboard()
            throws Exception {

        fx(() -> {
            var controlador = new ControladorCatalogo();

            var fondo = new BorderPane();
            var contenedor = new StackPane();
            var capa = new StackPane();
            var formulario = new ScrollPane();

            capa.getChildren().add(formulario);
            contenedor.getChildren().add(capa);
            fondo.setCenter(contenedor);

            Scene escena = new Scene(fondo, 1100, 750);

            campo(controlador, "capaFormulario", capa);

            ejecutar(controlador, "instalarCapaEnVentana");

            assertInstanceOf(
                    StackPane.class, escena.getRoot());

            StackPane raiz = (StackPane) escena.getRoot();

            assertEquals(2, raiz.getChildren().size());
            assertSame(fondo, raiz.getChildren().get(0));
            assertSame(capa, raiz.getChildren().get(1));

            assertEquals(
                    Pos.TOP_LEFT,
                    StackPane.getAlignment(fondo));

            assertEquals(980, formulario.getMaxWidth());
            assertEquals(620, formulario.getMaxHeight());

            assertEquals(Boolean.TRUE, obtener(
                    controlador, "capaFormularioEnVentana"));

            // Una segunda apertura no debe duplicar el modal.
            ejecutar(controlador, "instalarCapaEnVentana");

            assertEquals(2, raiz.getChildren().size());

            return null;
        });
    }

    @Test
    void modalSinEscenaNoModificaLaJerarquia()
            throws Exception {

        fx(() -> {
            var controlador = new ControladorCatalogo();
            var capa = new StackPane();

            campo(controlador, "capaFormulario", capa);

            ejecutar(controlador, "instalarCapaEnVentana");

            assertEquals(Boolean.FALSE, obtener(
                    controlador, "capaFormularioEnVentana"));

            return null;
        });
    }
}
