package com.carestock.view;

import com.carestock.model.Usuario;
import com.carestock.service.AuthenticationService;
import com.carestock.session.UserSession;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Interfaz de inicio de sesión de CareStock.
 *
 * HU-51 / Issue #314
 */
public class LoginFX extends Application {

    private final AuthenticationService authenticationService =
            new AuthenticationService();

    private final TextField txtEmail =
            new TextField();

    private final PasswordField txtPassword =
            new PasswordField();

    private final Button btnIngresar =
            new Button("Iniciar sesión");

    private final LoginNotification notification =
            new LoginNotification();

    @Override
    public void start(Stage primaryStage) {

        /*
         * Si ya existe una sesión válida,
         * redirige directamente al Dashboard.
         */
        if (UserSession.getInstance().isLoggedIn()) {

            MainDashboardFX dashboard =
                    new MainDashboardFX();

            dashboard.start(primaryStage);

            return;
        }

        Label titulo =
                new Label("CareStock");

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2C2C2A;"
        );

        Label subtitulo =
                new Label(
                        "Inicia sesión para continuar"
                );

        subtitulo.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #8A8880;"
        );

        Label lblCorreo =
                new Label(
                        "Correo institucional"
                );

        lblCorreo.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #4A4A46;"
        );

        txtEmail.setPromptText(
                "nombre@carestock.com"
        );

        txtEmail.setPrefHeight(40);
        txtEmail.setMaxWidth(320);

        txtEmail.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #D3D1C7;"
        );

        Label lblPassword =
                new Label("Contraseña");

        lblPassword.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #4A4A46;"
        );

        txtPassword.setPromptText(
                "Contraseña"
        );

        txtPassword.setPrefHeight(40);
        txtPassword.setMaxWidth(320);

        txtPassword.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #B7A6E0;" +
                "-fx-border-width: 1.5;"
        );

        btnIngresar.setMaxWidth(
                Double.MAX_VALUE
        );

        btnIngresar.setPrefHeight(42);

        btnIngresar.setDefaultButton(true);

        btnIngresar.setStyle(
                "-fx-background-color: #B7A6E0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );

        /*
         * Enter desde el campo correo lleva
         * directamente al campo contraseña.
         */
        txtEmail.setOnAction(
                e -> txtPassword.requestFocus()
        );

        /*
         * Enter desde contraseña intenta
         * realizar el inicio de sesión.
         */
        txtPassword.setOnAction(
                e -> iniciarSesion(primaryStage)
        );

        btnIngresar.setOnAction(
                e -> iniciarSesion(primaryStage)
        );

        VBox card =
                new VBox(
                        10,
                        titulo,
                        subtitulo,
                        lblCorreo,
                        txtEmail,
                        lblPassword,
                        txtPassword,
                        btnIngresar,
                        notification.getView()
                );

        card.setPadding(
                new Insets(30)
        );

        card.setMaxWidth(340);

        card.setAlignment(
                Pos.CENTER_LEFT
        );

        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 16;" +
                "-fx-effect: dropshadow(" +
                "gaussian, rgba(0,0,0,0.10), 12, 0, 0, 4);"
        );

        VBox root =
                new VBox(card);

        root.setAlignment(
                Pos.CENTER
        );

        root.setPadding(
                new Insets(40)
        );

        root.setStyle(
                "-fx-background-color: #FDFBF7;"
        );

        Scene scene =
                new Scene(
                        root,
                        440,
                        500
                );

        primaryStage.setTitle(
                "CareStock - Iniciar sesión"
        );

        primaryStage.setScene(scene);

        primaryStage.setResizable(false);

        primaryStage.show();

        txtEmail.requestFocus();
    }

    /**
     * Valida primero el formulario en el cliente
     * y luego ejecuta la autenticación.
     */
    private void iniciarSesion(
            Stage primaryStage
    ) {

        notification.limpiar();

        String email =
                txtEmail.getText() == null
                        ? ""
                        : txtEmail.getText().trim();

        String password =
                txtPassword.getText() == null
                        ? ""
                        : txtPassword.getText();

        /*
         * Validaciones del formulario antes
         * de consultar la base de datos.
         */
        if (email.isBlank() && password.isBlank()) {

            notification.mostrarError(
                    "Ingrese su correo y contraseña."
            );

            txtEmail.requestFocus();

            return;
        }

        if (email.isBlank()) {

            notification.mostrarError(
                    "Ingrese su correo."
            );

            txtEmail.requestFocus();

            return;
        }

        if (password.isBlank()) {

            notification.mostrarError(
                    "Ingrese su contraseña."
            );

            txtPassword.requestFocus();

            return;
        }

        btnIngresar.setDisable(true);
        btnIngresar.setText("Ingresando...");

        try {

            Usuario usuario =
                    authenticationService.autenticar(
                            email,
                            password
                    );

            /*
             * Autenticación correcta:
             * se crea la sesión global.
             */
            UserSession
                    .getInstance()
                    .setCurrentUser(usuario);

            /*
             * Redirección automática
             * al Dashboard.
             */
            MainDashboardFX dashboard =
                    new MainDashboardFX();

            dashboard.start(
                    primaryStage
            );

        } catch (IllegalArgumentException e) {

            /*
             * Credenciales incorrectas,
             * usuario inexistente,
             * INACTIVO o BLOQUEADO:
             *
             * siempre se usa el mismo mensaje.
             */
            notification.mostrarError(
                    AuthenticationService.ERROR_CREDENCIALES
            );

            txtPassword.clear();

            txtPassword.requestFocus();

        } catch (SQLException e) {

            /*
             * No se exponen errores internos
             * de PostgreSQL al usuario.
             */
            notification.mostrarError(
                    "No fue posible iniciar sesión. Intente nuevamente."
            );

            System.err.println(
                    "Error de autenticación: "
                    + e.getMessage()
            );

        } catch (RuntimeException e) {

            notification.mostrarError(
                    "No fue posible iniciar sesión. Intente nuevamente."
            );

            System.err.println(
                    "Error inesperado en Login: "
                    + e.getMessage()
            );

        } finally {

            btnIngresar.setDisable(false);
            btnIngresar.setText("Iniciar sesión");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
