package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import org.example.carestock.CareStockApp;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.service.*;
import org.example.carestock.session.SesionUsuario;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/** HU.137 / #991: consulta, filtros y exportacion sin modificar inventario. */
public class ControladorReposicion {

    @FXML private Label lblFarmacia;
    @FXML private Label lblEstado;
    @FXML private Label lblConteo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbSeveridad;
    @FXML private TextField txtBuscar;
    @FXML private Button btnActualizar;
    @FXML private Button btnExportarCsv;

    @FXML private TableView<RegistroReposicion> tablaReposicion;
    @FXML private TableColumn<RegistroReposicion, String> colCodigo;
    @FXML private TableColumn<RegistroReposicion, String> colProducto;
    @FXML private TableColumn<RegistroReposicion, String> colCategoria;
    @FXML private TableColumn<RegistroReposicion, Integer> colExistencias;
    @FXML private TableColumn<RegistroReposicion, Integer> colMinimo;
    @FXML private TableColumn<RegistroReposicion, Integer> colFaltante;
    @FXML private TableColumn<RegistroReposicion, String> colSeveridad;

    private final ServicioPlanificacionReposicion servicio =
            new ServicioPlanificacionReposicion();

    private final AccionExportarReposicion exportacion =
            new AccionExportarReposicion();

    private SesionUsuario.DatosUsuario sesionVista;
    private List<RegistroReposicion> registros = List.of();
    private boolean ocupado;

