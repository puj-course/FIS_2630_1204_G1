package org.example.carestock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.carestock.CareStockApp;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.service.AuthenticationService;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRecordarme;
    @FXML private Button btnIngresar;
    @FXML private ProgressIndicator loaderCarga; // Indicador de carga animado
    @FXML private Label lblNotificacion;

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    public void initialize() {
        configurarFoco(txtEmail);
        configurarFoco(txtPassword);
        if (loaderCarga != null) {
            loaderCarga.setVisible(false); // Oculto por defecto
        }
    }

    private void configurarFoco(TextField campo) {
        campo.focusedProperty().addListener((obs, oldVal, enFoco) -> {
            if (enFoco) {
                campo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #0E7C7B; -fx-border-width: 1.6; -fx-padding: 0 12;");
            } else {
                campo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #D6E6E4; -fx-border-width: 1; -fx-padding: 0 12;");
            }
        });
    }

    @FXML
    private void onEmailAction() {
        txtPassword.requestFocus();
    }

    @FXML
    private void onPasswordAction() {
        ejecutarLogin();
    }

    @FXML
    private void onIngresar() {
        ejecutarLogin();
    }

    private void ejecutarLogin() {
        lblNotificacion.setVisible(false);

        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();

        // Desactivar controles y mostrar animación de carga
        cambiarEstadoCarga(true);

        // Simulamos un leve respiro visual o ejecutamos directo
        try {
            Usuario usuarioValido = authService.autenticar(email, password);

            mostrarMensaje("Acceso concedido. Bienvenido/a " + usuarioValido.getNombre() + "...", false);

            SesionUsuario.iniciar(usuarioValido);

            // Transición suave al Dashboard
            CareStockApp.getInstance().mostrarDashboard();

        } catch (ReglaNegocioException e) {
            mostrarMensaje(e.getMessage(), true);
            txtPassword.clear();
            txtPassword.requestFocus();
            cambiarEstadoCarga(false);
        } catch (Exception e) {
            mostrarMensaje("Error al conectar con la base de datos: " + e.getMessage(), true);
            e.printStackTrace();
            cambiarEstadoCarga(false);
        }
    }

    private void cambiarEstadoCarga(boolean cargando) {
        btnIngresar.setDisable(cargando);
        txtEmail.setDisable(cargando);
        txtPassword.setDisable(cargando);
        chkRecordarme.setDisable(cargando);

        if (loaderCarga != null) {
            loaderCarga.setVisible(cargando);
        }

        btnIngresar.setText(cargando ? "Validando..." : "Iniciar sesión");
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblNotificacion.setText(mensaje);
        lblNotificacion.setStyle(
                "-fx-padding: 8 12;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-color: " + (esError ? "#FDEDEC;" : "#EAFAF1;") +
                        "-fx-text-fill: " + (esError ? "#C0392B;" : "#27AE60;") +
                        "-fx-border-color: " + (esError ? "#F5B7B1;" : "#A9DFBF;") +
                        "-fx-border-radius: 6;"
        );
        lblNotificacion.setVisible(true);
    }
}