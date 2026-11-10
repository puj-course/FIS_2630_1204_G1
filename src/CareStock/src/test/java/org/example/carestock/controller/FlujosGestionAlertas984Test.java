package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import org.example.carestock.CareStockApp;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.model.Lote;
import org.example.carestock.service.ServicioCrudLotes;
import org.example.carestock.service.ServicioCrudLotes.MedicamentoOpcion;
import org.example.carestock.service.ServicioCrudLotes.UbicacionOpcion;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlujosGestionAlertas984Test {

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch iniciado = new CountDownLatch(1);
        try {
            Platform.startup(iniciado::countDown);
        } catch (IllegalStateException yaIniciado) {
            iniciado.countDown();
        }
        assertTrue(iniciado.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T conSesion(
            String rol, Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(() -> {
            Field sesion = SesionUsuario.class.getDeclaredField("actual");
            sesion.setAccessible(true);
            Object anterior = sesion.get(null);

            try {
                sesion.set(null, actor(rol));
                return accion.call();
            } finally {
                sesion.set(null, anterior);
            }
        });

        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static SesionUsuario.DatosUsuario actor(String rol) {
        return new SesionUsuario.DatosUsuario(
                7, "QA", "qa@test.local", rol, 4, "Farmacia QA");
    }

    private static void poner(
            Object objeto, String nombre, Object valor) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(
            Object objeto, String nombre) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(
            Object objeto, String nombre) throws Exception {
        Method metodo = objeto.getClass().getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(objeto);
    }

    private static AlertaInventario stock() {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                Categoria.ASEO,
                21, "ASEO-21", "Producto Aseo",
                null, 1, 5, null, null,
                Severidad.ADVERTENCIA, null, null, null);
    }

    private static AlertaInventario vencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO,
                Categoria.MEDICAMENTO,
                31, "MED-31", "Medicamento",
                "L-31", 2, null,
                LocalDate.now().plusDays(5), 5,
                Severidad.CRITICA, null, null, null);
    }

    private static ControladorAlertas prepararAlerta(
            AlertaInventario alerta,
            SesionUsuario.DatosUsuario vista) throws Exception {

        ControladorAlertas c = new ControladorAlertas();
        TableView<AlertaInventario> tabla = new TableView<>();

        if (alerta != null) {
            tabla.getItems().add(alerta);
            tabla.getSelectionModel().select(alerta);
        }

        poner(c, "tablaAlertas", tabla);
        poner(c, "btnGestionar", new Button());
        poner(c, "btnVerDetalle", new Button());
        poner(c, "lblEstado", new Label());
        poner(c, "sesionVista", vista);

        return c;
    }

    @Test
    void gestionDeStockYVencimientoNavegaSoloCuandoAutorizado()
            throws Exception {

        conSesion("FARMACEUTICO", () -> {
            CareStockApp app = mock(CareStockApp.class);

            try (MockedStatic<CareStockApp> acceso =
                    mockStatic(CareStockApp.class)) {

                acceso.when(CareStockApp::getInstance)
                        .thenReturn(app);

                SesionUsuario.DatosUsuario vista =
                        SesionUsuario.actual();

                ControladorAlertas c =
                        prepararAlerta(stock(), vista);

                ejecutar(c, "actualizarAcciones");

                Button boton = leer(c, "btnGestionar");

                assertTrue(boton.isVisible());
                assertEquals(
                        "Reponer existencias", boton.getText());

                ejecutar(c, "onGestionar");

                verify(app).mostrarGestionAlerta(stock(), 4);

                ControladorAlertas lotes =
                        prepararAlerta(vencimiento(), vista);

                ejecutar(lotes, "actualizarAcciones");

                Button gestionarLote =
                        leer(lotes, "btnGestionar");

                assertEquals(
                        "Gestionar lote", gestionarLote.getText());

                ejecutar(lotes, "onGestionar");

                verify(app).mostrarGestionAlerta(vencimiento(), 4);
            }

            return null;
        });
    }

    @Test
    void auxiliarNoPuedeEjecutarGestionAunqueInvoqueElEvento()
            throws Exception {

        conSesion("AUXILIAR", () -> {
            CareStockApp app = mock(CareStockApp.class);

            try (MockedStatic<CareStockApp> acceso =
                    mockStatic(CareStockApp.class)) {

                acceso.when(CareStockApp::getInstance)
                        .thenReturn(app);

                ControladorAlertas c =
                        prepararAlerta(stock(), SesionUsuario.actual());

                ejecutar(c, "onGestionar");

                Label estado = leer(c, "lblEstado");

                assertTrue(
                        estado.getText().contains("No fue posible"));

                Button boton = leer(c, "btnGestionar");
                assertFalse(boton.isVisible());

                verifyNoInteractions(app);
            }

            return null;
        });
    }

    @Test
    void cambioDeSesionYAlertaNoSeleccionadaImpidenGestion()
            throws Exception {

        conSesion("ADMINISTRADOR", () -> {
            CareStockApp app = mock(CareStockApp.class);

            try (MockedStatic<CareStockApp> acceso =
                    mockStatic(CareStockApp.class)) {

                acceso.when(CareStockApp::getInstance)
                        .thenReturn(app);

                ControladorAlertas c =
                        prepararAlerta(stock(), actor("ADMINISTRADOR"));

                ejecutar(c, "onGestionar");

                Label estado = leer(c, "lblEstado");
                assertTrue(
                        estado.getText().contains("sesión ha cambiado"));

                Button boton = leer(c, "btnGestionar");
                assertTrue(boton.isDisable());

                verifyNoInteractions(app);

                ControladorAlertas sinSeleccion =
                        prepararAlerta(null, SesionUsuario.actual());

                ejecutar(sinSeleccion, "onGestionar");

                Label estadoSinSeleccion =
                        leer(sinSeleccion, "lblEstado");

                assertTrue(estadoSinSeleccion.getText()
                        .contains("No fue posible"));

                verifyNoInteractions(app);
            }

            return null;
        });
    }

    @Test
    void crearLoteRespetaMedicamentoPreseleccionado()
            throws Exception {

        conSesion("FARMACEUTICO", () -> {
            ControladorInventario controlador =
                    new ControladorInventario();

            TableView<Lote> tabla = new TableView<>();
            new Scene(new StackPane(tabla), 400, 300);

            poner(controlador, "tablaLotes", tabla);

            try (MockedConstruction<GestorLotesDialogo> gestores =
                    mockConstruction(GestorLotesDialogo.class)) {

                poner(controlador, "idMedicamentoAlerta", 21);
                ejecutar(controlador, "onCrearLote");

                assertEquals(1, gestores.constructed().size());

                verify(gestores.constructed().get(0))
                        .crearParaMedicamento(21);

                verify(gestores.constructed().get(0), never())
                        .crear();

                poner(controlador, "idMedicamentoAlerta", null);
                ejecutar(controlador, "onCrearLote");

                assertEquals(2, gestores.constructed().size());

                verify(gestores.constructed().get(1)).crear();
            }

            return null;
        });
    }

    @Test
    void dialogoDeLotesValidaMedicamentoYPermiteCancelarSinEscribir()
            throws Exception {

        conSesion("FARMACEUTICO", () -> {
            try (
                MockedConstruction<ServicioCrudLotes> servicios =
                    mockConstruction(
                        ServicioCrudLotes.class,
                        (mock, ctx) -> {
                            when(mock.medicamentos()).thenReturn(List.of(
                                    new MedicamentoOpcion(
                                            21, "Medicamento 21")));

                            when(mock.ubicaciones()).thenReturn(List.of(
                                    new UbicacionOpcion(3, "A", "1")));
                        });

                MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class);

                MockedConstruction<Dialog> dialogos =
                    mockConstruction(
                        Dialog.class,
                        (mock, ctx) -> {
                            when(mock.getDialogPane())
                                    .thenReturn(new DialogPane());

                            when(mock.showAndWait())
                                    .thenReturn(Optional.empty());
                        })
            ) {
                GestorLotesDialogo gestor =
                        new GestorLotesDialogo(null);

                assertThrows(
                        IllegalArgumentException.class,
                        () -> gestor.crearParaMedicamento(0));

                assertFalse(gestor.crearParaMedicamento(99));

                assertEquals(1, alertas.constructed().size());
                assertEquals(0, dialogos.constructed().size());

                assertFalse(gestor.crearParaMedicamento(21));
                assertFalse(gestor.crear());

                Lote existente = mock(Lote.class);

                when(existente.getIdUbicacion()).thenReturn(3);
                when(existente.getNumeroLote()).thenReturn("LOTE-3");

                when(existente.getFechaVencimiento())
                        .thenReturn(LocalDate.now().plusDays(10));

                assertFalse(gestor.editar(existente));

                assertEquals(3, dialogos.constructed().size());

                ServicioCrudLotes servicio =
                        servicios.constructed().get(0);

                verify(servicio, never()).crear(
                        anyInt(), anyString(), anyInt(),
                        any(), anyInt());

                verify(servicio, never()).editar(
                        any(), anyString(), any(), anyInt());
            }

            return null;
        });
    }
}
