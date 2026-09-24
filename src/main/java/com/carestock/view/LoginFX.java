package com.carestock.view;

import com.carestock.model.Usuario;
import com.carestock.service.AuthenticationService;
import com.carestock.session.UserSession;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Interfaz de inicio de sesión de CareStock.
 *
 * Sistema de gestión de inventario farmacéutico.
 *
 * HU-51 / Issue #314
 */
public class LoginFX extends Application {

    /* ===================== PALETA CLÍNICA ===================== */
    private static final String COLOR_PRIMARY       = "#0E7C7B"; // teal principal
    private static final String COLOR_PRIMARY_DARK  = "#0B615F"; // hover / acentos
    private static final String COLOR_PRIMARY_LIGHT = "#E3F3F2"; // fondos suaves
    private static final String COLOR_ACCENT         = "#2FBF9F"; // verde salud
    private static final String COLOR_BG             = "#F4FAF9"; // fondo general
    private static final String COLOR_TEXT_DARK      = "#20302F";
    private static final String COLOR_TEXT_MUTED     = "#6D8683";
    private static final String COLOR_BORDER         = "#D6E6E4";
    private static final String COLOR_ERROR          = "#C0392B";
    /* ============================================================ */

    private final AuthenticationService authenticationService =
            new AuthenticationService();

    private final TextField txtEmail =
            new TextField();

    private final PasswordField txtPassword =
            new PasswordField();

    private final CheckBox chkRecordarme =
            new CheckBox("Recordar mi correo");

    private final Button btnIngresar =
            new Button("Iniciar sesión");

    private final LoginNotification notification =
            new LoginNotification();

    private String mensajeInicial;

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

        /* ---------- Logo / identidad clínica ---------- */

        StackPane logo = crearLogoFarmacia();

        Label titulo =
                new Label("CareStock");

        titulo.setFont(Font.font("System", FontWeight.BOLD, 26));
        titulo.setStyle("-fx-text-fill: " + COLOR_TEXT_DARK + ";");

        Label subtitulo =
                new Label("Sistema de Gestión Farmacéutica");

