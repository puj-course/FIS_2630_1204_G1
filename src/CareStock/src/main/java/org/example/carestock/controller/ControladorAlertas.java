package org.example.carestock.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import org.example.carestock.CareStockApp;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.facade.Inventario;
import org.example.carestock.service.ServicioAlertasInventario;
import org.example.carestock.session.SesionUsuario;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ControladorAlertas {

    private static final String TODOS = "Todos";
    private static final Duration INTERVALO_REFRESCO = Duration.seconds(60);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private Label lblFarmacia;
    @FXML private Label lblCriticas;
    @FXML private Label lblAdvertencias;
    @FXML private Label lblTotal;
    @FXML private Label lblActualizado;
    @FXML private Label lblEstado;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbSeveridad;
    @FXML private Button btnActualizar;
    @FXML private TableView<AlertaInventario> tablaAlertas;
    @FXML private TableColumn<AlertaInventario, String> colSeveridad;
    @FXML private TableColumn<AlertaInventario, String> colTipo;
    @FXML private TableColumn<AlertaInventario, String> colCategoria;
    @FXML private TableColumn<AlertaInventario, String> colProducto;
    @FXML private TableColumn<AlertaInventario, String> colReferencia;
    @FXML private TableColumn<AlertaInventario, String> colExistencias;
    @FXML private TableColumn<AlertaInventario, String> colDetalle;
    @FXML private TableColumn<AlertaInventario, String> colUltimoMovimiento;

    private List<AlertaInventario> todas = List.of();
    private Inventario inventario;
    private Timeline refresco;
    private boolean cargando;

    @FXML
    public void initialize() {
        if (!SesionUsuario.estaActiva()) {
            throw new IllegalStateException("No hay sesion autenticada");
        }

        var sesion = SesionUsuario.actual();
        if (sesion.idFarmacia() == null || sesion.idFarmacia() <= 0) {
            throw new IllegalStateException("El usuario no tiene farmacia asignada");
        }

        lblFarmacia.setText(
                "Farmacia: "
                        + (sesion.farmacia() == null
                        ? "#" + sesion.idFarmacia()
                        : sesion.farmacia() + " (#" + sesion.idFarmacia() + ")")
        );

        inventario = new Inventario(SesionUsuario.usuarioParaFachada());

        configurarColumnas();
        configurarFilas();
        configurarFiltros();
        configurarRefresco();
        cargar();
    }

    private void configurarColumnas() {
        colSeveridad.setCellValueFactory(c -> celda(c.getValue().severidad().etiqueta()));
        colTipo.setCellValueFactory(c -> celda(c.getValue().tipoAlerta().etiqueta()));
        colCategoria.setCellValueFactory(c -> celda(c.getValue().categoria().etiqueta()));
        colProducto.setCellValueFactory(c -> celda(c.getValue().producto()));
        colReferencia.setCellValueFactory(c -> celda(c.getValue().referencia()));
        colExistencias.setCellValueFactory(
                c -> celda(Integer.toString(c.getValue().existencias())));
        colDetalle.setCellValueFactory(c -> celda(c.getValue().mensaje()));
        colUltimoMovimiento.setCellValueFactory(
                c -> celda(textoUltimoMovimiento(c.getValue())));
    }

    private void configurarFilas() {
        tablaAlertas.setRowFactory(tabla -> new TableRow<>() {
            @Override
            protected void updateItem(AlertaInventario alerta, boolean vacia) {
                super.updateItem(alerta, vacia);
                if (alerta == null || vacia) {
                    setStyle("");
                } else if (alerta.severidad() == Severidad.CRITICA) {
                    setStyle("-fx-background-color: #FDECEA;");
                } else {
                    setStyle("-fx-background-color: #FFF6E0;");
                }
            }
        });
    }

    private void configurarFiltros() {
        cmbTipo.setItems(FXCollections.observableArrayList(
                opciones(TODOS, TipoAlerta.STOCK_MINIMO.etiqueta(),
                        TipoAlerta.VENCIMIENTO.etiqueta())));
        cmbCategoria.setItems(FXCollections.observableArrayList(
                opciones(TODOS, Categoria.MEDICAMENTO.etiqueta(),
                        Categoria.ASEO.etiqueta(), Categoria.MATERNIDAD.etiqueta())));
        cmbSeveridad.setItems(FXCollections.observableArrayList(
                opciones(TODOS, Severidad.CRITICA.etiqueta(),
                        Severidad.ADVERTENCIA.etiqueta())));

        for (ComboBox<String> combo : List.of(cmbTipo, cmbCategoria, cmbSeveridad)) {
            combo.getSelectionModel().selectFirst();
            combo.valueProperty().addListener((obs, anterior, actual) -> aplicarFiltros());
        }
    }

    private void configurarRefresco() {
        refresco = new Timeline(new KeyFrame(INTERVALO_REFRESCO, e -> cargar()));
        refresco.setCycleCount(Timeline.INDEFINITE);
        refresco.play();

        tablaAlertas.sceneProperty().addListener((obs, anterior, actual) -> {
            if (actual == null) {
                refresco.stop();
            }
        });
    }

    private void cargar() {
        if (cargando) {
            return;
        }
        cargando = true;
        btnActualizar.setDisable(true);
        lblEstado.setText("Consultando...");

        Task<List<AlertaInventario>> tarea = new Task<>() {
            @Override
            protected List<AlertaInventario> call() throws Exception {
                return inventario.listarAlertas();
            }
        };

        tarea.setOnSucceeded(e -> {
            cargando = false;
            btnActualizar.setDisable(false);
            todas = tarea.getValue();
            aplicarFiltros();
            lblActualizado.setText("Actualizado: " + LocalTime.now().format(FORMATO_HORA));
            actualizarResumen();
        });

        tarea.setOnFailed(e -> {
            cargando = false;
            btnActualizar.setDisable(false);
            Throwable error = tarea.getException();
            lblEstado.setText("No se pudieron consultar las alertas: "
                    + (error.getMessage() == null
                    ? error.getClass().getSimpleName()
                    : error.getMessage()));
        });

        Thread hilo = new Thread(tarea, "alertas-inventario");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void actualizarResumen() {
        ServicioAlertasInventario.Resumen resumen = ServicioAlertasInventario.resumir(todas);
        lblCriticas.setText("Críticas: " + resumen.criticas());
        lblAdvertencias.setText("Advertencias: " + resumen.advertencias());
        lblTotal.setText("Total: " + resumen.total());
    }

    private void aplicarFiltros() {
        List<AlertaInventario> visibles = ServicioAlertasInventario.filtrar(
                todas,
                tipoSeleccionado(),
                categoriaSeleccionada(),
                severidadSeleccionada());

        tablaAlertas.setItems(FXCollections.observableArrayList(visibles));

        if (todas.isEmpty()) {
            lblEstado.setText("No hay alertas activas para esta farmacia.");
        } else {
            lblEstado.setText("Mostrando " + visibles.size() + " de " + todas.size() + " alertas.");
        }
    }

    private TipoAlerta tipoSeleccionado() {
        String valor = cmbTipo.getValue();
        for (TipoAlerta tipo : TipoAlerta.values()) {
            if (tipo.etiqueta().equals(valor)) {
                return tipo;
            }
        }
        return null;
    }

    private Categoria categoriaSeleccionada() {
        String valor = cmbCategoria.getValue();
        for (Categoria categoria : Categoria.values()) {
            if (categoria.etiqueta().equals(valor)) {
                return categoria;
            }
        }
        return null;
    }

    private Severidad severidadSeleccionada() {
        String valor = cmbSeveridad.getValue();
        for (Severidad severidad : Severidad.values()) {
            if (severidad.etiqueta().equals(valor)) {
                return severidad;
            }
        }
        return null;
    }

    private static List<String> opciones(String... valores) {
        return new ArrayList<>(List.of(valores));
    }

    private static String textoUltimoMovimiento(AlertaInventario alerta) {
        LocalDateTime fecha = alerta.ultimoMovimientoFecha();
        if (fecha == null) {
            return "Sin movimientos registrados";
        }
        String tipo = alerta.ultimoMovimientoTipo() == null ? "" : alerta.ultimoMovimientoTipo();
        String usuario = alerta.ultimoMovimientoUsuario() == null
                ? "" : alerta.ultimoMovimientoUsuario();
        return tipo + " · " + usuario + " · " + fecha.format(FORMATO_FECHA);
    }

    private static ObservableValue<String> celda(String texto) {
        return new ReadOnlyStringWrapper(texto == null ? "" : texto);
    }

    @FXML
    private void onActualizar() {
        cargar();
    }

    @FXML
    private void onVolver() {
        CareStockApp.getInstance().mostrarDashboard();
    }

    @FXML
    private void onCerrarSesion() {
        CareStockApp.getInstance().cerrarSesion();
    }
}
