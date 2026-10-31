package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorCatalogoFormularioFxTest {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);

        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        }

        assertTrue(listo.await(20, TimeUnit.SECONDS),
                "JavaFX no pudo iniciar");
    }

    @AfterAll
    static void cerrarJavaFx() {
        Platform.exit();
    }

    private static <T> T enFx(Callable<T> accion)
            throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void campo(
            ControladorCatalogo controlador,
            String nombre,
            Object valor) throws Exception {
        Field campo = ControladorCatalogo.class
                .getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(controlador, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T campo(
            ControladorCatalogo controlador,
            String nombre) throws Exception {
        Field campo = ControladorCatalogo.class
                .getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(controlador);
    }

    private static String texto(
            ControladorCatalogo controlador,
            String nombre) throws Exception {
        TextField control = campo(controlador, nombre);
        return control.getText();
    }

    private static void ejecutar(
            ControladorCatalogo controlador,
            String metodo,
            Class<?>[] tipos,
            Object... argumentos) throws Exception {
        Method m = ControladorCatalogo.class
                .getDeclaredMethod(metodo, tipos);
        m.setAccessible(true);
        m.invoke(controlador, argumentos);
    }

    private static ControladorCatalogo formulario()
            throws Exception {
        ControladorCatalogo controlador =
                new ControladorCatalogo();

        String[] textos = {
                "txtCodigo", "txtNombre",
                "txtDescripcion", "txtPrecio", "txtStock",
                "txtTipoAseo", "txtComponentesActivos",
                "txtStockMinimoAseo",
                "txtEtapa", "txtEdadGestacional",
                "txtStockMinimoMaternidad",
                "txtPrincipioActivo", "txtConcentracion",
                "txtFormaFarmaceutica", "txtPresentacion",
                "txtStockMinimo", "txtPrecioMedicamento"
        };

        for (String nombre : textos) {
            campo(controlador, nombre, new TextField());
        }

        campo(controlador, "chkBiodegradable",
                new CheckBox());
        campo(controlador, "chkHipoalergenico",
                new CheckBox());

        campo(controlador, "lblSeleccion", new Label());
        campo(controlador, "lblEstado", new Label());
        campo(controlador, "lblStockMedicamento",
                new Label());

        campo(controlador, "tablaProductos",
                new TableView<>());

        ComboBox<String> categorias = new ComboBox<>();
        categorias.getItems().addAll(
                "Medicamentos", "Aseo", "Maternidad");
        campo(controlador, "cmbCategoria", categorias);

        ComboBox<ControladorCatalogo.CategoriaMedicamento>
                categoriasMedicamento = new ComboBox<>();

        categoriasMedicamento.getItems().add(
                new ControladorCatalogo.CategoriaMedicamento(
                        5, "Analgesicos"));

        campo(controlador, "cmbCategoriaMedicamento",
                categoriasMedicamento);

        return controlador;
    }

    private static void llenar(
            ControladorCatalogo controlador,
            Object producto) throws Exception {
        ejecutar(controlador, "llenarFormulario",
                new Class<?>[]{Object.class}, producto);
    }

    private static void limpiar(
            ControladorCatalogo controlador) throws Exception {
        ejecutar(controlador, "limpiar",
                new Class<?>[]{});
    }

    @Test
    void llenaFormularioDeAseo() throws Exception {
        Aseo producto = mock(Aseo.class);
        when(producto.getCodigo()).thenReturn("ASE-001");
        when(producto.getNombre()).thenReturn("Jabon");
        when(producto.getDescripcion())
                .thenReturn("Jabon liquido");
        when(producto.getPrecio()).thenReturn(12000.0);
        when(producto.getStock()).thenReturn(8);
        when(producto.getStockMinimo()).thenReturn(3);
        when(producto.getTipoAseo()).thenReturn("Higiene");
        when(producto.getComponentesActivos())
                .thenReturn("Glicerina");
        when(producto.isBiodegradable()).thenReturn(true);

        enFx(() -> {
            ControladorCatalogo c = formulario();
            llenar(c, producto);

            assertEquals("ASE-001",
                    texto(c, "txtCodigo"));
            assertEquals("Jabon",
                    texto(c, "txtNombre"));
            assertEquals("Jabon liquido",
                    texto(c, "txtDescripcion"));
            assertEquals("12000.0",
                    texto(c, "txtPrecio"));
            assertEquals("8",
                    texto(c, "txtStock"));
            assertEquals("3",
                    texto(c, "txtStockMinimoAseo"));
            assertEquals("Higiene",
                    texto(c, "txtTipoAseo"));
            assertEquals("Glicerina",
                    texto(c, "txtComponentesActivos"));

            CheckBox biodegradable =
                    campo(c, "chkBiodegradable");
            assertTrue(biodegradable.isSelected());

            return null;
        });
    }

    @Test
    void llenaFormularioDeMaternidad()
            throws Exception {
        Maternidad producto = mock(Maternidad.class);
        when(producto.getCodigo()).thenReturn("MAT-001");
        when(producto.getNombre())
                .thenReturn("Kit maternal");
        when(producto.getDescripcion())
                .thenReturn("Cuidado prenatal");
        when(producto.getPrecio()).thenReturn(45000.0);
        when(producto.getStock()).thenReturn(4);
        when(producto.getStockMinimo()).thenReturn(2);
        when(producto.getEtapaRecomendada())
                .thenReturn("Gestacion");
        when(producto.getEdadGestacionalSugerida())
                .thenReturn(28);
        when(producto.isHipoalergenico())
                .thenReturn(true);

        enFx(() -> {
            ControladorCatalogo c = formulario();
            llenar(c, producto);

            assertEquals("MAT-001",
                    texto(c, "txtCodigo"));
            assertEquals("45000.0",
                    texto(c, "txtPrecio"));
            assertEquals("4",
                    texto(c, "txtStock"));
            assertEquals("Gestacion",
                    texto(c, "txtEtapa"));
            assertEquals("28",
                    texto(c, "txtEdadGestacional"));
            assertEquals("2",
                    texto(c, "txtStockMinimoMaternidad"));

            CheckBox hipoalergenico =
                    campo(c, "chkHipoalergenico");
            assertTrue(hipoalergenico.isSelected());

            return null;
        });
    }

    @Test
    void llenaMedicamentoSinPrecioHistorico()
            throws Exception {
        Medicamento producto = mock(Medicamento.class);
        when(producto.getCodigoInvima())
                .thenReturn("INV-001");
        when(producto.getNombreComercial())
                .thenReturn("Acetaminofen");
        when(producto.getPrincipioActivo())
                .thenReturn("Paracetamol");
        when(producto.getStockMinimo()).thenReturn(5);
        when(producto.getStockTotal()).thenReturn(17);
        when(producto.getIdCategoria()).thenReturn(5);

        enFx(() -> {
            ControladorCatalogo c = formulario();
            llenar(c, producto);

            assertEquals("INV-001",
                    texto(c, "txtCodigo"));
            assertEquals("Acetaminofen",
                    texto(c, "txtNombre"));
            assertEquals("Paracetamol",
                    texto(c, "txtPrincipioActivo"));
            assertEquals("5",
                    texto(c, "txtStockMinimo"));
            assertEquals("",
                    texto(c, "txtPrecioMedicamento"));

            Label stock = campo(c, "lblStockMedicamento");
            assertTrue(stock.getText().contains("17"));

            ComboBox<?> categoria =
                    campo(c, "cmbCategoriaMedicamento");
            assertNotNull(categoria.getValue());

            return null;
        });
    }

    @Test
    void llenaMedicamentoConPrecio() throws Exception {
        Medicamento producto = mock(Medicamento.class);
        when(producto.getCodigoInvima())
                .thenReturn("INV-002");
        when(producto.getNombreComercial())
                .thenReturn("Loratadina");
        when(producto.getPrecio())
                .thenReturn(new BigDecimal("3500.50"));

        enFx(() -> {
            ControladorCatalogo c = formulario();
            llenar(c, producto);

            assertEquals("3500.50",
                    texto(c, "txtPrecioMedicamento"));
            assertEquals("Loratadina",
                    texto(c, "txtNombre"));

            return null;
        });
    }

    @Test
    void limpiarRestableceFormulario()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            TextField codigo = campo(c, "txtCodigo");
            codigo.setText("ASE-001");

            TextField precio = campo(c, "txtPrecio");
            precio.setText("1000");

            TextField stock = campo(c, "txtStock");
            stock.setText("10");

            CheckBox biodegradable =
                    campo(c, "chkBiodegradable");
            biodegradable.setSelected(true);

            CheckBox hipoalergenico =
                    campo(c, "chkHipoalergenico");
            hipoalergenico.setSelected(true);

            ComboBox<?> categoria =
                    campo(c, "cmbCategoriaMedicamento");
            categoria.getSelectionModel().selectFirst();

            limpiar(c);

            assertEquals("", codigo.getText());
            assertEquals("", precio.getText());
            assertEquals("", stock.getText());
            assertFalse(biodegradable.isSelected());
            assertFalse(hipoalergenico.isSelected());
            assertNull(categoria.getValue());

            Label seleccion = campo(c, "lblSeleccion");
            assertEquals("Nuevo producto",
                    seleccion.getText());

            return null;
        });
    }

    @Test
    void consultaRechazaCategoriaDesconocida()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                assertThrows(IllegalArgumentException.class,
                        () -> c.abrirDesdeConsulta(
                                "Desconocida", null));
            }

            return null;
        });
    }

    @Test
    void consultaSinPermisosNoPermiteEditar()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(false);

                c.abrirDesdeConsulta("Aseo", 10);

                Label estado = campo(c, "lblEstado");
                assertTrue(estado.getText()
                        .contains("Sin permisos"));
            }

            return null;
        });
    }

    @Test
    void consultaIniciaRegistroNuevo()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                c.abrirDesdeConsulta("Aseo", null);

                Label estado = campo(c, "lblEstado");
                assertTrue(estado.getText()
                        .contains("listo para registrar"));
            }

            return null;
        });
    }

    @Test
    void consultaInformaProductoNoDisponible()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                c.abrirDesdeConsulta("Medicamentos", 999);

                Label estado = campo(c, "lblEstado");
                assertTrue(estado.getText()
                        .contains("no disponible"));
            }

            return null;
        });
    }

    private static void escribirCampo(
            ControladorCatalogo controlador,
            String nombre,
            String valor) throws Exception {
        TextField campoTexto = campo(controlador, nombre);
        campoTexto.setText(valor);
    }

    private static <T> T construirProducto(
            ControladorCatalogo controlador,
            String nombreMetodo,
            Class<T> tipo) throws Exception {
        Method metodo = ControladorCatalogo.class
                .getDeclaredMethod(nombreMetodo);
        metodo.setAccessible(true);
        return tipo.cast(metodo.invoke(controlador));
    }

    @Test
    void construyeAseoDesdeFormulario() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            escribirCampo(c, "txtCodigo", "ASE-100");
            escribirCampo(c, "txtNombre", "Jabon liquido");
            escribirCampo(c, "txtDescripcion", "Higiene");
            escribirCampo(c, "txtPrecio", "12500.50");
            escribirCampo(c, "txtStock", "12");
            escribirCampo(c, "txtStockMinimoAseo", "4");
            escribirCampo(c, "txtTipoAseo", "Personal");
            escribirCampo(c, "txtComponentesActivos", "Glicerina");

            CheckBox biodegradable = campo(c, "chkBiodegradable");
            biodegradable.setSelected(true);

            Aseo producto = construirProducto(
                    c, "construirAseo", Aseo.class);

            assertAll(
                    () -> assertEquals("ASE-100",
                            producto.getCodigo()),
                    () -> assertEquals("Jabon liquido",
                            producto.getNombre()),
                    () -> assertEquals(12500.50,
                            producto.getPrecio()),
                    () -> assertEquals(12,
                            producto.getStock()),
                    () -> assertEquals(4,
                            producto.getStockMinimo()),
                    () -> assertEquals("Personal",
                            producto.getTipoAseo()),
                    () -> assertTrue(producto.isBiodegradable())
            );

            return null;
        });
    }

    @Test
    void construyeMaternidadDesdeFormulario() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            escribirCampo(c, "txtCodigo", "MAT-100");
            escribirCampo(c, "txtNombre", "Kit prenatal");
            escribirCampo(c, "txtDescripcion", "Cuidado maternal");
            escribirCampo(c, "txtPrecio", "45000");
            escribirCampo(c, "txtStock", "7");
            escribirCampo(c, "txtStockMinimoMaternidad", "2");
            escribirCampo(c, "txtEdadGestacional", "28");
            escribirCampo(c, "txtEtapa", "Gestacion");

            CheckBox hipoalergenico =
                    campo(c, "chkHipoalergenico");
            hipoalergenico.setSelected(true);

            Maternidad producto = construirProducto(
                    c, "construirMaternidad", Maternidad.class);

            assertAll(
                    () -> assertEquals("MAT-100",
                            producto.getCodigo()),
                    () -> assertEquals(7,
                            producto.getStock()),
                    () -> assertEquals(2,
                            producto.getStockMinimo()),
                    () -> assertEquals(28,
                            producto.getEdadGestacionalSugerida()),
                    () -> assertEquals("Gestacion",
                            producto.getEtapaRecomendada()),
                    () -> assertTrue(producto.isHipoalergenico())
            );

            return null;
        });
    }

    @Test
    void construyeMedicamentoDesdeFormulario() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            escribirCampo(c, "txtCodigo", "INV-100");
            escribirCampo(c, "txtNombre", "Acetaminofen");
            escribirCampo(c, "txtPrincipioActivo", "Paracetamol");
            escribirCampo(c, "txtConcentracion", "500 mg");
            escribirCampo(c, "txtFormaFarmaceutica", "Tableta");
            escribirCampo(c, "txtPresentacion", "Caja");
            escribirCampo(c, "txtStockMinimo", "5");
            escribirCampo(c, "txtPrecioMedicamento", "2500.50");

            ComboBox<ControladorCatalogo.CategoriaMedicamento>
                    categoria = campo(
                            c, "cmbCategoriaMedicamento");
            categoria.getSelectionModel().selectFirst();

            Medicamento producto = construirProducto(
                    c, "construirMedicamento", Medicamento.class);

            assertAll(
                    () -> assertEquals("INV-100",
                            producto.getCodigoInvima()),
                    () -> assertEquals("Acetaminofen",
                            producto.getNombreComercial()),
                    () -> assertEquals("Paracetamol",
                            producto.getPrincipioActivo()),
                    () -> assertEquals(5,
                            producto.getStockMinimo()),
                    () -> assertEquals(
                            new BigDecimal("2500.50"),
                            producto.getPrecio()),
                    () -> assertEquals(0,
                            producto.getStockTotal()),
                    () -> assertEquals("ACTIVO",
                            producto.getEstado())
            );

            return null;
        });
    }

    @Test
    void rechazaMedicamentoSinCategoria() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            java.lang.reflect.InvocationTargetException error =
                    assertThrows(
                        java.lang.reflect.InvocationTargetException.class,
                        () -> construirProducto(
                                c, "construirMedicamento",
                                Medicamento.class)
                    );

            assertInstanceOf(
                    IllegalArgumentException.class,
                    error.getCause());

            return null;
        });
    }

    @Test
    void rechazaMedicamentoConPrecioInvalido()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            escribirCampo(c, "txtCodigo", "INV-200");
            escribirCampo(c, "txtNombre", "Loratadina");
            escribirCampo(c, "txtStockMinimo", "2");
            escribirCampo(c, "txtPrecioMedicamento", "-500");

            ComboBox<ControladorCatalogo.CategoriaMedicamento>
                    categoria = campo(
                            c, "cmbCategoriaMedicamento");
            categoria.getSelectionModel().selectFirst();

            java.lang.reflect.InvocationTargetException error =
                    assertThrows(
                        java.lang.reflect.InvocationTargetException.class,
                        () -> construirProducto(
                                c, "construirMedicamento",
                                Medicamento.class)
                    );

            assertInstanceOf(
                    IllegalArgumentException.class,
                    error.getCause());

            return null;
        });
    }

    @Test
    void rechazaAseoConStockMinimoNegativo()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            escribirCampo(c, "txtCodigo", "ASE-200");
            escribirCampo(c, "txtNombre", "Desinfectante");
            escribirCampo(c, "txtDescripcion", "Limpieza");
            escribirCampo(c, "txtPrecio", "5000");
            escribirCampo(c, "txtStock", "10");
            escribirCampo(c, "txtStockMinimoAseo", "-1");

            java.lang.reflect.InvocationTargetException error =
                    assertThrows(
                        java.lang.reflect.InvocationTargetException.class,
                        () -> construirProducto(
                                c, "construirAseo", Aseo.class)
                    );

            assertInstanceOf(
                    IllegalStateException.class,
                    error.getCause());

            return null;
        });
    }

}