    @FXML
    public void initialize() {
        sesionVista = PermisosExportacionReposicion
                .exigirAdministrador();

        lblFarmacia.setText(
                "Farmacia: " + sesionVista.farmacia()
                + " (#" + sesionVista.idFarmacia() + ")");

        colCodigo.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        c.getValue().codigo()));

        colProducto.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        c.getValue().nombre()));

        colCategoria.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        c.getValue().categoria().etiqueta()));

        colExistencias.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().existenciasActuales()));

        colMinimo.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().stockMinimo()));

        colFaltante.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().cantidadFaltante()));

        colSeveridad.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        c.getValue().severidad().etiqueta()));

        cmbCategoria.getItems().add("Todas");

        for (Categoria categoria : Categoria.values()) {
            cmbCategoria.getItems().add(
                    categoria.etiqueta());
        }

        cmbSeveridad.getItems().add("Todas");

        for (Severidad severidad : Severidad.values()) {
            cmbSeveridad.getItems().add(
                    severidad.etiqueta());
        }

        cmbCategoria.getSelectionModel().selectFirst();
        cmbSeveridad.getSelectionModel().selectFirst();

        cmbCategoria.valueProperty().addListener(
                (obs, anterior, actual) -> filtrar());

        cmbSeveridad.valueProperty().addListener(
                (obs, anterior, actual) -> filtrar());

        txtBuscar.textProperty().addListener(
                (obs, anterior, actual) -> filtrar());

        filtrar();
        cargar();
    }

    private static Categoria categoriaElegida(
            String etiqueta) {

        for (Categoria categoria : Categoria.values()) {
            if (categoria.etiqueta().equals(etiqueta)) {
                return categoria;
            }
        }

        return null;
    }

    private static Severidad severidadElegida(
            String etiqueta) {

        for (Severidad severidad : Severidad.values()) {
            if (severidad.etiqueta().equals(etiqueta)) {
                return severidad;
            }
        }

        return null;
    }

    private void filtrar() {
        List<RegistroReposicion> visibles =
                FiltroReposicion.aplicar(
                        registros,
                        categoriaElegida(cmbCategoria.getValue()),
                        severidadElegida(cmbSeveridad.getValue()),
                        txtBuscar.getText());

        tablaReposicion.setItems(
                FXCollections.observableArrayList(visibles));

        lblConteo.setText(
                "Productos visibles: " + visibles.size());

        btnExportarCsv.setDisable(
                ocupado || visibles.isEmpty());
    }

    private void cambiarOcupado(boolean valor) {
        ocupado = valor;

        btnActualizar.setDisable(valor);

        btnExportarCsv.setDisable(
                valor || tablaReposicion.getItems().isEmpty());
    }

    @FXML
    private void onActualizar() {
        cargar();
    }

    private void cargar() {
        if (ocupado) {
            return;
        }

        try {
            PermisosExportacionReposicion
                    .comprobarMismaSesion(sesionVista);
        } catch (SecurityException excepcion) {
            registros = List.of();
            filtrar();
            lblEstado.setText(
                    "Acceso denegado: " + excepcion.getMessage());
            return;
        }

        cambiarOcupado(true);
        lblEstado.setText(
                "Consultando necesidades de reposición...");

        Task<List<RegistroReposicion>> tarea = new Task<>() {
            @Override
            protected List<RegistroReposicion> call()
                    throws Exception {

                PermisosExportacionReposicion
                        .comprobarMismaSesion(sesionVista);

                List<RegistroReposicion> datos =
                        servicio.listarPorFarmacia(
                                sesionVista.idFarmacia());

                PermisosExportacionReposicion
                        .comprobarMismaSesion(sesionVista);

                return datos;
            }
        };

        tarea.setOnSucceeded(e -> {
            try {
                PermisosExportacionReposicion
                        .comprobarMismaSesion(sesionVista);

                registros = List.copyOf(tarea.getValue());

                lblEstado.setText(registros.isEmpty()
                        ? "No hay productos que requieran reposición."
                        : "Consulta actualizada. Filtros conservados.");

            } catch (RuntimeException error) {
                registros = List.of();
                lblEstado.setText(
                        "Consulta descartada: " + error.getMessage());
            }

            cambiarOcupado(false);
            filtrar();
        });

        tarea.setOnFailed(e -> {
            registros = List.of();
            cambiarOcupado(false);
            filtrar();

            lblEstado.setText(
                    "No se pudo actualizar: "
                    + tarea.getException().getMessage());
        });

        Thread hilo = new Thread(
                tarea, "reposicion-consulta");

        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onExportarCsv() {
        if (ocupado) {
            return;
        }

        try {
            PermisosExportacionReposicion
                    .comprobarMismaSesion(sesionVista);

            List<RegistroReposicion> visibles =
                    List.copyOf(tablaReposicion.getItems());

            if (visibles.isEmpty()) {
                lblEstado.setText(
                        "No hay productos visibles para exportar.");
                return;
            }

            FileChooser selector = new FileChooser();
            selector.setTitle(
                    "Exportar planificación de reposición");

            selector.setInitialFileName(
                    "reposicion-" + LocalDate.now() + ".csv");

            selector.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "CSV (*.csv)", "*.csv"));

            File archivo = selector.showSaveDialog(
                    tablaReposicion.getScene() == null
                    ? null
                    : tablaReposicion.getScene().getWindow());

            if (archivo == null) {
                lblEstado.setText("Exportación cancelada.");
                return;
            }

            PermisosExportacionReposicion
                    .comprobarMismaSesion(sesionVista);

            cambiarOcupado(true);
            lblEstado.setText(
                    "Verificando existencias actuales antes de exportar...");

            Task<AccionExportarReposicion.Resultado> tarea =
                    new Task<>() {
                @Override
                protected AccionExportarReposicion.Resultado call()
                        throws Exception {

                    return exportacion.ejecutar(
                            visibles, archivo::toPath);
                }
            };

            tarea.setOnSucceeded(e -> {
                cambiarOcupado(false);

                lblEstado.setText(
                        "Archivo CSV guardado: "
                        + archivo.getName()
                        + " (" + visibles.size() + " productos).");
            });

            tarea.setOnFailed(e -> {
                cambiarOcupado(false);

                lblEstado.setText(
                        "Exportación cancelada: "
                        + tarea.getException().getMessage());
            });

            Thread hilo = new Thread(
                    tarea, "reposicion-exportacion");

            hilo.setDaemon(true);
            hilo.start();

        } catch (RuntimeException error) {
            lblEstado.setText(
                    "No se pudo exportar: " + error.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        CareStockApp.getInstance().mostrarDashboard();
    }
}
