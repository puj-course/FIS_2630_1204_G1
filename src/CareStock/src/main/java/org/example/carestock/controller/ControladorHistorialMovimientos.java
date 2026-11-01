package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.carestock.CareStockApp;
import org.example.carestock.dao.HistorialMovimientosDao;
import org.example.carestock.DataTransferObject.MovimientoHistorial;
import org.example.carestock.session.SesionUsuario;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * HU-796: Interfaz JavaFX del historial unificado.
 * Consulta de solo lectura por farmacia activa.
 */
public final class ControladorHistorialMovimientos {

    private static final String TODOS = "Todos";
    private static final int TAMANO_PAGINA = 500;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblFarmacia;
    @FXML private Label lblUsuario;
    @FXML private Label lblEstado;
    @FXML private Label lblTotal;

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private DatePicker fechaDesde;
    @FXML private DatePicker fechaHasta;

    @FXML private Button btnActualizar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<MovimientoHistorial> tablaHistorial;
    @FXML private TableColumn<MovimientoHistorial, OffsetDateTime> colFecha;
    @FXML private TableColumn<MovimientoHistorial, String> colProducto;
    @FXML private TableColumn<MovimientoHistorial, String> colCategoria;
    @FXML private TableColumn<MovimientoHistorial, String> colTipo;
    @FXML private TableColumn<MovimientoHistorial, Long> colCantidad;
    @FXML private TableColumn<MovimientoHistorial, String> colUsuario;
    @FXML private TableColumn<MovimientoHistorial, String> colMotivo;

    private final ObservableList<MovimientoHistorial> registros =
            FXCollections.observableArrayList();

    private FilteredList<MovimientoHistorial> filtrados;

    private int idUsuario;
    private int idFarmacia;
    private boolean cargando;
    private boolean vistaActiva = true;

    private Task<List<MovimientoHistorial>> tareaActual;

    static boolean rolPermitido(String rol) {
        return rol != null
                && ("ADMINISTRADOR".equalsIgnoreCase(rol.trim())
                || "FARMACEUTICO".equalsIgnoreCase(rol.trim()));
    }

    @FXML
    private void initialize() {

        var sesion = SesionUsuario.actual();

        if (!rolPermitido(sesion.rol())
                || sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0) {

            throw new IllegalStateException(
                    "Solo Administrador o Farmaceutico "
                    + "con farmacia asignada pueden consultar el historial.");
        }

        idUsuario = sesion.id();
        idFarmacia = sesion.idFarmacia();

        lblUsuario.setText(sesion.nombre());
        lblFarmacia.setText(
                (sesion.farmacia() == null
                        ? "Farmacia" : sesion.farmacia())
                        + " (#" + idFarmacia + ")");

        configurarColumnas();
        configurarFiltros();

        filtrados = new FilteredList<>(registros, r -> true);

        SortedList<MovimientoHistorial> ordenados =
                new SortedList<>(filtrados);

        ordenados.comparatorProperty().bind(
                tablaHistorial.comparatorProperty());

        tablaHistorial.setItems(ordenados);
        tablaHistorial.setEditable(false);

        colFecha.setSortType(TableColumn.SortType.DESCENDING);
        tablaHistorial.getSortOrder().setAll(colFecha);

        tablaHistorial.setPlaceholder(
                new Label("No existen movimientos registrados."));

        cargarHistorial();
    }

