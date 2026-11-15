package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import org.example.carestock.DataTransferObject.MovimientoHistorial;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorHistorialProfundoTest {

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

    private static <T> T enFx(Callable<T> operacion)
            throws Exception {
        FutureTask<T> tarea = new FutureTask<>(operacion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void asignar(Object o, String campo, Object valor)
            throws Exception {
        Field f = o.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(o, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T obtener(Object o, String campo)
            throws Exception {
        Field f = o.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(o);
    }

    private static Object ejecutar(Object o, String nombre)
            throws Exception {
        Method metodo = o.getClass().getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        return metodo.invoke(o);
    }

    private static Object ejecutar(
            Object o, String nombre, Class<?> tipo, Object valor)
            throws Exception {
        Method metodo = o.getClass()
            .getDeclaredMethod(nombre, tipo);
        metodo.setAccessible(true);
        return metodo.invoke(o, valor);
    }

    private static MovimientoHistorial movimiento(
            String categoria, String tipo, int id) {
        return new MovimientoHistorial(
            "LOG_MOVIMIENTOS", id, categoria, tipo, id,
            "Jabon", "ASE-1", 2L, "Ajuste",
            9, 7, -2L, 8, 7, "QA",
            OffsetDateTime.parse("2026-11-08T12:00:00-05:00"));
    }

    private static ControladorHistorialMovimientos formulario()
            throws Exception {
        var c = new ControladorHistorialMovimientos();

        asignar(c, "lblEstado", new Label());
        asignar(c, "lblTotal", new Label());
        asignar(c, "cmbCategoria", new ComboBox<String>());
        asignar(c, "cmbTipo", new ComboBox<String>());
        asignar(c, "fechaDesde", new DatePicker());
        asignar(c, "fechaHasta", new DatePicker());
        asignar(c, "btnActualizar", new Button());
        asignar(c, "btnLimpiar", new Button());
        asignar(c, "tablaHistorial",
            new TableView<MovimientoHistorial>());

        ComboBox<String> categorias = obtener(c, "cmbCategoria");
        categorias.setItems(FXCollections.observableArrayList(
            "Todos", "MEDICAMENTO", "ASEO", "MATERNIDAD"));
        categorias.setValue("Todos");

        ComboBox<String> tipos = obtener(c, "cmbTipo");
        tipos.setItems(FXCollections.observableArrayList(
            "Todos", "ENTRADA", "SALIDA", "AJUSTE"));
        tipos.setValue("Todos");

        ObservableList<MovimientoHistorial> registros =
            obtener(c, "registros");
        asignar(c, "filtrados",
            new FilteredList<>(registros, m -> true));
        asignar(c, "idUsuario", 7);
        asignar(c, "idFarmacia", 8);

        return c;
    }

    @Test
    void historialVacioInformaCeroMovimientos()
            throws Exception {
        enFx(() -> {
            var c = formulario();
            ejecutar(c, "aplicarFiltros");

            Label total = obtener(c, "lblTotal");
            Label estado = obtener(c, "lblEstado");

            assertEquals("Mostrando: 0 / 0", total.getText());
            assertTrue(estado.getText().contains("No hay movimientos"));
            return null;
        });
    }

    @Test
    void filtraCategoriaYTipoCorrectamente()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            ObservableList<MovimientoHistorial> registros =
                obtener(c, "registros");
            registros.addAll(
                movimiento("ASEO", "ENTRADA", 1),
                movimiento("MATERNIDAD", "SALIDA", 2));

            ComboBox<String> categoria =
                obtener(c, "cmbCategoria");
            ComboBox<String> tipo =
                obtener(c, "cmbTipo");

            categoria.setValue("ASEO");
            tipo.setValue("ENTRADA");
            ejecutar(c, "aplicarFiltros");

            Label total = obtener(c, "lblTotal");
            assertEquals("Mostrando: 1 / 2", total.getText());

            tipo.setValue("SALIDA");
            ejecutar(c, "aplicarFiltros");

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains("Sin resultados"));
            return null;
        });
    }

    @Test
    void rechazaRangoDeFechasInvertido()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            DatePicker desde = obtener(c, "fechaDesde");
            DatePicker hasta = obtener(c, "fechaHasta");

            desde.setValue(LocalDate.of(2026, 12, 10));
            hasta.setValue(LocalDate.of(2026, 11, 10));

            ejecutar(c, "aplicarFiltros");

            Label estado = obtener(c, "lblEstado");
            Label total = obtener(c, "lblTotal");

            assertEquals("Mostrando: 0", total.getText());
            assertTrue(estado.getText()
                .contains("fecha inicial"));
            return null;
        });
    }

    @Test
    void limpiarFiltrosRestableceValores()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            ComboBox<String> categoria =
                obtener(c, "cmbCategoria");
            ComboBox<String> tipo =
                obtener(c, "cmbTipo");
            DatePicker desde = obtener(c, "fechaDesde");

            categoria.setValue("ASEO");
            tipo.setValue("SALIDA");
            desde.setValue(LocalDate.of(2026, 11, 1));

            ejecutar(c, "onLimpiarFiltros");

            assertEquals("Todos", categoria.getValue());
            assertEquals("Todos", tipo.getValue());
            assertNull(desde.getValue());
            return null;
        });
    }

    @Test
    void impideLimpiarDuranteCarga()
            throws Exception {
        enFx(() -> {
            var c = formulario();
            asignar(c, "cargando", true);

            ComboBox<String> categoria =
                obtener(c, "cmbCategoria");
            categoria.setValue("ASEO");

            ejecutar(c, "onLimpiarFiltros");

            assertEquals("ASEO", categoria.getValue());
            return null;
        });
    }

    @Test
    void bloquearFiltrosCambiaTodosLosControles()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            ejecutar(c, "bloquearFiltros",
                boolean.class, true);

            Button actualizar = obtener(c, "btnActualizar");
            DatePicker hasta = obtener(c, "fechaHasta");

            assertTrue(actualizar.isDisabled());
            assertTrue(hasta.isDisabled());
            assertTrue((Boolean) obtener(c, "cargando"));

            ejecutar(c, "bloquearFiltros",
                boolean.class, false);

            assertFalse(actualizar.isDisabled());
            assertFalse(hasta.isDisabled());
            assertFalse((Boolean) obtener(c, "cargando"));
            return null;
        });
    }

    @Test
    void validaIdentidadYFarmaciaDeSesion()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            var usuario = new SesionUsuario.DatosUsuario(
                7, "QA", "qa@test.local",
                "ADMINISTRADOR", 8, "Farmacia QA");

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                    .thenReturn(usuario);

                assertTrue((Boolean)
                    ejecutar(c, "sesionValida"));

                asignar(c, "idFarmacia", 9);

                assertFalse((Boolean)
                    ejecutar(c, "sesionValida"));

                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

                assertFalse((Boolean)
                    ejecutar(c, "sesionValida"));
            }
            return null;
        });
    }

    @Test
    void cancelarConsultaLiberaEstadoDeVista()
            throws Exception {
        enFx(() -> {
            var c = formulario();
            Label estado = obtener(c, "lblEstado");

            estado.textProperty().bind(
                new SimpleStringProperty("Cargando"));

            ejecutar(c, "cancelarConsulta");

            assertFalse((Boolean) obtener(c, "vistaActiva"));
            assertFalse(estado.textProperty().isBound());
            return null;
        });
    }

    @Test
    void mostrarErrorNoConsultaBaseDeDatos()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            try (MockedConstruction<Alert> alertas =
                     mockConstruction(Alert.class)) {

                ejecutar(c, "mostrarError",
                    String.class, "Sin permiso");

                Label estado = obtener(c, "lblEstado");

                assertEquals("Sin permiso", estado.getText());
                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
            }

            return null;
        });
    }
}
