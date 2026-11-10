package org.example.carestock;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.example.carestock.controller.ControladorInventario;
import org.example.carestock.controller.ControladorMovimientos;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CareStockAppGestion984Test {

    private static final int FARMACIA = 8;

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
            String rol, Integer farmacia, Callable<T> accion)
            throws Exception {

        return enFx(() -> {
            Field campo = SesionUsuario.class
                    .getDeclaredField("actual");
            campo.setAccessible(true);
            Object anterior = campo.get(null);

            try {
                campo.set(null, rol == null ? null :
                        new SesionUsuario.DatosUsuario(
                                7, "Usuario QA", "qa@test.local",
                                rol, farmacia, "Farmacia QA"));
                return accion.call();
            } finally {
                campo.set(null, anterior);
            }
        });
    }

    private static AlertaInventario stock(
            Categoria categoria) {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                categoria,
                17, "COD-17", "Producto 17",
                null, 2, 5, null, null,
                Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario vencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO,
                Categoria.MEDICAMENTO,
                44, "MED-44", "Medicamento 44",
                "LOT-44", 3, null,
                LocalDate.now().plusDays(5), 5,
                Severidad.CRITICA,
                null, null, null);
    }

    private static CareStockApp preparar(Stage stage)
            throws Exception {
        CareStockApp app = new CareStockApp();
        Field campo = CareStockApp.class
                .getDeclaredField("primaryStage");
        campo.setAccessible(true);
        campo.set(app, stage);
        return app;
    }

    private static URL gestionar(
            AlertaInventario alerta, Object controlador)
            throws Exception {

        Stage stage = mock(Stage.class);
        CareStockApp app = preparar(stage);
        AtomicReference<URL> ruta = new AtomicReference<>();

        try (MockedConstruction<FXMLLoader> cargadores =
                mockConstruction(FXMLLoader.class,
                        (loader, contexto) -> {
                            ruta.set((URL) contexto.arguments().get(0));
                            when(loader.load()).thenReturn(new StackPane());
                            when(loader.getController()).thenReturn(controlador);
                        })) {

            app.mostrarGestionAlerta(alerta, FARMACIA);

            assertEquals(1, cargadores.constructed().size());
            verify(stage).setScene(any(Scene.class));
        }

        assertNotNull(ruta.get());
        return ruta.get();
    }

    @ParameterizedTest
    @EnumSource(value = Categoria.class,
            names = {"ASEO", "MATERNIDAD"})
    void reponeAseoYMaternidad(Categoria categoria)
            throws Exception {

        conSesion("FARMACEUTICO", FARMACIA, () -> {
            ControladorMovimientos controlador =
                    mock(ControladorMovimientos.class);

            URL ruta = gestionar(stock(categoria), controlador);

            assertTrue(ruta.toString().endsWith(
                    "/movimientos-view.fxml"));

            var captor = org.mockito.ArgumentCaptor
                    .forClass(Destino.class);

            verify(controlador).abrirDesdeAlerta(captor.capture());

            assertEquals(categoria, captor.getValue().categoria());
            assertEquals(17, captor.getValue().idReferencia());
            assertEquals(FARMACIA, captor.getValue().idFarmacia());
            return null;
        });
    }

    @Test
    void reponeMedicamentoMedianteGestionDeLotes()
            throws Exception {

        conSesion("ADMINISTRADOR", FARMACIA, () -> {
            ControladorInventario controlador =
                    mock(ControladorInventario.class);

            URL ruta = gestionar(
                    stock(Categoria.MEDICAMENTO), controlador);

            assertTrue(ruta.toString().endsWith(
                    "/inventario-view.fxml"));

            verify(controlador).abrirGestionStockMedicamento(
                    argThat(d -> d.idReferencia() == 17
                            && d.idFarmacia() == FARMACIA
                            && d.categoria() == Categoria.MEDICAMENTO));
            return null;
        });
    }

    @Test
    void vencimientoDirigeAlLoteCorrecto()
            throws Exception {

        conSesion("FARMACEUTICO", FARMACIA, () -> {
            ControladorInventario controlador =
                    mock(ControladorInventario.class);

            URL ruta = gestionar(vencimiento(), controlador);

            assertTrue(ruta.toString().endsWith(
                    "/inventario-view.fxml"));

            verify(controlador).abrirDesdeAlerta(
                    argThat(d -> d.idReferencia() == 44
                            && "LOT-44".equals(d.numeroLote())
                            && d.idFarmacia() == FARMACIA));
            return null;
        });
    }

    @Test
    void impideGestionSinSesion() throws Exception {
        conSesion(null, null, () -> {
            CareStockApp app = new CareStockApp();

            assertThrows(SecurityException.class,
                    () -> app.mostrarGestionAlerta(
                            stock(Categoria.ASEO), FARMACIA));
            return null;
        });
    }

    @Test
    void impideGestionAlAuxiliar() throws Exception {
        conSesion("AUXILIAR", FARMACIA, () -> {
            CareStockApp app = new CareStockApp();

            assertThrows(SecurityException.class,
                    () -> app.mostrarGestionAlerta(
                            stock(Categoria.ASEO), FARMACIA));
            return null;
        });
    }

    @Test
    void impideGestionEntreFarmacias() throws Exception {
        conSesion("FARMACEUTICO", FARMACIA, () -> {
            CareStockApp app = new CareStockApp();

            assertThrows(SecurityException.class,
                    () -> app.mostrarGestionAlerta(
                            stock(Categoria.MEDICAMENTO), 99));
            return null;
        });
    }
}
