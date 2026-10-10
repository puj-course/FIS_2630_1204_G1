
package org.example.carestock.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.example.carestock.CareStockApp;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.dao.LoteDAO;
import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.dao.MedicamentoDAO;
import org.example.carestock.dao.MedicamentoDAOImpl;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;
import org.example.carestock.service.FarmacovigilanciaService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class InventarioController {

    // ========================================
    // TABLA DE INVENTARIO
    // ========================================

    @FXML private TableView<Lote> tablaLotes;
    @FXML private TableColumn<Lote, Integer> colIdLote;
    @FXML private TableColumn<Lote, String> colNumeroLote;
    @FXML private TableColumn<Lote, Integer> colIdMedicamento;
    @FXML private TableColumn<Lote, Integer> colCantidad;
    @FXML private TableColumn<Lote, LocalDate> colFechaVencimiento;
    @FXML private TableColumn<Lote, String> colEstado;

    // ========================================
    // INFORMACION DEL USUARIO AUTENTICADO
    // ========================================

    @FXML private Label lblUsuarioSesion;
    @FXML private Label lblEmailSesion;
    @FXML private Label lblRolSesion;
    @FXML private Label lblFarmaciaSesion;

    // ========================================
    // INDICADORES DEL DASHBOARD
    // ========================================

    @FXML private Label lblTotalLotes;
    @FXML private Label lblLotesVencidos;
    @FXML private Label lblLoteSeleccionadoInfo;

    // ========================================
    // CONTROLES DE BUSQUEDA
    // ========================================

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbCategoria;

    private static final String CATEGORIA_TODAS = "Todas";

    // ========================================
    // DEPENDENCIAS
    // ========================================

    private final LoteDAO loteDAO;
    private final MedicamentoDAO medicamentoDAO;
    private final FarmacovigilanciaService farmacovigilanciaService;

    private final ObservableList<Lote> listaLotes;

    private FilteredList<Lote> datosFiltrados;
    private Lote loteSeleccionado;

    private Predicate<Lote> filtroTexto = lote -> true;
    private Predicate<Lote> filtroEstado = lote -> true;
    private Predicate<Lote> filtroCategoria = lote -> true;

    private Map<Integer, String> categoriaPorMedicamento =
            new HashMap<>();

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public InventarioController() {

        this.loteDAO = new LoteDAOImpl();

        this.medicamentoDAO = new MedicamentoDAOImpl();

        this.farmacovigilanciaService =
                new FarmacovigilanciaService();

        this.listaLotes =
                FXCollections.observableArrayList();
    }

    // ========================================
    // INICIALIZACION
    // ========================================

    @FXML
    public void initialize() {

        cargarInformacionSesion();

        colIdLote.setCellValueFactory(
                new PropertyValueFactory<>("idLote")
        );

        colNumeroLote.setCellValueFactory(
                new PropertyValueFactory<>("numeroLote")
        );

        colIdMedicamento.setCellValueFactory(
                new PropertyValueFactory<>("idMedicamento")
        );

        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidadActual")
        );

        colFechaVencimiento.setCellValueFactory(
                new PropertyValueFactory<>("fechaVencimiento")
        );

        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estadoLote")
        );

        cmbCategoria.getItems().add(CATEGORIA_TODAS);
        cmbCategoria.getSelectionModel().selectFirst();

        cargarCategorias();
        configurarEstiloFilas();
        configurarFiltroBusqueda();

        tablaLotes.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {

                    loteSeleccionado = newSel;

                    if (newSel != null) {

                        lblLoteSeleccionadoInfo.setText(
                                "ID " + newSel.getIdLote()
                                        + " | " + newSel.getNumeroLote()
                                        + " (Med #"
                                        + newSel.getIdMedicamento()
                                        + ") - Stock: "
                                        + newSel.getCantidadActual()
                        );

                        lblLoteSeleccionadoInfo.setStyle(
                                "-fx-text-fill: #1A365D;"
                                        + "-fx-font-weight: bold;"
                        );

                    } else {

                        lblLoteSeleccionadoInfo.setText(
                                "Ninguno (Haga clic en una fila)"
                        );

                        lblLoteSeleccionadoInfo.setStyle(
                                "-fx-text-fill: #4A5568;"
                                        + "-fx-font-style: italic;"
                        );
                    }
                });

        cargarDatosTabla();
    }

    // ========================================
    // MOSTRAR DATOS DEL USUARIO
    // ========================================

    private void cargarInformacionSesion() {

        if (!SesionUsuario.estaActiva()) {
            throw new IllegalStateException(
                    "No hay sesion autenticada"
            );
        }

        var sesion = SesionUsuario.actual();

        lblUsuarioSesion.setText(
                sesion.nombre() == null
                        ? "Usuario sin nombre"
                        : sesion.nombre()
        );

        lblEmailSesion.setText(
                sesion.email() == null
                        ? "Correo no disponible"
                        : sesion.email()
        );

        lblRolSesion.setText(sesion.rol());

        if (sesion.farmacia() == null) {

            lblFarmaciaSesion.setText(
                    "Sin farmacia asignada"
            );

        } else {

            lblFarmaciaSesion.setText(
                    sesion.farmacia()
                            + " (#" + sesion.idFarmacia() + ")"
            );
        }
    }

    // ========================================
    // CONFIGURACION DE FILTROS
    // ========================================

    private void configurarFiltroBusqueda() {

        datosFiltrados =
                new FilteredList<>(listaLotes, p -> true);

        txtBuscar.textProperty().addListener(
                (obs, oldVal, newVal) ->
                        aplicarFiltroTexto(newVal)
        );

        cmbCategoria.valueProperty().addListener(
                (obs, oldVal, newVal) ->
                        aplicarFiltroCategoria(newVal)
        );

        SortedList<Lote> datosOrdenados =
                new SortedList<>(datosFiltrados);

        datosOrdenados.comparatorProperty()
                .bind(tablaLotes.comparatorProperty());

        tablaLotes.setItems(datosOrdenados);

        tablaLotes.setPlaceholder(
                new Label(
                        "No hay datos disponibles para los filtros seleccionados."
                )
        );
    }

    private void aplicarFiltros() {

        datosFiltrados.setPredicate(
                filtroTexto
                        .and(filtroEstado)
                        .and(filtroCategoria)
        );
    }

    private void aplicarFiltroTexto(String texto) {

        String filtro = texto == null
                ? ""
                : texto.toLowerCase().trim();

        filtroTexto = lote ->
                filtro.isEmpty()
                        || (lote.getNumeroLote() != null
                        && lote.getNumeroLote()
                        .toLowerCase().contains(filtro))
                        || String.valueOf(
                        lote.getIdMedicamento()
                ).contains(filtro)
                        || (lote.getEstadoLote() != null
                        && lote.getEstadoLote()
                        .toLowerCase().contains(filtro));

        aplicarFiltros();
    }

    private void aplicarFiltroCategoria(
            String categoria
    ) {

        if (categoria == null ||
                CATEGORIA_TODAS.equals(categoria)) {

            filtroCategoria = lote -> true;

        } else {

            filtroCategoria = lote ->
                    categoria.equalsIgnoreCase(
                            categoriaPorMedicamento.get(
                                    lote.getIdMedicamento()
                            )
                    );
        }

        aplicarFiltros();
    }

    // ========================================
    // FILTROS DEL DASHBOARD
    // ========================================

    @FXML
    private void onFiltrarTodos() {

        filtroEstado = lote -> true;

        txtBuscar.clear();

        cmbCategoria.getSelectionModel().selectFirst();

        aplicarFiltros();
    }

    @FXML
    private void onFiltrarVencidos() {

        filtroEstado = lote ->
                lote.getFechaVencimiento() != null
                        && lote.getFechaVencimiento()
                        .isBefore(LocalDate.now());

        aplicarFiltros();
    }

    @FXML
    private void onFiltrarDisponibles() {

        filtroEstado = Lote::esAptoParaDispensar;

        aplicarFiltros();
    }

    // ========================================
    // ESTILOS DE FILAS
    // ========================================

    private void configurarEstiloFilas() {

        tablaLotes.setRowFactory(
                tv -> new TableRow<Lote>() {

                    @Override
                    protected void updateItem(
                            Lote item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (item == null || empty) {

                            setStyle("");

                        } else if (
                                item.getFechaVencimiento() != null
                                        && item.getFechaVencimiento()
                                        .isBefore(LocalDate.now())
                        ) {

                            setStyle(
                                    "-fx-background-color: #FED7D7;"
                                            + "-fx-text-fill: #742A2A;"
                                            + "-fx-font-weight: bold;"
                            );

                        } else if (
                                !"DISPONIBLE".equalsIgnoreCase(
                                        item.getEstadoLote()
                                )
                        ) {

                            setStyle(
                                    "-fx-background-color: #FEEBC8;"
                                            + "-fx-text-fill: #7B341E;"
                            );

                        } else {

                            setStyle("");
                        }
                    }
                }
        );
    }

    // ========================================
    // NAVEGACION
    // ========================================

    @FXML
    private void onAbrirCatalogo() {

        CareStockApp.getInstance()
                .mostrarCatalogo();
    }

    @FXML
    private void onCerrarSesion() {

        CareStockApp.getInstance()
                .cerrarSesion();
    }

    // ========================================
    // ACTUALIZAR INVENTARIO
    // ========================================

    @FXML
    private void onRefrescar() {

        cargarDatosTabla();
    }

    // ========================================
    // DISPENSACION
    // ========================================

    @FXML
    private void onDispensar() {

        if (loteSeleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccion Requerida",
                    "Seleccione un lote de la tabla."
            );

            return;
        }

        String strCantidad = txtCantidad.getText();

        if (strCantidad == null ||
                strCantidad.trim().isEmpty()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campo Incompleto",
                    "Ingrese la cantidad a dispensar."
            );

            return;
        }

        try {

            int cantidad =
                    Integer.parseInt(strCantidad.trim());

            farmacovigilanciaService.dispensarMedicamento(
                    loteSeleccionado.getIdLote(),
                    cantidad
            );

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Dispensacion Exitosa",
                    "Se dispensaron "
                            + cantidad
                            + " unidades del lote "
                            + loteSeleccionado.getNumeroLote()
            );

            txtCantidad.clear();

            cargarDatosTabla();

        } catch (NumberFormatException e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de Formato",
                    "La cantidad debe ser un numero entero."
            );

        } catch (ReglaNegocioException e) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Alerta de Farmacovigilancia",
                    e.getMessage()
            );

        } catch (Exception e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error del Sistema",
                    e.getMessage()
            );
        }
    }

    // ========================================
    // CARGA DE DATOS
    // ========================================

    private void cargarDatosTabla() {

        try {

            List<Lote> lotes =
                    loteDAO.listarTodos();

            listaLotes.setAll(lotes);

            categoriaPorMedicamento =
                    medicamentoDAO.categoriaPorMedicamento();

            lblTotalLotes.setText(
                    String.valueOf(lotes.size())
            );

            long vencidos = lotes.stream()
                    .filter(l ->
                            l.getFechaVencimiento() != null
                                    && l.getFechaVencimiento()
                                    .isBefore(LocalDate.now())
                    )
                    .count();

            lblLotesVencidos.setText(
                    String.valueOf(vencidos)
            );

        } catch (Exception e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de Conexion",
                    "No se pudieron obtener los datos: "
                            + e.getMessage()
            );
        }
    }

    private void cargarCategorias() {

        try {

            cmbCategoria.getItems().addAll(
                    medicamentoDAO.listarCategorias()
                            .values()
            );

        } catch (Exception e) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de Conexion",
                    "No se pudieron obtener las categorias: "
                            + e.getMessage()
            );
        }
    }

    // ========================================
    // MENSAJES
    // ========================================

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {

        Alert alert = new Alert(tipo);

        alert.setTitle(
                "CareStock - " + titulo
        );

        alert.setHeaderText(titulo);

        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}
