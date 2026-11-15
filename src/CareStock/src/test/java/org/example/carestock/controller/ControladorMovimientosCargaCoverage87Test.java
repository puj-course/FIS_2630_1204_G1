package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.facade.Inventario;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Usuario;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorMovimientosCargaCoverage87Test {

    private static final SesionUsuario.DatosUsuario ADMIN =
        new SesionUsuario.DatosUsuario(
            7, "Administrador QA", "qa@test.local",
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

    private static void poner(Object objeto, String campo, Object valor)
            throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String campo)
            throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(objeto);
    }

    private static Object llamar(Object objeto, String nombre)
            throws Exception {
        Method m = objeto.getClass().getDeclaredMethod(nombre);
        m.setAccessible(true);
        return m.invoke(objeto);
    }

    private static Object llamarTarea(Task<?> tarea)
            throws Exception {
        Method m = tarea.getClass().getDeclaredMethod("call");
        m.setAccessible(true);
        return m.invoke(tarea);
    }

    private static MockedStatic<SesionUsuario> sesion() {
        MockedStatic<SesionUsuario> s =
            mockStatic(SesionUsuario.class);

        s.when(SesionUsuario::actual).thenReturn(ADMIN);
        s.when(SesionUsuario::estaActiva).thenReturn(true);
        s.when(SesionUsuario::usuarioParaFachada)
            .thenReturn(mock(Usuario.class));

        return s;
    }

    private static ControladorMovimientos formulario()
            throws Exception {
        var c = new ControladorMovimientos();

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

    private static Object opcion(
            String categoria, int id,
            String codigo, String nombre, int stock)
            throws Exception {
        Class<?> tipo = Class.forName(
            "org.example.carestock.controller."
            + "ControladorMovimientos$OpcionProducto");

        Constructor<?> constructor = tipo.getDeclaredConstructor(
            String.class, int.class, String.class,
            String.class, int.class);
        constructor.setAccessible(true);

        return constructor.newInstance(
            categoria, id, codigo, nombre, stock);
    }

    private static void prepararCategorias(ControladorMovimientos c)
            throws Exception {
        ComboBox<String> categorias = leer(c, "cmbCategoria");
        categorias.setItems(
            FXCollections.observableArrayList("Aseo", "Maternidad"));

        categorias.valueProperty().addListener(
            (obs, anterior, nueva) -> {
                try {
                    llamar(c, "filtrarProductos");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @SuppressWarnings("unchecked")
    private static void agregar(ControladorMovimientos c, Object opcion)
            throws Exception {
        ObservableList<Object> productos = leer(c, "productos");
        productos.add(opcion);
    }

    private static Destino alerta(
            TipoAlerta tipo, Categoria categoria,
            int id, String codigo, int farmacia) {
        return new Destino(
            tipo, categoria, id, codigo,
            "Producto QA", null, farmacia);
    }

    private static RegistroCatalogo<Aseo> aseo(
            int id, String nombre, String estado) {
        Aseo producto = mock(Aseo.class);
        when(producto.getNombre()).thenReturn(nombre);
        when(producto.getCodigo()).thenReturn("ASE-" + id);
        when(producto.getStock()).thenReturn(6);
        return new RegistroCatalogo<>(id, producto, estado);
    }

    private static RegistroCatalogo<Maternidad> maternidad(
            int id, String nombre, String estado) {
        Maternidad producto = mock(Maternidad.class);
        when(producto.getNombre()).thenReturn(nombre);
        when(producto.getCodigo()).thenReturn("MAT-" + id);
        when(producto.getStock()).thenReturn(3);
        return new RegistroCatalogo<>(id, producto, estado);
    }

    private static Task<?> capturarCarga(ControladorMovimientos c)
            throws Exception {
        AtomicReference<Task<?>> capturada = new AtomicReference<>();

        doAnswer(invocacion -> {
            capturada.set(invocacion.getArgument(0));
            return null;
        }).when(c).iniciarConsulta(any(Task.class));

        llamar(c, "cargarProductos");

        assertNotNull(capturada.get());
        assertTrue(((Button) leer(c, "btnActualizar")).isDisabled());
        assertTrue(((Button) leer(c, "btnValidar")).isDisabled());

        return capturada.get();
    }

    @Test
    void inicializacionConfiguraFormularioYCapturaConsulta()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = spy(formulario());

                doNothing().when(c).iniciarConsulta(any(Task.class));

                llamar(c, "initialize");

                ComboBox<String> categorias = leer(c, "cmbCategoria");
                ComboBox<String> tipos = leer(c, "cmbMovimiento");

                assertEquals(
                    List.of("Aseo", "Maternidad"),
                    categorias.getItems());
                assertEquals(
                    List.of("ENTRADA", "SALIDA"),
                    tipos.getItems());

                assertEquals("Aseo", categorias.getValue());
                assertEquals("Administrador QA",
                    ((Label) leer(c, "lblUsuario")).getText());
                assertTrue(((Label) leer(c, "lblFarmacia"))
                    .getText().contains("Farmacia QA"));

                verify(c).iniciarConsulta(any(Task.class));
            }
            return null;
        });
    }

    @Test
    void inicializacionRechazaUsuarioSinFarmacia()
            throws Exception {
        fx(() -> {
            var usuario = new SesionUsuario.DatosUsuario(
                7, "QA", "qa@test.local",
                "ADMINISTRADOR", null, null);

            try (var s = mockStatic(SesionUsuario.class)) {
                s.when(SesionUsuario::actual).thenReturn(usuario);

                InvocationTargetException error = assertThrows(
                    InvocationTargetException.class,
                    () -> llamar(formulario(), "initialize"));

                assertInstanceOf(
                    IllegalStateException.class, error.getCause());
            }
            return null;
        });
    }

    @Test
    void inicializacionRechazaAuxiliar()
            throws Exception {
        fx(() -> {
            var auxiliar = new SesionUsuario.DatosUsuario(
                7, "QA", "qa@test.local",
                "AUXILIAR", 8, "Farmacia QA");

            try (var s = mockStatic(SesionUsuario.class)) {
                s.when(SesionUsuario::actual).thenReturn(auxiliar);

                InvocationTargetException error = assertThrows(
                    InvocationTargetException.class,
                    () -> llamar(formulario(), "initialize"));

                assertInstanceOf(
                    SecurityException.class, error.getCause());
            }
            return null;
        });
    }

    @Test
    void consultaFiltraInactivosYOrdenaAmbasCategorias()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = spy(formulario());
                Task<?> tarea = capturarCarga(c);

                List<RegistroCatalogo<Aseo>> datosAseo = List.of(
                    aseo(1, "Zeta", "ACTIVO"),
                    aseo(2, "Alba", "activo"),
                    aseo(3, "Inactivo", "INACTIVO")
                );
                List<RegistroCatalogo<Maternidad>> datosMaternidad = List.of(
                    maternidad(4, "Beta", "ACTIVO"),
                    maternidad(5, "Oculto", "INACTIVO")
                );

                try (MockedConstruction<Inventario> inventarios =
                        mockConstruction(Inventario.class,
                            (inventario, contexto) -> {
                                when(inventario.listarAseo()).thenReturn(datosAseo);
                                when(inventario.listarMaternidad()).thenReturn(datosMaternidad);
                            })) {

                    List<?> resultado = (List<?>) llamarTarea(tarea);

                    assertEquals(3, resultado.size());
                    assertTrue(resultado.get(0).toString()
                        .contains("Alba"));
                    assertTrue(resultado.get(1).toString()
                        .contains("Beta"));
                    assertTrue(resultado.get(2).toString()
                        .contains("Zeta"));

                    assertEquals(1, inventarios.constructed().size());
                    verify(inventarios.constructed().get(0))
                        .listarAseo();
                    verify(inventarios.constructed().get(0))
                        .listarMaternidad();
                }
            }
            return null;
        });
    }

    @Test
    void consultaSinProductosDevuelveListaVacia()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = spy(formulario());
                Task<?> tarea = capturarCarga(c);

                try (var inventarios =
                        mockConstruction(Inventario.class,
                            (inventario, contexto) -> {
                                when(inventario.listarAseo())
                                    .thenReturn(List.of());
                                when(inventario.listarMaternidad())
                                    .thenReturn(List.of());
                            })) {

                    List<?> resultado = (List<?>) llamarTarea(tarea);
                    assertTrue(resultado.isEmpty());
                }
            }
            return null;
        });
    }

    @Test
    void errorJdbcEnConsultaSePropagaSinModificarInventario()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                ControladorMovimientos c = spy(formulario());
                Task<?> tarea = capturarCarga(c);

                try (var inventarios =
                        mockConstruction(Inventario.class,
                            (inventario, contexto) ->
                                when(inventario.listarAseo())
                                    .thenThrow(new SQLException(
                                        "Error JDBC de prueba")))) {

                    InvocationTargetException error = assertThrows(
                        InvocationTargetException.class,
                        () -> llamarTarea(tarea));

                    assertInstanceOf(
                        SQLException.class, error.getCause());
                }
            }
            return null;
        });
    }

    @Test
    void cambioDeSesionImpideConsultarProductos()
            throws Exception {
        fx(() -> {
            ControladorMovimientos c = spy(formulario());

            try (var s = mockStatic(SesionUsuario.class);
                 var alertas = mockConstruction(Alert.class)) {

                s.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

                llamar(c, "cargarProductos");

                verify(c, never()).iniciarConsulta(any(Task.class));
                assertTrue(((Label) leer(c, "lblEstado"))
                    .getText().contains("sesión cambió"));
                assertEquals(1, alertas.constructed().size());
            }
            return null;
        });
    }

    @Test
    void alertaDeAseoSeleccionaProductoYEntrada()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                var c = formulario();
                prepararCategorias(c);
                agregar(c, opcion(
                    "Aseo", 11, "ASE-11", "Jabon", 5));
                poner(c, "productosCargados", true);

                c.abrirDesdeAlerta(alerta(
                    TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                    11, "ase-11", 8));

                ComboBox<Object> productos = leer(c, "cmbProducto");
                ComboBox<String> movimientos = leer(c, "cmbMovimiento");

                assertNotNull(productos.getValue());
                assertTrue(productos.getValue().toString()
                    .contains("Jabon"));
                assertEquals("ENTRADA", movimientos.getValue());
                assertTrue(((Label) leer(c, "lblEstado"))
                    .getText().contains("Reposición"));
            }
            return null;
        });
    }

    @Test
    void alertaMaternidadSeleccionaCategoriaCorrecta()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                var c = formulario();
                prepararCategorias(c);
                agregar(c, opcion(
                    "Maternidad", 12, "MAT-12", "Pañales", 3));
                poner(c, "productosCargados", true);

                c.abrirDesdeAlerta(alerta(
                    TipoAlerta.STOCK_MINIMO, Categoria.MATERNIDAD,
                    12, "MAT-12", 8));

                assertEquals("Maternidad",
                    ((ComboBox<?>) leer(c, "cmbCategoria")).getValue());
                assertNotNull(
                    ((ComboBox<?>) leer(c, "cmbProducto")).getValue());
            }
            return null;
        });
    }

    @Test
    void alertaConCodigoDistintoNoSeleccionaProducto()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                var c = formulario();
                prepararCategorias(c);
                agregar(c, opcion(
                    "Aseo", 11, "ASE-11", "Jabon", 5));
                poner(c, "productosCargados", true);

                c.abrirDesdeAlerta(alerta(
                    TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                    11, "CODIGO-INCORRECTO", 8));

                assertNull(
                    ((ComboBox<?>) leer(c, "cmbProducto")).getValue());
                assertTrue(((Label) leer(c, "lblEstado"))
                    .getText().contains("no está activo"));
            }
            return null;
        });
    }

    @Test
    void alertaConCodigoNuloPermiteBuscarPorIdentificador()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                var c = formulario();
                prepararCategorias(c);
                agregar(c, opcion(
                    "Aseo", 11, null, "Producto sin codigo", 2));
                poner(c, "productosCargados", true);

                c.abrirDesdeAlerta(alerta(
                    TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                    11, null, 8));

                assertNotNull(
                    ((ComboBox<?>) leer(c, "cmbProducto")).getValue());
            }
            return null;
        });
    }

    @Test
    void alertaQuedaPendienteSiProductosNoEstanCargados()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                var c = formulario();
                prepararCategorias(c);

                c.abrirDesdeAlerta(alerta(
                    TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                    11, "ASE-11", 8));

                assertNotNull(leer(c, "destinoAlerta"));
                assertNull(
                    ((ComboBox<?>) leer(c, "cmbProducto")).getValue());
            }
            return null;
        });
    }

    @Test
    void rechazaAlertasIncorrectasYOtraFarmacia()
            throws Exception {
        fx(() -> {
            try (var s = sesion()) {
                var c = formulario();

                Destino[] invalidas = {
                    alerta(TipoAlerta.VENCIMIENTO,
                        Categoria.ASEO, 11, "ASE-11", 8),
                    alerta(TipoAlerta.STOCK_MINIMO,
                        Categoria.MEDICAMENTO, 11, "MED-11", 8),
                    alerta(TipoAlerta.STOCK_MINIMO,
                        Categoria.ASEO, 11, "ASE-11", 99)
                };

                for (Destino destino : invalidas) {
                    assertThrows(SecurityException.class,
                        () -> c.abrirDesdeAlerta(destino));
                }
            }
            return null;
        });
    }

    @Test
    void lanzadorNormalEjecutaTareaEnHilo()
            throws Exception {
        CountDownLatch ejecutada = new CountDownLatch(1);

        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() {
                ejecutada.countDown();
                return null;
            }
        };

        new ControladorMovimientos().iniciarConsulta(tarea);

        assertTrue(ejecutada.await(10, TimeUnit.SECONDS));
    }
}
