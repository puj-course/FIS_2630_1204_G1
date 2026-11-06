package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.service.*;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorReposicionFlujosTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    1, "Administrador QA", "qa@test.local",
                    "ADMINISTRADOR", 7, "Farmacia QA");

    @TempDir
    Path temporal;

    private Field campoSesion;
    private SesionUsuario.DatosUsuario sesionAnterior;

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

    @BeforeEach
    void establecerSesionDePrueba() throws Exception {
        campoSesion = SesionUsuario.class
                .getDeclaredField("actual");

        campoSesion.setAccessible(true);

        sesionAnterior =
                (SesionUsuario.DatosUsuario) campoSesion.get(null);

        // Datos en memoria; no se consulta PostgreSQL.
        campoSesion.set(null, ADMIN);
    }

    @AfterEach
    void restaurarSesion() throws Exception {
        campoSesion.set(null, sesionAnterior);
    }

    private static <T> T fx(Callable<T> accion)
            throws Exception {

        if (Platform.isFxApplicationThread()) {
            return accion.call();
        }

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
    private static <T> T leer(
            Object objeto, String nombre) throws Exception {

        Field campo = objeto.getClass()
                .getDeclaredField(nombre);

        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(
            Object objeto, String nombre) throws Exception {

        Method metodo = objeto.getClass()
                .getDeclaredMethod(nombre);

        metodo.setAccessible(true);
        metodo.invoke(objeto);
    }

    private static RegistroReposicion producto(
            Categoria categoria,
            int id,
            String codigo,
            String nombre,
            int stock,
            int minimo) {

        return new RegistroReposicion(
                categoria,
                id,
                codigo,
                nombre,
                stock,
                minimo,
                Math.max(0, minimo - stock),
                stock == 0
                        ? Severidad.CRITICA
                        : Severidad.ADVERTENCIA);
    }

    private static ControladorReposicion preparar(
            ServicioPlanificacionReposicion servicio,
            boolean omitirCargaInicial) throws Exception {

        var c = new ControladorReposicion();

        asignar(c, "servicio", servicio);
        asignar(c, "lblFarmacia", new Label());
        asignar(c, "lblEstado", new Label());
        asignar(c, "lblConteo", new Label());

        asignar(c, "cmbCategoria", new ComboBox<String>());
        asignar(c, "cmbSeveridad", new ComboBox<String>());
        asignar(c, "txtBuscar", new TextField());

        asignar(c, "btnActualizar", new Button());
        asignar(c, "btnExportarCsv", new Button());

        asignar(
                c, "tablaReposicion",
                new TableView<RegistroReposicion>());

        asignar(c, "colCodigo",
                new TableColumn<RegistroReposicion, String>());

        asignar(c, "colProducto",
                new TableColumn<RegistroReposicion, String>());

        asignar(c, "colCategoria",
                new TableColumn<RegistroReposicion, String>());

        asignar(c, "colExistencias",
                new TableColumn<RegistroReposicion, Integer>());

        asignar(c, "colMinimo",
                new TableColumn<RegistroReposicion, Integer>());

        asignar(c, "colFaltante",
                new TableColumn<RegistroReposicion, Integer>());

        asignar(c, "colSeveridad",
                new TableColumn<RegistroReposicion, String>());

        // Asociar las columnas a la tabla, como hace el FXML real.
        TableView<RegistroReposicion> tabla =
                leer(c, "tablaReposicion");

        tabla.getColumns().addAll(
                leer(c, "colCodigo"),
                leer(c, "colProducto"),
                leer(c, "colCategoria"),
                leer(c, "colExistencias"),
                leer(c, "colMinimo"),
                leer(c, "colFaltante"),
                leer(c, "colSeveridad")
        );

        // Permite probar initialize sin disparar una consulta
        // cuando el escenario no la necesita.
        asignar(c, "ocupado", omitirCargaInicial);

        c.initialize();

        if (omitirCargaInicial) {
            asignar(c, "ocupado", false);
        }

        return c;
    }

    private static void esperarEstado(
            ControladorReposicion controlador,
            String fragmento) throws Exception {

        long limite = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(10);

        String ultimo = "";

        while (System.nanoTime() < limite) {
            ultimo = fx(() -> {
                Label estado = leer(
                        controlador, "lblEstado");

                return estado.getText();
            });

            if (ultimo.contains(fragmento)) {
                return;
            }

            Thread.sleep(30);
        }

        fail("No apareció el estado esperado: "
                + fragmento + ". Último estado: " + ultimo);
    }

    @Test
    void inicializacionConfiguraColumnasYConsultaFarmacia()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var fila = producto(
                Categoria.ASEO, 10,
                "ASE-10", "Jabón", 2, 6);

        when(servicio.listarPorFarmacia(7))
                .thenReturn(List.of(fila));

        ControladorReposicion c = fx(
                () -> preparar(servicio, false));

        esperarEstado(c, "Consulta actualizada");

        fx(() -> {
            Label farmacia = leer(c, "lblFarmacia");

            assertTrue(farmacia.getText()
                    .contains("Farmacia QA"));

            ComboBox<String> categorias =
                    leer(c, "cmbCategoria");

            ComboBox<String> severidades =
                    leer(c, "cmbSeveridad");

            assertEquals("Todas", categorias.getValue());
            assertEquals("Todas", severidades.getValue());

            assertTrue(categorias.getItems()
                    .contains("Aseo"));

            assertTrue(severidades.getItems()
                    .contains("Crítica"));

            TableView<RegistroReposicion> tabla =
                    leer(c, "tablaReposicion");

            assertEquals(List.of(fila), tabla.getItems());

            TableColumn<RegistroReposicion, String> nombre =
                    leer(c, "colProducto");

            TableColumn<RegistroReposicion, Integer> faltante =
                    leer(c, "colFaltante");

            assertEquals(
                    "Jabón",
                    nombre.getCellObservableValue(fila)
                            .getValue());

            assertEquals(
                    4,
                    faltante.getCellObservableValue(fila)
                            .getValue().intValue());

            return null;
        });

        verify(servicio).listarPorFarmacia(7);
    }

    @Test
    void actualizarRecalculaYConservaFiltros()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var agotado = producto(
                Categoria.ASEO, 20,
                "ASE-20", "Jabón", 0, 5);

        // Primera consulta: necesita reposición.
        // Segunda consulta: una entrada cubrió su mínimo.
        when(servicio.listarPorFarmacia(7))
                .thenReturn(
                        List.of(agotado),
                        List.of());

        ControladorReposicion c = fx(
                () -> preparar(servicio, true));

        fx(() -> {
            ComboBox<String> categoria =
                    leer(c, "cmbCategoria");

            TextField buscar = leer(c, "txtBuscar");

            categoria.setValue("Aseo");
            buscar.setText("jab");

            ejecutar(c, "onActualizar");

            return null;
        });

        esperarEstado(c, "Consulta actualizada");

        fx(() -> {
            TableView<RegistroReposicion> tabla =
                    leer(c, "tablaReposicion");

            assertEquals(List.of(agotado), tabla.getItems());

            ejecutar(c, "onActualizar");
            return null;
        });

        esperarEstado(
                c, "No hay productos que requieran reposición");

        fx(() -> {
            TableView<RegistroReposicion> tabla =
                    leer(c, "tablaReposicion");

            ComboBox<String> categoria =
                    leer(c, "cmbCategoria");

            TextField buscar = leer(c, "txtBuscar");

            Button exportar = leer(c, "btnExportarCsv");

            assertTrue(tabla.getItems().isEmpty());
            assertEquals("Aseo", categoria.getValue());
            assertEquals("jab", buscar.getText());
            assertTrue(exportar.isDisabled());

            return null;
        });

        verify(servicio, times(2)).listarPorFarmacia(7);
    }

    @Test
    void consultaFallidaLimpiaDatosAnteriores()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        when(servicio.listarPorFarmacia(7))
                .thenThrow(
                        new IllegalStateException("BD no disponible"));

        ControladorReposicion c = fx(
                () -> preparar(servicio, true));

        fx(() -> {
            asignar(c, "registros", List.of(
                    producto(Categoria.ASEO, 30,
                            "ASE-30", "Champú", 1, 5)));

            ejecutar(c, "filtrar");

            TableView<RegistroReposicion> tabla =
                    leer(c, "tablaReposicion");

            assertEquals(1, tabla.getItems().size());

            ejecutar(c, "onActualizar");

            return null;
        });

        esperarEstado(c, "No se pudo actualizar");

        fx(() -> {
            TableView<RegistroReposicion> tabla =
                    leer(c, "tablaReposicion");

            Button exportar =
                    leer(c, "btnExportarCsv");

            Label estado = leer(c, "lblEstado");

            assertTrue(tabla.getItems().isEmpty());
            assertTrue(exportar.isDisabled());
            assertTrue(estado.getText()
                    .contains("BD no disponible"));

            return null;
        });
    }

    @Test
    void cancelarSelectorNoIniciaExportacion()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var exportacion = mock(
                AccionExportarReposicion.class);

        var fila = producto(
                Categoria.ASEO, 40,
                "ASE-40", "Jabón", 0, 5);

        fx(() -> {
            ControladorReposicion c =
                    preparar(servicio, true);

            asignar(c, "exportacion", exportacion);

            TableView<RegistroReposicion> tabla =
                    leer(c, "tablaReposicion");

            tabla.setItems(
                    FXCollections.observableArrayList(fila));

            try (MockedConstruction<FileChooser> selectores =
                    mockConstruction(
                            FileChooser.class,
                            (selector, contexto) -> {
                                when(selector.getExtensionFilters())
                                        .thenReturn(
                                                FXCollections.observableArrayList());

                                when(selector.showSaveDialog(
                                        nullable(Window.class)))
                                        .thenReturn(null);
                            })) {

                ejecutar(c, "onExportarCsv");

                assertEquals(
                        1, selectores.constructed().size());
            }

            Label estado = leer(c, "lblEstado");

            assertEquals(
                    "Exportación cancelada.",
                    estado.getText());

            return null;
        });

        verifyNoInteractions(exportacion);
    }

    @Test
    void exportacionExitosaNotificaAlAdministrador()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var exportacion = mock(
                AccionExportarReposicion.class);

        var fila = producto(
                Categoria.MATERNIDAD, 50,
                "MAT-50", "Pañales", 1, 4);

        Path destino = temporal.resolve(
                "reposicion-qa.csv");

        when(exportacion.ejecutar(anyList(), any()))
                .thenReturn(
                        AccionExportarReposicion.Resultado.EXPORTADO);

        ControladorReposicion c = fx(() -> {
            var controlador =
                    preparar(servicio, true);

            asignar(
                    controlador, "exportacion", exportacion);

            TableView<RegistroReposicion> tabla =
                    leer(controlador, "tablaReposicion");

            tabla.setItems(
                    FXCollections.observableArrayList(fila));

            try (MockedConstruction<FileChooser> selectores =
                    mockConstruction(
                            FileChooser.class,
                            (selector, contexto) -> {
                                when(selector.getExtensionFilters())
                                        .thenReturn(
                                                FXCollections.observableArrayList());

                                when(selector.showSaveDialog(
                                        nullable(Window.class)))
                                        .thenReturn(destino.toFile());
                            })) {

                ejecutar(controlador, "onExportarCsv");
            }

            return controlador;
        });

        esperarEstado(c, "Archivo CSV guardado");

        fx(() -> {
            Label estado = leer(c, "lblEstado");

            assertTrue(estado.getText()
                    .contains("reposicion-qa.csv"));

            assertTrue(estado.getText()
                    .contains("1 productos"));

            Button exportar = leer(c, "btnExportarCsv");

            assertFalse(exportar.isDisabled());

            return null;
        });

        verify(exportacion, times(1))
                .ejecutar(eq(List.of(fila)), any());
    }

    @Test
    void errorDuranteExportacionInformaElProblema()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var exportacion = mock(
                AccionExportarReposicion.class);

        var fila = producto(
                Categoria.ASEO, 60,
                "ASE-60", "Jabón", 0, 4);

        Path destino = temporal.resolve(
                "reporte-fallido.csv");

        when(exportacion.ejecutar(anyList(), any()))
                .thenThrow(new IOException("Disco sin espacio"));

        ControladorReposicion c = fx(() -> {
            var controlador =
                    preparar(servicio, true);

            asignar(
                    controlador, "exportacion", exportacion);

            TableView<RegistroReposicion> tabla =
                    leer(controlador, "tablaReposicion");

            tabla.setItems(
                    FXCollections.observableArrayList(fila));

            try (MockedConstruction<FileChooser> selectores =
                    mockConstruction(
                            FileChooser.class,
                            (selector, contexto) -> {
                                when(selector.getExtensionFilters())
                                        .thenReturn(
                                                FXCollections.observableArrayList());

                                when(selector.showSaveDialog(
                                        nullable(Window.class)))
                                        .thenReturn(destino.toFile());
                            })) {

                ejecutar(controlador, "onExportarCsv");
            }

            return controlador;
        });

        esperarEstado(c, "Exportación cancelada");

        fx(() -> {
            Label estado = leer(c, "lblEstado");

            assertTrue(estado.getText()
                    .contains("Disco sin espacio"));

            Button actualizar =
                    leer(c, "btnActualizar");

            assertFalse(actualizar.isDisabled());

            return null;
        });
    }
}
