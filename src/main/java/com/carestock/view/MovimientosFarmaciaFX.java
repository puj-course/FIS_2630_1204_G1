package com.carestock.view;

import com.carestock.dao.MovimientoDAO;
import com.carestock.model.MovimientoInventario;
import com.carestock.session.UserSession;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovimientosFarmaciaFX {

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    private final TableView<MovimientoInventario> tabla = new TableView<>();

    private final ObservableList<MovimientoInventario> datos =
            FXCollections.observableArrayList();

    private final ComboBox<String> comboTipo = new ComboBox<>();

    private final Label lblSinDatos = new Label(
            "No hay movimientos registrados para esta farmacia."
    );

    private final StackPane contenedorTabla = new StackPane();

    public void mostrar(Window owner) {

        UserSession.CurrentUser usuarioActual =
                UserSession.getInstance().getCurrentUser();

        if (
                usuarioActual == null
                || !usuarioActual.tieneFarmaciaAsignada()
        ) {

            AlertUtil.mostrarError(
                    "Su usuario no tiene una farmacia asignada."
            );

            return;
        }

        Stage stage = new Stage();
        stage.setTitle("CareStock - Movimientos de inventario");
        stage.initModality(Modality.WINDOW_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        Label titulo = new Label("Movimientos de inventario");
        titulo.setStyle(
                "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        Label lblFiltro = new Label("Filtrar por tipo:");

        configurarComboTipo();

        comboTipo.setOnAction(
                e -> cargarDatos(usuarioActual.getIdFarmacia())
        );

        HBox filaFiltro = new HBox(
                10,
                lblFiltro,
                comboTipo
        );
        filaFiltro.setAlignment(Pos.CENTER_LEFT);

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
                filaFiltro,
                contenedorTabla
        );

        root.setPadding(new Insets(25));
        root.setPrefSize(760, 480);

        stage.setScene(new Scene(root));
        stage.setResizable(true);

        cargarDatos(usuarioActual.getIdFarmacia());

        stage.showAndWait();
    }

    private void configurarComboTipo() {

        ObservableList<String> opciones =
                FXCollections.observableArrayList(
                        "TODOS",
                        "ENTRADA",
                        "SALIDA",
                        "AJUSTE"
                );

        comboTipo.setItems(opciones);
        comboTipo.getSelectionModel().selectFirst();
    }

    private void configurarTabla() {

        TableColumn<MovimientoInventario, String> colTipo =
                new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipoMovimiento")
        );
        colTipo.setPrefWidth(90);

        TableColumn<MovimientoInventario, String> colLote =
                new TableColumn<>("Lote");
        colLote.setCellValueFactory(
                new PropertyValueFactory<>("numeroLote")
        );
        colLote.setPrefWidth(120);

        TableColumn<MovimientoInventario, String> colMedicamento =
                new TableColumn<>("Medicamento");
        colMedicamento.setCellValueFactory(
                new PropertyValueFactory<>("medicamento")
        );
        colMedicamento.setPrefWidth(180);

        TableColumn<MovimientoInventario, Number> colCantidad =
                new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidadAfectada")
        );
        colCantidad.setPrefWidth(80);

        TableColumn<MovimientoInventario, String> colUsuario =
                new TableColumn<>("Responsable");
        colUsuario.setCellValueFactory(
                new PropertyValueFactory<>("usuarioResponsable")
        );
        colUsuario.setPrefWidth(160);

        TableColumn<MovimientoInventario, String> colFecha =
                new TableColumn<>("Fecha y hora");
        colFecha.setCellValueFactory(dataFeature -> {

            java.time.LocalDateTime fecha =
                    dataFeature.getValue().getFechaHora();

            String texto = fecha == null
                    ? ""
                    : fecha.format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd HH:mm:ss"
                            )
                    );

            return new javafx.beans.property.SimpleStringProperty(
                    texto
            );
        });
        colFecha.setPrefWidth(150);

        tabla.getColumns().addAll(
                colTipo,
                colLote,
                colMedicamento,
                colCantidad,
                colUsuario,
                colFecha
        );

        tabla.setItems(datos);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void cargarDatos(int idFarmacia) {

        String tipoSeleccionado =
                comboTipo.getSelectionModel().getSelectedItem();

        String filtroTipo =
                (tipoSeleccionado == null || "TODOS".equals(tipoSeleccionado))
                        ? null
                        : tipoSeleccionado;

        try {

            List<MovimientoInventario> resultado =
                    movimientoDAO.listarPorFarmacia(
                            idFarmacia,
                            filtroTipo
                    );

            datos.setAll(resultado);

            boolean sinDatos = resultado.isEmpty();

            tabla.setVisible(!sinDatos);
            lblSinDatos.setVisible(sinDatos);

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar los movimientos: "
                    + e.getMessage()
            );
        }
    }
}
