package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import org.example.carestock.model.Lote;
import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

/** Pruebas del boton "Sugerir lote" (HU-152). */
class ControladorInventarioSugerirLoteTest {

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

    private static void poner(Object objeto, String campo, Object valor) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String campo) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(objeto);
    }

    private static void sugerir(ControladorInventario c) throws Exception {
        Method m = c.getClass().getDeclaredMethod("onSugerirLote");
        m.setAccessible(true);
        m.invoke(c);
    }

    private static Lote lote(int id, int idMedicamento, String estado, int diasParaVencer) {
        Lote lote = new Lote();
        lote.setIdLote(id);
        lote.setNumeroLote("L-" + id);
        lote.setIdMedicamento(idMedicamento);
        lote.setCantidadActual(10);
        lote.setEstadoLote(estado);
        lote.setFechaVencimiento(FechaNegocio.hoy().plusDays(diasParaVencer));
        return lote;
    }

    private static ControladorInventario vista(
            List<Lote> todos, List<Lote> visibles, Lote seleccionado) throws Exception {
        ControladorInventario c = new ControladorInventario();

        TableView<Lote> tabla = new TableView<>();
        tabla.setItems(FXCollections.observableArrayList(visibles));
        poner(c, "tablaLotes", tabla);

        ObservableList<Lote> lista = leer(c, "listaLotes");
        lista.setAll(todos);

        poner(c, "loteSeleccionado", seleccionado);
        return c;
    }

    @Test
    void sinLoteSeleccionadoAvisaYNoSeleccionaNada() throws Exception {
        fx(() -> {
            Lote cercano = lote(1, 10, "DISPONIBLE", 20);
            ControladorInventario c = vista(List.of(cercano), List.of(cercano), null);

            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sugerir(c);

                assertEquals(1, alertas.constructed().size());
            }

            TableView<Lote> tabla = leer(c, "tablaLotes");
            assertNull(tabla.getSelectionModel().getSelectedItem());
            return null;
        });
    }

    @Test
    void sinLotesAptosAvisa() throws Exception {
        fx(() -> {
            Lote retenido = lote(1, 10, "RETENIDO", 20);
            ControladorInventario c = vista(List.of(retenido), List.of(retenido), retenido);

            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sugerir(c);

                assertEquals(1, alertas.constructed().size());
            }

            TableView<Lote> tabla = leer(c, "tablaLotes");
            assertNull(tabla.getSelectionModel().getSelectedItem());
            return null;
        });
    }

    @Test
    void siElLoteYaEsElQueVenceAntesAvisa() throws Exception {
        fx(() -> {
            Lote cercano = lote(1, 10, "DISPONIBLE", 20);
            Lote lejano = lote(2, 10, "DISPONIBLE", 90);
            ControladorInventario c = vista(
                    List.of(cercano, lejano), List.of(cercano, lejano), cercano);

            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sugerir(c);

                assertEquals(1, alertas.constructed().size());
            }

            TableView<Lote> tabla = leer(c, "tablaLotes");
            assertNull(tabla.getSelectionModel().getSelectedItem());
            return null;
        });
    }

    @Test
    void siLosFiltrosOcultanElLoteSugeridoAvisa() throws Exception {
        fx(() -> {
            Lote cercano = lote(1, 10, "DISPONIBLE", 20);
            Lote lejano = lote(2, 10, "DISPONIBLE", 90);
            // La tabla solo muestra el lote lejano: el cercano esta filtrado.
            ControladorInventario c = vista(
                    List.of(cercano, lejano), List.of(lejano), lejano);

            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sugerir(c);

                assertEquals(1, alertas.constructed().size());
            }

            TableView<Lote> tabla = leer(c, "tablaLotes");
            assertNull(tabla.getSelectionModel().getSelectedItem());
            return null;
        });
    }

    @Test
    void seleccionaElLoteQueVenceAntes() throws Exception {
        fx(() -> {
            Lote cercano = lote(1, 10, "DISPONIBLE", 20);
            Lote lejano = lote(2, 10, "DISPONIBLE", 90);
            ControladorInventario c = vista(
                    List.of(cercano, lejano), List.of(cercano, lejano), lejano);

            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sugerir(c);

                assertEquals(0, alertas.constructed().size());
            }

            TableView<Lote> tabla = leer(c, "tablaLotes");
            assertSame(cercano, tabla.getSelectionModel().getSelectedItem());
            return null;
        });
    }
}
