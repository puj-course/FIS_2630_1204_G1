package org.example.carestock.controller;

import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import org.example.carestock.CareStockApp;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.service.AuthenticationService;
import org.example.carestock.service.ValidacionFormularioLogin;

import java.util.prefs.Preferences;

public class LoginController {

    private static final String CLAVE_CORREO = "correo_recordado";

    private static final PseudoClass INVALIDO =
            PseudoClass.getPseudoClass("invalido");

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkRecordarme;
    @FXML private Button btnIngresar;
    @FXML private Button btnMostrarPassword;
    @FXML private ProgressIndicator loaderCarga;
    @FXML private Label lblNotificacion;
    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;


    @FunctionalInterface
    interface InicioDeSesion {
        void iniciar(Usuario usuario) throws Exception;
    }

    private final AuthenticationService authService;
    private final java.util.function.Supplier<Preferences> proveedorPreferencias;
    private final InicioDeSesion inicioDeSesion;
    private final Runnable abrirDashboard;

    public LoginController() {
        this(
                new AuthenticationService(),
                () -> Preferences.userNodeForPackage(LoginController.class),
                SesionUsuario::iniciar,
                () -> CareStockApp.getInstance().mostrarDashboard()
        );
    }

    LoginController(
            AuthenticationService autenticacion,
            java.util.function.Supplier<Preferences> preferencias,
            InicioDeSesion inicioDeSesion,
            Runnable abrirDashboard) {

        this.authService = autenticacion;
        this.proveedorPreferencias = preferencias;
        this.inicioDeSesion = inicioDeSesion;
        this.abrirDashboard = abrirDashboard;
    }


    private boolean correoTocado;
    private boolean contrasenaTocada;

    @FXML
    public void initialize() {
        txtPasswordVisible.textProperty()
                .bindBidirectional(txtPassword.textProperty());

        mostrarContrasena(false);
        cambiarEstadoCarga(false);
        recuperarCorreo();

        txtEmail.focusedProperty().addListener(
                (obs, antes, ahora) -> {
                    if (!ahora) {
                        correoTocado = true;
                        validarCorreo();
                    }
                });

        txtEmail.textProperty().addListener(
                (obs, antes, ahora) -> {
                    ocultarMensaje();
                    if (correoTocado) validarCorreo();
                });

        txtPassword.focusedProperty().addListener(
                (obs, antes, ahora) -> {
                    if (!ahora) {
                        contrasenaTocada = true;
                        validarContrasena();
                    }
                });

        txtPasswordVisible.focusedProperty().addListener(
                (obs, antes, ahora) -> {
                    if (!ahora) {
                        contrasenaTocada = true;
                        validarContrasena();
                    }
                });

        txtPassword.textProperty().addListener(
                (obs, antes, ahora) -> {
                    ocultarMensaje();
                    if (contrasenaTocada) validarContrasena();
                });
    }

