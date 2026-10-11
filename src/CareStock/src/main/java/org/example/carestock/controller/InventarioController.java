package org.example.carestock.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import org.example.carestock.CareStockApp;
import org.example.carestock.session.SesionUsuario;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.carestock.facade.Inventario;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class InventarioController {

    @FXML private TableView<Lote> tablaLotes;
    @FXML private TableColumn<Lote, Integer> colIdLote;
    @FXML private TableColumn<Lote, String> colNumeroLote;
    @FXML private TableColumn<Lote, Integer> colIdMedicamento;
    @FXML private TableColumn<Lote, Integer> colCantidad;
    @FXML private TableColumn<Lote, LocalDate> colFechaVencimiento;
    @FXML private TableColumn<Lote, String> colEstado;

    @FXML private Label lblUsuarioSesion;
    @FXML private Label lblEmailSesion;
    @FXML private Label lblRolSesion;
    @FXML private Label lblFarmaciaSesion;

    @FXML private Label lblTotalLotes;
    @FXML private Label lblLotesVencidos;
    @FXML private Label lblLoteSeleccionadoInfo;

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbCategoria;

    private static final String CATEGORIA_TODAS = "Todas";

    private Inventario inventario;
    private final ObservableList<Lote> listaLotes;
    private FilteredList<Lote> datosFiltrados;
    private Lote loteSeleccionado;
    private Predicate<Lote> filtroTexto = lote -> true;
    private Predicate<Lote> filtroEstado = lote -> true;
    private Predicate<Lote> filtroCategoria = lote -> true;
    private Map<Integer, String> categoriaPorMedicamento = new HashMap<>();

    public InventarioController() {
        this.listaLotes = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        if (!SesionUsuario.estaActiva()) {
            throw new IllegalStateException("No hay sesion autenticada");
        }
        inventario = new Inventario(SesionUsuario.usuarioParaFachada());
        var sesion = SesionUsuario.actual();
        lblUsuarioSesion.setText(sesion.nombre());
        lblEmailSesion.setText(sesion.email());
        lblRolSesion.setText(sesion.rol());
        lblFarmaciaSesion.setText(sesion.farmacia() == null
                ? "Sin farmacia asignada"
                : sesion.farmacia() + " (#" + sesion.idFarmacia() + ")");

        colIdLote.setCellValueFactory(new PropertyValueFactory<>("idLote"));
        colNumeroLote.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colIdMedicamento.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));
        colFechaVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLote"));

        cmbCategoria.getItems().add(CATEGORIA_TODAS);
        cmbCategoria.getSelectionModel().selectFirst();
        cargarCategorias();

        configurarEstiloFilas();
        configurarFiltroBusqueda();

        // Selección intuitiva de fila
        tablaLotes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            loteSeleccionado = newSel;
            if (newSel != null) {
                lblLoteSeleccionadoInfo.setText("ID " + newSel.getIdLote() + " | " + newSel.getNumeroLote() +
                        " (Med #" + newSel.getIdMedicamento() + ") - Stock: " + newSel.getCantidadActual());
                lblLoteSeleccionadoInfo.setStyle("-fx-text-fill: #1A365D; -fx-font-weight: bold;");
            } else {
                lblLoteSeleccionadoInfo.setText("Ninguno (Haga clic en una fila de la tabla)");
                lblLoteSeleccionadoInfo.setStyle("-fx-text-fill: #4A5568; -fx-font-style: italic;");
            }
        });

        cargarDatosTabla();
    }

    private void configurarFiltroBusqueda() {
        datosFiltrados = new FilteredList<>(listaLotes, p -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroTexto(newVal));
        cmbCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroCategoria(newVal));

        SortedList<Lote> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaLotes.comparatorProperty());
        tablaLotes.setItems(datosOrdenados);
        tablaLotes.setPlaceholder(new Label("No hay datos disponibles para los filtros seleccionados."));
    }

    private void aplicarFiltros() {
        datosFiltrados.setPredicate(filtroTexto.and(filtroEstado).and(filtroCategoria));
    }

    private void aplicarFiltroTexto(String texto) {
        String filtro = texto == null ? "" : texto.toLowerCase().trim();
        filtroTexto = lote -> filtro.isEmpty()
                || (lote.getNumeroLote() != null && lote.getNumeroLote().toLowerCase().contains(filtro))
                || String.valueOf(lote.getIdMedicamento()).contains(filtro)
                || (lote.getEstadoLote() != null && lote.getEstadoLote().toLowerCase().contains(filtro));
        aplicarFiltros();
    }

    private void aplicarFiltroCategoria(String categoria) {
        if (categoria == null || CATEGORIA_TODAS.equals(categoria)) {
            filtroCategoria = lote -> true;
        } else {
            filtroCategoria = lote -> categoria.equalsIgnoreCase(categoriaPorMedicamento.get(lote.getIdMedicamento()));
        }
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarTodos() {
        filtroEstado = lote -> true;
        txtBuscar.clear();
        cmbCategoria.getSelectionModel().selectFirst();
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarVencidos() {
        filtroEstado = lote -> lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(LocalDate.now());
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarDisponibles() {
        filtroEstado = Lote::esAptoParaDispensar;
        aplicarFiltros();
    }

    private void configurarEstiloFilas() {
        tablaLotes.setRowFactory(tv -> new TableRow<Lote>() {
            @Override
            protected void updateItem(Lote item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getFechaVencimiento() != null && item.getFechaVencimiento().isBefore(LocalDate.now())) {
                    setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #742A2A; -fx-font-weight: bold;");
                } else if (!"DISPONIBLE".equalsIgnoreCase(item.getEstadoLote())) {
                    setStyle("-fx-background-color: #FEEBC8; -fx-text-fill: #7B341E;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    @FXML
    private void onAbrirCatalogo() {
        CareStockApp.getInstance().mostrarCatalogo();
    }

    @FXML
    private void onCerrarSesion() {
        CareStockApp.getInstance().cerrarSesion();
    }

    @FXML
    private void onRefrescar() {
        cargarDatosTabla();
    }

    @FXML
    private void onDispensar() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Por favor seleccione un lote directamente de la tabla.");
            return;
        }

        String strCantidad = txtCantidad.getText();
        if (strCantidad == null || strCantidad.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Incompleto", "Por favor ingrese la cantidad a dispensar.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(strCantidad.trim());

            inventario.dispensarMedicamento(loteSeleccionado.getIdLote(), cantidad);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Dispensación Exitosa",
                    "Se dispensaron " + cantidad + " unidades del lote " + loteSeleccionado.getNumeroLote() + " correctamente.");

            txtCantidad.clear();
            cargarDatosTabla();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "La cantidad a dispensar debe ser un número entero válido.");
        } catch (ReglaNegocioException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA DE FARMACOVIGILANCIA", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    private void cargarDatosTabla() {
        try {
            List<Lote> lotes = inventario.listarLotes();
            tablaLotes.getSelectionModel().clearSelection();
            loteSeleccionado = null;
            listaLotes.setAll(lotes);
            categoriaPorMedicamento = inventario.categoriaPorMedicamento();

            // Actualizar contadores KPI
            lblTotalLotes.setText(String.valueOf(lotes.size()));
            long vencidos = lotes.stream()
                    .filter(l -> l.getFechaVencimiento() != null && l.getFechaVencimiento().isBefore(LocalDate.now()))
                    .count();
            lblLotesVencidos.setText(String.valueOf(vencidos));

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudieron obtener los datos de Neon DB: " + e.getMessage());
        }
    }

    private void cargarCategorias() {
        try {
            cmbCategoria.getItems().addAll(inventario.listarCategoriasMedicamento().values());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudieron obtener las categorías: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("CareStock - " + titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}