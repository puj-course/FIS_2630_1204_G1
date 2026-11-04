package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.service.ResolucionNavegacionAlertas;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MovimientosContextuales984Test {

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

    private static <T> T enFx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static <T> T conSesion(
            String rol, Callable<T> accion) throws Exception {
        return enFx(() -> {
            Field f = SesionUsuario.class.getDeclaredField("actual");
            f.setAccessible(true);
            Object anterior = f.get(null);
            try {
                f.set(null, new SesionUsuario.DatosUsuario(
                        7, "QA", "qa@test.local", rol, 4, "Farmacia QA"));
                return accion.call();
            } finally {
                f.set(null, anterior);
            }
        });
    }

    private static void poner(
            Object objeto, String nombre, Object valor) throws Exception {
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

    private static void ejecutar(Object objeto, String nombre)
            throws Exception {
        Method m = objeto.getClass().getDeclaredMethod(nombre);
        m.setAccessible(true);
        m.invoke(objeto);
    }

    private static Object opcion(
            String categoria, int id, String codigo) throws Exception {
        Class<?> tipo = Class.forName(
                "org.example.carestock.controller."
                + "ControladorMovimientos$OpcionProducto");

        Constructor<?> constructor = tipo.getDeclaredConstructor(
                String.class, int.class, String.class,
                String.class, int.class);
        constructor.setAccessible(true);

        return constructor.newInstance(
                categoria, id, codigo, "Producto " + id, 3);
    }

    private static Destino destino(
            Categoria categoria, String codigo, int farmacia) {
        AlertaInventario alerta = new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                categoria, 17, codigo, "Producto 17",
                null, 1, 5, null, null,
                Severidad.ADVERTENCIA, null, null, null);

        return ResolucionNavegacionAlertas.resolver(
                alerta, farmacia, farmacia);
    }

    private static ControladorMovimientos preparar()
            throws Exception {
        ControladorMovimientos controlador =
                new ControladorMovimientos();

        poner(controlador, "idUsuario", 7);
        poner(controlador, "idFarmacia", 4);
        poner(controlador, "productosCargados", true);
        poner(controlador, "lblEstado", new Label());
        poner(controlador, "lblStock", new Label());

        ComboBox<String> categorias = new ComboBox<>();
        categorias.getItems().addAll("Aseo", "Maternidad");

        poner(controlador, "cmbCategoria", categorias);
        poner(controlador, "cmbProducto", new ComboBox<>());
        poner(controlador, "cmbMovimiento", new ComboBox<String>());

        ObservableList<Object> opciones =
                leer(controlador, "productos");

        opciones.addAll(
                opcion("Aseo", 17, "COD-A"),
                opcion("Maternidad", 17, "COD-M"),
                opcion("Aseo", 18, "OTRO"));

        categorias.valueProperty().addListener(
                (obs, anterior, actual) -> {
                    try {
                        ejecutar(controlador, "filtrarProductos");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        categorias.setValue("Aseo");
        return controlador;
    }

    @Test
    void seleccionaProductoExactoPorCategoriaYCodigoSinModificarStock()
            throws Exception {
        conSesion("FARMACEUTICO", () -> {
            ControladorMovimientos c = preparar();

            ComboBox<Object> producto = leer(c, "cmbProducto");
            ComboBox<String> categoria = leer(c, "cmbCategoria");
            ComboBox<String> movimiento = leer(c, "cmbMovimiento");
            Label estado = leer(c, "lblEstado");

            poner(c, "productosCargados", false);

            c.abrirDesdeAlerta(
                    destino(Categoria.ASEO, "cod-a", 4));

            assertNull(producto.getValue());

            poner(c, "productosCargados", true);
            ejecutar(c, "seleccionarDesdeAlerta");

            assertEquals("Aseo", categoria.getValue());
            assertTrue(producto.getValue()
                    .toString().contains("COD-A"));
            assertEquals("ENTRADA", movimiento.getValue());
            assertTrue(estado.getText().contains("Producto 17"));
            assertNull(leer(c, "destinoAlerta"));

            c.abrirDesdeAlerta(
                    destino(Categoria.MATERNIDAD, "COD-M", 4));

            assertEquals("Maternidad", categoria.getValue());
            assertTrue(producto.getValue()
                    .toString().contains("COD-M"));
            assertEquals("ENTRADA", movimiento.getValue());

            return null;
        });
    }

    @Test
    void productoNoCoincidenteNoQuedaSeleccionado()
            throws Exception {
        conSesion("ADMINISTRADOR", () -> {
            ControladorMovimientos c = preparar();

            c.abrirDesdeAlerta(
                    destino(Categoria.ASEO, "COD-INCORRECTO", 4));

            ComboBox<Object> producto = leer(c, "cmbProducto");
            Label estado = leer(c, "lblEstado");

            assertNull(producto.getValue());
            assertTrue(estado.getText().contains("no está activo"));
            return null;
        });
    }

    @Test
    void rechazaDestinoInvalidoFarmaciaAjenaYSesionAlterada()
            throws Exception {
        conSesion("FARMACEUTICO", () -> {
            ControladorMovimientos c = preparar();

            assertThrows(SecurityException.class,
                    () -> c.abrirDesdeAlerta(null));

            assertThrows(SecurityException.class,
                    () -> c.abrirDesdeAlerta(
                            destino(Categoria.ASEO, "COD-A", 8)));

            Destino invalido = new Destino(
                    TipoAlerta.VENCIMIENTO,
                    Categoria.ASEO,
                    17, "COD-A", "Producto 17", "LOTE", 4);

            assertThrows(SecurityException.class,
                    () -> c.abrirDesdeAlerta(invalido));

            poner(c, "idUsuario", 999);

            assertThrows(SecurityException.class,
                    () -> c.abrirDesdeAlerta(
                            destino(Categoria.ASEO, "COD-A", 4)));

            return null;
        });
    }

    @Test
    void auxiliarNoPuedeInicializarFormularioDeMovimientos()
            throws Exception {
        conSesion("AUXILIAR", () -> {
            ControladorMovimientos c = new ControladorMovimientos();

            InvocationTargetException error = assertThrows(
                    InvocationTargetException.class,
                    () -> ejecutar(c, "initialize"));

            assertInstanceOf(
                    SecurityException.class, error.getCause());

            return null;
        });
    }
}
