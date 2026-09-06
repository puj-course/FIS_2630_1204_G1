package com.carestock.view;

import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class MainDashboardFX extends Application {

    private TableView<Medicamento> tablaInventario = new TableView<>();
    private ObservableList<Medicamento> listaMedicamentos = FXCollections.observableArrayList();
    private FilteredList<Medicamento> listaFiltrada = new FilteredList<>(listaMedicamentos, p -> true);

    private MedicamentoDAO medicamentoDAO = new MedicamentoDAO();

    private Label lblTotalStock = new Label("0");
    private Label lblProximosVencer = new Label("0");
    private Label lblAlertasCriticas = new Label("0");

    private Button btnFiltrarCriticos;
    private boolean filtrandoCriticos = false;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #F8F9FA;");

        HBox topbar = buildTopbar();
        HBox metricCards = buildMetricCards();
        VBox tableContainer = buildTableSection();

        mainContent.getChildren().addAll(topbar, metricCards, tableContainer);
        root.setCenter(mainContent);

        cargarDatosDesdeBD();

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("CareStock - Módulo de Inventarios (Conectado a Neon DB)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cargarDatosDesdeBD() {
        try {
            List<Medicamento> desdeBD = medicamentoDAO.obtenerTodos();
            listaMedicamentos.setAll(desdeBD);

            int totalStock = medicamentoDAO.obtenerTotalUnidadesStock();
            int alertas = medicamentoDAO.obtenerAlertasCriticas();

            lblTotalStock.setText(String.format("%,d", totalStock));
            lblProximosVencer.setText("0");
            lblAlertasCriticas.setText(String.valueOf(alertas));

        } catch (Exception e) {
            System.err.println("Error al cargar datos desde Neon DB: " + e.getMessage());
        }
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #A3D9D2;");

        Label logo = new Label("CareStock");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1C313A;");

        Button btnDashboard = new Button("Dashboard");
        btnDashboard.setMaxWidth(Double.MAX_VALUE);
        btnDashboard.setStyle("-fx-background-color: #B39DDB; -fx-text-fill: white; -fx-font-weight: bold;");

        sidebar.getChildren().addAll(logo, new Separator(), btnDashboard);
        return sidebar;
    }

    private HBox buildTopbar() {
        HBox topbar = new HBox();
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Dashboard general");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox botonesAccion = new VBox(8);
        botonesAccion.setAlignment(Pos.CENTER_RIGHT);

        Button btnAgregar = new Button("+ Agregar Medicamento");
        btnAgregar.setStyle("-fx-background-color: #A3D9D2; -fx-font-weight: bold;");
        btnAgregar.setOnAction(e -> abrirFormularioAgregar());

        btnFiltrarCriticos = new Button("⚠️ Ver Alertas Críticas");
        btnFiltrarCriticos.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-weight: bold;");
        btnFiltrarCriticos.setOnAction(e -> alternarFiltroCriticos());

        botonesAccion.getChildren().addAll(btnAgregar, btnFiltrarCriticos);

        topbar.getChildren().addAll(title, spacer, botonesAccion);
        return topbar;
    }

    private void alternarFiltroCriticos() {
        filtrandoCriticos = !filtrandoCriticos;

        if (filtrandoCriticos) {
            listaFiltrada.setPredicate(m -> 
                m.getStockTotal() != null && m.getStockMinimo() != null && m.getStockTotal() <= m.getStockMinimo()
            );
            btnFiltrarCriticos.setText("📋 Ver Todos los Medicamentos");
            btnFiltrarCriticos.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold;");
        } else {
            listaFiltrada.setPredicate(p -> true);
            btnFiltrarCriticos.setText("⚠️ Ver Alertas Críticas");
            btnFiltrarCriticos.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-weight: bold;");
        }
    }

    private HBox buildMetricCards() {
        HBox container = new HBox(15);
        container.getChildren().addAll(
            createCard(lblTotalStock, "Unidades en stock"),
            createCard(lblProximosVencer, "Próximos a vencer"),
            createCard(lblAlertasCriticas, "Alertas críticas")
        );
        return container;
    }

    private VBox createCard(Label numLabel, String label) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        numLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label subText = new Label(label);
        subText.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

        card.getChildren().addAll(numLabel, subText);
        return card;
    }

    private VBox buildTableSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label lblSection = new Label("Inventario en Neon DB");
        lblSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableColumn<Medicamento, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colId.setPrefWidth(50);

        TableColumn<Medicamento, String> colInvima = new TableColumn<>("INVIMA");
        colInvima.setCellValueFactory(new PropertyValueFactory<>("codigoInvima"));

        TableColumn<Medicamento, String> colNombre = new TableColumn<>("NOMBRE");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreComercial"));

        TableColumn<Medicamento, String> colPrincipio = new TableColumn<>("PRINCIPIO");
        colPrincipio.setCellValueFactory(new PropertyValueFactory<>("principioActivo"));

        TableColumn<Medicamento, String> colCategoria = new TableColumn<>("CATEGORIA");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        TableColumn<Medicamento, Integer> colStock = new TableColumn<>("STOCK");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockTotal"));

        TableColumn<Medicamento, Integer> colStockMin = new TableColumn<>("STOCK MÍN");
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        tablaInventario.getColumns().clear();
        tablaInventario.getColumns().addAll(colId, colInvima, colNombre, colPrincipio, colCategoria, colStock, colStockMin);
        
        tablaInventario.setItems(listaFiltrada);
        tablaInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        section.getChildren().addAll(lblSection, tablaInventario);
        return section;
    }

    private void abrirFormularioAgregar() {
        FormularioMedicamentoDialog dialog = new FormularioMedicamentoDialog();
        Optional<Medicamento> result = dialog.showAndWait();
        result.ifPresent(medicamento -> {
            boolean exito = medicamentoDAO.guardar(medicamento);
            if (exito) {
                cargarDatosDesdeBD();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error al guardar el medicamento en Neon DB.");
                alert.showAndWait();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
