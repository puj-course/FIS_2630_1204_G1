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
import org.example.carestock.service.ServicioAlertasInventario;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javafx.concurrent.Task;
import org.example.carestock.proxy.InventarioConsultas;

public class ControladorInventario {

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
    @FXML private Button btnCatalogo;
    @FXML private Button btnHistorial;
    @FXML private Button btnAjustes;
    @FXML private Button btnAlertas;
    @FXML private Button btnExportarCSV;
    @FXML private Button btnNuevoLote;
    @FXML private Button btnEditarLote;
    @FXML private Button btnRetirarLote;

    @FXML private Label lblTotalLotes;
    @FXML private Label lblLotesVencidos;
    @FXML private Label lblLoteSeleccionadoInfo;

    @FXML private Label lblTotalMedicamentos;
    @FXML private Label lblTotalAseo;
    @FXML private Label lblTotalMaternidad;

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;

    private static final String CATEGORIA_TODAS = "Todas";

    private Inventario inventario;
    private final ObservableList<Lote> listaLotes;
    private FilteredList<Lote> datosFiltrados;
    private Lote loteSeleccionado;
    private Predicate<Lote> filtroTexto = lote -> true;
    private Predicate<Lote> filtroEstado = lote -> true;
    private Predicate<Lote> filtroCategoria = lote -> true;
    private Map<Integer, String> categoriaPorMedicamento = new HashMap<>();

