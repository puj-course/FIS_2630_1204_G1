package org.example.carestock.controller;

import javafx.application.Platform;

import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import org.example.carestock.facade.Inventario;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.mockito.MockedConstruction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.example.carestock.service.ServicioAlertasInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.model.Lote;
import org.example.carestock.facade.Inventario;
import javafx.scene.control.TableColumn;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
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


    // === Cobertura de Inventario y Alertas ===

    private static void insertar(Object objeto, String nombre, Object valor)
            throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T obtener(Object objeto, String nombre)
            throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static Object llamar(Object objeto, String metodo)
            throws Exception {
        Method m = objeto.getClass().getDeclaredMethod(metodo);
        m.setAccessible(true);
        return m.invoke(objeto);
    }

    private static ControladorInventario inventarioFormulario()
            throws Exception {
        ControladorInventario c = new ControladorInventario();

        for (String nombre : new String[]{
                "lblUsuarioSesion", "lblEmailSesion",
                "lblRolSesion", "lblFarmaciaSesion",
                "lblTotalLotes", "lblLotesVencidos",
                "lblLoteSeleccionadoInfo", "lblTotalMedicamentos",
                "lblTotalAseo", "lblTotalMaternidad"}) {
            insertar(c, nombre, new Label());
        }

        for (String nombre : new String[]{
                "btnCatalogo", "btnHistorial", "btnAjustes",
                "btnAlertas", "btnExportarCSV", "btnNuevoLote",
                "btnEditarLote", "btnRetirarLote"}) {
            insertar(c, nombre, new Button());
        }

        for (String nombre : new String[]{
                "colIdLote", "colNumeroLote",
                "colIdMedicamento", "colCantidad",
                "colFechaVencimiento", "colEstado"}) {
            insertar(c, nombre, new TableColumn<>());
        }

        insertar(c, "tablaLotes", new TableView<Lote>());
        insertar(c, "txtBuscar", new TextField());
        insertar(c, "txtCantidad", new TextField());
        insertar(c, "dateInicio", new DatePicker());
        insertar(c, "dateFin", new DatePicker());

        ComboBox<String> categorias = new ComboBox<>();
        categorias.getItems().addAll(
                "Todas", "Analgesicos", "Antibioticos");
        categorias.getSelectionModel().selectFirst();

        insertar(c, "cmbCategoria", categorias);
        insertar(c, "inventario", mock(Inventario.class));

        return c;
    }

    private static Lote lotePrueba(
            int id, String numero, String estado,
            int diasIngreso, int diasVencimiento) {
        Lote lote = new Lote();
        lote.setIdLote(id);
        lote.setIdMedicamento(id);
        lote.setNumeroLote(numero);
        lote.setEstadoLote(estado);
        lote.setCantidadActual(10);
        lote.setFechaIngreso(
                LocalDate.now().plusDays(diasIngreso).atStartOfDay());
        lote.setFechaVencimiento(
                LocalDate.now().plusDays(diasVencimiento));
        return lote;
    }

    @SuppressWarnings("unchecked")
    private static void prepararInventario(ControladorInventario c)
            throws Exception {
        llamar(c, "configurarFiltroBusqueda");

        javafx.collections.ObservableList<Lote> lista =
                obtener(c, "listaLotes");

        lista.setAll(
                lotePrueba(1, "LOT-A", "DISPONIBLE", -4, 15),
                lotePrueba(2, "LOT-B", "DISPONIBLE", -1, -2),
                lotePrueba(3, "LOT-C", "RETENIDO", -2, 8)
        );

        insertar(c, "categoriaPorMedicamento",
                Map.of(1, "Analgesicos",
                       2, "Antibioticos",
                       3, "Analgesicos"));

        llamar(c, "aplicarFiltros");
    }

    @Test
    void inventarioFiltraPorTextoYCategoria() throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            prepararInventario(c);

            TableView<Lote> tabla = obtener(c, "tablaLotes");
            TextField buscar = obtener(c, "txtBuscar");
            ComboBox<String> categorias = obtener(c, "cmbCategoria");

            assertEquals(3, tabla.getItems().size());

            buscar.setText("LOT-A");
            assertEquals(1, tabla.getItems().size());
            assertEquals("LOT-A",
                    tabla.getItems().get(0).getNumeroLote());

            buscar.setText("RETENIDO");
            assertEquals(1, tabla.getItems().size());

            buscar.clear();
            categorias.setValue("Analgesicos");
            assertEquals(2, tabla.getItems().size());

            categorias.setValue("Antibioticos");
            assertEquals(1, tabla.getItems().size());

            categorias.setValue("Todas");
            assertEquals(3, tabla.getItems().size());

            return null;
        });
    }

    @Test
    void inventarioFiltraPorRangoDeIngreso() throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            prepararInventario(c);

            DatePicker inicio = obtener(c, "dateInicio");
            DatePicker fin = obtener(c, "dateFin");
            TableView<Lote> tabla = obtener(c, "tablaLotes");

            inicio.setValue(LocalDate.now().minusDays(2));
            fin.setValue(LocalDate.now());
            llamar(c, "aplicarFiltros");

            assertEquals(2, tabla.getItems().size());

            inicio.setValue(LocalDate.now().plusDays(1));
            llamar(c, "aplicarFiltros");

            assertTrue(tabla.getItems().isEmpty());

            inicio.setValue(null);
            fin.setValue(null);
            llamar(c, "aplicarFiltros");
            assertEquals(3, tabla.getItems().size());

            return null;
        });
    }

    @Test
    void inventarioFiltraVencidosDisponiblesYTodos()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            prepararInventario(c);

            TableView<Lote> tabla = obtener(c, "tablaLotes");

            llamar(c, "onFiltrarVencidos");
            assertEquals(1, tabla.getItems().size());
            assertEquals("LOT-B",
                    tabla.getItems().get(0).getNumeroLote());

            llamar(c, "onFiltrarDisponibles");
            assertEquals(1, tabla.getItems().size());
            assertEquals("LOT-A",
                    tabla.getItems().get(0).getNumeroLote());

            llamar(c, "onFiltrarTodos");
            assertEquals(3, tabla.getItems().size());

            return null;
        });
    }

    @Test
    void inventarioPresentaCorrectamenteEstadosSanitarios()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();

            Method metodo = ControladorInventario.class
                    .getDeclaredMethod("estadoMostrado", Lote.class);
            metodo.setAccessible(true);

            Lote retenido = lotePrueba(
                    1, "R", "RETENIDO", -1, 10);
            Lote vencido = lotePrueba(
                    2, "V", "DISPONIBLE", -1, -1);
            Lote disponible = lotePrueba(
                    3, "D", "DISPONIBLE", -1, 10);
            Lote sinEstado = lotePrueba(
                    4, "S", null, -1, 10);

            assertEquals("RETENIDO", metodo.invoke(c, retenido));
            assertEquals("DESHABILITADO",
                    metodo.invoke(c, vencido));
            assertEquals("DISPONIBLE",
                    metodo.invoke(c, disponible));
            assertEquals("SIN ESTADO",
                    metodo.invoke(c, sinEstado));

            return null;
        });
    }

    @Test
    void inventarioCargaLotesYContadoresSinBaseDeDatos()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            llamar(c, "configurarFiltroBusqueda");

            Inventario fachada = obtener(c, "inventario");
            List<Lote> lotes = List.of(
                    lotePrueba(1, "OK", "DISPONIBLE", -1, 20),
                    lotePrueba(2, "VENCIDO", "DISPONIBLE", -2, -1)
            );

            when(fachada.listarLotes()).thenReturn(lotes);
            when(fachada.categoriaPorMedicamento())
                    .thenReturn(Map.of(1, "Analgesicos"));
            when(fachada.contarPorCategoria())
                    .thenReturn(Map.of(
                            "Medicamento", 2,
                            "Aseo", 4,
                            "Maternidad", 1));

            Button alertas = obtener(c, "btnAlertas");
            alertas.setVisible(false);

            llamar(c, "cargarDatosTabla");
            llamar(c, "cargarContadoresPorCategoria");

            TableView<Lote> tabla = obtener(c, "tablaLotes");
            Label total = obtener(c, "lblTotalLotes");
            Label vencidos = obtener(c, "lblLotesVencidos");
            Label medicamentos = obtener(c, "lblTotalMedicamentos");
            Label aseo = obtener(c, "lblTotalAseo");
            Label maternidad = obtener(c, "lblTotalMaternidad");

            assertEquals(2, tabla.getItems().size());
            assertEquals("2", total.getText());
            assertEquals("1", vencidos.getText());
            assertEquals("2", medicamentos.getText());
            assertEquals("4", aseo.getText());
            assertEquals("1", maternidad.getText());

            verify(fachada).listarLotes();
            verify(fachada).contarPorCategoria();

            return null;
        });
    }

    @Test
    void inventarioContinuaSiFallanLasCategorias()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            llamar(c, "configurarFiltroBusqueda");

            Inventario fachada = obtener(c, "inventario");
            when(fachada.listarLotes()).thenReturn(
                    List.of(lotePrueba(
                            1, "L1", "DISPONIBLE", -1, 20)));
            when(fachada.categoriaPorMedicamento())
                    .thenThrow(new IllegalStateException(
                            "Fallo simulado de categorias"));

            Button alertas = obtener(c, "btnAlertas");
            alertas.setVisible(false);

            llamar(c, "cargarDatosTabla");

            TableView<Lote> tabla = obtener(c, "tablaLotes");
            Map<?, ?> categorias =
                    obtener(c, "categoriaPorMedicamento");

            assertEquals(1, tabla.getItems().size());
            assertTrue(categorias.isEmpty());

            return null;
        });
    }

    @Test
    void inventarioResumenAlertasCubreTresSeveridades()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            Button boton = obtener(c, "btnAlertas");

            insertar(c, "btnAlertas", boton);

            Method resumen = ControladorInventario.class
                    .getDeclaredMethod(
                            "mostrarResumenAlertas",
                            ServicioAlertasInventario.Resumen.class);
            resumen.setAccessible(true);

            resumen.invoke(c,
                    new ServicioAlertasInventario.Resumen(
                            2, 1, 2, 1));
            assertEquals("Alertas (3)", boton.getText());
                      assertTrue(boton.getStyle().contains("#F0A8A8"));

            resumen.invoke(c,
                    new ServicioAlertasInventario.Resumen(
                            0, 2, 1, 1));
            assertEquals("Alertas (2)", boton.getText());
                       assertTrue(boton.getStyle().contains("#F6CE8E"));

            resumen.invoke(c,
                    new ServicioAlertasInventario.Resumen(
                            0, 0, 0, 0));
            assertEquals("Alertas", boton.getText());
                      assertTrue(boton.getStyle().contains("#EDE7F7"));
            return null;
        });
    }

    private static ControladorAlertas alertasFormulario()
            throws Exception {
        ControladorAlertas c = new ControladorAlertas();

        for (String nombre : new String[]{
                "lblFarmacia", "lblCriticas", "lblAdvertencias",
                "lblTotal", "lblActualizado", "lblEstado"}) {
            insertar(c, nombre, new Label());
        }

        for (String nombre : new String[]{
                "cmbTipo", "cmbCategoria", "cmbSeveridad"}) {
            insertar(c, nombre, new ComboBox<String>());
        }

        insertar(c, "btnActualizar", new Button());
        insertar(c, "tablaAlertas",
                new TableView<AlertaInventario>());

        for (String nombre : new String[]{
                "colSeveridad", "colTipo", "colCategoria",
                "colProducto", "colReferencia",
                "colExistencias", "colDetalle",
                "colUltimoMovimiento"}) {
            insertar(c, nombre, new TableColumn<>());
        }

        insertar(c, "inventario", mock(Inventario.class));
        return c;
    }

    private static AlertaInventario alertaStock() {
        return new AlertaInventario(
                AlertaInventario.TipoAlerta.STOCK_MINIMO,
                AlertaInventario.Categoria.ASEO,
                1, "ASE-1", "Jabon", null,
                0, 5, null, null,
                AlertaInventario.Severidad.CRITICA,
                null, null, null
        );
    }

    private static AlertaInventario alertaVencimiento() {
        return new AlertaInventario(
                AlertaInventario.TipoAlerta.VENCIMIENTO,
                AlertaInventario.Categoria.MEDICAMENTO,
                2, "MED-2", "Acetaminofen", "LOT-2",
                8, null, LocalDate.now().plusDays(10), 10,
                AlertaInventario.Severidad.ADVERTENCIA,
                LocalDateTime.of(2026, 10, 25, 10, 30),
                "ENTRADA", "Administrador"
        );
    }

    @Test
    void alertasFiltranPorTipoCategoriaYSeveridad()
            throws Exception {
        enFx(() -> {
            ControladorAlertas c = alertasFormulario();
            llamar(c, "configurarFiltros");

            insertar(c, "todas",
                    List.of(alertaStock(), alertaVencimiento()));

            llamar(c, "aplicarFiltros");

            TableView<AlertaInventario> tabla =
                    obtener(c, "tablaAlertas");
            assertEquals(2, tabla.getItems().size());

            ComboBox<String> tipo = obtener(c, "cmbTipo");
            tipo.setValue(
                    AlertaInventario.TipoAlerta.STOCK_MINIMO.etiqueta());

            assertEquals(1, tabla.getItems().size());
            assertEquals("Jabon",
                    tabla.getItems().get(0).producto());

            ComboBox<String> categoria =
                    obtener(c, "cmbCategoria");
            categoria.setValue(
                    AlertaInventario.Categoria.MATERNIDAD.etiqueta());
            assertEquals(0, tabla.getItems().size());

            tipo.setValue("Todos");
            categoria.setValue("Todos");

            ComboBox<String> severidad =
                    obtener(c, "cmbSeveridad");
            severidad.setValue(
                    AlertaInventario.Severidad.ADVERTENCIA.etiqueta());

            assertEquals(1, tabla.getItems().size());
            assertEquals("Acetaminofen",
                    tabla.getItems().get(0).producto());

            return null;
        });
    }

    @Test
    void alertasMuestranResumenYEstadoSinAlertas()
            throws Exception {
        enFx(() -> {
            ControladorAlertas c = alertasFormulario();
            llamar(c, "configurarFiltros");

            llamar(c, "aplicarFiltros");

            Label estado = obtener(c, "lblEstado");
            assertTrue(estado.getText().contains("No hay alertas"));

            insertar(c, "todas",
                    List.of(alertaStock(), alertaVencimiento()));

            llamar(c, "actualizarResumen");
            llamar(c, "aplicarFiltros");

            Label criticas = obtener(c, "lblCriticas");
            Label advertencias = obtener(c, "lblAdvertencias");
            Label total = obtener(c, "lblTotal");

            assertEquals("Críticas: 1", criticas.getText());
            assertEquals("Advertencias: 1",
                    advertencias.getText());
            assertEquals("Total: 2", total.getText());
            assertTrue(estado.getText().contains("Mostrando 2 de 2"));

            return null;
        });
    }

    @Test
    void alertasConfiguranColumnasDeLaTabla()
            throws Exception {
        enFx(() -> {
            ControladorAlertas c = alertasFormulario();
            llamar(c, "configurarColumnas");
            llamar(c, "configurarFilas");

            TableView<AlertaInventario> tabla =
                    obtener(c, "tablaAlertas");

            assertNotNull(tabla.getRowFactory());

            for (String nombre : new String[]{
                    "colSeveridad", "colTipo", "colCategoria",
                    "colProducto", "colReferencia",
                    "colExistencias", "colDetalle",
                    "colUltimoMovimiento"}) {
                TableColumn<?, ?> columna = obtener(c, nombre);
                assertNotNull(columna.getCellValueFactory(), nombre);
            }

            return null;
        });
    }

    @Test
    void alertasFormateanMovimientosConYSinFecha()
            throws Exception {
        Method metodo = ControladorAlertas.class
                .getDeclaredMethod(
                        "textoUltimoMovimiento",
                        AlertaInventario.class);
        metodo.setAccessible(true);

        assertEquals(
                "Sin movimientos registrados",
                metodo.invoke(null, alertaStock())
        );

        String resultado = (String) metodo.invoke(
                null, alertaVencimiento());

        assertTrue(resultado.contains("ENTRADA"));
        assertTrue(resultado.contains("Administrador"));
        assertTrue(resultado.contains("25/10/2026"));
    }

    @Test
    void inventarioRechazaInicializacionSinSesion()
            throws Exception {
        enFx(() -> {
            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(false);

                ControladorInventario c =
                        new ControladorInventario();

                assertThrows(
                        IllegalStateException.class,
                        c::initialize);
            }
            return null;
        });
    }

    @Test
    void alertasRechazanSesionSinFarmacia()
            throws Exception {
        enFx(() -> {
            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                        .thenReturn(true);
                sesion.when(SesionUsuario::actual)
                        .thenReturn(
                                new SesionUsuario.DatosUsuario(
                                        1, "Prueba", "test@local",
                                        "AUXILIAR", null, null));

                ControladorAlertas c = new ControladorAlertas();

                assertThrows(
                        IllegalStateException.class,
                        c::initialize);
            }
            return null;
        });
    }


    // ===== QA: cobertura de flujos del catalogo =====

    private static ControladorCatalogo catalogoPreparado(
            String categoria, Inventario fachada) throws Exception {
        ControladorCatalogo c = formulario();
        campo(c, "inventario", fachada);

        ComboBox<String> combo = campo(c, "cmbCategoria");
        combo.setValue(categoria);

        escribirCampo(c, "txtCodigo", "COD-001");
        escribirCampo(c, "txtNombre", "Producto QA");
        escribirCampo(c, "txtDescripcion", "Descripcion QA");
        escribirCampo(c, "txtPrecio", "12000");
        escribirCampo(c, "txtStock", "10");
        escribirCampo(c, "txtStockMinimoAseo", "2");
        escribirCampo(c, "txtStockMinimoMaternidad", "2");
        escribirCampo(c, "txtEdadGestacional", "28");
        escribirCampo(c, "txtStockMinimo", "2");
        escribirCampo(c, "txtPrecioMedicamento", "2500");
        escribirCampo(c, "txtPrincipioActivo", "Activo QA");

        ComboBox<ControladorCatalogo.CategoriaMedicamento> cat =
                campo(c, "cmbCategoriaMedicamento");
        cat.getSelectionModel().selectFirst();

        return c;
    }

    private static void ejecutarSinArgumentos(
            ControladorCatalogo c, String nombre) throws Exception {
        ejecutar(c, nombre, new Class<?>[]{});
    }

    private static void prepararColumnasCatalogo(
            ControladorCatalogo c) throws Exception {
        for (String nombre : new String[]{
                "colId", "colCodigo", "colNombre",
                "colDescripcion", "colPrecio",
                "colStock", "colEstado"}) {
            campo(c, nombre, new TableColumn<>());
        }
    }

    @Test
    void catalogoCoberturaCargaTresCategorias() throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);

            Aseo aseo = mock(Aseo.class);
            Maternidad maternidad = mock(Maternidad.class);
            Medicamento medicamento = mock(Medicamento.class);

            when(fachada.listarAseo()).thenReturn(
                    List.of(new RegistroCatalogo<>(
                            1, aseo, "ACTIVO")));
            when(fachada.listarMaternidad()).thenReturn(
                    List.of(new RegistroCatalogo<>(
                            2, maternidad, "ACTIVO")));
            when(fachada.listarMedicamentos()).thenReturn(
                    List.of(new RegistroCatalogo<>(
                            3, medicamento, "ACTIVO")));

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            TableView<RegistroCatalogo<?>> tabla =
                    campo(c, "tablaProductos");
            ComboBox<String> categorias =
                    campo(c, "cmbCategoria");
            Label estado = campo(c, "lblEstado");

            ejecutarSinArgumentos(c, "cargarTabla");
            assertEquals(1, tabla.getItems().get(0).id());
            assertTrue(estado.getText().contains("Aseo"));

            categorias.setValue("Maternidad");
            ejecutarSinArgumentos(c, "cargarTabla");
            assertEquals(2, tabla.getItems().get(0).id());

            categorias.setValue("Medicamentos");
            ejecutarSinArgumentos(c, "onRefrescar");
            assertEquals(3, tabla.getItems().get(0).id());

            verify(fachada).listarAseo();
            verify(fachada).listarMaternidad();
            verify(fachada).listarMedicamentos();

            return null;
        });
    }

    @Test
    void catalogoCoberturaCategoriasConservanSeleccion()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            when(fachada.listarCategoriasMedicamento())
                    .thenReturn(Map.of(
                            5, "Analgesicos",
                            7, "Antibioticos"));

            ControladorCatalogo c =
                    catalogoPreparado("Medicamentos", fachada);

            ComboBox<ControladorCatalogo.CategoriaMedicamento>
                    combo = campo(c, "cmbCategoriaMedicamento");

            ejecutarSinArgumentos(
                    c, "cargarCategoriasMedicamento");

            assertEquals(2, combo.getItems().size());
            assertEquals(5, combo.getValue().id());

            combo.getSelectionModel().clearSelection();
            ejecutarSinArgumentos(
                    c, "cargarCategoriasMedicamento");

            assertNull(combo.getValue());

            return null;
        });
    }

    @Test
    void catalogoCoberturaSeleccionActualizaFormulario()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Aseo", mock(Inventario.class));

            Aseo aseo = mock(Aseo.class);
            when(aseo.getCodigo()).thenReturn("ASE-045");
            when(aseo.getNombre()).thenReturn("Jabon QA");
            when(aseo.getStock()).thenReturn(12);

            TableView<RegistroCatalogo<?>> tabla =
                    campo(c, "tablaProductos");

            ejecutarSinArgumentos(c, "configurarSeleccion");

            RegistroCatalogo<Aseo> registro =
                    new RegistroCatalogo<>(
                            45, aseo, "ACTIVO");

            tabla.getItems().add(registro);
            tabla.getSelectionModel().select(registro);

            assertEquals("ASE-045", texto(c, "txtCodigo"));
            assertEquals("Jabon QA", texto(c, "txtNombre"));

            Label seleccion = campo(c, "lblSeleccion");
            assertTrue(seleccion.getText().contains("45"));

            tabla.getSelectionModel().clearSelection();
            assertEquals("Nuevo producto",
                    seleccion.getText());

            return null;
        });
    }

    @Test
    void catalogoCoberturaAlternaPanelesYCategorias()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            when(fachada.listarCategoriasMedicamento())
                    .thenReturn(Map.of(5, "Analgesicos"));

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            for (String nombre : new String[]{
                    "panelAseo", "panelMaternidad",
                    "panelMedicamentos", "panelDescripcion",
                    "panelPrecioStock"}) {
                campo(c, nombre, new VBox());
            }

            campo(c, "lblCodigoCampo", new Label());
            campo(c, "lblNombreCampo", new Label());

            ejecutarSinArgumentos(c, "configurarCategorias");

            ComboBox<String> categorias =
                    campo(c, "cmbCategoria");

            categorias.setValue("Maternidad");
            VBox panelMaternidad =
                    campo(c, "panelMaternidad");
            assertTrue(panelMaternidad.isVisible());

            categorias.setValue("Medicamentos");
            VBox panelMedicamentos =
                    campo(c, "panelMedicamentos");
            assertTrue(panelMedicamentos.isVisible());
            assertFalse(panelMaternidad.isVisible());

            Label codigo = campo(c, "lblCodigoCampo");
            assertEquals("Registro INVIMA", codigo.getText());

            categorias.setValue("Aseo");
            VBox panelAseo = campo(c, "panelAseo");
            assertTrue(panelAseo.isVisible());
            assertFalse(panelMedicamentos.isVisible());

            return null;
        });
    }

    @Test
    void catalogoCoberturaConfiguraColumnasYValores()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c =
                    catalogoPreparado(
                            "Aseo", mock(Inventario.class));
            prepararColumnasCatalogo(c);

            ejecutarSinArgumentos(c, "configurarTabla");

            Aseo producto = mock(Aseo.class);
            when(producto.getCodigo()).thenReturn("ASE-010");
            when(producto.getNombre()).thenReturn("Jabon");
            when(producto.getDescripcion())
                    .thenReturn("Higiene");
            when(producto.getPrecio()).thenReturn(1500.0);

            RegistroCatalogo<Aseo> fila =
                    new RegistroCatalogo<>(
                            10, producto, "ACTIVO");

            TableColumn<RegistroCatalogo<?>, String> codigo =
                    campo(c, "colCodigo");
            TableColumn<RegistroCatalogo<?>, String> nombre =
                    campo(c, "colNombre");
            TableColumn<RegistroCatalogo<?>, String> precio =
                    campo(c, "colPrecio");

            // Asociar columnas y fila a la tabla antes de leer valores.
            TableView<RegistroCatalogo<?>> tabla =
                    campo(c, "tablaProductos");

            tabla.getColumns().setAll(
                    codigo, nombre, precio);
            tabla.getItems().add(fila);

            assertEquals(
                    "ASE-010",
                    codigo.getCellObservableValue(fila).getValue());
            assertEquals(
                    "Jabon",
                    nombre.getCellObservableValue(fila).getValue());
            assertEquals(
                    "1500.0",
                    precio.getCellObservableValue(fila).getValue());

            assertNotNull(tabla.getRowFactory());

            return null;
        });
    }

    @Test
    void catalogoCoberturaRegistraTresTiposDeProducto()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            when(fachada.registrarAseo(any(Aseo.class)))
                    .thenReturn(11);
            when(fachada.registrarMaternidad(
                    any(Maternidad.class))).thenReturn(12);

            Medicamento registrado = mock(Medicamento.class);
            when(registrado.getIdMedicamento()).thenReturn(13);
            when(fachada.registrarMedicamento(
                    any(Medicamento.class))).thenReturn(registrado);

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos =
                         mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                for (String categoria : new String[]{
                        "Aseo", "Maternidad", "Medicamentos"}) {
                    ControladorCatalogo c =
                            catalogoPreparado(categoria, fachada);

                    ejecutarSinArgumentos(c, "onGuardar");

                    Label seleccion = campo(c, "lblSeleccion");
                    assertEquals(
                            "Nuevo producto", seleccion.getText());
                }

                verify(fachada).registrarAseo(any(Aseo.class));
                verify(fachada).registrarMaternidad(
                        any(Maternidad.class));
                verify(fachada).registrarMedicamento(
                        any(Medicamento.class));

                assertEquals(3, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void catalogoCoberturaActualizaTresTiposDeProducto()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);

            when(fachada.actualizarAseo(
                    eq(45), any(Aseo.class))).thenReturn(true);
            when(fachada.actualizarMaternidad(
                    eq(45), any(Maternidad.class)))
                    .thenReturn(true);
            when(fachada.actualizarMedicamento(
                    eq(45), any(Medicamento.class)))
                    .thenReturn(true);

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos =
                         mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                for (String categoria : new String[]{
                        "Aseo", "Maternidad", "Medicamentos"}) {
                    ControladorCatalogo c =
                            catalogoPreparado(categoria, fachada);

                    campo(c, "seleccionado",
                            new RegistroCatalogo<>(
                                    45, mock(Aseo.class), "ACTIVO"));

                    ejecutarSinArgumentos(c, "onActualizar");
                }

                verify(fachada).actualizarAseo(
                        eq(45), any(Aseo.class));
                verify(fachada).actualizarMaternidad(
                        eq(45), any(Maternidad.class));
                verify(fachada).actualizarMedicamento(
                        eq(45), any(Medicamento.class));

                assertEquals(3, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void catalogoCoberturaDesactivaTresCategorias()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);

            when(fachada.desactivarAseo(45)).thenReturn(true);
            when(fachada.desactivarMaternidad(45)).thenReturn(true);
            when(fachada.desactivarMedicamento(45))
                    .thenReturn(true);

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos =
                         mockConstruction(
                                 Alert.class,
                                 (alerta, contexto) ->
                                         when(alerta.showAndWait())
                                                 .thenReturn(Optional.of(
                                                         ButtonType.YES)))) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                for (String categoria : new String[]{
                        "Aseo", "Maternidad", "Medicamentos"}) {
                    ControladorCatalogo c =
                            catalogoPreparado(categoria, fachada);

                    campo(c, "seleccionado",
                            new RegistroCatalogo<>(
                                    45, mock(Aseo.class), "ACTIVO"));

                    ejecutarSinArgumentos(c, "onDesactivar");

                    Label estado = campo(c, "lblEstado");
                    assertEquals(
                            "Producto desactivado.",
                            estado.getText());
                }

                verify(fachada).desactivarAseo(45);
                verify(fachada).desactivarMaternidad(45);
                verify(fachada).desactivarMedicamento(45);

                assertEquals(3, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void catalogoCoberturaCancelacionNoDesactiva()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            campo(c, "seleccionado",
                    new RegistroCatalogo<>(
                            45, mock(Aseo.class), "ACTIVO"));

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos =
                         mockConstruction(
                                 Alert.class,
                                 (alerta, contexto) ->
                                         when(alerta.showAndWait())
                                                 .thenReturn(Optional.of(
                                                         ButtonType.NO)))) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                ejecutarSinArgumentos(c, "onDesactivar");

                verify(fachada, never()).desactivarAseo(anyInt());
            }

            return null;
        });
    }

    @Test
    void catalogoCoberturaGestionaErroresDeConsulta()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            when(fachada.listarAseo())
                    .thenThrow(new IllegalStateException(
                            "Error de consulta simulado"));

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            try (MockedConstruction<Alert> dialogos =
                         mockConstruction(Alert.class)) {
                ejecutarSinArgumentos(c, "cargarTabla");

                Label estado = campo(c, "lblEstado");
                TableView<?> tabla = campo(c, "tablaProductos");

                assertEquals(
                        "No se pudo cargar el catalogo",
                        estado.getText());
                assertTrue(tabla.getItems().isEmpty());
                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void catalogoCoberturaRechazaModificacionesSinPermiso()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos =
                         mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(false);

                ejecutarSinArgumentos(c, "onGuardar");
                ejecutarSinArgumentos(c, "onActualizar");
                ejecutarSinArgumentos(c, "onDesactivar");

                verifyNoInteractions(fachada);
                assertEquals(3, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void catalogoCoberturaRechazaIdDeRegistroInvalido()
            throws Exception {
        enFx(() -> {
            Inventario fachada = mock(Inventario.class);
            when(fachada.registrarAseo(any(Aseo.class)))
                    .thenReturn(0);

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            try (MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> dialogos =
                         mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                ejecutarSinArgumentos(c, "onGuardar");

                Label estado = campo(c, "lblEstado");
                assertEquals(
                        "No se pudo registrar", estado.getText());
                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }



    // ===== QA: segundo lote de cobertura Catalogo/Inventario =====

    private static Object invocarQa(
            Object destino, String metodo, Class<?>[] tipos,
            Object... argumentos) throws Exception {
        java.lang.reflect.Method m =
                destino.getClass().getDeclaredMethod(metodo, tipos);
        m.setAccessible(true);
        return m.invoke(destino, argumentos);
    }

    private static Object invocarQa(
            Object destino, String metodo) throws Exception {
        return invocarQa(destino, metodo, new Class<?>[0]);
    }

    @Test
    void catalogoQaPreciosValidos() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Medicamentos",
                    mock(Inventario.class));

            javafx.scene.control.TextField precio =
                    campo(c, "txtPrecioMedicamento");

            precio.clear();
            assertNull(invocarQa(c, "leerPrecioMedicamento"));

            precio.setText("  ");
            assertNull(invocarQa(c, "leerPrecioMedicamento"));

            precio.setText("0");
            assertEquals(
                    new java.math.BigDecimal("0"),
                    invocarQa(c, "leerPrecioMedicamento"));

            precio.setText("15000.50");
            assertEquals(
                    new java.math.BigDecimal("15000.50"),
                    invocarQa(c, "leerPrecioMedicamento"));

            precio.setText("9999999999.99");
            assertEquals(
                    new java.math.BigDecimal("9999999999.99"),
                    invocarQa(c, "leerPrecioMedicamento"));

            return null;
        });
    }

    @Test
    void catalogoQaPreciosInvalidos() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Medicamentos",
                    mock(Inventario.class));

            javafx.scene.control.TextField precio =
                    campo(c, "txtPrecioMedicamento");

            for (String invalido : new String[]{
                    "-1", "12.345", "10000000000",
                    "no-es-numero", "18,50"}) {

                precio.setText(invalido);

                java.lang.reflect.InvocationTargetException error =
                        assertThrows(
                                java.lang.reflect.InvocationTargetException.class,
                                () -> invocarQa(
                                        c, "leerPrecioMedicamento"));

                assertInstanceOf(
                        IllegalArgumentException.class,
                        error.getCause(),
                        "Precio invalido: " + invalido);
            }

            return null;
        });
    }

    @Test
    void catalogoQaLlenaFormularioAseo() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Aseo", mock(Inventario.class));

            Aseo p = new Aseo();
            p.setCodigo("ASE-QA");
            p.setNombre("Jabon antibacterial");
            p.setDescripcion("Producto de higiene");
            p.setPrecio(3500);
            p.setStock(12);
            p.setStockMinimo(3);
            p.setTipoAseo("Jabon");
            p.setComponentesActivos("Glicerina");
            p.setBiodegradable(true);

            invocarQa(
                    c, "llenarFormulario",
                    new Class<?>[]{Object.class}, p);

            javafx.scene.control.TextField codigo =
                    campo(c, "txtCodigo");
            javafx.scene.control.TextField nombre =
                    campo(c, "txtNombre");
            javafx.scene.control.TextField minimo =
                    campo(c, "txtStockMinimoAseo");
            javafx.scene.control.TextField tipo =
                    campo(c, "txtTipoAseo");
            javafx.scene.control.CheckBox biodegradable =
                    campo(c, "chkBiodegradable");

            assertEquals("ASE-QA", codigo.getText());
            assertEquals("Jabon antibacterial", nombre.getText());
            assertEquals("3", minimo.getText());
            assertEquals("Jabon", tipo.getText());
            assertTrue(biodegradable.isSelected());

            return null;
        });
    }

    @Test
    void catalogoQaLlenaFormularioMaternidad() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Maternidad", mock(Inventario.class));

            Maternidad p = new Maternidad();
            p.setCodigo("MAT-QA");
            p.setNombre("Producto prenatal");
            p.setDescripcion("Cuidado materno");
            p.setPrecio(25000);
            p.setStock(7);
            p.setStockMinimo(2);
            p.setEtapaRecomendada("Prenatal");
            p.setEdadGestacionalSugerida(28);
            p.setHipoalergenico(true);

            invocarQa(
                    c, "llenarFormulario",
                    new Class<?>[]{Object.class}, p);

            javafx.scene.control.TextField etapa =
                    campo(c, "txtEtapa");
            javafx.scene.control.TextField edad =
                    campo(c, "txtEdadGestacional");
            javafx.scene.control.TextField minimo =
                    campo(c, "txtStockMinimoMaternidad");
            javafx.scene.control.CheckBox hipoalergenico =
                    campo(c, "chkHipoalergenico");

            assertEquals("Prenatal", etapa.getText());
            assertEquals("28", edad.getText());
            assertEquals("2", minimo.getText());
            assertTrue(hipoalergenico.isSelected());

            return null;
        });
    }

    @Test
    void catalogoQaLlenaFormularioMedicamento() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Medicamentos",
                    mock(Inventario.class));

            Medicamento p = new Medicamento();
            p.setCodigoInvima("INVIMA-QA");
            p.setNombreComercial("Medicamento QA");
            p.setPrincipioActivo("Acetaminofen");
            p.setConcentracion("500 mg");
            p.setFormaFarmaceutica("Tableta");
            p.setPresentacion("Caja");
            p.setStockMinimo(5);
            p.setStockTotal(80);
            p.setIdCategoria(5);
            p.setPrecio(
                    new java.math.BigDecimal("1750.50"));

            javafx.scene.control.ComboBox<
                    ControladorCatalogo.CategoriaMedicamento> combo =
                    campo(c, "cmbCategoriaMedicamento");

            combo.getItems().add(
                    new ControladorCatalogo.CategoriaMedicamento(
                            5, "Analgesicos"));

            invocarQa(
                    c, "llenarFormulario",
                    new Class<?>[]{Object.class}, p);

            javafx.scene.control.TextField precio =
                    campo(c, "txtPrecioMedicamento");
            javafx.scene.control.TextField activo =
                    campo(c, "txtPrincipioActivo");
            javafx.scene.control.Label stock =
                    campo(c, "lblStockMedicamento");

            assertEquals("1750.50", precio.getText());
            assertEquals("Acetaminofen", activo.getText());
            assertTrue(stock.getText().contains("80"));
            assertEquals(5, combo.getValue().id());

            return null;
        });
    }

    @Test
    void catalogoQaValidaAccesoDesdeConsulta() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = catalogoPreparado(
                    "Aseo", mock(Inventario.class));

            javafx.scene.control.Label estado =
                    campo(c, "lblEstado");

            try (org.mockito.MockedStatic<SesionUsuario> sesion =
                         mockStatic(SesionUsuario.class)) {

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(false);

                c.abrirDesdeConsulta("Aseo", null);

                assertTrue(
                        estado.getText().contains("Sin permisos"));

                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                assertThrows(
                        IllegalArgumentException.class,
                        () -> c.abrirDesdeConsulta(
                                "Categoria desconocida", null));

                c.abrirDesdeConsulta("Maternidad", 99999);

                assertTrue(
                        estado.getText().contains(
                                "Producto no disponible"));
            }

            return null;
        });
    }

    @Test
    void inventarioQaRechazaDispensacionesInvalidas()
            throws Exception {
        enFx(() -> {
            ControladorInventario c =
                    inventarioFormulario();

            Inventario fachada = obtener(c, "inventario");
            javafx.scene.control.TextField cantidad =
                    obtener(c, "txtCantidad");

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> alertas =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                // Sin seleccion.
                llamar(c, "onDispensar");

                // Lote no disponible.
                insertar(c, "loteSeleccionado",
                        lotePrueba(
                                1, "RET-1", "RETENIDO", -2, 10));
                cantidad.setText("2");
                llamar(c, "onDispensar");

                // Lote disponible, cantidad vacia.
                insertar(c, "loteSeleccionado",
                        lotePrueba(
                                2, "DIS-2", "DISPONIBLE", -2, 10));

                cantidad.clear();
                llamar(c, "onDispensar");

                // Cantidad no numerica.
                cantidad.setText("cantidad-invalida");
                llamar(c, "onDispensar");

                verifyNoInteractions(fachada);

                assertEquals(
                        4, alertas.constructed().size());
            }

            return null;
        });
    }

    @Test
    void inventarioQaDispensacionExitosaSinNeon()
            throws Exception {
        enFx(() -> {
            ControladorInventario c =
                    inventarioFormulario();

            llamar(c, "configurarFiltroBusqueda");

            Inventario fachada = obtener(c, "inventario");

            Lote lote = lotePrueba(
                    7, "QA-007", "DISPONIBLE", -1, 20);

            insertar(c, "loteSeleccionado", lote);

            javafx.scene.control.TextField cantidad =
                    obtener(c, "txtCantidad");
            cantidad.setText("3");

            javafx.scene.control.Button botonAlertas =
                    obtener(c, "btnAlertas");
            botonAlertas.setVisible(false);

            when(fachada.listarLotes())
                    .thenReturn(java.util.List.of(lote));

            when(fachada.categoriaPorMedicamento())
                    .thenReturn(java.util.Map.of());

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> alertas =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                llamar(c, "onDispensar");

                verify(fachada)
                        .dispensarMedicamento(7, 3);

                assertEquals("", cantidad.getText());

                verify(fachada).listarLotes();
                assertEquals(
                        1, alertas.constructed().size());
            }

            return null;
        });
    }



    // ===== QA: rutas adicionales del inventario =====

    @Test
    void inventarioQaRechazaEdicionSinSeleccion() throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> dialogos =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                llamar(c, "onEditarLote");

                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void inventarioQaRechazaRetiroSinSeleccion() throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> dialogos =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                llamar(c, "onRetirarLote");

                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void inventarioQaRechazaExportacionSinRegistros()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> dialogos =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                llamar(c, "onExportarCSV");

                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void inventarioQaControlaErrorAlConsultarLotes()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            Inventario fachada = obtener(c, "inventario");

            when(fachada.listarLotes())
                    .thenThrow(new IllegalStateException(
                            "Error de consulta simulado"));

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> dialogos =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                llamar(c, "cargarDatosTabla");

                assertEquals(1, dialogos.constructed().size());
            }

            verify(fachada).listarLotes();

            return null;
        });
    }

    @Test
    void inventarioQaConsultaCategoriasYControlaErrores()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            Inventario fachada = obtener(c, "inventario");

            when(fachada.listarCategoriasMedicamento())
                    .thenReturn(java.util.Map.of(
                            1, "Analgesicos",
                            2, "Antibioticos"))
                    .thenThrow(new IllegalStateException(
                            "Error de categorias simulado"));

            llamar(c, "cargarCategorias");

            javafx.scene.control.ComboBox<String> combo =
                    obtener(c, "cmbCategoria");

            assertTrue(combo.getItems().contains("Analgesicos"));
            assertTrue(combo.getItems().contains("Antibioticos"));

            try (org.mockito.MockedConstruction<
                    javafx.scene.control.Alert> dialogos =
                         mockConstruction(
                                 javafx.scene.control.Alert.class)) {

                llamar(c, "cargarCategorias");

                assertEquals(1, dialogos.constructed().size());
            }

            verify(fachada, times(2))
                    .listarCategoriasMedicamento();

            return null;
        });
    }



    // ===== Lote 4: controladores, permisos y alertas =====

    private static Object qa4Invocar(
            ControladorCatalogo controlador,
            String nombre, Object producto) throws Exception {
        java.lang.reflect.Method metodo =
                ControladorCatalogo.class.getDeclaredMethod(
                        nombre, Object.class);
        metodo.setAccessible(true);
        return metodo.invoke(controlador, producto);
    }

    private static ControladorCatalogo qa4FormularioCompleto()
            throws Exception {
        ControladorCatalogo c = formulario();

        for (String nombre : new String[]{
                "panelAseo", "panelMaternidad",
                "panelMedicamentos", "panelDescripcion",
                "panelPrecioStock", "panelFormulario"}) {
            campo(c, nombre, new javafx.scene.layout.VBox());
        }

        for (String nombre : new String[]{
                "colId", "colCodigo", "colNombre",
                "colDescripcion", "colPrecio",
                "colStock", "colEstado"}) {
            campo(c, nombre,
                    new javafx.scene.control.TableColumn<>());
        }

        campo(c, "lblCodigoCampo",
                new javafx.scene.control.Label());
        campo(c, "lblNombreCampo",
                new javafx.scene.control.Label());

        return c;
    }

    @Test
    void qaLote4InicializacionCatalogo() throws Exception {
        enFx(() -> {
            for (boolean administrador : new boolean[]{
                    true, false}) {
                try (
                    org.mockito.MockedStatic<SesionUsuario> sesion =
                            mockStatic(SesionUsuario.class);
                    org.mockito.MockedConstruction<
                            org.example.carestock.facade.Inventario>
                            fachadas = mockConstruction(
                                org.example.carestock.facade.Inventario.class,
                                (mock, contexto) -> {
                                    when(mock.listarCategoriasMedicamento())
                                            .thenReturn(
                                                    java.util.Map.of(
                                                            5, "Analgesicos"));
                                    when(mock.listarMedicamentos())
                                            .thenReturn(
                                                    java.util.List.of());
                                })
                ) {
                    sesion.when(SesionUsuario::esAdministrador)
                            .thenReturn(administrador);

                    ControladorCatalogo c =
                            qa4FormularioCompleto();

                    c.initialize();

                    javafx.scene.control.ComboBox<String> categorias =
                            campo(c, "cmbCategoria");

                    javafx.scene.layout.VBox formulario =
                            campo(c, "panelFormulario");

                    javafx.scene.control.TableView<?> tabla =
                            campo(c, "tablaProductos");

                    assertEquals("Medicamentos",
                            categorias.getValue());
                    assertEquals(!administrador,
                            formulario.isDisabled());
                    assertNotNull(tabla.getRowFactory());
                    assertEquals(1, fachadas.constructed().size());

                    if (!administrador) {
                        javafx.scene.control.Label estado =
                                campo(c, "lblEstado");
                        assertTrue(
                                estado.getText().contains(
                                        "Modo consulta"));
                    }
                }
            }
            return null;
        });
    }

    @Test
    void qaLote4DetectaFxmlIncompleto() throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    c::initialize
            );

            assertTrue(error.getMessage().contains("FXML"));
            return null;
        });
    }

    @Test
    void qaLote4AtributosDeTodasLasCategorias()
            throws Exception {
        enFx(() -> {
            ControladorCatalogo c = formulario();

            Aseo aseo = mock(Aseo.class);
            when(aseo.getCodigo()).thenReturn("ASE-01");
            when(aseo.getNombre()).thenReturn("Jabon");
            when(aseo.getDescripcion()).thenReturn("Higiene");
            when(aseo.getPrecio()).thenReturn(1500.0);
            when(aseo.getStock()).thenReturn(8);
            when(aseo.getStockMinimo()).thenReturn(2);

            Maternidad maternidad = mock(Maternidad.class);
            when(maternidad.getCodigo()).thenReturn("MAT-02");
            when(maternidad.getNombre())
                    .thenReturn("Producto prenatal");
            when(maternidad.getDescripcion())
                    .thenReturn("Cuidado materno");
            when(maternidad.getPrecio()).thenReturn(2300.0);
            when(maternidad.getStock()).thenReturn(6);
            when(maternidad.getStockMinimo()).thenReturn(3);

            Medicamento medicamento = mock(Medicamento.class);
            when(medicamento.getCodigoInvima())
                    .thenReturn("INV-03");
            when(medicamento.getNombreComercial())
                    .thenReturn("Medicamento QA");
            when(medicamento.getStockTotal()).thenReturn(12);
            when(medicamento.getStockMinimo()).thenReturn(4);

            assertEquals("ASE-01",
                    qa4Invocar(c, "codigo", aseo));
            assertEquals("MAT-02",
                    qa4Invocar(c, "codigo", maternidad));
            assertEquals("INV-03",
                    qa4Invocar(c, "codigo", medicamento));

            assertEquals("Jabon",
                    qa4Invocar(c, "nombre", aseo));
            assertEquals("Producto prenatal",
                    qa4Invocar(c, "nombre", maternidad));
            assertEquals("Medicamento QA",
                    qa4Invocar(c, "nombre", medicamento));

            assertEquals("Higiene",
                    qa4Invocar(c, "descripcion", aseo));
            assertEquals("Cuidado materno",
                    qa4Invocar(c, "descripcion", maternidad));
            assertEquals("",
                    qa4Invocar(c, "descripcion", medicamento));

            when(medicamento.getPrincipioActivo())
                    .thenReturn("Acetaminofen");

            assertEquals("Principio activo: Acetaminofen",
                    qa4Invocar(c, "descripcion", medicamento));

            assertEquals("1500.0",
                    qa4Invocar(c, "precioTexto", aseo));
            assertEquals("2300.0",
                    qa4Invocar(c, "precioTexto", maternidad));
            assertEquals("Sin precio",
                    qa4Invocar(c, "precioTexto", medicamento));

            when(medicamento.getPrecio())
                    .thenReturn(
                            new java.math.BigDecimal("1250.50"));

            assertEquals("1250.50",
                    qa4Invocar(c, "precioTexto", medicamento));

            assertEquals(8, qa4Invocar(c, "stock", aseo));
            assertEquals(6, qa4Invocar(c, "stock", maternidad));
            assertEquals(12,
                    qa4Invocar(c, "stock", medicamento));

            assertEquals(2,
                    qa4Invocar(c, "stockMinimoDe", aseo));
            assertEquals(3,
                    qa4Invocar(c, "stockMinimoDe", maternidad));
            assertEquals(4,
                    qa4Invocar(c, "stockMinimoDe", medicamento));
            assertNull(
                    qa4Invocar(c, "stockMinimoDe", new Object()));

            return null;
        });
    }

    @Test
    void qaLote4ActualizacionSinSeleccion()
            throws Exception {
        enFx(() -> {
            org.example.carestock.facade.Inventario fachada =
                    mock(org.example.carestock.facade.Inventario.class);

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            try (
                org.mockito.MockedStatic<SesionUsuario> sesion =
                        mockStatic(SesionUsuario.class);
                org.mockito.MockedConstruction<
                        javafx.scene.control.Alert> dialogos =
                        mockConstruction(
                                javafx.scene.control.Alert.class)
            ) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                java.lang.reflect.Method metodo =
                        ControladorCatalogo.class
                                .getDeclaredMethod("onActualizar");
                metodo.setAccessible(true);
                metodo.invoke(c);

                verifyNoInteractions(fachada);
                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void qaLote4DesactivacionRechazadaPorFachada()
            throws Exception {
        enFx(() -> {
            org.example.carestock.facade.Inventario fachada =
                    mock(org.example.carestock.facade.Inventario.class);

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            campo(c, "seleccionado",
                    new RegistroCatalogo<>(
                            45, mock(Aseo.class), "ACTIVO"));

            when(fachada.desactivarAseo(45)).thenReturn(false);

            try (
                org.mockito.MockedStatic<SesionUsuario> sesion =
                        mockStatic(SesionUsuario.class);
                org.mockito.MockedConstruction<
                        javafx.scene.control.Alert> dialogos =
                        mockConstruction(
                                javafx.scene.control.Alert.class,
                                (alerta, contexto) ->
                                        when(alerta.showAndWait())
                                                .thenReturn(
                                                    java.util.Optional.of(
                                                        javafx.scene.control
                                                            .ButtonType.YES)))
            ) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                java.lang.reflect.Method metodo =
                        ControladorCatalogo.class
                                .getDeclaredMethod("onDesactivar");
                metodo.setAccessible(true);
                metodo.invoke(c);

                verify(fachada).desactivarAseo(45);
                assertTrue(dialogos.constructed().size() >= 2);
            }

            return null;
        });
    }

    @Test
    void qaLote4RegistroRechazaCategoriaDesconocida()
            throws Exception {
        enFx(() -> {
            org.example.carestock.facade.Inventario fachada =
                    mock(org.example.carestock.facade.Inventario.class);

            ControladorCatalogo c =
                    catalogoPreparado("Aseo", fachada);

            javafx.scene.control.ComboBox<String> categorias =
                    campo(c, "cmbCategoria");
            categorias.setValue("CATEGORIA_QA_DESCONOCIDA");

            try (
                org.mockito.MockedStatic<SesionUsuario> sesion =
                        mockStatic(SesionUsuario.class);
                org.mockito.MockedConstruction<
                        javafx.scene.control.Alert> dialogos =
                        mockConstruction(
                                javafx.scene.control.Alert.class)
            ) {
                sesion.when(SesionUsuario::esAdministrador)
                        .thenReturn(true);

                java.lang.reflect.Method metodo =
                        ControladorCatalogo.class
                                .getDeclaredMethod("onGuardar");
                metodo.setAccessible(true);
                metodo.invoke(c);

                javafx.scene.control.Label estado =
                        campo(c, "lblEstado");

                assertEquals("No se pudo registrar",
                        estado.getText());
                verifyNoInteractions(fachada);
                assertEquals(1, dialogos.constructed().size());
            }

            return null;
        });
    }

    @Test
    void qaLote4CargaAlertasAsincronaExitosa()
            throws Exception {
        java.util.concurrent.CountDownLatch terminado =
                new java.util.concurrent.CountDownLatch(1);

        ControladorAlertas c = enFx(() -> {
            ControladorAlertas controlador = alertasFormulario();

            llamar(controlador, "configurarFiltros");

            org.example.carestock.facade.Inventario fachada =
                    obtener(controlador, "inventario");

            when(fachada.listarAlertas()).thenReturn(
                    java.util.List.of(
                            alertaStock(), alertaVencimiento()));

            javafx.scene.control.Button boton =
                    obtener(controlador, "btnActualizar");

            boton.disabledProperty().addListener(
                    (obs, antes, ahora) -> {
                        if (!ahora) terminado.countDown();
                    });

            llamar(controlador, "onActualizar");
            assertTrue(boton.isDisabled());

            return controlador;
        });

        assertTrue(
                terminado.await(
                        15, java.util.concurrent.TimeUnit.SECONDS),
                "La consulta asincrona no finalizo"
        );

        enFx(() -> {
            javafx.scene.control.TableView<?> tabla =
                    obtener(c, "tablaAlertas");
            javafx.scene.control.Label total =
                    obtener(c, "lblTotal");
            javafx.scene.control.Label actualizado =
                    obtener(c, "lblActualizado");
            javafx.scene.control.Button boton =
                    obtener(c, "btnActualizar");

            assertEquals(2, tabla.getItems().size());
            assertEquals("Total: 2", total.getText());
            assertTrue(actualizado.getText()
                    .startsWith("Actualizado:"));
            assertFalse(boton.isDisabled());
            return null;
        });
    }

    @Test
    void qaLote4CargaAlertasAsincronaFallida()
            throws Exception {
        java.util.concurrent.CountDownLatch terminado =
                new java.util.concurrent.CountDownLatch(1);

        ControladorAlertas c = enFx(() -> {
            ControladorAlertas controlador = alertasFormulario();

            llamar(controlador, "configurarFiltros");

            org.example.carestock.facade.Inventario fachada =
                    obtener(controlador, "inventario");

            when(fachada.listarAlertas()).thenThrow(
                    new IllegalStateException(
                            "Fallo de consulta QA"));

            javafx.scene.control.Button boton =
                    obtener(controlador, "btnActualizar");

            boton.disabledProperty().addListener(
                    (obs, antes, ahora) -> {
                        if (!ahora) terminado.countDown();
                    });

            llamar(controlador, "onActualizar");
            return controlador;
        });

        assertTrue(
                terminado.await(
                        15, java.util.concurrent.TimeUnit.SECONDS),
                "La consulta de error no finalizo"
        );

        enFx(() -> {
            javafx.scene.control.Label estado =
                    obtener(c, "lblEstado");
            javafx.scene.control.Button boton =
                    obtener(c, "btnActualizar");

            assertTrue(estado.getText()
                    .contains("Fallo de consulta QA"));
            assertFalse(boton.isDisabled());
            return null;
        });
    }

    @Test
    void qaLote4EvitaConsultasSimultaneas()
            throws Exception {
        enFx(() -> {
            ControladorAlertas c = alertasFormulario();

            insertar(c, "cargando", true);
            llamar(c, "onActualizar");

            org.example.carestock.facade.Inventario fachada =
                    obtener(c, "inventario");

            verifyNoInteractions(fachada);
            return null;
        });
    }

    @Test
    void qaLote4RolesYFarmaciaEnInventario()
            throws Exception {
        enFx(() -> {
            record Escenario(
                    String rol, Integer farmacia,
                    boolean lotes, boolean historial,
                    boolean ajustes, boolean catalogo) {}

            java.util.List<Escenario> escenarios =
                    java.util.List.of(
                        new Escenario(
                                "ADMINISTRADOR", 7,
                                true, true, true, true),
                        new Escenario(
                                "FARMACEUTICO", 7,
                                true, true, false, true),
                        new Escenario(
                                "AUXILIAR", 7,
                                false, false, false, true),
                        new Escenario(
                                "SUPER_ADMIN", null,
                                false, false, false, false)
                    );

            for (Escenario escenario : escenarios) {
                try (
                    org.mockito.MockedStatic<SesionUsuario> sesion =
                            mockStatic(SesionUsuario.class);
                    org.mockito.MockedConstruction<
                            org.example.carestock.facade.Inventario>
                            fachadas = mockConstruction(
                                org.example.carestock.facade.Inventario.class,
                                (mock, contexto) -> {
                                    when(mock.listarCategoriasMedicamento())
                                            .thenReturn(java.util.Map.of());
                                    when(mock.listarLotes())
                                            .thenReturn(java.util.List.of());
                                    when(mock.categoriaPorMedicamento())
                                            .thenReturn(java.util.Map.of());
                                    when(mock.contarPorCategoria())
                                            .thenReturn(java.util.Map.of());
                                    when(mock.resumenAlertas())
                                            .thenReturn(
                                                new org.example.carestock
                                                    .service
                                                    .ServicioAlertasInventario
                                                    .Resumen(0, 0, 0, 0));
                                })
                ) {
                    sesion.when(SesionUsuario::estaActiva)
                            .thenReturn(true);
                    sesion.when(SesionUsuario::actual)
                            .thenReturn(
                                new SesionUsuario.DatosUsuario(
                                    7, "Usuario QA", "qa@local",
                                    escenario.rol(),
                                    escenario.farmacia(),
                                    escenario.farmacia() == null
                                        ? null : "Farmacia QA"));
                    sesion.when(SesionUsuario::esAdministrador)
                            .thenReturn(
                                "ADMINISTRADOR".equals(escenario.rol()));

                    ControladorInventario controlador =
                            inventarioFormulario();

                    controlador.initialize();

                    javafx.scene.control.Button catalogo =
                            obtener(controlador, "btnCatalogo");
                    javafx.scene.control.Button lotes =
                            obtener(controlador, "btnNuevoLote");
                    javafx.scene.control.Button historial =
                            obtener(controlador, "btnHistorial");
                    javafx.scene.control.Button ajustes =
                            obtener(controlador, "btnAjustes");

                    assertEquals(
                            escenario.catalogo(),
                            catalogo.isVisible());
                    assertEquals(
                            escenario.lotes(), lotes.isVisible());
                    assertEquals(
                            escenario.historial(),
                            historial.isVisible());
                    assertEquals(
                            escenario.ajustes(),
                            ajustes.isVisible());

                    assertEquals(1, fachadas.constructed().size());
                }
            }

            return null;
        });
    }


    // ===== QUALITY GATE: COLUMNAS DE ALERTAS =====

    @Test
    void alertasEjecutanTodasLasFuncionesDeColumnas()
            throws Exception {
        enFx(() -> {
            ControladorAlertas controlador =
                    alertasFormulario();

            llamar(controlador, "configurarColumnas");

            TableView<AlertaInventario> tabla =
                    obtener(controlador, "tablaAlertas");

            AlertaInventario alerta = alertaStock();

            Map<String, String> esperados = Map.of(
                    "colSeveridad", "Crítica",
                    "colTipo", "Stock mínimo",
                    "colCategoria", "Aseo",
                    "colProducto", "Jabon",
                    "colReferencia", "ASE-1",
                    "colExistencias", "0",
                    "colDetalle", "Sin existencias (mínimo 5)",
                    "colUltimoMovimiento",
                    "Sin movimientos registrados"
            );

            for (var entrada : esperados.entrySet()) {
                TableColumn<AlertaInventario, String> columna =
                        obtener(controlador, entrada.getKey());

                var datos = new TableColumn.CellDataFeatures<>(
                        tabla, columna, alerta);

                var valor = columna.getCellValueFactory()
                        .call(datos);

                assertEquals(
                        entrada.getValue(),
                        valor.getValue(),
                        entrada.getKey());
            }

            return null;
        });
    }

}
