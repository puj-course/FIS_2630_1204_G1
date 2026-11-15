package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorMovimientosCoverage80Test {

    private static final SesionUsuario.DatosUsuario ADMIN =
        new SesionUsuario.DatosUsuario(
            7, "QA", "qa@test.local",
            "ADMINISTRADOR", 8, "Farmacia QA");

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Platform.startup(inicio::countDown);
        } catch (IllegalStateException yaIniciado) {
            inicio.countDown();
        }
        assertTrue(inicio.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T enFx(Callable<T> accion)
            throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void asignar(
            Object objeto, String nombre, Object valor)
            throws Exception {
        Field campo = objeto.getClass()
            .getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String nombre)
            throws Exception {
        Field campo = objeto.getClass()
            .getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static Object llamar(
            Object objeto, String metodo,
            Class<?>[] tipos, Object... valores)
            throws Exception {
        Method m = objeto.getClass()
            .getDeclaredMethod(metodo, tipos);
        m.setAccessible(true);
        return m.invoke(objeto, valores);
    }

    private static Object llamar(
            Object objeto, String metodo) throws Exception {
        return llamar(objeto, metodo, new Class<?>[0]);
    }

    private static Object producto(
            String categoria, int id, int stock)
            throws Exception {
        Class<?> tipo = Class.forName(
            "org.example.carestock.controller."
            + "ControladorMovimientos$OpcionProducto");

        Constructor<?> constructor =
            tipo.getDeclaredConstructor(
                String.class, int.class,
                String.class, String.class, int.class);
        constructor.setAccessible(true);

        return constructor.newInstance(
            categoria, id, "COD-" + id, "Producto QA", stock);
    }

    private static ControladorMovimientos formulario()
            throws Exception {
        var c = new ControladorMovimientos();

        asignar(c, "lblFarmacia", new Label());
        asignar(c, "lblUsuario", new Label());
        asignar(c, "lblStock", new Label());
        asignar(c, "lblEstado", new Label());
        asignar(c, "cmbCategoria", new ComboBox<String>());
        asignar(c, "cmbProducto", new ComboBox<>());
        asignar(c, "cmbMovimiento", new ComboBox<String>());
        asignar(c, "txtCantidad", new TextField());
        asignar(c, "txtMotivo", new TextArea());
        asignar(c, "btnValidar", new Button());
        asignar(c, "btnActualizar", new Button());
        asignar(c, "idUsuario", 7);
        asignar(c, "idFarmacia", 8);

        return c;
    }

    private static <T> T conSesion(Callable<T> accion)
            throws Exception {
        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                .thenReturn(ADMIN);
            return accion.call();
        }
    }

    private static String registrar(
            boolean conProducto, String tipo,
            String cantidad, String motivo, int stock)
            throws Exception {

        return enFx(() -> conSesion(() -> {
            var c = formulario();

            ComboBox<Object> productos =
                leer(c, "cmbProducto");
            ComboBox<String> movimientos =
                leer(c, "cmbMovimiento");
            TextField campoCantidad =
                leer(c, "txtCantidad");
            TextArea campoMotivo =
                leer(c, "txtMotivo");

            if (conProducto) {
                productos.setValue(producto("Aseo", 11, stock));
            }

            movimientos.setValue(tipo);
            campoCantidad.setText(cantidad);
            campoMotivo.setText(motivo);

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class,
                        (a, contexto) ->
                            when(a.showAndWait()).thenReturn(
                                Optional.of(ButtonType.CANCEL)))) {

                llamar(c, "onRegistrar");

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0))
                    .showAndWait();
            }

            Label estado = leer(c, "lblEstado");
            return estado.getText();
        }));
    }

    @Test
    void rechazaRegistroSinProducto() throws Exception {
        assertTrue(registrar(false, "ENTRADA", "3",
            "Reposicion", 10).contains("Selecciona un producto"));
    }

    @Test
    void rechazaRegistroSinMovimiento() throws Exception {
        assertTrue(registrar(true, null, "3",
            "Reposicion", 10).contains("ENTRADA o SALIDA"));
    }

    @Test
    void rechazaCantidadNoNumerica() throws Exception {
        assertTrue(registrar(true, "ENTRADA", "abc",
            "Reposicion", 10).contains("entero positivo"));
    }

    @Test
    void rechazaCantidadVacia() throws Exception {
        assertTrue(registrar(true, "ENTRADA", "",
            "Reposicion", 10).contains("entero positivo"));
    }

    @Test
    void rechazaCantidadCero() throws Exception {
        assertTrue(registrar(true, "ENTRADA", "0",
            "Reposicion", 10).contains("mayor que cero"));
    }

    @Test
    void rechazaCantidadNegativa() throws Exception {
        assertTrue(registrar(true, "ENTRADA", "-5",
            "Reposicion", 10).contains("mayor que cero"));
    }

    @Test
    void rechazaMotivoVacio() throws Exception {
        assertTrue(registrar(true, "ENTRADA", "3",
            "   ", 10).contains("máximo 250"));
    }

    @Test
    void rechazaMotivoDemasiadoLargo() throws Exception {
        assertTrue(registrar(true, "ENTRADA", "3",
            "X".repeat(251), 10).contains("máximo 250"));
    }

    @Test
    void rechazaSalidaConStockInsuficiente()
            throws Exception {
        assertTrue(registrar(true, "SALIDA", "11",
            "Dispensacion", 10).contains("insuficiente"));
    }

    @Test
    void permiteCancelarUnaOperacionValida()
            throws Exception {
        assertEquals("", registrar(true, "ENTRADA",
            "3", "Reposicion", 10));
    }

    @Test
    void impideRegistroDuplicado() throws Exception {
        enFx(() -> {
            var c = formulario();
            asignar(c, "registroEnCurso", true);

            llamar(c, "onRegistrar");

            assertTrue((Boolean) leer(c, "registroEnCurso"));
            assertEquals("", ((Label) leer(c, "lblEstado"))
                .getText());
            return null;
        });
    }

    @Test
    void validaUsuarioYFarmaciaActuales() throws Exception {
        enFx(() -> conSesion(() -> {
            var c = formulario();

            assertTrue((Boolean)
                llamar(c, "sesionSinCambios"));

            asignar(c, "idUsuario", 99);

            assertFalse((Boolean)
                llamar(c, "sesionSinCambios"));

            asignar(c, "idUsuario", 7);
            asignar(c, "idFarmacia", 99);

            assertFalse((Boolean)
                llamar(c, "sesionSinCambios"));
            return null;
        }));
    }

    @Test
    void bloqueaYDesbloqueaControles()
            throws Exception {
        enFx(() -> {
            var c = formulario();

            llamar(c, "bloquearFormulario",
                new Class<?>[]{boolean.class}, true);

            assertTrue(((Button) leer(c, "btnValidar"))
                .isDisabled());
            assertTrue(((TextField) leer(c, "txtCantidad"))
                .isDisabled());
            assertTrue(((ComboBox<?>) leer(c, "cmbProducto"))
                .isDisabled());

            llamar(c, "bloquearFormulario",
                new Class<?>[]{boolean.class}, false);

            assertFalse(((Button) leer(c, "btnValidar"))
                .isDisabled());
            assertFalse(((TextField) leer(c, "txtCantidad"))
                .isDisabled());
            assertFalse(((ComboBox<?>) leer(c, "cmbProducto"))
                .isDisabled());
            return null;
        });
    }

    @Test
    void filtraProductosPorCategoria() throws Exception {
        enFx(() -> {
            var c = formulario();

            ObservableList<Object> lista =
                leer(c, "productos");

            lista.add(producto("Aseo", 11, 15));
            lista.add(producto("Maternidad", 12, 8));

            ComboBox<String> categorias =
                leer(c, "cmbCategoria");
            ComboBox<Object> selector =
                leer(c, "cmbProducto");

            categorias.setValue("Aseo");
            llamar(c, "filtrarProductos");
            assertEquals(1, selector.getItems().size());

            categorias.setValue("Maternidad");
            llamar(c, "filtrarProductos");
            assertEquals(1, selector.getItems().size());

            categorias.setValue("Otra");
            llamar(c, "filtrarProductos");
            assertTrue(selector.getItems().isEmpty());
            return null;
        });
    }

    @Test
    void muestraExistenciasDelProducto()
            throws Exception {
        enFx(() -> {
            var c = formulario();
            Object p = producto("Aseo", 11, 24);

            llamar(c, "mostrarStock",
                new Class<?>[]{p.getClass()}, p);

            Label stock = leer(c, "lblStock");
            assertTrue(stock.getText().contains("24 unidades"));

            llamar(c, "mostrarStock",
                new Class<?>[]{p.getClass()}, (Object) null);

            assertTrue(stock.getText()
                .contains("Selecciona un producto"));
            return null;
        });
    }

    @Test
    void rechazaAlertaNula() throws Exception {
        enFx(() -> {
            var c = formulario();

            assertThrows(SecurityException.class,
                () -> c.abrirDesdeAlerta(null));
            return null;
        });
    }

    @Test
    void noPermiteSalirMientrasRegistra()
            throws Exception {
        enFx(() -> {
            var c = formulario();
            asignar(c, "registroEnCurso", true);

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
}
