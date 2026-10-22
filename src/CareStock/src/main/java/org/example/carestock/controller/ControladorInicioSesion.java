package org.example.carestock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.carestock.AplicacionCareStock;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.service.ServicioAutenticacion;

public class ControladorInicioSesion {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRecordarme;
    @FXML private Button btnIngresar;
    @FXML private ProgressIndicator loaderCarga; // Indicador de carga animado
    @FXML private Label lblNotificacion;

    private final ServicioAutenticacion authService;

    public ControladorInicioSesion() {
        this.authService = new ServicioAutenticacion();
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
campo.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B7A6E0; -fx-border-width: 1.6; -fx-padding: 0 12;");
            } else {
               campo.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #D0D0D0; -fx-border-width: 1; -fx-padding: 0 12;");
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
            AplicacionCareStock.getInstance().mostrarDashboard();

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
                    "-fx-background-color: " + (esError ? "#FDF5F5;" : "#E9F4F4;") +
                    "-fx-text-fill: " + (esError ? "#7A2E2E;" : "#1E1E1E;") +
                    "-fx-border-color: " + (esError ? "#F0A8A8;" : "#A7D8D8;") +
                        "-fx-border-radius: 6;"
        );
        lblNotificacion.setVisible(true);
    }
}
