package com.carestock.view;

import com.carestock.config.AppConfig;
import com.carestock.controller.IngresoLoteController;
import com.carestock.dao.LoteDAO;
import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainDashboardFX extends Application {

    private Stage primaryStage;

    private final TableView<Medicamento> tablaInventario =
            new TableView<>();

    private final ObservableList<Medicamento> listaMedicamentos =
            FXCollections.observableArrayList();

    private final FilteredList<Medicamento> listaFiltrada =
            new FilteredList<>(listaMedicamentos, p -> true);

    private final MedicamentoDAO medicamentoDAO =
            new MedicamentoDAO();

    private final LoteDAO loteDAO =
            new LoteDAO();

    private final Label lblTotalStock =
            new Label("0");

    private final Label lblProximosVencer =
            new Label("0");

    private final Label lblAlertasCriticas =
            new Label("0");

    private Button btnFiltrarCriticos;

    private boolean filtrandoCriticos = false;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        BorderPane root = new BorderPane();

        root.setLeft(buildSidebar());

        VBox mainContent = new VBox(20);

        mainContent.setPadding(
                new Insets(20)
        );

        mainContent.setStyle(
                "-fx-background-color: #F8F9FA;"
        );

        mainContent.getChildren().addAll(
                buildTopbar(),
                buildMetricCards(),
                buildTableSection()
        );

        root.setCenter(mainContent);

        Scene scene =
                new Scene(root, 1200, 700);

        primaryStage.setTitle(
                "CareStock - Gestión de Inventario"
        );

        primaryStage.setScene(scene);

        primaryStage.setResizable(true);

        primaryStage.show();

        cargarDatosDesdeBD();
    }

    public void cargarDatosDesdeBD() {

        List<Medicamento> desdeBD =
                medicamentoDAO.obtenerTodos();

        listaMedicamentos.setAll(desdeBD);

        lblTotalStock.setText(
                String.format(
                        "%,d",
                        medicamentoDAO.obtenerTotalUnidadesStock()
                )
        );

        lblAlertasCriticas.setText(
                String.valueOf(
                        medicamentoDAO.obtenerAlertasCriticas()
                )
        );

        try {

            lblProximosVencer.setText(
                    String.valueOf(
                            loteDAO.contarProximosAVencer(30)
                    )
            );

        } catch (SQLException e) {

            lblProximosVencer.setText("0");

            System.err.println(
                    "No fue posible consultar lotes próximos a vencer: "
                    + e.getMessage()
            );
        }
    }

    private VBox buildSidebar() {

        VBox sidebar =
                new VBox(15);

        sidebar.setPadding(
                new Insets(20)
        );

        sidebar.setPrefWidth(210);

        sidebar.setStyle(
                "-fx-background-color: #A3D9D2;"
        );

        Label logo =
                new Label("CareStock");

        logo.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1C313A;"
        );

        Button btnDashboard =
                new Button("Dashboard");

        btnDashboard.setMaxWidth(
                Double.MAX_VALUE
        );

        btnDashboard.setStyle(
                "-fx-background-color: #B39DDB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        Button btnIngresoLote =
                new Button("Ingreso de lotes");

        btnIngresoLote.setMaxWidth(
                Double.MAX_VALUE
        );

        btnIngresoLote.setOnAction(
                e -> abrirIngresoLote()
        );

        sidebar.getChildren().addAll(
                logo,
                new Separator(),
                btnDashboard,
                btnIngresoLote
        );

        return sidebar;
    }

    private HBox buildTopbar() {

        HBox topbar =
                new HBox(10);

        topbar.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label("Dashboard general");

        title.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label lblUsuario =
                new Label(
                        "Sesión: "
                        + AppConfig.getCurrentUserEmail()
                );

        lblUsuario.setStyle(
                "-fx-text-fill: #607D8B;" +
                "-fx-font-size: 12px;"
        );

        Button btnCerrarSesion =
                new Button("Cerrar sesión");

        btnCerrarSesion.setStyle(
                "-fx-background-color: #ECEFF1;" +
                "-fx-text-fill: #37474F;" +
                "-fx-font-weight: bold;"
        );

        btnCerrarSesion.setOnAction(
                e -> cerrarSesion()
        );

        VBox botonesAccion =
                new VBox(8);

        botonesAccion.setAlignment(
                Pos.CENTER_RIGHT
        );

        HBox filaPrincipal =
                new HBox(8);

        Button btnAgregar =
                new Button("+ Agregar medicamento");

        btnAgregar.setStyle(
                "-fx-background-color: #A3D9D2;" +
                "-fx-font-weight: bold;"
        );

        btnAgregar.setOnAction(
                e -> abrirFormularioAgregar()
        );

        Button btnLote =
                new Button("+ Ingresar lote");

        btnLote.setStyle(
                "-fx-background-color: #B39DDB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        btnLote.setOnAction(
                e -> abrirIngresoLote()
        );

        filaPrincipal.getChildren().addAll(
                btnAgregar,
                btnLote
        );

        btnFiltrarCriticos =
                new Button(
                        "Ver alertas críticas"
                );

        btnFiltrarCriticos.setStyle(
                "-fx-background-color: #FFCDD2;" +
                "-fx-text-fill: #C62828;" +
                "-fx-font-weight: bold;"
        );

        btnFiltrarCriticos.setOnAction(
                e -> alternarFiltroCriticos()
        );

        botonesAccion.getChildren().addAll(
                filaPrincipal,
                btnFiltrarCriticos
        );

        topbar.getChildren().addAll(
                title,
                spacer,
                lblUsuario,
                btnCerrarSesion,
                botonesAccion
        );

        return topbar;
    }

    private void alternarFiltroCriticos() {

        filtrandoCriticos =
                !filtrandoCriticos;

        if (filtrandoCriticos) {

            listaFiltrada.setPredicate(
                    m ->
                            m.getStockTotal() != null
                            && m.getStockMinimo() != null
                            && m.getStockTotal()
                               <= m.getStockMinimo()
            );

            btnFiltrarCriticos.setText(
                    "Ver todos los medicamentos"
            );

            btnFiltrarCriticos.setStyle(
                    "-fx-background-color: #E0E0E0;" +
                    "-fx-text-fill: #333333;" +
                    "-fx-font-weight: bold;"
            );

        } else {

            listaFiltrada.setPredicate(
                    p -> true
            );

            btnFiltrarCriticos.setText(
                    "Ver alertas críticas"
            );

            btnFiltrarCriticos.setStyle(
                    "-fx-background-color: #FFCDD2;" +
                    "-fx-text-fill: #C62828;" +
                    "-fx-font-weight: bold;"
            );
        }
    }

    private HBox buildMetricCards() {

        HBox container =
                new HBox(15);

        container.getChildren().addAll(

                createCard(
                        lblTotalStock,
                        "Unidades en stock"
                ),

                createCard(
                        lblProximosVencer,
                        "Lotes próximos a vencer (30 días)"
                ),

                createCard(
                        lblAlertasCriticas,
                        "Alertas críticas"
                )
        );

        return container;
    }

    private VBox createCard(
            Label numLabel,
            String label
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(15)
        );

        card.setPrefWidth(230);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8;"
        );

        numLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label subText =
                new Label(label);

        subText.setStyle(
                "-fx-text-fill: #7F8C8D;" +
                "-fx-font-size: 11px;"
        );

        card.getChildren().addAll(
                numLabel,
                subText
        );

        return card;
    }

    private VBox buildTableSection() {

        VBox section =
                new VBox(10);

        section.setPadding(
                new Insets(15)
        );

        section.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8;"
        );

        Label lblSection =
                new Label(
                        "Inventario PostgreSQL"
                );

        lblSection.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        TableColumn<Medicamento, Long> colId =
                new TableColumn<>("ID");

        colId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idMedicamento"
                )
        );

        colId.setPrefWidth(50);

        TableColumn<Medicamento, String> colInvima =
                new TableColumn<>("INVIMA");

        colInvima.setCellValueFactory(
                new PropertyValueFactory<>(
                        "codigoInvima"
                )
        );

        TableColumn<Medicamento, String> colNombre =
                new TableColumn<>("NOMBRE");

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nombreComercial"
                )
        );

        TableColumn<Medicamento, String> colPrincipio =
                new TableColumn<>("PRINCIPIO");

        colPrincipio.setCellValueFactory(
                new PropertyValueFactory<>(
                        "principioActivo"
                )
        );

        TableColumn<Medicamento, String> colCategoria =
                new TableColumn<>("CATEGORÍA");

        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>(
                        "categoria"
                )
        );

        TableColumn<Medicamento, Integer> colStock =
                new TableColumn<>("STOCK");

        colStock.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockTotal"
                )
        );

        TableColumn<Medicamento, Integer> colStockMin =
                new TableColumn<>("STOCK MÍN");

        colStockMin.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockMinimo"
                )
        );

        tablaInventario.getColumns().setAll(
                colId,
                colInvima,
                colNombre,
                colPrincipio,
                colCategoria,
                colStock,
                colStockMin
        );

        tablaInventario.setItems(
                listaFiltrada
        );

        tablaInventario.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        section.getChildren().addAll(
                lblSection,
                tablaInventario
        );

        return section;
    }

    private void abrirFormularioAgregar() {

        FormularioMedicamentoDialog dialog =
                new FormularioMedicamentoDialog();

        Optional<Medicamento> result =
                dialog.showAndWait();

        result.ifPresent(
                medicamento -> {

                    if (
                        medicamentoDAO.guardar(
                                medicamento
                        )
                    ) {

                        cargarDatosDesdeBD();

                        AlertUtil.mostrarExito(
                                "El medicamento \""
                                + medicamento.getNombreComercial()
                                + "\" se registró correctamente."
                        );

                    } else {

                        AlertUtil.mostrarError(
                                "No se pudo guardar el medicamento. "
                                + "Verifique la categoría, los datos "
                                + "y la conexión a PostgreSQL."
                        );
                    }
                }
        );
    }

    private void abrirIngresoLote() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/carestock/view/IngresoLote.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            IngresoLoteController controller =
                    loader.getController();

            controller.setOnSaved(
                    this::cargarDatosDesdeBD
            );

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "CareStock - Ingreso de lote"
            );

            stage.initModality(
                    Modality.WINDOW_MODAL
            );

            if (
                tablaInventario.getScene()
                != null
            ) {

                stage.initOwner(
                        tablaInventario
                                .getScene()
                                .getWindow()
                );
            }

            stage.setScene(
                    new Scene(root)
            );

            stage.setResizable(false);

            stage.showAndWait();

        } catch (
                IOException
                | RuntimeException e
        ) {

            AlertUtil.mostrarError(
                    "No fue posible abrir el formulario "
                    + "de ingreso de lote. "
                    + e.getMessage()
            );
        }
    }

    private void cerrarSesion() {

        AppConfig.clearCurrentUser();

        try {

            LoginFX login =
                    new LoginFX();

            login.start(
                    primaryStage
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    "No fue posible cerrar "
                    + "la sesión correctamente."
            );

            System.err.println(
                    "Error cerrando sesión: "
                    + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
