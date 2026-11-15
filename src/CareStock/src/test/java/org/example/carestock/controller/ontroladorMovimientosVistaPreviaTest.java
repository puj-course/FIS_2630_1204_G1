package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.carestock.model.Usuario;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

/** Pruebas de la vista previa del stock resultante en movimientos (HU-153). */
class ControladorMovimientosVistaPreviaTest {

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

    private static <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void poner(Object objeto, String campo, Object valor) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String campo) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(objeto);
    }

    private static Object llamar(Object objeto, String nombre, Class<?>[] tipos, Object... valores)
            throws Exception {
        Method m = objeto.getClass().getDeclaredMethod(nombre, tipos);
        m.setAccessible(true);
        return m.invoke(objeto, valores);
    }

    private static MockedStatic<SesionUsuario> sesion() {
        MockedStatic<SesionUsuario> s = mockStatic(SesionUsuario.class);
        s.when(SesionUsuario::actual).thenReturn(ADMIN);
        s.when(SesionUsuario::estaActiva).thenReturn(true);
        s.when(SesionUsuario::usuarioParaFachada).thenReturn(mock(Usuario.class));
        return s;
    }

    private static Class<?> tipoOpcion() throws Exception {
        return Class.forName(
                "org.example.carestock.controller.ControladorMovimientos$OpcionProducto");
    }

    private static Object opcion(int id, int stock) throws Exception {
        Constructor<?> constructor = tipoOpcion().getDeclaredConstructor(
                String.class, int.class, String.class, String.class, int.class);
        constructor.setAccessible(true);
        return constructor.newInstance("Aseo", id, "COD-" + id, "Producto " + id, stock);
    }

    private static ControladorMovimientos formulario() throws Exception {
        ControladorMovimientos c = new ControladorMovimientos();
        poner(c, "lblFarmacia", new Label());
        poner(c, "lblUsuario", new Label());
        poner(c, "lblStock", new Label());
        poner(c, "lblEstado", new Label());
        poner(c, "cmbCategoria", new ComboBox<String>());
        poner(c, "cmbProducto", new ComboBox<>());
        poner(c, "cmbMovimiento", new ComboBox<String>());
        poner(c, "txtCantidad", new TextField());
        poner(c, "txtMotivo", new TextArea());
        poner(c, "btnValidar", new Button());
        poner(c, "btnActualizar", new Button());
        poner(c, "idUsuario", 7);
        poner(c, "idFarmacia", 8);
        return c;
    }

    /** Formulario inicializado: los listeners de HU-153 ya estan registrados. */
    private static ControladorMovimientos inicializado() throws Exception {
        ControladorMovimientos c = spy(formulario());
        doNothing().when(c).iniciarConsulta(any(Task.class));
        llamar(c, "initialize", new Class<?>[0]);
        return c;
    }

    private static String textoStock(ControladorMovimientos c) throws Exception {
        Label etiqueta = leer(c, "lblStock");
        return etiqueta.getText();
    }

    private static String estiloStock(ControladorMovimientos c) throws Exception {
        Label etiqueta = leer(c, "lblStock");
        return etiqueta.getStyle();
    }

    @Test
    void sinProductoPideSeleccionarUnoYUsaEstiloNormal() throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = inicializado();

                llamar(c, "mostrarStock", new Class<?>[]{tipoOpcion()}, new Object[]{null});

                assertTrue(textoStock(c).contains("Selecciona un producto"));
                assertFalse(estiloStock(c).contains("#F0A8A8"));
            }
            return null;
        });
    }

    @Test
    void sinCantidadMuestraGuionEnElResultante() throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = inicializado();
                ComboBox<Object> productos = leer(c, "cmbProducto");

                productos.setValue(opcion(11, 24));

                assertTrue(textoStock(c).contains("24 unidades"));
                assertTrue(textoStock(c).contains("Stock resultante: —"));
                assertFalse(estiloStock(c).contains("#F0A8A8"));
            }
            return null;
        });
    }

    @Test
    void entradaSumaALasExistencias() throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = inicializado();
                ComboBox<Object> productos = leer(c, "cmbProducto");
                ComboBox<String> tipos = leer(c, "cmbMovimiento");
                TextField cantidad = leer(c, "txtCantidad");

                productos.setValue(opcion(11, 24));
                tipos.setValue("ENTRADA");
                cantidad.setText("6");

                assertTrue(textoStock(c).contains("Stock resultante: 30"));
                assertFalse(estiloStock(c).contains("#F0A8A8"));
            }
            return null;
        });
    }

    @Test
    void salidaRestaDeLasExistencias() throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = inicializado();
                ComboBox<Object> productos = leer(c, "cmbProducto");
                ComboBox<String> tipos = leer(c, "cmbMovimiento");
                TextField cantidad = leer(c, "txtCantidad");

                productos.setValue(opcion(11, 24));
                tipos.setValue("SALIDA");
                cantidad.setText("4");

                assertTrue(textoStock(c).contains("Stock resultante: 20"));
                assertFalse(estiloStock(c).contains("#F0A8A8"));
            }
            return null;
        });
    }

    @Test
    void salidaMayorAlStockSeMarcaEnCoral() throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = inicializado();
                ComboBox<Object> productos = leer(c, "cmbProducto");
                ComboBox<String> tipos = leer(c, "cmbMovimiento");
                TextField cantidad = leer(c, "txtCantidad");

                productos.setValue(opcion(11, 3));
                tipos.setValue("SALIDA");
                cantidad.setText("7");

                assertTrue(textoStock(c).contains("stock insuficiente"));
                assertTrue(estiloStock(c).contains("#F0A8A8"));

                // Al corregir la cantidad vuelve al estilo normal.
                cantidad.setText("2");
                assertTrue(textoStock(c).contains("Stock resultante: 1"));
                assertFalse(estiloStock(c).contains("#F0A8A8"));
            }
            return null;
        });
    }

    @Test
    void cambiarElTipoActualizaLaVistaPrevia() throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = inicializado();
                ComboBox<Object> productos = leer(c, "cmbProducto");
                ComboBox<String> tipos = leer(c, "cmbMovimiento");
                TextField cantidad = leer(c, "txtCantidad");

                productos.setValue(opcion(11, 10));
                cantidad.setText("5");
                tipos.setValue("ENTRADA");
                assertTrue(textoStock(c).contains("Stock resultante: 15"));

                tipos.setValue("SALIDA");
                assertTrue(textoStock(c).contains("Stock resultante: 5"));
            }
            return null;
        });
    }

    @Test
    void sinControlesDeMovimientoNiCantidadNoFalla() throws Exception {
        fx(() -> {
            ControladorMovimientos c = formulario();
            poner(c, "cmbMovimiento", null);
            poner(c, "txtCantidad", null);

            llamar(c, "mostrarStock", new Class<?>[]{tipoOpcion()}, new Object[]{opcion(11, 9)});

            assertTrue(textoStock(c).contains("9 unidades"));
            assertTrue(textoStock(c).contains("Stock resultante: —"));
            assertEquals(false, estiloStock(c).contains("#F0A8A8"));
            return null;
        });
    }
}
