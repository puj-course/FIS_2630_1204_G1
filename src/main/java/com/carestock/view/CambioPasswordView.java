package com.carestock.view;

import com.carestock.dao.UsuarioDAO;
import com.carestock.session.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Pantalla de cambio de contraseña. HU.58 - Tarea #338.
 * Permite al usuario autenticado cambiar su contraseña desde el perfil.
 */
public class CambioPasswordView {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void mostrar() {
        Stage stage = new Stage();
        stage.setTitle("CareStock - Cambiar contraseña");

        VBox card = new VBox(12);
        card.setPadding(new Insets(30));
        card.setMaxWidth(360);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16;");

        Label titulo = new Label("Cambiar contraseña");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2C2C2A;");

        Label subtitulo = new Label("Actualiza tu contraseña de acceso");
        subtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #8A8880;");

        Label lblActual = new Label("Contraseña actual");
        PasswordField txtActual = new PasswordField();
        txtActual.setPromptText("Ingresa tu contraseña actual");
        txtActual.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #D3D1C7;");

        Label lblNueva = new Label("Nueva contraseña");
        PasswordField txtNueva = new PasswordField();
        txtNueva.setPromptText("Ingresa la nueva contraseña");
        txtNueva.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B7A6E0;");

        Label lblConfirmar = new Label("Confirmar nueva contraseña");
        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Repite la nueva contraseña");
        txtConfirmar.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B7A6E0;");

        Label lblMensaje = new Label();
        lblMensaje.setStyle("-fx-font-size: 11px;");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);

        Button btnGuardar = new Button("Guardar cambios");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle("-fx-background-color: #B7A6E0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #2C2C2A; -fx-background-radius: 8;");

        btnCancelar.setOnAction(e -> stage.close());

        btnGuardar.setOnAction(e -> {
            String actual = txtActual.getText();
            String nueva = txtNueva.getText();
            String confirmar = txtConfirmar.getText();

            if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                mostrarMensaje(lblMensaje, "Todos los campos son obligatorios", true);
                return;
            }

            if (!nueva.equals(confirmar)) {
                mostrarMensaje(lblMensaje, "Las contraseñas nuevas no coinciden", true);
                return;
            }

            try {
                int idUsuario = UserSession.getInstance().getCurrentUser().getId();
                boolean exito = usuarioDAO.cambiarPassword(idUsuario, actual, nueva);

                if (exito) {
                    mostrarMensaje(lblMensaje, "Contraseña actualizada correctamente", false);
                    Alert ok = new Alert(Alert.AlertType.INFORMATION, "Tu contraseña ha sido actualizada.");
                    ok.showAndWait();
                    stage.close();
                } else {
                    mostrarMensaje(lblMensaje, "La contraseña actual es incorrecta", true);
                }
            } catch (SQLException ex) {
                mostrarMensaje(lblMensaje, "Error de conexión: " + ex.getMessage(), true);
            }
        });

        card.getChildren().addAll(
                titulo, subtitulo,
                lblActual, txtActual,
                lblNueva, txtNueva,
                lblConfirmar, txtConfirmar,
                btnGuardar, btnCancelar,
                lblMensaje
        );

        VBox root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #FDFBF7;");

        Scene scene = new Scene(root, 460, 560);
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarMensaje(Label lbl, String mensaje, boolean esError) {
        lbl.setText(mensaje);
        lbl.setStyle(esError
                ? "-fx-text-fill: #7A2E2E; -fx-font-size: 11px;"
                : "-fx-text-fill: #2E7A4A; -fx-font-size: 11px;");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}