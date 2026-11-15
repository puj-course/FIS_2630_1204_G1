package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimuladorCompraCatalogoTest {

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

    private static void ejecutarEnFx(Runnable accion)
            throws Exception {

        FutureTask<Void> tarea = new FutureTask<>(() -> {
            accion.run();
            return null;
        });

        Platform.runLater(tarea);
        tarea.get(20, TimeUnit.SECONDS);
    }

    @Test
    void validaCantidadesDeCompra() {
        assertEquals(5,
                ControladorCatalogo.leerCantidadCompra(" 5 "));
        assertEquals(1,
                ControladorCatalogo.leerCantidadCompra("1"));

        assertThrows(IllegalArgumentException.class,
                () -> ControladorCatalogo.leerCantidadCompra(""));
        assertThrows(IllegalArgumentException.class,
                () -> ControladorCatalogo.leerCantidadCompra(null));
        assertThrows(IllegalArgumentException.class,
                () -> ControladorCatalogo.leerCantidadCompra("abc"));
        assertThrows(IllegalArgumentException.class,
                () -> ControladorCatalogo.leerCantidadCompra("0"));
        assertThrows(IllegalArgumentException.class,
                () -> ControladorCatalogo.leerCantidadCompra("-3"));
    }

    @Test
    void clasificaCorrectamenteElStock() throws Exception {
        ejecutarEnFx(() -> {
            Label resultado = new Label();

            ControladorCatalogo.mostrarResultadoCompra(
                    resultado, 4, 10, 10);
            assertTrue(resultado.getText().contains("Stock Óptimo"));
            assertTrue(resultado.getStyle().contains("#27AE60"));

            ControladorCatalogo.mostrarResultadoCompra(
                    resultado, 4, 8, 10);
            assertTrue(resultado.getText().contains("Stock Bajo"));
            assertTrue(resultado.getStyle().contains("#E67E22"));

            ControladorCatalogo.mostrarResultadoCompra(
                    resultado, 12, 16, 10);
            assertTrue(resultado.getStyle().contains("#6B5B95"));

            ControladorCatalogo.mostrarResultadoCompra(
                    resultado, 1, 2, null);
            assertTrue(resultado.getStyle().contains("#6B5B95"));
        });
    }

    @Test
    void procesaCalculosYErrores() throws Exception {
        ejecutarEnFx(() -> {
            ControladorCatalogo controlador =
                    new ControladorCatalogo();

            TextField entrada = new TextField("6");
            Label resultado = new Label();

            controlador.procesarSimulacion(
                    4, 10, entrada, resultado);
            assertTrue(resultado.getText().contains("10 unidades"));

            entrada.setText("no numerico");
            controlador.procesarSimulacion(
                    4, 10, entrada, resultado);
            assertTrue(resultado.getText().startsWith("Error:"));

            controlador.procesarSimulacion(
                    4, 10, null, resultado);
            assertTrue(resultado.getText()
                    .startsWith("Error inesperado:"));
        });
    }
}
