package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.service.ResolucionNavegacionAlertas;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccionesAlertasControladores984Test {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);

        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        }

        assertTrue(listo.await(20, TimeUnit.SECONDS),
                "JavaFX no pudo inicializarse");

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

    private static SesionUsuario.DatosUsuario actor(
            String rol, int farmacia) {
        return new SesionUsuario.DatosUsuario(
                7, "Prueba", "p@test.local",
                rol, farmacia, "Farmacia");
    }

    private static AlertaInventario stock() {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                Categoria.ASEO,
                21, "SKU-21", "Producto",
                null, 1, 5, null, null,
                Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario vencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO,
                Categoria.MEDICAMENTO,
                21, "MED-21", "Medicamento",
                "LOT-21", 1, null,
                LocalDate.now().plusDays(3), 3,
                Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static void campo(
            Object objeto, String nombre, Object valor)
            throws Exception {
        Field campo = objeto.getClass()
                .getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    private static Object valor(
            Object objeto, String nombre) throws Exception {
        Field campo = objeto.getClass()
                .getDeclaredField(nombre);
        campo.setAccessible(true);
        return campo.get(objeto);
    }

    private static void ejecutar(
            Object objeto, String metodo) throws Exception {
        Method m = objeto.getClass()
                .getDeclaredMethod(metodo);
        m.setAccessible(true);
        m.invoke(objeto);
    }

    private static ControladorAlertas prepararAlerta(
            AlertaInventario alerta) throws Exception {
        ControladorAlertas controlador =
                new ControladorAlertas();

        TableView<AlertaInventario> tabla =
                new TableView<>();

        tabla.getItems().add(alerta);
        tabla.getSelectionModel().select(alerta);

        campo(controlador, "tablaAlertas", tabla);
        campo(controlador, "btnVerDetalle", new Button());
        campo(controlador, "btnGestionar", new Button());

        return controlador;
    }

    private static Button boton(
            ControladorAlertas controlador, String nombre)
            throws Exception {
        return (Button) valor(controlador, nombre);
    }

    @Test
    void botonGestionVisibleSoloParaGestorDeLaFarmacia()
            throws Exception {
        enFx(() -> {
            ControladorAlertas controlador =
                    prepararAlerta(stock());

            SesionUsuario.DatosUsuario admin =
                    actor("ADMINISTRADOR", 4);

            campo(controlador, "sesionVista", admin);

            try (MockedStatic<SesionUsuario> sesion =
                    mockStatic(SesionUsuario.class)) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(admin);

                ejecutar(controlador, "actualizarAcciones");

                Button gestionar =
                        boton(controlador, "btnGestionar");
                Button consultar =
                        boton(controlador, "btnVerDetalle");

                assertTrue(gestionar.isVisible());
                assertTrue(gestionar.isManaged());
                assertFalse(gestionar.isDisable());
                assertEquals("Reponer existencias",
                        gestionar.getText());
                assertFalse(consultar.isDisable());
            }
            return null;
        });
    }

    @Test
    void auxiliarSoloPuedeConsultarLaAlerta()
            throws Exception {
        enFx(() -> {
            ControladorAlertas controlador =
                    prepararAlerta(vencimiento());

            SesionUsuario.DatosUsuario auxiliar =
                    actor("AUXILIAR", 4);

            campo(controlador, "sesionVista", auxiliar);

            try (MockedStatic<SesionUsuario> sesion =
                    mockStatic(SesionUsuario.class)) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(auxiliar);

                ejecutar(controlador, "actualizarAcciones");

                Button gestionar =
                        boton(controlador, "btnGestionar");
                Button consultar =
                        boton(controlador, "btnVerDetalle");

                assertFalse(gestionar.isVisible());
                assertFalse(gestionar.isManaged());
                assertTrue(gestionar.isDisable());
                assertFalse(consultar.isDisable());
            }
            return null;
        });
    }

    @Test
    void sesionCambiadaDesactivaAcciones()
            throws Exception {
        enFx(() -> {
            ControladorAlertas controlador =
                    prepararAlerta(stock());

            campo(controlador, "sesionVista",
                    actor("ADMINISTRADOR", 4));

            try (MockedStatic<SesionUsuario> sesion =
                    mockStatic(SesionUsuario.class)) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(actor("ADMINISTRADOR", 4));

                ejecutar(controlador, "actualizarAcciones");

                assertTrue(boton(controlador,
                        "btnVerDetalle").isDisable());
                assertFalse(boton(controlador,
                        "btnGestionar").isVisible());
            }
            return null;
        });
    }

    @Test
    void medicamentoDeOtraFarmaciaNoSeGestiona()
            throws Exception {
        ControladorInventario controlador =
                new ControladorInventario();

        try (MockedStatic<SesionUsuario> sesion =
                mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(actor("FARMACEUTICO", 5));

            var destino = ResolucionNavegacionAlertas.resolver(
                    new AlertaInventario(
                            TipoAlerta.STOCK_MINIMO,
                            Categoria.MEDICAMENTO,
                            21, "MED-21", "Medicamento",
                            null, 1, 5, null, null,
                            Severidad.ADVERTENCIA,
                            null, null, null),
                    4, 4);

            assertThrows(SecurityException.class,
                    () -> controlador
                            .abrirGestionStockMedicamento(destino));

            assertNull(valor(controlador,
                    "idMedicamentoAlerta"));
        }
    }

    @Test
    void gestionaSoloElMedicamentoSeleccionadoSinEscribir()
            throws Exception {
        enFx(() -> {
            ControladorInventario controlador =
                    new ControladorInventario();

            Label estado = new Label();

            campo(controlador,
                    "lblLoteSeleccionadoInfo", estado);

            try (MockedStatic<SesionUsuario> sesion =
                    mockStatic(SesionUsuario.class)) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(actor("FARMACEUTICO", 4));

                var destino =
                        ResolucionNavegacionAlertas.resolver(
                                new AlertaInventario(
                                        TipoAlerta.STOCK_MINIMO,
                                        Categoria.MEDICAMENTO,
                                        21, "MED-21", "Medicamento",
                                        null, 1, 5, null, null,
                                        Severidad.ADVERTENCIA,
                                        null, null, null),
                                4, 4);

                controlador.abrirGestionStockMedicamento(destino);

                assertEquals(21,
                        valor(controlador, "idMedicamentoAlerta"));
                assertTrue(estado.getText().contains("Nuevo lote"));
            }
            return null;
        });
    }

    @Test
    void movimientosRechazanFarmaciaCruzada()
            throws Exception {
        ControladorMovimientos controlador =
                new ControladorMovimientos();

        campo(controlador, "idUsuario", 7);
        campo(controlador, "idFarmacia", 5);

        try (MockedStatic<SesionUsuario> sesion =
                mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(actor("FARMACEUTICO", 5));

            var destino =
                    ResolucionNavegacionAlertas.resolver(
                            stock(), 4, 4);

            assertThrows(SecurityException.class,
                    () -> controlador.abrirDesdeAlerta(destino));
        }
    }
}
