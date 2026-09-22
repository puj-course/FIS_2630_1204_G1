package com.carestock.view;

import com.carestock.config.AppConfig;
import com.carestock.model.Usuario;
import com.carestock.service.AuthenticationService;

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

public class LoginFX extends Application {

    private final AuthenticationService authenticationService =
            new AuthenticationService();

    private final TextField txtEmail = new TextField();
    private final PasswordField txtPassword = new PasswordField();
    private final Label lblMensaje = new Label();
    private final Button btnIngresar = new Button("Iniciar sesión");

    @Override
    public void start(Stage primaryStage) {

        Label lblLogo = new Label("CareStock");
        lblLogo.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1C313A;"
        );

        Label lblTitulo = new Label("Inicio de sesión");
        lblTitulo.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label lblDescripcion =
                new Label("Ingrese sus credenciales para continuar");

        lblDescripcion.setStyle(
                "-fx-text-fill: #607D8B;"
        );

        txtEmail.setPromptText("Correo electrónico");
        txtEmail.setPrefHeight(40);
        txtEmail.setMaxWidth(320);

        txtPassword.setPromptText("Contraseña");
        txtPassword.setPrefHeight(40);
        txtPassword.setMaxWidth(320);

        btnIngresar.setPrefHeight(40);
        btnIngresar.setMaxWidth(320);
        btnIngresar.setDefaultButton(true);

        btnIngresar.setStyle(
                "-fx-background-color: #B39DDB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;"
        );

        lblMensaje.setStyle(
                "-fx-text-fill: #C62828;" +
                "-fx-font-size: 12px;"
        );

        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(320);

        btnIngresar.setOnAction(
                event -> iniciarSesion(primaryStage)
        );

        VBox formulario = new VBox(
                12,
                lblLogo,
                lblTitulo,
                lblDescripcion,
                txtEmail,
                txtPassword,
                btnIngresar,
                lblMensaje
        );

        formulario.setAlignment(Pos.CENTER);
        formulario.setPadding(new Insets(40));
        formulario.setMaxWidth(400);

        formulario.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;"
        );

        VBox root = new VBox(formulario);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));

        root.setStyle(
                "-fx-background-color: #A3D9D2;"
        );

        Scene scene = new Scene(root, 520, 520);

        primaryStage.setTitle("CareStock - Inicio de sesión");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        txtEmail.requestFocus();
    }

    private void iniciarSesion(Stage primaryStage) {

        lblMensaje.setText("");
        btnIngresar.setDisable(true);

        String email = txtEmail.getText();
        String password = txtPassword.getText();

        try {

            Usuario usuario =
                    authenticationService.autenticar(email, password);

            AppConfig.setCurrentUserEmail(
                    usuario.getEmail()
            );

            abrirDashboard(primaryStage);

        } catch (IllegalArgumentException e) {

            lblMensaje.setText(
                    AuthenticationService.ERROR_CREDENCIALES
            );

            txtPassword.clear();
            txtPassword.requestFocus();

        } catch (SQLException e) {

            lblMensaje.setText(
                    "No fue posible iniciar sesión. Intente nuevamente."
            );

            System.err.println(
                    "Error de base de datos durante autenticación: "
                    + e.getMessage()
            );

        } catch (RuntimeException e) {

            lblMensaje.setText(
                    "No fue posible iniciar sesión. Intente nuevamente."
            );

            System.err.println(
                    "Error inesperado durante autenticación: "
                    + e.getMessage()
            );

        } finally {

            btnIngresar.setDisable(false);
        }
    }

    private void abrirDashboard(Stage primaryStage) {

        try {

            MainDashboardFX dashboard =
                    new MainDashboardFX();

            dashboard.start(primaryStage);

        } catch (RuntimeException e) {

            AppConfig.clearCurrentUser();

            lblMensaje.setText(
                    "No fue posible abrir CareStock."
            );

            System.err.println(
                    "Error al abrir dashboard: "
                    + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
