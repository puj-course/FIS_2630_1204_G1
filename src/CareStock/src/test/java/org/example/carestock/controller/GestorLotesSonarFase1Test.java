package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.carestock.model.Lote;
import org.example.carestock.service.ServicioCrudLotes;
import org.example.carestock.service.ServicioCrudLotes.MedicamentoOpcion;
import org.example.carestock.service.ServicioCrudLotes.UbicacionOpcion;
import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestorLotesSonarFase1Test {
    @BeforeAll
    static void fxInicializado() throws Exception {
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

    private static GestorLotesDialogo gestor(ServicioCrudLotes servicio)
            throws Exception {
        GestorLotesDialogo gestor = new GestorLotesDialogo(null);
        Field campo = GestorLotesDialogo.class.getDeclaredField("servicio");
        campo.setAccessible(true);
        campo.set(gestor, servicio);
        return gestor;
    }

    @Test
    void formularioNuevoTieneVencimientoFuturoYSePuedeCancelar() throws Exception {
        fx(() -> {
            ServicioCrudLotes servicio = mock(ServicioCrudLotes.class);
            when(servicio.medicamentos()).thenReturn(List.of(new MedicamentoOpcion(10,"Medicamento")));
            when(servicio.ubicaciones()).thenReturn(List.of(new UbicacionOpcion(5,"A","1")));
            GestorLotesDialogo gestor = gestor(servicio);
            DialogPane pane = new DialogPane();
            try (MockedConstruction<Dialog> dialogos = mockConstruction(Dialog.class,
                    (dialogo, ctx) -> {
                        when(dialogo.getDialogPane()).thenReturn(pane);
                        when(dialogo.showAndWait()).thenReturn(Optional.empty());
                    })) {
                assertFalse(gestor.crear());
                assertEquals(1, dialogos.constructed().size());
                GridPane form = (GridPane) pane.getContent();
                DatePicker fecha = (DatePicker) form.getChildren().stream()
                        .filter(DatePicker.class::isInstance).findFirst().orElseThrow();
                assertEquals(FechaNegocio.hoy().plusMonths(12), fecha.getValue());
            }
            verify(servicio).medicamentos();
            verify(servicio).ubicaciones();
            return null;
        });
    }

    @Test
    void formularioEdicionConservaFechaOriginalYSePuedeCancelar() throws Exception {
        fx(() -> {
            ServicioCrudLotes servicio = mock(ServicioCrudLotes.class);
            when(servicio.ubicaciones()).thenReturn(List.of(new UbicacionOpcion(5,"A","1")));
            GestorLotesDialogo gestor = gestor(servicio);
            Lote lote = new Lote("L-10", 10, 4, FechaNegocio.hoy().plusDays(3), 5,
                    "DISPONIBLE", 7);
            lote.setIdLote(9);
            DialogPane pane = new DialogPane();
            try (MockedConstruction<Dialog> dialogos = mockConstruction(Dialog.class,
                    (dialogo, ctx) -> {
                        when(dialogo.getDialogPane()).thenReturn(pane);
                        when(dialogo.showAndWait()).thenReturn(Optional.empty());
                    })) {
                assertFalse(gestor.editar(lote));
                GridPane form = (GridPane) pane.getContent();
                DatePicker fecha = (DatePicker) form.getChildren().stream()
                        .filter(DatePicker.class::isInstance).findFirst().orElseThrow();
                assertEquals(lote.getFechaVencimiento(), fecha.getValue());
            }
            verify(servicio, never()).editar(any(), anyString(), any(), anyInt());
            return null;
        });
    }

    @Test
    void noPermiteRetirarLoteConExistenciasYVigente() throws Exception {
        fx(() -> {
            ServicioCrudLotes servicio = mock(ServicioCrudLotes.class);
            Lote lote = new Lote("ACTIVO", 10, 3, FechaNegocio.hoy().plusDays(4),
                    5, "DISPONIBLE", 7);
            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                assertFalse(gestor(servicio).retirar(lote));
                assertEquals(1, alertas.constructed().size());
            }
            verifyNoInteractions(servicio);
            return null;
        });
    }

    @Test
    void permiteCancelarRetiroDeLoteSinExistencias() throws Exception {
        fx(() -> {
            ServicioCrudLotes servicio = mock(ServicioCrudLotes.class);
            Lote lote = new Lote("AGOTADO", 10, 0, FechaNegocio.hoy().minusDays(2),
                    5, "AGOTADO", 7);
            try (MockedConstruction<Alert> alertas = mockConstruction(Alert.class,
                    (alerta, ctx) -> when(alerta.showAndWait())
                            .thenReturn(Optional.of(ButtonType.NO)))) {
                assertFalse(gestor(servicio).retirar(lote));
                assertEquals(1, alertas.constructed().size());
            }
            verifyNoInteractions(servicio);
            return null;
        });
    }
}
