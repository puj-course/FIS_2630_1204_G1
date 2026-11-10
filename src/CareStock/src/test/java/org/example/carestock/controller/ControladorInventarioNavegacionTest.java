package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.model.Lote;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ControladorInventarioNavegacionTest {

    private static final int FARMACIA = 8;

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);

        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        }

        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T enFx(Callable<T> accion)
            throws Exception {

        if (Platform.isFxApplicationThread()) {
            return accion.call();
        }

        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static <T> T conSesion(
            Integer farmacia, Callable<T> accion)
            throws Exception {

        return enFx(() -> {
            Field campo = SesionUsuario.class
                    .getDeclaredField("actual");
            campo.setAccessible(true);

            Object anterior = campo.get(null);

            try {
                if (farmacia == null) {
                    campo.set(null, null);
                } else {
                    campo.set(null, new SesionUsuario.DatosUsuario(
                            7, "Usuario QA", "qa@test.local",
                            "FARMACEUTICO", farmacia, "Farmacia QA"));
                }

                return accion.call();
            } finally {
                campo.set(null, anterior);
            }
        });
    }

    private static void poner(
            Object objeto, String nombre, Object valor)
            throws Exception {

        Field campo = objeto.getClass()
                .getDeclaredField(nombre);

        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T obtener(Object objeto, String nombre)
            throws Exception {

        Field campo = objeto.getClass()
                .getDeclaredField(nombre);

        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(Object objeto, String metodo)
            throws Exception {

        Method accion = objeto.getClass()
                .getDeclaredMethod(metodo);

        accion.setAccessible(true);
        accion.invoke(objeto);
    }

    private static ControladorInventario preparar()
            throws Exception {

        var controlador = new ControladorInventario();

        poner(controlador, "tablaLotes", new TableView<Lote>());
        poner(controlador, "txtBuscar", new TextField());
        poner(controlador, "cmbCategoria", new ComboBox<String>());
        poner(controlador, "dateInicio", new DatePicker());
        poner(controlador, "dateFin", new DatePicker());
        poner(controlador, "lblLoteSeleccionadoInfo", new Label());

        ComboBox<String> categoria =
                obtener(controlador, "cmbCategoria");

        categoria.getItems().addAll(
                "Todas", "Medicamento", "Aseo", "Maternidad");
        categoria.getSelectionModel().selectFirst();

        ejecutar(controlador, "configurarFiltroBusqueda");

        return controlador;
    }

    private static Lote lote(
            int id, String numero, boolean vencido) {

        Lote lote = new Lote();
        lote.setIdLote(id);
        lote.setNumeroLote(numero);
        lote.setIdMedicamento(15);
        lote.setCantidadActual(3);
        lote.setEstadoLote(
                vencido ? "VENCIDO" : "DISPONIBLE");

        lote.setFechaIngreso(
                LocalDateTime.now().minusDays(15));

        lote.setFechaVencimiento(
                vencido
                        ? LocalDate.now().minusDays(2)
                        : LocalDate.now().plusDays(10));

        return lote;
    }

    private static Destino destino(
            int id, String numero, int farmacia) {

        return new Destino(
                TipoAlerta.VENCIMIENTO,
                Categoria.MEDICAMENTO,
                id,
                "MED-15",
                "Medicamento 15",
                numero,
                farmacia);
    }

    @SuppressWarnings("unchecked")
    private static ObservableList<Lote> lista(
            ControladorInventario controlador)
            throws Exception {

        return obtener(controlador, "listaLotes");
    }

    // =====================================
    // NAVEGACION CORRECTA
    // =====================================

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void seleccionaLoteVencidoOProximoAVencer(
            boolean vencido) throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();
            Lote esperado = lote(44, "LOT-44", vencido);
            lista(controlador).add(esperado);

            controlador.abrirDesdeAlerta(
                    destino(44, "LOT-44", FARMACIA));

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertSame(
                    esperado,
                    tabla.getSelectionModel().getSelectedItem());

            assertEquals(1, tabla.getItems().size());

            return null;
        });
    }

    @Test
    void seleccionaIdExactoCuandoHayNumerosRepetidos()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            Lote otro = lote(31, "LOT-44", false);
            Lote esperado = lote(44, "LOT-44", true);

            lista(controlador).addAll(otro, esperado);

            controlador.abrirDesdeAlerta(
                    destino(44, "LOT-44", FARMACIA));

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertSame(
                    esperado,
                    tabla.getSelectionModel().getSelectedItem());

            assertEquals(1, tabla.getItems().size());

            return null;
        });
    }

    @Test
    void restableceFiltrosAnterioresParaEncontrarLote()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            Lote esperado = lote(44, "LOT-44", true);
            lista(controlador).add(esperado);

            TextField buscar = obtener(
                    controlador, "txtBuscar");

            DatePicker fechaInicio = obtener(
                    controlador, "dateInicio");

            buscar.setText("OTRO-LOTE");
            fechaInicio.setValue(LocalDate.now());

            controlador.abrirDesdeAlerta(
                    destino(44, "LOT-44", FARMACIA));

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertEquals("", buscar.getText());
            assertNull(fechaInicio.getValue());
            assertSame(
                    esperado,
                    tabla.getSelectionModel().getSelectedItem());

            return null;
        });
    }

    @Test
    void numeroDeLoteAceptaDiferenciasDeMayusculas()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            Lote esperado = lote(44, "LOT-44", false);
            lista(controlador).add(esperado);

            controlador.abrirDesdeAlerta(
                    destino(44, " lot-44 ", FARMACIA));

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertSame(
                    esperado,
                    tabla.getSelectionModel().getSelectedItem());

            return null;
        });
    }

    // =====================================
    // REFERENCIAS NO DISPONIBLES
    // =====================================

    @Test
    void loteInexistenteMuestraMensaje()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            controlador.abrirDesdeAlerta(
                    destino(999, "NO-EXISTE", FARMACIA));

            Label estado = obtener(
                    controlador, "lblLoteSeleccionadoInfo");

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertTrue(
                    estado.getText().contains("ya no existe"));

            assertNull(
                    tabla.getSelectionModel().getSelectedItem());

            return null;
        });
    }

    @Test
    void mismoIdConNumeroIncorrectoNoSeSelecciona()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            lista(controlador).add(
                    lote(44, "LOT-REAL", false));

            controlador.abrirDesdeAlerta(
                    destino(44, "LOT-INCORRECTO", FARMACIA));

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertNull(
                    tabla.getSelectionModel().getSelectedItem());

            Label estado = obtener(
                    controlador, "lblLoteSeleccionadoInfo");

            assertTrue(
                    estado.getText().contains("no esta disponible"));

            return null;
        });
    }

    @Test
    void alertaDeStockNoDebeAbrirUnLote()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();
            lista(controlador).add(
                    lote(44, "LOT-44", false));

            var destinoIncorrecto = new Destino(
                    TipoAlerta.STOCK_MINIMO,
                    Categoria.MEDICAMENTO,
                    44, "MED-15",
                    "Medicamento 15",
                    null, FARMACIA);

            controlador.abrirDesdeAlerta(destinoIncorrecto);

            TableView<Lote> tabla =
                    obtener(controlador, "tablaLotes");

            assertNull(
                    tabla.getSelectionModel().getSelectedItem());

            return null;
        });
    }

    // =====================================
    // SEGURIDAD Y FARMACIA
    // =====================================

    @Test
    void rechazaNavegacionDeOtraFarmacia()
            throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            controlador.abrirDesdeAlerta(
                    destino(44, "LOT-44", 99));

            Label estado = obtener(
                    controlador, "lblLoteSeleccionadoInfo");

            assertTrue(estado.getText().contains(
                    "farmacia activa ha cambiado"));

            return null;
        });
    }

    @Test
    void rechazaDestinoNulo() throws Exception {

        conSesion(FARMACIA, () -> {
            var controlador = preparar();

            controlador.abrirDesdeAlerta(null);

            Label estado = obtener(
                    controlador, "lblLoteSeleccionadoInfo");

            assertTrue(estado.getText().contains(
                    "No es posible abrir el lote"));

            return null;
        });
    }

    @Test
    void rechazaNavegacionSinSesion()
            throws Exception {

        conSesion(null, () -> {
            var controlador = preparar();

            controlador.abrirDesdeAlerta(
                    destino(44, "LOT-44", FARMACIA));

            Label estado = obtener(
                    controlador, "lblLoteSeleccionadoInfo");

            assertTrue(estado.getText().contains(
                    "farmacia activa ha cambiado"));

            return null;
        });
    }
}
