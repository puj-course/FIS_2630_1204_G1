package org.example.carestock;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.example.carestock.controller.ControladorInventario;
import org.example.carestock.controller.ConsultaInventarioController;
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
import java.net.URL;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CareStockAppNavegacionAlertasTest {

    private static final int FARMACIA = 8;

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

    private static CareStockApp aplicacion(Stage stage)
            throws Exception {

        CareStockApp app = new CareStockApp();

        Field campo = CareStockApp.class
                .getDeclaredField("primaryStage");

        campo.setAccessible(true);
        campo.set(app, stage);

        return app;
    }

    private static AlertaInventario stock(
            Categoria categoria) {

        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                categoria,
                17,
                "COD-17",
                "Producto 17",
                null,
                2,
                5,
                null,
                null,
                Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario vencimiento(
            String numeroLote) {

        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO,
                Categoria.MEDICAMENTO,
                44,
                "MED-44",
                "Medicamento 44",
                numeroLote,
                3,
                null,
                LocalDate.now().plusDays(5),
                5,
                Severidad.CRITICA,
                null, null, null);
    }

    private static URL ejecutarNavegacion(
            AlertaInventario alerta,
            Object controladorDestino,
            boolean reutilizarEscena)
            throws Exception {

        Stage stage = mock(Stage.class);
        Scene escenaOriginal = null;

        if (reutilizarEscena) {
            escenaOriginal = new Scene(new StackPane());
            when(stage.getScene()).thenReturn(escenaOriginal);
        }

        CareStockApp app = aplicacion(stage);
        AtomicReference<URL> ruta = new AtomicReference<>();

        try (MockedConstruction<FXMLLoader> cargadores =
                mockConstruction(
                        FXMLLoader.class,
                        (loader, contexto) -> {
                            ruta.set((URL)
                                    contexto.arguments().get(0));

                            when(loader.load())
                                    .thenReturn(new StackPane());

                            when(loader.getController())
                                    .thenReturn(controladorDestino);
                        })) {

            app.mostrarDetalleAlerta(alerta, FARMACIA);

            assertEquals(1, cargadores.constructed().size());

            if (reutilizarEscena) {
                assertNotNull(escenaOriginal);
                assertNotNull(escenaOriginal.getRoot());

                verify(stage, never())
                        .setScene(any(Scene.class));
            } else {
                verify(stage).setScene(any(Scene.class));
            }
        }

        return ruta.get();
    }

    @ParameterizedTest
    @EnumSource(Categoria.class)
    void stockAbreConsultaUnificada(Categoria categoria)
            throws Exception {

        conSesion(FARMACIA, () -> {
            ConsultaInventarioController controlador =
                    mock(ConsultaInventarioController.class);

            URL ruta = ejecutarNavegacion(
                    stock(categoria), controlador, false);

            assertNotNull(ruta);
            assertTrue(ruta.toString().endsWith(
                    "/consulta-inventario-view.fxml"));

            var captor = org.mockito.ArgumentCaptor
                    .forClass(Destino.class);

            verify(controlador)
                    .abrirDesdeAlerta(captor.capture());

            assertEquals(
                    TipoAlerta.STOCK_MINIMO,
                    captor.getValue().tipo());

            assertEquals(categoria, captor.getValue().categoria());
            assertEquals(17, captor.getValue().idReferencia());
            assertEquals(FARMACIA, captor.getValue().idFarmacia());

            return null;
        });
    }

    @Test
    void vencimientoAbreGestionDeLotes()
            throws Exception {

        conSesion(FARMACIA, () -> {
            ControladorInventario controlador =
                    mock(ControladorInventario.class);

            URL ruta = ejecutarNavegacion(
                    vencimiento("LOT-44"), controlador, false);

            assertNotNull(ruta);
            assertTrue(ruta.toString().endsWith(
                    "/inventario-view.fxml"));

            var captor = org.mockito.ArgumentCaptor
                    .forClass(Destino.class);

            verify(controlador)
                    .abrirDesdeAlerta(captor.capture());

            assertEquals(
                    TipoAlerta.VENCIMIENTO,
                    captor.getValue().tipo());

            assertEquals(44, captor.getValue().idReferencia());
            assertEquals("LOT-44", captor.getValue().numeroLote());
            assertEquals(FARMACIA, captor.getValue().idFarmacia());

            return null;
        });
    }

    @Test
    void conservaEscenaExistenteEnLaNavegacion()
            throws Exception {

        conSesion(FARMACIA, () -> {
            ConsultaInventarioController controlador =
                    mock(ConsultaInventarioController.class);

            URL ruta = ejecutarNavegacion(
                    stock(Categoria.ASEO), controlador, true);

            assertNotNull(ruta);

            verify(controlador)
                    .abrirDesdeAlerta(any(Destino.class));

            return null;
        });
    }

    @Test
    void noPermiteNavegarSinSesion()
            throws Exception {

        conSesion(null, () -> {
            CareStockApp app = spy(new CareStockApp());

            doNothing().when(app).mostrarLogin();

            app.mostrarDetalleAlerta(
                    stock(Categoria.ASEO), FARMACIA);

            verify(app).mostrarLogin();

            return null;
        });
    }

    @Test
    void rechazaFarmaciaDistinta()
            throws Exception {

        conSesion(FARMACIA, () -> {
            CareStockApp app = new CareStockApp();

            assertThrows(IllegalStateException.class,
                    () -> app.mostrarDetalleAlerta(
                            stock(Categoria.ASEO), 99));

            return null;
        });
    }

    @Test
    void rechazaVencimientoSinIdentificacionDeLote()
            throws Exception {

        conSesion(FARMACIA, () -> {
            CareStockApp app = new CareStockApp();

            assertThrows(IllegalArgumentException.class,
                    () -> app.mostrarDetalleAlerta(
                            vencimiento(""), FARMACIA));

            return null;
        });
    }
}
