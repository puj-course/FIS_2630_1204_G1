package org.example.carestock.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.carestock.dao.LoteDAO;
import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;
import org.example.carestock.service.FarmacovigilanciaService;

import java.time.LocalDate;
import java.util.List;

public class InventarioController {

    @FXML private TableView<Lote> tablaLotes;
    @FXML private TableColumn<Lote, Integer> colIdLote;
    @FXML private TableColumn<Lote, String> colNumeroLote;
    @FXML private TableColumn<Lote, Integer> colIdMedicamento;
    @FXML private TableColumn<Lote, Integer> colCantidad;
    @FXML private TableColumn<Lote, LocalDate> colFechaVencimiento;
    @FXML private TableColumn<Lote, String> colEstado;

    @FXML private Label lblTotalLotes;
    @FXML private Label lblLotesVencidos;
    @FXML private Label lblLoteSeleccionadoInfo;

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCantidad;

    private final LoteDAO loteDAO;
    private final FarmacovigilanciaService farmacovigilanciaService;
    private final ObservableList<Lote> listaLotes;
    private FilteredList<Lote> datosFiltrados;
    private Lote loteSeleccionado;

    public InventarioController() {
        this.loteDAO = new LoteDAOImpl();
        this.farmacovigilanciaService = new FarmacovigilanciaService();
        this.listaLotes = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        colIdLote.setCellValueFactory(new PropertyValueFactory<>("idLote"));
        colNumeroLote.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colIdMedicamento.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));
        colFechaVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLote"));

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

        SortedList<Lote> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaLotes.comparatorProperty());
        tablaLotes.setItems(datosOrdenados);
    }

    private void aplicarFiltroTexto(String texto) {
        datosFiltrados.setPredicate(lote -> {
            if (texto == null || texto.trim().isEmpty()) return true;
            String filtro = texto.toLowerCase().trim();

            return (lote.getNumeroLote() != null && lote.getNumeroLote().toLowerCase().contains(filtro))
                    || String.valueOf(lote.getIdMedicamento()).contains(filtro)
                    || (lote.getEstadoLote() != null && lote.getEstadoLote().toLowerCase().contains(filtro));
        });
    }

    @FXML
    private void onFiltrarTodos() {
        txtBuscar.clear();
        datosFiltrados.setPredicate(lote -> true);
    }

    @FXML
    private void onFiltrarVencidos() {
        txtBuscar.clear();
        datosFiltrados.setPredicate(lote ->
                lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(LocalDate.now())
        );
    }

    @FXML
    private void onFiltrarDisponibles() {
        txtBuscar.clear();
        datosFiltrados.setPredicate(Lote::esAptoParaDispensar);
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

            farmacovigilanciaService.dispensarMedicamento(loteSeleccionado.getIdLote(), cantidad);

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
            List<Lote> lotes = loteDAO.listarTodos();
            listaLotes.setAll(lotes);

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

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("CareStock - " + titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}