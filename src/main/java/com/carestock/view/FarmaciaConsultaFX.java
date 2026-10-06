package com.carestock.view;

import com.carestock.dao.FarmaciaDAO;
import com.carestock.model.Farmacia;
import com.carestock.session.UserSession;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.util.List;

public class FarmaciaConsultaFX {

    private final FarmaciaDAO farmaciaDAO = new FarmaciaDAO();

    private final TableView<Farmacia> tabla = new TableView<>();

    private final ObservableList<Farmacia> datos =
            FXCollections.observableArrayList();

    private final TextField txtBuscar = new TextField();

    private final Label lblSinDatos = new Label(
            "No hay farmacias registradas que coincidan con la búsqueda."
    );

    private final StackPane contenedorTabla = new StackPane();

    public void mostrar(Window owner) {

        UserSession.CurrentUser usuarioActual =
                UserSession.getInstance().getCurrentUser();

        if (
                usuarioActual == null
                || !usuarioActual.esAdministrador()
        ) {

            AlertUtil.mostrarError(
                    "No tiene permisos para consultar farmacias."
            );

            return;
        }

        Stage stage = new Stage();
        stage.setTitle("CareStock - Farmacias registradas");
        stage.initModality(Modality.WINDOW_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        Label titulo = new Label("Farmacias registradas");
        titulo.setStyle(
                "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        Label lblBuscar = new Label("Buscar por nombre:");

        txtBuscar.setPromptText("Escriba el nombre de la farmacia");
        txtBuscar.setPrefWidth(260);

        txtBuscar.textProperty().addListener(
                (obs, valorAnterior, valorNuevo) -> cargarDatos(valorNuevo)
        );

        HBox filaBusqueda = new HBox(
                10,
                lblBuscar,
                txtBuscar
        );
        filaBusqueda.setAlignment(Pos.CENTER_LEFT);

        configurarTabla();

        lblSinDatos.setStyle(
                "-fx-text-fill: #8A8880;"
                + "-fx-font-size: 13px;"
        );

        contenedorTabla.getChildren().addAll(
                tabla,
                lblSinDatos
        );

        VBox root = new VBox(
                15,
                titulo,
                filaBusqueda,
                contenedorTabla
        );

        root.setPadding(new Insets(25));
        root.setPrefSize(600, 460);

        stage.setScene(new Scene(root));
        stage.setResizable(true);

        cargarDatos(null);

        stage.showAndWait();
    }

    private void configurarTabla() {

        TableColumn<Farmacia, String> colNombre =
                new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );
        colNombre.setPrefWidth(240);

        TableColumn<Farmacia, String> colCodigo =
                new TableColumn<>("Código");
        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );
        colCodigo.setPrefWidth(140);

        TableColumn<Farmacia, String> colEstado =
                new TableColumn<>("Estado");
        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );
        colEstado.setPrefWidth(120);

        tabla.getColumns().addAll(
                colNombre,
                colCodigo,
                colEstado
        );

        tabla.setItems(datos);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void cargarDatos(String filtro) {

        try {

            List<Farmacia> resultado =
                    farmaciaDAO.buscarPorNombre(filtro);

            datos.setAll(resultado);

            boolean sinDatos = resultado.isEmpty();

            tabla.setVisible(!sinDatos);
            lblSinDatos.setVisible(sinDatos);

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar las farmacias: "
                    + e.getMessage()
            );
        }
    }
}
