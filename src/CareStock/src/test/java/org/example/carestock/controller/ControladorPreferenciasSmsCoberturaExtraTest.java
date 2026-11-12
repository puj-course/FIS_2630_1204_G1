package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.sms.ServicioDirectorioSms;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorPreferenciasSmsCoberturaExtraTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    7, "QA", "qa@test.local",
                    "ADMINISTRADOR", 8, "Farmacia QA");

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
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void campo(
            Object objeto, String nombre, Object valor)
            throws Exception {
        Field f = objeto.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        f.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(
            Object objeto, String nombre) throws Exception {
        Field f = objeto.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        return (T) f.get(objeto);
    }

    private static Object llamar(
            Object objeto, String metodo,
            Class<?>[] tipos, Object... args)
            throws Exception {
        Method m = objeto.getClass()
                .getDeclaredMethod(metodo, tipos);
        m.setAccessible(true);
        return m.invoke(objeto, args);
    }

    private static Object llamar(
            Object objeto, String metodo) throws Exception {
        return llamar(objeto, metodo, new Class<?>[0]);
    }

    private static ServicioDirectorioSms.Destinatario
    destinatario(boolean configurado) {
        return new ServicioDirectorioSms.Destinatario(
                12, "Analista", "analista@test.local",
                configurado ? "+573001234567" : null,
                configurado, configurado, true, false);
    }

    private static ControladorPreferenciasSms formulario()
            throws Exception {
        var c = new ControladorPreferenciasSms();

        campo(c, "lblSeleccion", new Label());
        campo(c, "lblEstado", new Label());
        campo(c, "tablaDestinatarios",
                new TableView<ServicioDirectorioSms.Destinatario>());
        campo(c, "txtTelefono", new TextField());
        campo(c, "txtInicio", new TextField());
        campo(c, "txtFin", new TextField());
        campo(c, "chkAutorizado", new CheckBox());
        campo(c, "chkConfirmacion", new CheckBox());
        campo(c, "chkStock", new CheckBox());
        campo(c, "chkVencimientos", new CheckBox());
        campo(c, "chkAccesos", new CheckBox());
        campo(c, "cmbZona", new ComboBox<String>());
        campo(c, "btnGuardar", new Button());
        campo(c, "btnDeshabilitar", new Button());
        campo(c, "idSesion", 7);

        return c;
    }

    @Test
    void limpiarFormularioReiniciaConsentimientoYHorario()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            ((TextField) leer(c, "txtTelefono"))
                    .setText("+573001234567");
            ((TextField) leer(c, "txtInicio"))
                    .setText("08:00");
            ((CheckBox) leer(c, "chkStock"))
                    .setSelected(true);
            ((CheckBox) leer(c, "chkConfirmacion"))
                    .setSelected(true);

            llamar(c, "limpiarFormulario");

            assertEquals("",
                    ((TextField) leer(c, "txtTelefono"))
                            .getText());
            assertEquals("",
                    ((TextField) leer(c, "txtInicio"))
                            .getText());
            assertFalse(
                    ((CheckBox) leer(c, "chkStock"))
                            .isSelected());
            assertFalse(
                    ((CheckBox) leer(c, "chkConfirmacion"))
                            .isSelected());
            assertEquals("America/Bogota",
                    ((ComboBox<?>) leer(c, "cmbZona"))
                            .getValue());
            return null;
        });
    }

    @Test
    void destinatarioNoConfiguradoNoDisparaConsulta()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            llamar(c, "cargarDetalle",
                    new Class<?>[]{
                            ServicioDirectorioSms.Destinatario.class
                    },
                    destinatario(false));

            assertTrue(
                    ((Label) leer(c, "lblSeleccion"))
                            .getText().contains("Analista"));
            assertTrue(
                    ((Label) leer(c, "lblEstado"))
                            .getText().contains("sin configurar"));
            return null;
        });
    }

    @Test
    void noCambiaDetalleDuranteOperacion()
            throws Exception {
        enFx(() -> {
            var c = formulario();
            campo(c, "ocupado", true);

            llamar(c, "cargarDetalle",
                    new Class<?>[]{
                            ServicioDirectorioSms.Destinatario.class
                    },
                    destinatario(false));

            assertEquals("",
                    ((Label) leer(c, "lblSeleccion"))
                            .getText());
            return null;
        });
    }

    @Test
    void guardarSinSeleccionSeRechaza()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            llamar(c, "onGuardar");

            assertTrue(
                    ((Label) leer(c, "lblEstado"))
                            .getText().contains("Selecciona"));
            assertFalse((Boolean) leer(c, "ocupado"));
            return null;
        });
    }

    @Test
    void guardarTelefonoInvalidoNoAccedeALaRed()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            TableView<ServicioDirectorioSms.Destinatario>
                    tabla = leer(c, "tablaDestinatarios");

            tabla.getItems().add(destinatario(false));
            tabla.getSelectionModel().selectFirst();

            ((TextField) leer(c, "txtTelefono"))
                    .setText("invalido");

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                llamar(c, "onGuardar");

                assertEquals(1,
                        alertas.constructed().size());
                verify(alertas.constructed().get(0))
                        .showAndWait();
                assertFalse((Boolean) leer(c, "ocupado"));
            }
            return null;
        });
    }

    @Test
    void deshabilitarSinConfiguracionSeRechaza()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            TableView<ServicioDirectorioSms.Destinatario>
                    tabla = leer(c, "tablaDestinatarios");

            tabla.getItems().add(destinatario(false));
            tabla.getSelectionModel().selectFirst();

            llamar(c, "onDeshabilitar");

            assertTrue(
                    ((Label) leer(c, "lblEstado"))
                            .getText()
                            .contains("configuracion activa"));
            return null;
        });
    }

    @Test
    void deshabilitarCanceladoNoRealizaOperacion()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            TableView<ServicioDirectorioSms.Destinatario>
                    tabla = leer(c, "tablaDestinatarios");

            tabla.getItems().add(destinatario(true));
            tabla.getSelectionModel().selectFirst();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class,
                            (alerta, contexto) ->
                                    when(alerta.showAndWait())
                                            .thenReturn(
                                                    Optional.of(
                                                            ButtonType.NO)))) {

                llamar(c, "onDeshabilitar");

                assertEquals(1,
                        alertas.constructed().size());
                assertFalse((Boolean) leer(c, "ocupado"));
            }
            return null;
        });
    }

    @Test
    void sesionVigenteRequiereAdministradorCorrecto()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            try (var sesion =
                    mockStatic(SesionUsuario.class)) {

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(ADMIN);

                assertTrue((Boolean)
                        llamar(c, "sesionVigente"));

                campo(c, "idSesion", 99);

                assertFalse((Boolean)
                        llamar(c, "sesionVigente"));

                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(false);

                assertFalse((Boolean)
                        llamar(c, "sesionVigente"));
            }
            return null;
        });
    }

    @Test
    void formateaHorarioSinPerderCerosIniciales()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            assertEquals("06:03",
                    llamar(c, "hora",
                            new Class<?>[]{LocalTime.class},
                            LocalTime.of(6, 3)));

            assertEquals("",
                    llamar(c, "hora",
                            new Class<?>[]{LocalTime.class},
                            (Object) null));

            return null;
        });
    }
}