    public ControladorInventario() {
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
        boolean puedeConsultarCatalogo = sesion.idFarmacia() != null;
        btnCatalogo.setVisible(puedeConsultarCatalogo);
        btnCatalogo.setManaged(puedeConsultarCatalogo);
        boolean puedeGestionarLotes = SesionUsuario.esAdministrador()
                || "SUPER_ADMIN".equalsIgnoreCase(sesion.rol())
                || "FARMACEUTICO".equalsIgnoreCase(sesion.rol());
        puedeGestionarLotes = puedeGestionarLotes
                && sesion.idFarmacia() != null;
        for (Button boton : List.of(btnNuevoLote, btnEditarLote, btnRetirarLote)) {
            boton.setVisible(puedeGestionarLotes);
            boton.setManaged(puedeGestionarLotes);
        }
        String rolHistorial = sesion.rol() == null ? "" : sesion.rol().trim();
        boolean puedeVerHistorial = puedeConsultarCatalogo
                && ("ADMINISTRADOR".equalsIgnoreCase(rolHistorial)
                    || "FARMACEUTICO".equalsIgnoreCase(rolHistorial));
        btnHistorial.setVisible(puedeVerHistorial);
        btnHistorial.setManaged(puedeVerHistorial);
        String rol = sesion.rol() == null ? "" : sesion.rol().trim();
        boolean puedeAjustar = puedeConsultarCatalogo
                && ("ADMINISTRADOR".equalsIgnoreCase(rol)
                    || "SUPER_ADMIN".equalsIgnoreCase(rol));
        btnAjustes.setVisible(puedeAjustar);
        btnAjustes.setManaged(puedeAjustar);
        btnAlertas.setVisible(puedeConsultarCatalogo);
        btnAlertas.setManaged(puedeConsultarCatalogo);

        colIdLote.setCellValueFactory(new PropertyValueFactory<>("idLote"));
        colNumeroLote.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colIdMedicamento.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));
        colFechaVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colFechaVencimiento.setCellFactory(col -> new TableCell<Lote, LocalDate>() {
    @Override
    protected void updateItem(LocalDate fecha, boolean empty) {
        super.updateItem(fecha, empty);
        if (empty || fecha == null) {
            setText(null);
            setTooltip(null);
        } else {
            setText(fecha.toString());
            long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fecha);
            String mensaje;
            if (dias > 0) {
                mensaje = "Vence en " + dias + " días";
            } else if (dias == 0) {
                mensaje = "Vence hoy";
            } else {
                mensaje = "Vencido hace " + Math.abs(dias) + " días";
            }
            setTooltip(new Tooltip(mensaje));
        }
    }
});
        colEstado.setCellValueFactory(datos ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        estadoMostrado(datos.getValue())));

        cmbCategoria.getItems().add(CATEGORIA_TODAS);
        cmbCategoria.getSelectionModel().selectFirst();
        cargarCategorias();

        configurarEstiloFilas();
        configurarFiltroBusqueda();
                // #809: Configurar los DatePickers para el filtro
        dateInicio.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

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
        cargarContadoresPorCategoria();
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

        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();

        Predicate<Lote> filtroFechas = lote -> {
            if (lote.getFechaIngreso() == null) return false;
            LocalDate fechaLote = lote.getFechaIngreso().toLocalDate();
            
            boolean cumpleInicio = (inicio == null) || !fechaLote.isBefore(inicio);
            boolean cumpleFin = (fin == null) || !fechaLote.isAfter(fin);
            
            return cumpleInicio && cumpleFin;
        };

        datosFiltrados.setPredicate(filtroTexto.and(filtroEstado).and(filtroCategoria).and(filtroFechas));

        if (datosFiltrados.isEmpty()) {
            tablaLotes.setPlaceholder(new Label("No hay movimientos en este rango de fechas."));
        } else {
            tablaLotes.setPlaceholder(new Label("No hay datos disponibles para los filtros seleccionados."));
        }
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
        dateInicio.setValue(null); 
        dateFin.setValue(null);   
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarVencidos() {
        filtroEstado = lote -> lote.getFechaVencimiento() != null && !lote.getFechaVencimiento().isAfter(LocalDate.now());
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarDisponibles() {
        filtroEstado = Lote::esAptoParaDispensar;
        aplicarFiltros();
    }

    private String estadoMostrado(Lote lote) {
        if ("RETENIDO".equalsIgnoreCase(lote.getEstadoLote())) {
            return "RETENIDO";
        }
        if (lote.getFechaVencimiento() != null
                && !lote.getFechaVencimiento().isAfter(LocalDate.now())) {
            return "DESHABILITADO";
        }
        return lote.getEstadoLote() == null
                ? "SIN ESTADO"
                : lote.getEstadoLote();
    }

    private void configurarEstiloFilas() {
        tablaLotes.setRowFactory(tv -> new TableRow<Lote>() {
            @Override
            protected void updateItem(Lote item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getFechaVencimiento() != null && !item.getFechaVencimiento().isAfter(LocalDate.now())) {
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
    private void onCrearLote() {
        GestorLotesDialogo gestor = new GestorLotesDialogo(
                tablaLotes.getScene().getWindow());
        if (gestor.crear()) onRefrescar();
    }

    @FXML
    private void onEditarLote() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccion requerida",
                    "Selecciona primero un lote de la tabla.");
            return;
        }
        GestorLotesDialogo gestor = new GestorLotesDialogo(
                tablaLotes.getScene().getWindow());
        if (gestor.editar(loteSeleccionado)) onRefrescar();
    }

    @FXML
    private void onRetirarLote() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccion requerida",
                    "Selecciona primero un lote de la tabla.");
            return;
        }
        GestorLotesDialogo gestor = new GestorLotesDialogo(
                tablaLotes.getScene().getWindow());
        if (gestor.retirar(loteSeleccionado)) onRefrescar();
    }

    @FXML
    private void onAbrirMovimientos() {
        org.example.carestock.CareStockApp.getInstance()
                .mostrarFormularioMovimientos();
    }



    @FXML
    private void onAbrirAjustes() {
        CareStockApp.getInstance().mostrarFormularioAjustes();
    }

    @FXML
    private void onAbrirHistorial() {
        CareStockApp.getInstance().mostrarHistorialMovimientos();
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
        cargarContadoresPorCategoria();
    }

    private void cargarResumenAlertas() {
        if (!btnAlertas.isVisible()) {
            return;
        }

        Task<ServicioAlertasInventario.Resumen> tarea = new Task<>() {
            @Override
            protected ServicioAlertasInventario.Resumen call() throws Exception {
                return inventario.resumenAlertas();
            }
        };

        tarea.setOnSucceeded(e -> mostrarResumenAlertas(tarea.getValue()));
        tarea.setOnFailed(e -> btnAlertas.setText("Alertas"));

        Thread hilo = new Thread(tarea, "resumen-alertas");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResumenAlertas(ServicioAlertasInventario.Resumen resumen) {
        String color = resumen.criticas() > 0
                ? "#B3261E"
                : resumen.advertencias() > 0 ? "#C77700" : "#0E7C7B";

        btnAlertas.setText(resumen.total() == 0
                ? "Alertas"
                : "Alertas (" + resumen.total() + ")");
        btnAlertas.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 16;");
    }

    @FXML
    private void onAbrirAlertas() {
        CareStockApp.getInstance().mostrarAlertas();
    }

    @FXML
    private void onDispensar() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Por favor seleccione un lote directamente de la tabla.");
            return;
        }

        if (!loteSeleccionado.esAptoParaDispensar()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Lote deshabilitado",
                    "No se puede dispensar un lote vencido, retenido o agotado.");
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

    private void cargarContadoresPorCategoria() {
    try {
        Map<String, Integer> conteo = inventario.contarPorCategoria();
        lblTotalMedicamentos.setText(String.valueOf(conteo.getOrDefault("Medicamento", 0)));
        lblTotalAseo.setText(String.valueOf(conteo.getOrDefault("Aseo", 0)));
        lblTotalMaternidad.setText(String.valueOf(conteo.getOrDefault("Maternidad", 0)));
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void cargarDatosTabla() {
        try {
            List<Lote> lotes = inventario.listarLotes();
            tablaLotes.getSelectionModel().clearSelection();
            loteSeleccionado = null;
            listaLotes.setAll(lotes);
            try {
                categoriaPorMedicamento = inventario.categoriaPorMedicamento();
            } catch (Exception ex) {
                categoriaPorMedicamento = Map.of();
                System.err.println("No fue posible consultar las categorias: " + ex.getMessage());
            }
            aplicarFiltros();

            // Actualizar contadores KPI
            lblTotalLotes.setText(String.valueOf(lotes.size()));
            long vencidos = lotes.stream()
                    .filter(l -> l.getFechaVencimiento() != null && !l.getFechaVencimiento().isAfter(LocalDate.now()))
                    .count();
            lblLotesVencidos.setText(String.valueOf(vencidos));
            cargarResumenAlertas();

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
               @FXML
    private void onExportarCSV() {
        if (tablaLotes.getItems() == null || tablaLotes.getItems().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin datos para exportar", 
                    "No hay lotes visibles en la tabla. Si aplicaste filtros, intenta limpiarlos antes de exportar.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar Historial de Lotes");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Archivos CSV (*.csv)", "*.csv")
        );
        
        java.io.File archivo = fileChooser.showSaveDialog(tablaLotes.getScene().getWindow());

        if (archivo != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(archivo, "UTF-8")) {
                
                writer.println("ID Lote;Numero Lote;ID Medicamento;Cantidad;Fecha Vencimiento;Estado");

                for (Lote lote : tablaLotes.getItems()) {
                    writer.println(
                            lote.getIdLote() + ";" +
                            lote.getNumeroLote() + ";" +
                            lote.getIdMedicamento() + ";" +
                            lote.getCantidadActual() + ";" +
                            lote.getFechaVencimiento() + ";" +
                            lote.getEstadoLote()
                    );
                }

                mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación Exitosa", 
                        "El archivo CSV se guardó correctamente con " + tablaLotes.getItems().size() + " registros.\n\nRuta:\n" + archivo.getAbsolutePath());

            } catch (java.io.IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Exportación", 
                        "No se pudo guardar el archivo: " + e.getMessage());
            }
        }
    }
}