        subtitulo.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: " + COLOR_TEXT_MUTED + ";" +
                "-fx-font-weight: bold;" +
                "-fx-letter-spacing: 0.5;"
        );

        VBox encabezado = new VBox(2, titulo, subtitulo);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        HBox marca = new HBox(14, logo, encabezado);
        marca.setAlignment(Pos.CENTER_LEFT);

        Label lema =
                new Label("Inicia sesión para gestionar tu inventario de medicamentos");

        lema.setWrapText(true);
        lema.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
        );

        /* ---------- Campo correo ---------- */

        Label lblCorreo =
                new Label("Correo institucional");

        lblCorreo.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        txtEmail.setPromptText(
                "nombre@carestock.com"
        );

        txtEmail.setPrefHeight(42);
        txtEmail.setMaxWidth(320);

        estilizarCampo(txtEmail, false);

        txtEmail.focusedProperty().addListener(
                (obs, perdioFoco, tieneFoco) ->
                        estilizarCampo(txtEmail, tieneFoco)
        );

        /* ---------- Campo contraseña ---------- */

        Label lblPassword =
                new Label("Contraseña");

        lblPassword.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        txtPassword.setPromptText(
                "Contraseña"
        );

        txtPassword.setPrefHeight(42);
        txtPassword.setMaxWidth(320);

        estilizarCampo(txtPassword, false);

        txtPassword.focusedProperty().addListener(
                (obs, perdioFoco, tieneFoco) ->
                        estilizarCampo(txtPassword, tieneFoco)
        );

        /* ---------- Recordarme ---------- */

        chkRecordarme.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
        );

        /* ---------- Botón ingresar ---------- */

        btnIngresar.setMaxWidth(
                Double.MAX_VALUE
        );

        btnIngresar.setPrefHeight(44);

        btnIngresar.setDefaultButton(true);

        btnIngresar.setStyle(estiloBotonNormal());

        btnIngresar.setOnMouseEntered(
                e -> btnIngresar.setStyle(estiloBotonHover())
        );

        btnIngresar.setOnMouseExited(
                e -> btnIngresar.setStyle(estiloBotonNormal())
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

        /* ---------- Pie de tarjeta ---------- */

        Separator separador = new Separator();
        separador.setStyle("-fx-background-color: " + COLOR_BORDER + ";");

        Label pie = new Label(
                "Acceso exclusivo para personal autorizado de farmacia."
        );
        pie.setWrapText(true);
        pie.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
        );

        VBox card =
                new VBox(
                        16,
                        marca,
                        lema,
                        new VBox(6, lblCorreo, txtEmail),
                        new VBox(6, lblPassword, txtPassword),
                        chkRecordarme,
                        btnIngresar,
                        notification.getView(),
                        separador,
                        pie
                );

        card.setPadding(
                new Insets(32)
        );

        card.setMaxWidth(360);

        card.setAlignment(
                Pos.CENTER_LEFT
        );

        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(" +
                "gaussian, rgba(14,124,123,0.12), 20, 0, 0, 8);"
        );

        /* ---------- Panel lateral de marca (opcional, refuerza identidad) ---------- */

        VBox panelLateral = crearPanelLateral();

        HBox contenido = new HBox(panelLateral, envolverCard(card));
        HBox.setHgrow(panelLateral, Priority.SOMETIMES);

        Scene scene =
                new Scene(contenido, 1200, 700);

        primaryStage.setTitle(
                "CareStock - Iniciar sesión"
        );

        primaryStage.setScene(scene);

        primaryStage.setResizable(false);

        primaryStage.show();

        if (
                mensajeInicial != null
                && !mensajeInicial.isBlank()
        ) {
            notification.mostrarExito(
                    mensajeInicial
            );
        }

        txtEmail.requestFocus();
    }

    /**
     * Envuelve la tarjeta de login centrada
     * sobre el fondo claro del formulario.
     */
    /**
     * Define un mensaje que se mostrará al abrir la pantalla
     * de inicio de sesión.
     *
     * @param mensajeInicial mensaje informativo para el usuario
     */
    public void setMensajeInicial(
            String mensajeInicial
    ) {

        this.mensajeInicial =
                mensajeInicial;
    }


    private Region envolverCard(VBox card) {

        VBox root = new VBox(card);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + COLOR_BG + ";");

        HBox.setHgrow(root, Priority.ALWAYS);

        return root;
    }

    /**
     * Panel decorativo lateral con la identidad
     * de la farmacia (color sólido + mensaje).
     */
    private VBox crearPanelLateral() {

        Label frase = new Label(
                "Control de inventario,\nlotes y vencimientos\nen un solo lugar."
        );

        frase.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );
        frase.setWrapText(true);

        Label detalle = new Label(
                "Trazabilidad de medicamentos para tu farmacia."
        );
        detalle.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: rgba(255,255,255,0.85);"
        );
        detalle.setWrapText(true);

        VBox textos = new VBox(10, frase, detalle);
        textos.setMaxWidth(280);

        VBox panel = new VBox(textos);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(60));
        panel.setPrefWidth(420);
        panel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                COLOR_PRIMARY + ", " + COLOR_PRIMARY_DARK + ");"
        );

        return panel;
    }

    /**
     * Ícono simple de cruz médica dentro de un
     * círculo, construido con formas nativas de
     * JavaFX (sin dependencias de imágenes externas).
     */
    private StackPane crearLogoFarmacia() {

        Circle fondo = new Circle(22);
        fondo.setFill(Color.web(COLOR_PRIMARY_LIGHT));

        Rectangle barraVertical = new Rectangle(6, 22);
        barraVertical.setArcWidth(3);
        barraVertical.setArcHeight(3);
        barraVertical.setFill(Color.web(COLOR_PRIMARY));

        Rectangle barraHorizontal = new Rectangle(22, 6);
        barraHorizontal.setArcWidth(3);
        barraHorizontal.setArcHeight(3);
        barraHorizontal.setFill(Color.web(COLOR_PRIMARY));

        StackPane logo = new StackPane(
                fondo, barraVertical, barraHorizontal
        );

        logo.setPrefSize(44, 44);

        return logo;
    }

    private void estilizarCampo(TextField campo, boolean enFoco) {

        String colorBorde = enFoco ? COLOR_PRIMARY : COLOR_BORDER;
        String grosor = enFoco ? "1.6" : "1";

        campo.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: " + colorBorde + ";" +
                "-fx-border-width: " + grosor + ";" +
                "-fx-padding: 0 12 0 12;"
        );
    }

    private String estiloBotonNormal() {
        return
                "-fx-background-color: " + COLOR_PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";
    }

    private String estiloBotonHover() {
        return
                "-fx-background-color: " + COLOR_PRIMARY_DARK + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";
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
