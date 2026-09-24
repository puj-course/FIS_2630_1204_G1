package com.carestock.view;

import com.carestock.dao.AccesoDAO;
import com.carestock.dao.UsuarioDAO;
import com.carestock.model.AccesoLog;
import com.carestock.model.Usuario;
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

/**
 * Pantalla de consulta del historial de inicios de sesión.
 *
 * Solo debe abrirse para usuarios con rol ADMINISTRADOR; esta clase
 * hace una segunda validación por seguridad, además de la que ya
 * hace MainDashboardFX antes de mostrar el botón.
 *
 * HU: Consultar el historial de inicios de sesión de los usuarios.
 */
public class HistorialAccesosFX {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final AccesoDAO accesoDAO = new AccesoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final TableView<AccesoLog> tabla = new TableView<>();

    private final ObservableList<AccesoLog> datos =
            FXCollections.observableArrayList();

    private final ComboBox<Usuario> comboUsuario = new ComboBox<>();

    private final Label lblSinDatos = new Label(
            "No hay accesos registrados para este usuario."
    );

    private final StackPane contenedorTabla = new StackPane();

    public void mostrar(Window owner) {

        UserSession.CurrentUser usuarioActual =
                UserSession.getInstance().getCurrentUser();

        if (
                usuarioActual == null
                || !ROL_ADMINISTRADOR.equalsIgnoreCase(
                        usuarioActual.getRol()
                )
        ) {

            AlertUtil.mostrarError(
                    "No tiene permisos para acceder al historial de accesos."
            );

            return;
        }

        Stage stage = new Stage();
        stage.setTitle("CareStock - Historial de accesos");
        stage.initModality(Modality.WINDOW_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        Label titulo = new Label("Historial de accesos");
        titulo.setStyle(
                "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        Label lblFiltro = new Label("Filtrar por usuario:");

        configurarComboUsuario();

        comboUsuario.setOnAction(
                e -> cargarDatos()
        );

        HBox filaFiltro = new HBox(
                10,
                lblFiltro,
                comboUsuario
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
        root.setPrefSize(700, 480);

        stage.setScene(new Scene(root));
        stage.setResizable(true);

        cargarDatos();

        stage.showAndWait();
    }

    private void configurarComboUsuario() {

        Usuario opcionTodos = null;

        ObservableList<Usuario> opciones =
                FXCollections.observableArrayList();

        // Opción "Todos" representada como null en la lista.
        opciones.add(opcionTodos);

        try {

            List<Usuario> usuarios = usuarioDAO.listarTodos();
            opciones.addAll(usuarios);

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar la lista de usuarios: "
                    + e.getMessage()
            );
        }

        comboUsuario.setItems(opciones);

        comboUsuario.setConverter(
                new javafx.util.StringConverter<Usuario>() {

                    @Override
                    public String toString(Usuario usuario) {
                        return usuario == null
                                ? "Todos los usuarios"
                                : usuario.getNombreCompleto();
                    }

                    @Override
                    public Usuario fromString(String s) {
                        return null;
                    }
                }
        );

        comboUsuario.getSelectionModel().selectFirst();
    }

    private void configurarTabla() {

        TableColumn<AccesoLog, String> colUsuario =
                new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(
                new PropertyValueFactory<>("nombreCompleto")
        );
        colUsuario.setPrefWidth(180);

        TableColumn<AccesoLog, String> colEmail =
                new TableColumn<>("Correo");
        colEmail.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );
        colEmail.setPrefWidth(200);

        TableColumn<AccesoLog, String> colResultado =
                new TableColumn<>("Resultado");
        colResultado.setCellValueFactory(
                new PropertyValueFactory<>("resultado")
        );
        colResultado.setPrefWidth(100);

        TableColumn<AccesoLog, String> colFecha =
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
        colFecha.setPrefWidth(160);

        tabla.getColumns().addAll(
                colUsuario,
                colEmail,
                colResultado,
                colFecha
        );

        tabla.setItems(datos);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void cargarDatos() {

        Usuario seleccionado =
                comboUsuario.getSelectionModel().getSelectedItem();

        Integer idUsuarioFiltro =
                seleccionado == null
                        ? null
                        : seleccionado.getIdUsuario();

        try {

            List<AccesoLog> resultado =
                    accesoDAO.listarHistorial(idUsuarioFiltro);

            datos.setAll(resultado);

            boolean sinDatos = resultado.isEmpty();

            tabla.setVisible(!sinDatos);
            lblSinDatos.setVisible(sinDatos);

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar el historial de accesos: "
                    + e.getMessage()
            );
        }
    }
}
