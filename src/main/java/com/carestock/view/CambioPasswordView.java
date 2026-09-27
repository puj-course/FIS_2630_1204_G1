package com.carestock.view;

import com.carestock.service.PasswordChangeService;
import com.carestock.service.PasswordChangeService.ResultadoCambio;
import com.carestock.session.UserSession;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;

/**
 * Pantalla de cambio seguro de contraseña.
 *
 * HU.58 - #308
 */
public class CambioPasswordView {

    private final PasswordChangeService passwordChangeService =
            new PasswordChangeService();

    public void mostrar(
            Window owner
    ) {

        /*
         * La función solamente puede utilizarse desde
         * una sesión autenticada.
         */
        if (
                !ProtectedNavigationGuard
                        .ensureAuthenticated(
                                owner
                        )
        ) {

            return;
        }

        Stage stage =
                new Stage();

        stage.setTitle(
                "CareStock - Cambiar contraseña"
        );

        stage.initModality(
                Modality.WINDOW_MODAL
        );

        if (owner != null) {

            stage.initOwner(
                    owner
            );
        }

        VBox card =
                new VBox(12);

        card.setPadding(
                new Insets(30)
        );

        card.setMaxWidth(
                360
        );

        card.setStyle(
                "-fx-background-color: #FFFFFF;"
                + "-fx-background-radius: 16;"
        );

        Label titulo =
                new Label(
                        "Cambiar contraseña"
                );

        titulo.setStyle(
                "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #2C2C2A;"
        );

        Label subtitulo =
                new Label(
                        "Actualiza tu contraseña de acceso"
                );

        subtitulo.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-text-fill: #8A8880;"
        );

        Label lblActual =
                new Label(
                        "Contraseña actual"
                );

        PasswordField txtActual =
                new PasswordField();

        txtActual.setPromptText(
                "Ingresa tu contraseña actual"
        );

        Label lblNueva =
                new Label(
                        "Nueva contraseña"
                );

        PasswordField txtNueva =
                new PasswordField();

        txtNueva.setPromptText(
                "Mínimo "
                + PasswordChangeService
                        .LONGITUD_MINIMA_PASSWORD
                + " caracteres"
        );

        Label lblConfirmar =
                new Label(
                        "Confirmar nueva contraseña"
                );

        PasswordField txtConfirmar =
                new PasswordField();

        txtConfirmar.setPromptText(
                "Repite la nueva contraseña"
        );

        Label lblMensaje =
                new Label();

        lblMensaje.setStyle(
                "-fx-font-size: 11px;"
        );

        lblMensaje.setWrapText(
                true
        );

        lblMensaje.setVisible(
                false
        );

        lblMensaje.setManaged(
                false
        );

        Button btnGuardar =
                new Button(
                        "Guardar cambios"
                );

        btnGuardar.setMaxWidth(
                Double.MAX_VALUE
        );

        btnGuardar.setStyle(
                "-fx-background-color: #B7A6E0;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
        );

        Button btnCancelar =
                new Button(
                        "Cancelar"
                );

        btnCancelar.setMaxWidth(
                Double.MAX_VALUE
        );

        btnCancelar.setOnAction(
                e -> stage.close()
        );

        btnGuardar.setOnAction(
                e -> intentarCambiarPassword(
                        stage,
                        lblMensaje,
                        txtActual,
                        txtNueva,
                        txtConfirmar
                )
        );

        card
                .getChildren()
                .addAll(
                        titulo,
                        subtitulo,
                        lblActual,
                        txtActual,
                        lblNueva,
                        txtNueva,
                        lblConfirmar,
                        txtConfirmar,
                        btnGuardar,
                        btnCancelar,
                        lblMensaje
                );

        VBox root =
                new VBox(card);

        root.setAlignment(
                Pos.CENTER
        );

        root.setStyle(
                "-fx-background-color: #FDFBF7;"
        );

        Scene scene =
                new Scene(
                        root,
                        460,
                        560
                );

        stage.setScene(
                scene
        );

        stage.setResizable(
                false
        );

        stage.showAndWait();
    }


    private void intentarCambiarPassword(
            Stage stage,
            Label lblMensaje,
            PasswordField txtActual,
            PasswordField txtNueva,
            PasswordField txtConfirmar
    ) {

        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (usuario == null) {

            stage.close();

            AlertUtil.mostrarSesionExpirada();

            return;
        }

        try {

            ResultadoCambio resultado =
                    passwordChangeService
                            .cambiarPassword(
                                    usuario.getId(),
                                    txtActual.getText(),
                                    txtNueva.getText(),
                                    txtConfirmar.getText()
                            );

            switch (resultado) {

                case EXITOSO -> {

                    txtActual.clear();
                    txtNueva.clear();
                    txtConfirmar.clear();

                    AlertUtil.mostrarExito(
                            "Tu contraseña fue actualizada "
                            + "correctamente."
                    );

                    stage.close();
                }

                case CAMPOS_INCOMPLETOS ->
                        mostrarMensaje(
                                lblMensaje,
                                "Todos los campos son obligatorios.",
                                true
                        );

                case NUEVAS_NO_COINCIDEN ->
                        mostrarMensaje(
                                lblMensaje,
                                "Las nuevas contraseñas no coinciden.",
                                true
                        );

                case NUEVA_DEMASIADO_CORTA ->
                        mostrarMensaje(
                                lblMensaje,
                                "La nueva contraseña debe tener al menos "
                                + PasswordChangeService
                                        .LONGITUD_MINIMA_PASSWORD
                                + " caracteres.",
                                true
                        );

                case ACTUAL_INCORRECTA -> {

                    txtActual.clear();

                    mostrarMensaje(
                            lblMensaje,
                            "La contraseña actual es incorrecta.",
                            true
                    );
                }

                case NO_ACTUALIZADO ->
                        mostrarMensaje(
                                lblMensaje,
                                "La contraseña no pudo actualizarse. "
                                + "Intenta nuevamente.",
                                true
                        );
            }

        } catch (SQLException e) {

            /*
             * No mostrar detalles internos de PostgreSQL
             * ni información de conexión al usuario.
             */
            mostrarMensaje(
                    lblMensaje,
                    "No fue posible actualizar la contraseña "
                    + "por un problema de conexión.",
                    true
            );

            System.err.println(
                    "Error actualizando contraseña: "
                    + e.getMessage()
            );
        }
    }


    private void mostrarMensaje(
            Label lbl,
            String mensaje,
            boolean esError
    ) {

        lbl.setText(
                mensaje
        );

        lbl.setStyle(
                esError
                        ? "-fx-text-fill: #7A2E2E;"
                          + "-fx-font-size: 11px;"
                        : "-fx-text-fill: #2E7A4A;"
                          + "-fx-font-size: 11px;"
        );

        lbl.setVisible(
                true
        );

        lbl.setManaged(
                true
        );
    }
}
