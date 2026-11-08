package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.example.carestock.CareStockApp;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.sms.ServicioDirectorioSms;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorPreferenciasSmsCasosLimiteCoverage87Test {

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

    private static <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void poner(Object obj, String nombre, Object valor)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        f.set(obj, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object obj, String nombre)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    private static Object llamar(Object obj, String nombre)
            throws Exception {
        Method m = obj.getClass().getDeclaredMethod(nombre);
        m.setAccessible(true);
        return m.invoke(obj);
    }

    private static ServicioDirectorioSms.Destinatario destinatario(
            boolean configurado) {
        return new ServicioDirectorioSms.Destinatario(
            12, "Analista QA", "analista@test.local",
            configurado ? "+573001234567" : null,
            configurado, configurado, true, false);
    }

    private static ControladorPreferenciasSms formulario()
            throws Exception {
        var c = new ControladorPreferenciasSms();

        poner(c, "lblSeleccion", new Label());
        poner(c, "lblEstado", new Label());
        poner(c, "tablaDestinatarios",
            new TableView<ServicioDirectorioSms.Destinatario>());
        poner(c, "txtTelefono", new TextField());
        poner(c, "txtInicio", new TextField());
        poner(c, "txtFin", new TextField());
        poner(c, "chkAutorizado", new CheckBox());
        poner(c, "chkConfirmacion", new CheckBox());
        poner(c, "chkStock", new CheckBox());
        poner(c, "chkVencimientos", new CheckBox());
        poner(c, "cmbZona", new ComboBox<String>());
        poner(c, "btnGuardar", new Button());
        poner(c, "btnDeshabilitar", new Button());
        poner(c, "idSesion", 7);

        ComboBox<String> zona = leer(c, "cmbZona");
        zona.setValue("America/Bogota");

        return c;
    }

    private static void seleccionar(
            ControladorPreferenciasSms c, boolean configurado)
            throws Exception {
        TableView<ServicioDirectorioSms.Destinatario> tabla =
            leer(c, "tablaDestinatarios");
        tabla.getItems().add(destinatario(configurado));
        tabla.getSelectionModel().selectFirst();
    }

    @Test
    void seleccionInicialEsNula() throws Exception {
        fx(() -> {
            var c = formulario();
            assertNull(llamar(c, "seleccionado"));
            return null;
        });
    }

    @Test
    void seleccionDevuelveUsuarioCorrecto() throws Exception {
        fx(() -> {
            var c = formulario();
            seleccionar(c, true);

            var seleccionado = (ServicioDirectorioSms.Destinatario)
                llamar(c, "seleccionado");

            assertEquals(12, seleccionado.idUsuario());
            assertEquals("Analista QA", seleccionado.nombre());
            return null;
        });
    }

    @Test
    void actualizarMientrasEstaOcupadoNoCambiaEstado()
            throws Exception {
        fx(() -> {
            var c = formulario();
            poner(c, "ocupado", true);

            Label estado = leer(c, "lblEstado");
            estado.setText("Operacion en curso");

            llamar(c, "onActualizar");

            assertEquals("Operacion en curso", estado.getText());
            assertTrue((Boolean) leer(c, "ocupado"));
            return null;
        });
    }

    @Test
    void guardarMientrasEstaOcupadoNoIniciaOtraOperacion()
            throws Exception {
        fx(() -> {
            var c = formulario();
            seleccionar(c, true);
            poner(c, "ocupado", true);

            llamar(c, "onGuardar");

            Label estado = leer(c, "lblEstado");
            assertEquals("Selecciona un destinatario.",
                estado.getText());
            assertTrue((Boolean) leer(c, "ocupado"));
            return null;
        });
    }

    @Test
    void deshabilitarMientrasEstaOcupadoNoAbreConfirmacion()
            throws Exception {
        fx(() -> {
            var c = formulario();
            seleccionar(c, true);
            poner(c, "ocupado", true);

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {
                llamar(c, "onDeshabilitar");

                assertTrue(alertas.constructed().isEmpty());
                assertTrue(((Label) leer(c, "lblEstado"))
                    .getText().contains("configuracion activa"));
            }
            return null;
        });
    }

    @Test
    void deshabilitarSinSeleccionNoAbreConfirmacion()
            throws Exception {
        fx(() -> {
            var c = formulario();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {
                llamar(c, "onDeshabilitar");

                assertTrue(alertas.constructed().isEmpty());
                assertTrue(((Label) leer(c, "lblEstado"))
                    .getText().contains("Selecciona"));
            }
            return null;
        });
    }

    @Test
    void guardarAutorizacionExigeConsentimiento()
            throws Exception {
        fx(() -> {
            var c = formulario();
            seleccionar(c, false);

            ((TextField) leer(c, "txtTelefono"))
                .setText("+573001234567");
            ((CheckBox) leer(c, "chkAutorizado"))
                .setSelected(true);
            ((CheckBox) leer(c, "chkConfirmacion"))
                .setSelected(false);

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {
                llamar(c, "onGuardar");

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                assertFalse((Boolean) leer(c, "ocupado"));
            }
            return null;
        });
    }

    @ParameterizedTest
    @CsvSource({
        "08:00,,America/Bogota",
        "08:00,08:00,America/Bogota",
        "hora,09:00,America/Bogota",
        "08:00,09:00,Zona/Imposible"
    })
    void rechazaHorariosOZonasInvalidas(
            String inicio, String fin, String zona)
            throws Exception {
        fx(() -> {
            var c = formulario();
            seleccionar(c, false);

            ((TextField) leer(c, "txtTelefono"))
                .setText("+573001234567");
            ((TextField) leer(c, "txtInicio"))
                .setText(inicio);
            ((TextField) leer(c, "txtFin"))
                .setText(fin == null ? "" : fin);
            ((ComboBox<String>) leer(c, "cmbZona"))
                .setValue(zona);

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {
                llamar(c, "onGuardar");

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                assertFalse((Boolean) leer(c, "ocupado"));
            }
            return null;
        });
    }

    @Test
    void sesionConRolAuxiliarNoEsVigente()
            throws Exception {
        fx(() -> {
            var c = formulario();
            var auxiliar = new SesionUsuario.DatosUsuario(
                7, "Auxiliar QA", "qa@test.local",
                "AUXILIAR", 8, "Farmacia QA");

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                    .thenReturn(auxiliar);

                assertFalse((Boolean) llamar(c, "sesionVigente"));
            }
            return null;
        });
    }

    @Test
    void sesionSinFarmaciaNoEsVigente()
            throws Exception {
        fx(() -> {
            var c = formulario();
            var admin = new SesionUsuario.DatosUsuario(
                7, "Admin QA", "qa@test.local",
                "ADMINISTRADOR", null, null);

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                    .thenReturn(admin);

                assertFalse((Boolean) llamar(c, "sesionVigente"));
            }
            return null;
        });
    }

    @Test
    void regresarAbreDashboard() throws Exception {
        fx(() -> {
            var c = formulario();
            CareStockApp app = mock(CareStockApp.class);

            try (var aplicacion = mockStatic(CareStockApp.class)) {
                aplicacion.when(CareStockApp::getInstance)
                    .thenReturn(app);

                llamar(c, "onVolver");
                verify(app).mostrarDashboard();
            }
            return null;
        });
    }
}
