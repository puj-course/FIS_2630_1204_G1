package com.carestock.view;

import com.carestock.dao.RolDAO;
import com.carestock.dao.UsuarioDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Rol;
import com.carestock.security.AccessControl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.util.List;

public class CrearUsuarioFX {

    private static final int LONGITUD_MINIMA_PASSWORD = 8;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    private final TextField txtNombre = new TextField();
    private final TextField txtEmail = new TextField();
    private final PasswordField txtPassword = new PasswordField();
    private final ComboBox<Rol> comboRol = new ComboBox<>();

    private final Label lblError = new Label();

    public void mostrar(Window owner) {

        try {

            AccessControl.requireRole("ADMINISTRADOR");

        } catch (AccesoDenegadoException | IllegalStateException e) {

            AlertUtil.mostrarError(
                    "No tiene permisos para crear usuarios."
            );

            return;
        }

        Stage stage = new Stage();
        stage.setTitle("CareStock - Crear usuario");
        stage.initModality(Modality.WINDOW_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        Label titulo = new Label("Crear nuevo usuario");
        titulo.setStyle(
                "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        txtNombre.setPromptText("Nombre completo");
        txtEmail.setPromptText("Correo electrónico");
        txtPassword.setPromptText(
                "Contraseña (mínimo "
                + LONGITUD_MINIMA_PASSWORD
                + " caracteres)"
        );

        cargarRoles();
        comboRol.setPromptText("Seleccione un rol");

        lblError.setStyle(
                "-fx-text-fill: #C62828;"
                + "-fx-font-size: 12px;"
        );
        lblError.setWrapText(true);
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnCrear = new Button("Crear usuario");
        btnCrear.setStyle(
                "-fx-background-color: #A3D9D2;"
                + "-fx-font-weight: bold;"
        );

        Button btnCancelar = new Button("Cancelar");

        btnCancelar.setOnAction(
                e -> stage.close()
        );

        btnCrear.setOnAction(
                e -> intentarCrearUsuario(stage)
        );

        HBox filaBotones = new HBox(
                10,
                btnCrear,
                btnCancelar
        );
        filaBotones.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(
                12,
                titulo,
                new Label("Nombre completo:"),
                txtNombre,
                new Label("Correo electrónico:"),
                txtEmail,
                new Label("Contraseña:"),
                txtPassword,
                new Label("Rol:"),
                comboRol,
                lblError,
                filaBotones
        );

        root.setPadding(new Insets(25));
        root.setPrefWidth(380);

        stage.setScene(new Scene(root));
        stage.setResizable(false);

        stage.showAndWait();
    }

    private void cargarRoles() {

        ObservableList<Rol> opciones =
                FXCollections.observableArrayList();

        try {

            List<Rol> roles = rolDAO.listarTodos();
            opciones.addAll(roles);

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar la lista de roles: "
                    + e.getMessage()
            );
        }

        comboRol.setItems(opciones);
    }

    private void intentarCrearUsuario(Stage stage) {

        ocultarError();

        String nombre = txtNombre.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        Rol rolSeleccionado =
                comboRol.getSelectionModel().getSelectedItem();

        if (
                nombre == null || nombre.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || rolSeleccionado == null
        ) {

            mostrarError(
                    "Todos los campos son obligatorios."
            );

            return;
        }

        if (password.length() < LONGITUD_MINIMA_PASSWORD) {

            mostrarError(
                    "La contraseña debe tener al menos "
                    + LONGITUD_MINIMA_PASSWORD
                    + " caracteres."
            );

            return;
        }

        try {

            usuarioDAO.crear(
                    nombre,
                    email,
                    password,
                    rolSeleccionado.getIdRol()
            );

            AlertUtil.mostrarExito(
                    "El usuario \""
                    + nombre
                    + "\" se creó correctamente."
            );

            stage.close();

        } catch (AccesoDenegadoException e) {

            mostrarError(e.getMessage());

        } catch (SQLException e) {

            String mensaje =
                    e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")
                            ? "Ya existe un usuario registrado con "
                              + "ese correo electrónico."
                            : "No fue posible crear el usuario. "
                              + "Verifique los datos ingresados.";

            mostrarError(mensaje);
        }
    }

    private void mostrarError(String mensaje) {

        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {

        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