    private void configurarColumnas() {

        colFecha.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().fechaMovimiento()));

        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(
                    OffsetDateTime fecha, boolean vacio) {

                super.updateItem(fecha, vacio);

                if (vacio || fecha == null) {
                    setText(null);
                } else {
                    setText(FORMATO_FECHA.format(
                            fecha.atZoneSameInstant(
                                    ZoneId.systemDefault())));
                }
            }
        });

        colProducto.setCellValueFactory(c -> {
            var movimiento = c.getValue();
            String nombre = texto(movimiento.producto());
            String referencia = movimiento.referencia();

            if (referencia != null && !referencia.isBlank()) {
                nombre += " (" + referencia + ")";
            }

            return new ReadOnlyStringWrapper(nombre);
        });

        colCategoria.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        texto(c.getValue().categoria())));

        colTipo.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        texto(c.getValue().tipoMovimiento())));

        colCantidad.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().cantidad()));

        colUsuario.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        texto(c.getValue().usuario())));

        colMotivo.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        texto(c.getValue().motivo())));

        colMotivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String motivo, boolean vacio) {

                super.updateItem(motivo, vacio);

                if (vacio || motivo == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(motivo);
                    setTooltip(new Tooltip(motivo));
                }
            }
        });
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private void configurarFiltros() {

        cmbCategoria.setItems(
                FXCollections.observableArrayList(
                        TODOS, "MEDICAMENTO", "ASEO", "MATERNIDAD"));

        cmbTipo.setItems(
                FXCollections.observableArrayList(
                        TODOS, "ENTRADA", "SALIDA", "AJUSTE"));

        cmbCategoria.getSelectionModel().selectFirst();
        cmbTipo.getSelectionModel().selectFirst();

        cmbCategoria.valueProperty().addListener(
                (obs, antes, despues) -> aplicarFiltros());

        cmbTipo.valueProperty().addListener(
                (obs, antes, despues) -> aplicarFiltros());

        fechaDesde.valueProperty().addListener(
                (obs, antes, despues) -> aplicarFiltros());

        fechaHasta.valueProperty().addListener(
                (obs, antes, despues) -> aplicarFiltros());
    }

    /**
     * Regla pura para filtrar, comprobable con JUnit.
     * Las fechas inicial y final son inclusivas.
     */
    static boolean coincideFiltros(
            MovimientoHistorial movimiento,
            String categoria,
            String tipo,
            LocalDate desde,
            LocalDate hasta
    ) {

        if (movimiento == null) {
            return false;
        }

        if (categoria != null
                && !TODOS.equalsIgnoreCase(categoria)
                && !categoria.equalsIgnoreCase(
                        movimiento.categoria())) {
            return false;
        }

        if (tipo != null
                && !TODOS.equalsIgnoreCase(tipo)
                && !tipo.equalsIgnoreCase(
                        movimiento.tipoMovimiento())) {
            return false;
        }

        if (desde != null || hasta != null) {

            OffsetDateTime fecha =
                    movimiento.fechaMovimiento();

            if (fecha == null) {
                return false;
            }

            LocalDate dia = fecha.atZoneSameInstant(
                    ZoneId.systemDefault()).toLocalDate();

            if (desde != null && dia.isBefore(desde)) {
                return false;
            }

            if (hasta != null && dia.isAfter(hasta)) {
                return false;
            }
        }

        return true;
    }

    private void aplicarFiltros() {

        if (filtrados == null) {
            return;
        }

        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();

        if (desde != null
                && hasta != null
                && desde.isAfter(hasta)) {

            filtrados.setPredicate(r -> false);
            tablaHistorial.setPlaceholder(
                    new Label("El rango de fechas no es valido."));
            lblEstado.setText(
                    "La fecha inicial no puede superar la fecha final.");
            lblTotal.setText("Mostrando: 0");
            return;
        }

        filtrados.setPredicate(m -> coincideFiltros(
                m,
                cmbCategoria.getValue(),
                cmbTipo.getValue(),
                desde,
                hasta));

        int visibles = filtrados.size();

        lblTotal.setText(
                "Mostrando: " + visibles
                        + " / " + registros.size());

        if (registros.isEmpty()) {

            tablaHistorial.setPlaceholder(
                    new Label("No existen movimientos registrados."));

            lblEstado.setText(
                    "No hay movimientos para la farmacia activa.");

        } else if (visibles == 0) {

            tablaHistorial.setPlaceholder(
                    new Label(
                            "No se encontraron movimientos con esos filtros."));

            lblEstado.setText(
                    "Sin resultados. Modifica los filtros.");

        } else {

            lblEstado.setText(
                    "Historial actualizado. "
                    + visibles + " movimientos visibles.");
        }
    }

    private boolean sesionValida() {

        if (!SesionUsuario.estaActiva()) {
            return false;
        }

        var sesion = SesionUsuario.actual();

        return sesion.id() == idUsuario
                && Objects.equals(
                        sesion.idFarmacia(), idFarmacia)
                && rolPermitido(sesion.rol());
    }

    private void bloquearFiltros(boolean bloquear) {

        cargando = bloquear;

        cmbCategoria.setDisable(bloquear);
        cmbTipo.setDisable(bloquear);
        fechaDesde.setDisable(bloquear);
        fechaHasta.setDisable(bloquear);
        btnActualizar.setDisable(bloquear);
        btnLimpiar.setDisable(bloquear);
    }

    @FXML
    private void onActualizar() {
        if (!cargando) {
            cargarHistorial();
        }
    }

    private void cargarHistorial() {

        if (!sesionValida()) {
            mostrarError("La sesion ya no es valida.");
            return;
        }

        bloquearFiltros(true);

        Task<List<MovimientoHistorial>> tarea = new Task<>() {

            @Override
            protected List<MovimientoHistorial> call()
                    throws Exception {

                HistorialMovimientosDao dao =
                        new HistorialMovimientosDao();

                LinkedHashMap<String, MovimientoHistorial> unicos =
                        new LinkedHashMap<>();

                int desplazamiento = 0;
                List<MovimientoHistorial> pagina;

                do {

                    if (isCancelled()) {
                        return List.of();
                    }

                    pagina = dao.listar(
                            TAMANO_PAGINA, desplazamiento);

                    for (MovimientoHistorial movimiento : pagina) {
                        unicos.putIfAbsent(
                                movimiento.claveUnica(),
                                movimiento);
                    }

                    updateMessage(
                            "Consultando historial... "
                            + unicos.size() + " registros");

                    desplazamiento = Math.addExact(
                            desplazamiento, pagina.size());

                } while (pagina.size() == TAMANO_PAGINA);

                return new ArrayList<>(unicos.values());
            }
        };

        tareaActual = tarea;

        lblEstado.textProperty().bind(
                tarea.messageProperty());

        tarea.setOnSucceeded(evento -> {

            if (!vistaActiva) {
                return;
            }

            lblEstado.textProperty().unbind();
            bloquearFiltros(false);

            if (!sesionValida()) {
                registros.clear();
                mostrarError(
                        "La sesion cambio durante la consulta.");
                return;
            }

            registros.setAll(tarea.getValue());
            aplicarFiltros();
        });

        tarea.setOnFailed(evento -> {

            if (!vistaActiva) {
                return;
            }

            lblEstado.textProperty().unbind();
            bloquearFiltros(false);

            registros.clear();
            filtrados.setPredicate(r -> true);

            tablaHistorial.setPlaceholder(
                    new Label("No se pudo cargar el historial."));

            lblTotal.setText("Mostrando: 0");

            mostrarError(
                    "No se pudo consultar el historial: "
                    + tarea.getException().getMessage());
        });

        Thread hilo = new Thread(
                tarea, "consulta-historial-movimientos");

        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onLimpiarFiltros() {

        if (cargando) {
            return;
        }

        cmbCategoria.getSelectionModel().selectFirst();
        cmbTipo.getSelectionModel().selectFirst();

        fechaDesde.setValue(null);
        fechaHasta.setValue(null);

        aplicarFiltros();
    }

    private void cancelarConsulta() {

        vistaActiva = false;

        if (tareaActual != null
                && tareaActual.isRunning()) {

            tareaActual.cancel(true);
        }

        if (lblEstado.textProperty().isBound()) {
            lblEstado.textProperty().unbind();
        }
    }

    @FXML
    private void onVolver() {

        cancelarConsulta();
        CareStockApp.getInstance().mostrarDashboard();
    }

    @FXML
    private void onCerrarSesion() {

        cancelarConsulta();
        CareStockApp.getInstance().cerrarSesion();
    }

    private void mostrarError(String mensaje) {

        lblEstado.setText(mensaje);

        Alert alerta = new Alert(
                Alert.AlertType.WARNING, mensaje);

        alerta.setHeaderText(
                "Consulta de historial");

        alerta.showAndWait();
    }
}
