package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.service.RegistroReposicion;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorReposicionTest {

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

    private static void ejecutar(
            Object objeto, String metodo) throws Exception {

        Method m = objeto.getClass().getDeclaredMethod(metodo);
        m.setAccessible(true);
        m.invoke(objeto);
    }

    private static RegistroReposicion producto(
            Categoria categoria, int id, String codigo,
            String nombre, int stock, int minimo) {

        return new RegistroReposicion(
                categoria, id, codigo, nombre,
                stock, minimo, minimo - stock,
                stock == 0
                        ? Severidad.CRITICA
                        : Severidad.ADVERTENCIA);
    }

    private static ControladorReposicion preparar()
            throws Exception {

        var controlador = new ControladorReposicion();

        var categorias = new ComboBox<String>();
        categorias.getItems().add("Todas");

        for (Categoria categoria : Categoria.values()) {
            categorias.getItems().add(categoria.etiqueta());
        }

        categorias.setValue("Todas");

        var severidades = new ComboBox<String>();
        severidades.getItems().add("Todas");

        for (Severidad severidad : Severidad.values()) {
            severidades.getItems().add(severidad.etiqueta());
        }

        severidades.setValue("Todas");

        asignar(controlador, "cmbCategoria", categorias);
        asignar(controlador, "cmbSeveridad", severidades);
        asignar(controlador, "txtBuscar", new TextField());
        asignar(controlador, "tablaReposicion",
                new TableView<RegistroReposicion>());
        asignar(controlador, "lblEstado", new Label());
        asignar(controlador, "lblConteo", new Label());
        asignar(controlador, "btnActualizar", new Button());
        asignar(controlador, "btnExportarCsv", new Button());
        asignar(controlador, "sesionVista", ADMIN);

        return controlador;
    }

    @Test
    void filtraPorCategoriaSeveridadYNombre()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            var agotado = producto(
                    Categoria.ASEO, 1, "ASE-1",
                    "Jabón antibacterial", 0, 5);

            var bajo = producto(
                    Categoria.ASEO, 2, "ASE-2",
                    "Champú", 2, 6);

            var medicamento = producto(
                    Categoria.MEDICAMENTO, 3, "MED-3",
                    "Acetaminofén", 0, 8);

            asignar(controlador, "registros",
                    List.of(agotado, bajo, medicamento));

            ejecutar(controlador, "filtrar");

            TableView<RegistroReposicion> tabla =
                    leer(controlador, "tablaReposicion");

            assertEquals(3, tabla.getItems().size());

            ComboBox<String> categoria =
                    leer(controlador, "cmbCategoria");

            ComboBox<String> severidad =
                    leer(controlador, "cmbSeveridad");

            TextField buscar = leer(controlador, "txtBuscar");

            categoria.setValue("Aseo");
            severidad.setValue("Crítica");
            buscar.setText("jab");

            ejecutar(controlador, "filtrar");

            assertEquals(1, tabla.getItems().size());
            assertEquals(agotado, tabla.getItems().get(0));

            Label conteo = leer(controlador, "lblConteo");

            assertEquals("Productos visibles: 1",
                    conteo.getText());

            return null;
        });
    }

    @Test
    void busquedaSinCoincidenciasDeshabilitaExportacion()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            asignar(controlador, "registros",
                    List.of(producto(
                            Categoria.ASEO, 1,
                            "ASE-1", "Jabón", 0, 5)));

            TextField buscar = leer(
                    controlador, "txtBuscar");

            buscar.setText("No existe");

            ejecutar(controlador, "filtrar");

            TableView<?> tabla = leer(
                    controlador, "tablaReposicion");

            Button exportar = leer(
                    controlador, "btnExportarCsv");

            assertTrue(tabla.getItems().isEmpty());
            assertTrue(exportar.isDisabled());

            return null;
        });
    }

    @Test
    void mientrasEstaOcupadoBloqueaLosBotones()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            TableView<RegistroReposicion> tabla =
                    leer(controlador, "tablaReposicion");

            tabla.setItems(FXCollections.observableArrayList(
                    producto(Categoria.ASEO, 1,
                            "ASE-1", "Jabón", 0, 5)));

            Method metodo = controlador.getClass()
                    .getDeclaredMethod(
                            "cambiarOcupado", boolean.class);

            metodo.setAccessible(true);

            metodo.invoke(controlador, true);

            Button actualizar =
                    leer(controlador, "btnActualizar");

            Button exportar =
                    leer(controlador, "btnExportarCsv");

            assertTrue(actualizar.isDisabled());
            assertTrue(exportar.isDisabled());

            metodo.invoke(controlador, false);

            assertFalse(actualizar.isDisabled());
            assertFalse(exportar.isDisabled());

            return null;
        });
    }

    @Test
    void sinRegistrosInformaQueNoHayNadaParaExportar()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);

                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                ejecutar(controlador, "onExportarCsv");
            }

            Label estado = leer(
                    controlador, "lblEstado");

            assertTrue(estado.getText().contains(
                    "No hay productos visibles"));

            return null;
        });
    }

    @Test
    void usuarioSinSesionNoPuedeExportar()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(false);

                ejecutar(controlador, "onExportarCsv");
            }

            Label estado = leer(
                    controlador, "lblEstado");

            assertTrue(estado.getText().contains(
                    "No se pudo exportar"));

            return null;
        });
    }

    @Test
    void alPerderSesionEliminaDatosDeLaVista()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            asignar(controlador, "registros",
                    List.of(producto(
                            Categoria.ASEO, 1,
                            "ASE-1", "Jabón", 0, 5)));

            ejecutar(controlador, "filtrar");

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(false);

                ejecutar(controlador, "cargar");
            }

            TableView<?> tabla = leer(
                    controlador, "tablaReposicion");

            Label estado = leer(
                    controlador, "lblEstado");

            assertTrue(tabla.getItems().isEmpty());
            assertTrue(estado.getText().contains(
                    "Acceso denegado"));

            return null;
        });
    }

    @Test
    void operacionOcupadaNoIniciaNuevaExportacion()
            throws Exception {

        fx(() -> {
            var controlador = preparar();

            asignar(controlador, "ocupado", true);

            Label estado = leer(controlador, "lblEstado");
            estado.setText("Operación en curso");

            ejecutar(controlador, "onExportarCsv");

            assertEquals("Operación en curso",
                    estado.getText());

            return null;
        });
    }

    @Test
    void recursoFXMLContieneLaTablaYElBotonCsv()
            throws Exception {

        try (var recurso = getClass().getResourceAsStream(
                "/org/example/carestock/reposicion-view.fxml")) {

            assertNotNull(recurso);

            String fxml = new String(
                    recurso.readAllBytes(),
                    StandardCharsets.UTF_8);

            assertTrue(fxml.contains(
                    "ControladorReposicion"));

            assertTrue(fxml.contains(
                    "fx:id=\"tablaReposicion\""));

            assertTrue(fxml.contains(
                    "onAction=\"#onExportarCsv\""));

            assertTrue(fxml.contains(
                    "onAction=\"#onActualizar\""));
        }
    }
}
