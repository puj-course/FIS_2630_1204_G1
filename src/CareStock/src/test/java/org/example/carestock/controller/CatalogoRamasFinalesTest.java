package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Medicamento;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoRamasFinalesTest {

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

    private static void ejecutarFx(Runnable tarea)
            throws Exception {
        FutureTask<Void> pendiente = new FutureTask<>(() -> {
            tarea.run();
            return null;
        });

        Platform.runLater(pendiente);
        pendiente.get(20, TimeUnit.SECONDS);
    }

    private static RegistroCatalogo<Medicamento> registro(
            String estado, int stock, int minimo) {

        Medicamento med = new Medicamento();
        med.setStockTotal(stock);
        med.setStockMinimo(minimo);

        return new RegistroCatalogo<>(1, med, estado);
    }

    private static Tooltip consultarTooltip(
            ControladorCatalogo controlador,
            RegistroCatalogo<?> registro) {

        try {
            Method metodo = ControladorCatalogo.class
                    .getDeclaredMethod(
                            "tooltipStock",
                            RegistroCatalogo.class);

            metodo.setAccessible(true);
            return (Tooltip) metodo.invoke(
                    controlador, registro);

        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(
                    "No fue posible consultar tooltipStock", ex);
        }
    }

    @Test
    void tooltipRespetaTodosLosLimitesDeStock()
            throws Exception {

        ejecutarFx(() -> {
            ControladorCatalogo controlador =
                    new ControladorCatalogo();

            assertNull(consultarTooltip(controlador, null));

            assertNull(consultarTooltip(controlador,
                    registro("INACTIVO", 3, 10)));

            assertNull(consultarTooltip(controlador,
                    registro("ACTIVO", 10, 10)));

            assertNull(consultarTooltip(controlador,
                    registro("ACTIVO", 11, 10)));

            assertNull(consultarTooltip(controlador,
                    registro("ACTIVO", 1, 0)));

            Tooltip alerta = consultarTooltip(controlador,
                    registro("ACTIVO", 9, 10));

            assertNotNull(alerta);
            assertTrue(alerta.getText()
                    .contains("stock mínimo"));

            RegistroCatalogo<Object> sinMinimo =
                    new RegistroCatalogo<>(
                            2, new Object(), "ACTIVO");

            assertNull(consultarTooltip(
                    controlador, sinMinimo));
        });
    }

    @Test
    void celdaStockManejaValoresNulosYVacios()
            throws Exception {

        ejecutarFx(() -> {
            try {
                ControladorCatalogo controlador =
                        new ControladorCatalogo();

                TableColumn<RegistroCatalogo<?>, String> columna =
                        new TableColumn<>("Stock");

                Field campo = ControladorCatalogo.class
                        .getDeclaredField("colStock");
                campo.setAccessible(true);
                campo.set(controlador, columna);

                Method configurar = ControladorCatalogo.class
                        .getDeclaredMethod(
                                "configurarCeldaStock");
                configurar.setAccessible(true);
                configurar.invoke(controlador);

                TableCell<RegistroCatalogo<?>, String> celda =
                        columna.getCellFactory().call(columna);

                celda.updateTableRow(new TableRow<>());

                Method actualizar = celda.getClass()
                        .getDeclaredMethod(
                                "updateItem",
                                String.class,
                                boolean.class);

                actualizar.setAccessible(true);

                actualizar.invoke(celda, null, false);
                assertNull(celda.getText());
                assertNull(celda.getTooltip());

                actualizar.invoke(celda, "5", false);
                assertEquals("5", celda.getText());
                assertNull(celda.getTooltip());

                actualizar.invoke(celda, null, true);
                assertNull(celda.getText());
                assertNull(celda.getTooltip());

            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(
                        "Fallo en la prueba de la celda Stock", ex);
            }
        });
    }

    @Test
    void reglasDeColoresIncluyenValoresLimite() {

        assertEquals(
                EstiloAlertas.Nivel.CRITICO,
                EstiloAlertas.nivelStockCatalogo(-1, 10));

        assertEquals(
                EstiloAlertas.Nivel.NINGUNO,
                EstiloAlertas.nivelStockCatalogo(1, -1));

        assertEquals(
                EstiloAlertas.Nivel.NINGUNO,
                EstiloAlertas.nivelStockCatalogo(1, null));

        assertEquals(
                EstiloAlertas.Nivel.ADVERTENCIA,
                EstiloAlertas.nivelStockCatalogo(9, 10));

        assertEquals(
                EstiloAlertas.Nivel.NINGUNO,
                EstiloAlertas.nivelStockCatalogo(10, 10));

        assertTrue(EstiloAlertas.estiloFilaCatalogo(
                null, 10, 10).contains("#D8F2DF"));

        assertTrue(EstiloAlertas.estiloFilaCatalogo(
                "INACTIVO", 10, 10).contains("#ECEFF2"));

        assertTrue(EstiloAlertas.estiloFilaCatalogo(
                "ACTIVO", 0, 10).contains("#F0A8A8"));
    }
}