    @FXML
    private void onTogglePassword() {
        mostrarContrasena(!txtPasswordVisible.isVisible());

        if (txtPasswordVisible.isVisible()) {
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(
                    txtPasswordVisible.getLength());
        } else {
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getLength());
        }
    }

    private void mostrarContrasena(boolean visible) {
        txtPasswordVisible.setVisible(visible);
        txtPasswordVisible.setManaged(visible);

        txtPassword.setVisible(!visible);
        txtPassword.setManaged(!visible);

        btnMostrarPassword.setText(visible ? "◉" : "👁");

        btnMostrarPassword.setTooltip(new Tooltip(
                visible ? "Ocultar contraseña"
                        : "Mostrar contraseña"));

        btnMostrarPassword.setAccessibleText(
                visible ? "Ocultar contraseña"
                        : "Mostrar contraseña");
    }

    private void validarCorreo() {
        presentarError(
                lblEmailError,
                txtEmail,
                ValidacionFormularioLogin.errorCorreo(
                        txtEmail.getText()));
    }

    private void validarContrasena() {
        String error =
                ValidacionFormularioLogin.errorContrasena(
                        txtPassword.getText());

        presentarError(lblPasswordError, txtPassword, error);

        txtPasswordVisible.pseudoClassStateChanged(
                INVALIDO, !error.isEmpty());
    }

    private void presentarError(
            Label etiqueta,
            TextField campo,
            String error) {

        boolean hayError = !error.isEmpty();

        etiqueta.setText(error);
        etiqueta.setVisible(hayError);
        etiqueta.setManaged(hayError);

        campo.pseudoClassStateChanged(INVALIDO, hayError);
    }

    @FXML
    private void onEmailAction() {
        (txtPasswordVisible.isVisible()
                ? txtPasswordVisible : txtPassword).requestFocus();
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
        ocultarMensaje();

        correoTocado = true;
        contrasenaTocada = true;

        validarCorreo();
        validarContrasena();

        if (lblEmailError.isVisible()
                || lblPasswordError.isVisible()) {

            if (lblEmailError.isVisible()) {
                txtEmail.requestFocus();
            } else {
                (txtPasswordVisible.isVisible()
                        ? txtPasswordVisible : txtPassword)
                        .requestFocus();
            }
            return;
        }

        final String correo = txtEmail.getText().trim();
        final String contrasena = txtPassword.getText();

        cambiarEstadoCarga(true);

        Task<Usuario> tarea = new Task<>() {
            @Override
            protected Usuario call() throws Exception {
                return authService.autenticar(correo, contrasena);
            }
        };

        tarea.setOnSucceeded(evento -> {
            cambiarEstadoCarga(false);

            try {
                guardarCorreo(correo);
                inicioDeSesion.iniciar(tarea.getValue());
                abrirDashboard.run();
            } catch (Exception ex) {
                mostrarMensaje(
                        "No fue posible abrir la sesión. Inténtalo nuevamente.");
            }
        });

        tarea.setOnFailed(evento -> {
            cambiarEstadoCarga(false);

            Throwable causa = tarea.getException();

            txtPassword.clear();
            contrasenaTocada = false;

            presentarError(lblPasswordError, txtPassword, "");
            txtPasswordVisible.pseudoClassStateChanged(
                    INVALIDO, false);

            mostrarContrasena(false);
            txtPassword.requestFocus();

            // Mostrar despues de limpiar la contraseña:
            // el listener del campo oculta mensajes anteriores.
            mostrarMensaje(causa instanceof ReglaNegocioException
                    ? causa.getMessage()
                    : "No fue posible verificar las credenciales. "
                      + "Inténtalo nuevamente.");
        });

        Thread hilo = new Thread(tarea, "carestock-login");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void cambiarEstadoCarga(boolean cargando) {
        btnIngresar.setDisable(cargando);
        btnIngresar.setText(
                cargando ? "Validando..." : "Iniciar sesión");

        txtEmail.setDisable(cargando);
        txtPassword.setDisable(cargando);
        txtPasswordVisible.setDisable(cargando);
        btnMostrarPassword.setDisable(cargando);
        chkRecordarme.setDisable(cargando);

        loaderCarga.setVisible(cargando);
        loaderCarga.setManaged(cargando);
    }

    private void mostrarMensaje(String mensaje) {
        lblNotificacion.setText(mensaje);
        lblNotificacion.setVisible(true);
        lblNotificacion.setManaged(true);
    }

    private void ocultarMensaje() {
        lblNotificacion.setVisible(false);
        lblNotificacion.setManaged(false);
    }

    private void recuperarCorreo() {
        try {
            String correo = proveedorPreferencias.get()
                    .get(CLAVE_CORREO, "");

            if (!correo.isBlank()) {
                txtEmail.setText(correo);
                chkRecordarme.setSelected(true);
            }
        } catch (SecurityException ignored) {
            // La preferencia no es obligatoria para ingresar.
        }
    }

    private void guardarCorreo(String correo) {
        try {
            Preferences preferencias =
                    proveedorPreferencias.get();

            if (chkRecordarme.isSelected()) {
                preferencias.put(CLAVE_CORREO, correo);
            } else {
                preferencias.remove(CLAVE_CORREO);
            }
        } catch (SecurityException ignored) {
            // No bloquear la autenticación por esta preferencia.
        }
    }
}
