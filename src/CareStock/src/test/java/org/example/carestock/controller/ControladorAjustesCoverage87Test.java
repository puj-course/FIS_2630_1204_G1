package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.example.carestock.CareStockApp;
import org.example.carestock.model.AjusteExistencias.Categoria;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorAjustesCoverage87Test {

    private static final SesionUsuario.DatosUsuario ADMIN =
        new SesionUsuario.DatosUsuario(
            7, "QA", "qa@test.local",
            "ADMINISTRADOR", 8, "Farmacia QA");

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

    private static void poner(Object obj, String campo, Object valor)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(obj, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object obj, String campo)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    private static Object llamar(
            Object obj, String metodo,
            Class<?>[] tipos, Object... args) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(metodo, tipos);
        m.setAccessible(true);
        return m.invoke(obj, args);
    }

    private static Object llamar(Object obj, String metodo)
            throws Exception {
        return llamar(obj, metodo, new Class<?>[0]);
    }

    private static ControladorAjustes vista()
            throws Exception {
        var c = new ControladorAjustes();

        poner(c, "lblUsuario", new Label());
        poner(c, "lblFarmacia", new Label());
        poner(c, "lblExistencias", new Label());
        poner(c, "lblEstado", new Label());
        poner(c, "cmbCategoria", new ComboBox<Categoria>());
        poner(c, "cmbReferencia", new ComboBox<>());
        poner(c, "txtNuevaCantidad", new TextField());
        poner(c, "txtMotivo", new TextArea());
        poner(c, "btnActualizar", new Button());
        poner(c, "btnAplicar", new Button());
        poner(c, "idUsuario", 7);
        poner(c, "idFarmacia", 8);

        ComboBox<Categoria> categoria = leer(c, "cmbCategoria");
        categoria.getItems().setAll(Categoria.values());
        categoria.setValue(Categoria.ASEO);

        return c;
    }

    private static Object opcion(
            Categoria categoria, int id, int stock)
            throws Exception {
        Class<?> tipo = Class.forName(
            "org.example.carestock.controller.ControladorAjustes$Opcion");

        Constructor<?> constructor = tipo.getDeclaredConstructor(
            Categoria.class, int.class, String.class, int.class);
        constructor.setAccessible(true);

        return constructor.newInstance(
            categoria, id, "Producto QA " + id, stock);
    }

    private static <T> T conSesion(Callable<T> accion)
            throws Exception {
        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva).thenReturn(true);
            sesion.when(SesionUsuario::actual).thenReturn(ADMIN);
            return accion.call();
        }
    }

    private static String aplicar(
            boolean conSeleccion,
            String cantidad, String motivo) throws Exception {

        return fx(() -> conSesion(() -> {
            var c = vista();

            if (conSeleccion) {
                ComboBox<Object> referencias = leer(c, "cmbReferencia");
                referencias.setValue(opcion(Categoria.ASEO, 11, 10));
            }

            TextField nueva = leer(c, "txtNuevaCantidad");
            TextArea explicacion = leer(c, "txtMotivo");
            nueva.setText(cantidad);
            explicacion.setText(motivo);

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class,
                        (alerta, contexto) ->
                            when(alerta.showAndWait())
                                .thenReturn(Optional.of(
                                    ButtonType.CANCEL)))) {

                llamar(c, "onAplicar");
                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0))
                    .showAndWait();
            }

            Label estado = leer(c, "lblEstado");
            assertFalse((Boolean) leer(c, "operando"));
            return estado.getText();
        }));
    }

    @Test
    void reconoceRolesAdministrativos() throws Exception {
        fx(() -> {
            var c = vista();

            assertTrue((Boolean) llamar(
                c, "administrador",
                new Class<?>[]{String.class}, "ADMINISTRADOR"));

            assertTrue((Boolean) llamar(
                c, "administrador",
                new Class<?>[]{String.class}, " super_admin "));

            assertFalse((Boolean) llamar(
                c, "administrador",
                new Class<?>[]{String.class}, "AUXILIAR"));

            assertFalse((Boolean) llamar(
                c, "administrador",
                new Class<?>[]{String.class}, (Object) null));
            return null;
        });
    }

    @Test
    void validaSesionYFarmacia() throws Exception {
        fx(() -> conSesion(() -> {
            var c = vista();

            assertTrue((Boolean) llamar(c, "sesionValida"));

            poner(c, "idFarmacia", 99);
            assertFalse((Boolean) llamar(c, "sesionValida"));

            poner(c, "idFarmacia", 8);
            poner(c, "idUsuario", 99);
            assertFalse((Boolean) llamar(c, "sesionValida"));
            return null;
        }));
    }

    @Test
    void rechazaSesionInactiva() throws Exception {
        fx(() -> {
            var c = vista();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

                assertFalse((Boolean) llamar(c, "sesionValida"));
            }
            return null;
        });
    }

    @Test
    void bloqueaTodosLosControlesDuranteOperacion()
            throws Exception {
        fx(() -> {
            var c = vista();

            llamar(c, "bloquear",
                new Class<?>[]{boolean.class}, true);

            assertTrue((Boolean) leer(c, "operando"));
            assertTrue(((ComboBox<?>) leer(c, "cmbCategoria"))
                .isDisabled());
            assertTrue(((ComboBox<?>) leer(c, "cmbReferencia"))
                .isDisabled());
            assertTrue(((TextField) leer(c, "txtNuevaCantidad"))
                .isDisabled());
            assertTrue(((TextArea) leer(c, "txtMotivo"))
                .isDisabled());
            assertTrue(((Button) leer(c, "btnAplicar"))
                .isDisabled());
            assertTrue(((Button) leer(c, "btnActualizar"))
                .isDisabled());

            llamar(c, "bloquear",
                new Class<?>[]{boolean.class}, false);

            assertFalse((Boolean) leer(c, "operando"));
            assertFalse(((Button) leer(c, "btnAplicar"))
                .isDisabled());
            return null;
        });
    }

    @Test
    void seleccionaSoloReferenciasDeCategoria()
            throws Exception {
        fx(() -> {
            var c = vista();
            List<Object> opciones = leer(c, "opciones");
            Object aseo = opcion(Categoria.ASEO, 11, 10);
            Object maternidad = opcion(Categoria.MATERNIDAD, 12, 5);
            opciones.add(aseo);
            opciones.add(maternidad);

            Class<?> tipo = aseo.getClass();

            llamar(c, "filtrarReferencias",
                new Class<?>[]{tipo}, (Object) null);

            ComboBox<Object> referencias = leer(c, "cmbReferencia");
            assertEquals(1, referencias.getItems().size());
            assertSame(aseo, referencias.getItems().get(0));

            ComboBox<Categoria> categoria = leer(c, "cmbCategoria");
            categoria.setValue(Categoria.MATERNIDAD);

            llamar(c, "filtrarReferencias",
                new Class<?>[]{tipo}, (Object) null);

            assertEquals(1, referencias.getItems().size());
            assertSame(maternidad, referencias.getItems().get(0));
            return null;
        });
    }

    @Test
    void conservaSeleccionAnteriorSiSigueDisponible()
            throws Exception {
        fx(() -> {
            var c = vista();
            Object seleccionado = opcion(Categoria.ASEO, 11, 10);
            List<Object> opciones = leer(c, "opciones");
            opciones.add(seleccionado);

            llamar(c, "filtrarReferencias",
                new Class<?>[]{seleccionado.getClass()},
                seleccionado);

            ComboBox<Object> referencias = leer(c, "cmbReferencia");
            assertSame(seleccionado, referencias.getValue());
            return null;
        });
    }

    @Test
    void limpiaSeleccionSiYaNoExiste() throws Exception {
        fx(() -> {
            var c = vista();
            Object anterior = opcion(Categoria.ASEO, 11, 10);

            llamar(c, "filtrarReferencias",
                new Class<?>[]{anterior.getClass()}, anterior);

            ComboBox<Object> referencias = leer(c, "cmbReferencia");
            Label existencias = leer(c, "lblExistencias");

            assertNull(referencias.getValue());
            assertTrue(existencias.getText()
                .contains("Selecciona un producto"));
            return null;
        });
    }

    @Test
    void rechazaAplicarMientrasEstaOperando()
            throws Exception {
        fx(() -> {
            var c = vista();
            poner(c, "operando", true);

            llamar(c, "onAplicar");

            assertTrue((Boolean) leer(c, "operando"));
            return null;
        });
    }

    @Test
    void rechazaAplicarSinSeleccion() throws Exception {
        assertTrue(aplicar(false, "12", "Conteo fisico")
            .contains("Selecciona un producto"));
    }

    @Test
    void rechazaCantidadNoNumerica() throws Exception {
        assertTrue(aplicar(true, "abc", "Conteo fisico")
            .contains("entero"));
    }

    @Test
    void rechazaCantidadVacia() throws Exception {
        assertTrue(aplicar(true, "", "Conteo fisico")
            .contains("entero"));
    }

    @Test
    void rechazaExistenciasNegativas() throws Exception {
        assertTrue(aplicar(true, "-1", "Conteo fisico")
            .contains("diferente"));
    }

    @Test
    void rechazaAjusteSinCambioEnStock() throws Exception {
        assertTrue(aplicar(true, "10", "Conteo fisico")
            .contains("diferente"));
    }

    @Test
    void rechazaMotivoEnBlanco() throws Exception {
        assertTrue(aplicar(true, "12", "   ")
            .contains("motivo"));
    }

    @Test
    void rechazaMotivoMayorA250Caracteres()
            throws Exception {
        assertTrue(aplicar(true, "12", "X".repeat(251))
            .contains("motivo"));
    }

    @Test
    void cancelarConfirmacionNoEjecutaAjuste()
            throws Exception {
        assertEquals("", aplicar(
            true, "12", "Correccion por conteo fisico"));
    }

    @Test
    void impideSalirMientrasEstaOperando()
            throws Exception {
        fx(() -> {
            var c = vista();
            poner(c, "operando", true);

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {
                llamar(c, "onVolver");

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0))
                    .showAndWait();
            }
            return null;
        });
    }

    @Test
    void permiteVolverCuandoNoHayOperacion()
            throws Exception {
        fx(() -> {
            var c = vista();
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

    @Test
    void rechazaActualizarSiLaSesionCambio()
            throws Exception {
        fx(() -> {
            var c = vista();

            try (var sesion = mockStatic(SesionUsuario.class);
                 var alertas = mockConstruction(Alert.class)) {
                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

                llamar(c, "onActualizar");

                Label estado = leer(c, "lblEstado");
                assertTrue(estado.getText().contains("sesion cambio"));
                assertEquals(1, alertas.constructed().size());
            }
            return null;
        });
    }

    @Test
    void noActualizaDuranteOtraOperacion()
            throws Exception {
        fx(() -> {
            var c = vista();
            poner(c, "operando", true);

            llamar(c, "onActualizar");

            assertTrue((Boolean) leer(c, "operando"));
            return null;
        });
    }

    @Test
    void mensajeDeErrorToleraNulosYExcepcionesSinTexto()
            throws Exception {
        fx(() -> {
            var c = vista();

            assertEquals("Error no identificado",
                llamar(c, "mensaje",
                    new Class<?>[]{Throwable.class},
                    (Object) null));

            assertEquals("Error no identificado",
                llamar(c, "mensaje",
                    new Class<?>[]{Throwable.class},
                    new IllegalStateException()));

            assertEquals("Error controlado",
                llamar(c, "mensaje",
                    new Class<?>[]{Throwable.class},
                    new IllegalStateException("Error controlado")));
            return null;
        });
    }
}
