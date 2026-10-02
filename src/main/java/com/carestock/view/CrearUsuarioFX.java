package com.carestock.view;

import com.carestock.dao.FarmaciaDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Farmacia;
import com.carestock.security.AccessControl;
import com.carestock.service.FarmaciaService;
import com.carestock.service.UsuarioService;
import com.carestock.session.UserSession;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

/**
 * Diálogo para crear usuarios (ADMINISTRADOR / FARMACEUTICO).
 *
 * Comparte la identidad visual de LoginFX: tarjeta blanca con
 * sombra teal, logo de cruz médica, campos con borde de foco y
 * mensajes mediante {@link LoginNotification}.
 */
public class CrearUsuarioFX {

    /* ===================== PALETA CLÍNICA ===================== */
    private static final String COLOR_PRIMARY       = "#0E7C7B";
    private static final String COLOR_PRIMARY_DARK  = "#0B615F";
    private static final String COLOR_PRIMARY_LIGHT = "#E3F3F2";
    private static final String COLOR_BG            = "#F4FAF9";
    private static final String COLOR_TEXT_DARK     = "#20302F";
    private static final String COLOR_TEXT_MUTED    = "#6D8683";
    private static final String COLOR_BORDER        = "#D6E6E4";
    private static final String COLOR_ERROR         = "#C0392B";
    /* ============================================================ */

    private static final int LONGITUD_MINIMA_PASSWORD = 8;
    private static final double ANCHO_VENTANA = 470;
    private static final double ALTO_MAXIMO   = 700;

    private final UsuarioService usuarioService =
            new UsuarioService();

    private final FarmaciaService farmaciaService =
            new FarmaciaService();

    private final FarmaciaDAO farmaciaDAO =
            new FarmaciaDAO();

    private final TextField txtNombre =
            new TextField();

    private final TextField txtEmail =
            new TextField();

    private final PasswordField txtPassword =
            new PasswordField();

    private final ComboBox<Farmacia> comboFarmacia =
            new ComboBox<>();

    private final TextField txtCodigoFarmacia =
            new TextField();

    private final TextField txtNombreFarmacia =
            new TextField();

    private final TitledPane panelCrearFarmacia =
            new TitledPane();

    private final Label lblRolDestino =
            new Label();

    private final Label lblFarmaciaFija =
            new Label();

    private final Label lblContadorFarmacias =
            new Label();

    private final LoginNotification notification =
            new LoginNotification();

    private UserSession.CurrentUser actor;

    public void mostrar(
            Window owner
    ) {

        try {

            AccessControl.requireRole(
                    "SUPER_ADMIN",
                    "ADMINISTRADOR"
            );

            actor =
                    UserSession
                            .getInstance()
                            .getCurrentUser();

        } catch (
                AccesoDenegadoException
                | IllegalStateException e
        ) {

            AlertUtil.mostrarError(
                    "No tiene permisos para crear usuarios."
            );

            return;
        }

        Stage stage =
                new Stage();

        stage.setTitle(
                "CareStock - Crear usuario"
        );

        stage.initModality(
                Modality.WINDOW_MODAL
        );

        if (owner != null) {
            stage.initOwner(owner);
        }

        /* ---------- Encabezado / identidad clínica ---------- */

        Label titulo =
                new Label("Crear nuevo usuario");

        titulo.setFont(
                Font.font("System", FontWeight.BOLD, 22)
        );

        titulo.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        Label subtitulo =
                new Label("CareStock · Sistema de Gestión Farmacéutica");

        subtitulo.setStyle(
                "-fx-font-size: 11px;"
                + "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
                + "-fx-font-weight: bold;"
        );

        VBox encabezado =
                new VBox(2, titulo, subtitulo);

        encabezado.setAlignment(Pos.CENTER_LEFT);

        HBox marca =
                new HBox(14, crearLogoFarmacia(), encabezado);

        marca.setAlignment(Pos.CENTER_LEFT);

        Label lema =
                new Label(
                        "Registre al personal autorizado "
                        + "para gestionar el inventario."
                );

        lema.setWrapText(true);

        lema.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
        );

        /* ---------- Campos ---------- */

        configurarCampo(
                txtNombre,
                "Nombre completo"
        );

        configurarCampo(
                txtEmail,
                "nombre@carestock.com"
        );

        configurarCampo(
                txtPassword,
                "Mínimo "
                + LONGITUD_MINIMA_PASSWORD
                + " caracteres"
        );

        configurarComboFarmacia();

        configurarContexto();

        configurarPanelCrearFarmacia();

        /* ---------- Notificaciones ---------- */

        notification.getView().setMaxWidth(Double.MAX_VALUE);

        /* ---------- Botones ---------- */

        Button btnCrear =
                new Button("Crear usuario");

        btnCrear.setPrefHeight(44);
        btnCrear.setMaxWidth(Double.MAX_VALUE);
        btnCrear.setDefaultButton(true);
        btnCrear.setStyle(estiloBotonPrimario());

        btnCrear.setOnMouseEntered(
                e -> btnCrear.setStyle(estiloBotonPrimarioHover())
        );

        btnCrear.setOnMouseExited(
                e -> btnCrear.setStyle(estiloBotonPrimario())
        );

        btnCrear.setOnAction(
                e -> intentarCrearUsuario(stage)
        );

        Button btnCancelar =
                new Button("Cancelar");

        btnCancelar.setPrefHeight(44);
        btnCancelar.setStyle(estiloBotonSecundario());

        btnCancelar.setOnMouseEntered(
                e -> btnCancelar.setStyle(estiloBotonSecundarioHover())
        );

        btnCancelar.setOnMouseExited(
                e -> btnCancelar.setStyle(estiloBotonSecundario())
        );

        btnCancelar.setCancelButton(true);

        btnCancelar.setOnAction(
                e -> stage.close()
        );

        HBox filaBotones =
                new HBox(10, btnCrear, btnCancelar);

        HBox.setHgrow(
                btnCrear,
                javafx.scene.layout.Priority.ALWAYS
        );

        filaBotones.setAlignment(Pos.CENTER);

        /* ---------- Pie ---------- */

        Separator separador =
                new Separator();

        Label pie =
                new Label(
                        "Los usuarios creados quedan asociados "
                        + "a la farmacia indicada."
                );

        pie.setWrapText(true);

        pie.setStyle(
                "-fx-font-size: 10px;"
                + "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
        );

        /* ---------- Tarjeta ---------- */

        VBox card =
                new VBox(
                        14,
                        marca,
                        lema,
                        grupo("Nombre completo", txtNombre),
                        grupo("Correo electrónico", txtEmail),
                        grupo("Contraseña", txtPassword),
                        grupo("Rol a crear", lblRolDestino),
                        crearGrupoFarmacia(),
                        panelCrearFarmacia,
                        notification.getView(),
                        filaBotones,
                        separador,
                        pie
                );

        card.setPadding(
                new Insets(28)
        );

        card.setAlignment(
                Pos.CENTER_LEFT
        );

        card.setStyle(
                "-fx-background-color: #FFFFFF;"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow("
                + "gaussian, rgba(14,124,123,0.12), 20, 0, 0, 8);"
        );

        VBox root =
                new VBox(card);

        root.setPadding(
                new Insets(24)
        );

        root.setPrefWidth(
                ANCHO_VENTANA
        );

        root.setStyle(
                "-fx-background-color: " + COLOR_BG + ";"
        );

        ScrollPane scroll =
                new ScrollPane(root);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setStyle(
                "-fx-background: " + COLOR_BG + ";"
                + "-fx-background-color: " + COLOR_BG + ";"
        );

        stage.setScene(
                new Scene(scroll)
        );

        ajustarAlto(
                stage,
                scroll,
                root
        );

        panelCrearFarmacia
                .expandedProperty()
                .addListener(
                        (obs, antes, ahora) ->
                                Platform.runLater(
                                        () -> ajustarAlto(
                                                stage,
                                                scroll,
                                                root
                                        )
                                )
                );

        stage.setResizable(false);

        stage.showAndWait();
    }

    /* ===================== COMPONENTES VISUALES ===================== */

    private StackPane crearLogoFarmacia() {

        Circle fondo =
                new Circle(22);

        fondo.setFill(
                Color.web(COLOR_PRIMARY_LIGHT)
        );

        Rectangle barraVertical =
                new Rectangle(6, 22);

        barraVertical.setArcWidth(3);
        barraVertical.setArcHeight(3);
        barraVertical.setFill(
                Color.web(COLOR_PRIMARY)
        );

        Rectangle barraHorizontal =
                new Rectangle(22, 6);

        barraHorizontal.setArcWidth(3);
        barraHorizontal.setArcHeight(3);
        barraHorizontal.setFill(
                Color.web(COLOR_PRIMARY)
        );

        StackPane logo =
                new StackPane(
                        fondo,
                        barraVertical,
                        barraHorizontal
                );

        logo.setPrefSize(
                44,
                44
        );

        return logo;
    }

    private VBox grupo(
            String etiqueta,
            javafx.scene.Node control
    ) {

        return new VBox(
                6,
                crearEtiqueta(etiqueta),
                control
        );
    }

    private Label crearEtiqueta(
            String texto
    ) {

        Label label =
                new Label(texto);

        label.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        return label;
    }

    private void configurarCampo(
            TextInputControl campo,
            String prompt
    ) {

        campo.setPromptText(prompt);

        campo.setPrefHeight(42);

        estilizarCampo(
                campo,
                false
        );

        campo.focusedProperty().addListener(
                (obs, perdioFoco, tieneFoco) ->
                        estilizarCampo(
                                campo,
                                tieneFoco
                        )
        );
    }

    private void estilizarCampo(
            TextInputControl campo,
            boolean enFoco
    ) {

        String colorBorde =
                enFoco
                        ? COLOR_PRIMARY
                        : COLOR_BORDER;

        String grosor =
                enFoco
                        ? "1.6"
                        : "1";

        campo.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + colorBorde + ";"
                + "-fx-border-width: " + grosor + ";"
                + "-fx-padding: 0 12 0 12;"
        );
    }

    private void estilizarCombo(
            boolean enFoco
    ) {

        String colorBorde =
                enFoco
                        ? COLOR_PRIMARY
                        : COLOR_BORDER;

        String grosor =
                enFoco
                        ? "1.6"
                        : "1";

        comboFarmacia.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + colorBorde + ";"
                + "-fx-border-width: " + grosor + ";"
                + "-fx-pref-height: 42;"
        );
    }

    private void estilizarChip(
            Label label,
            String colorTexto
    ) {

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setWrapText(true);

        label.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 10 12 10 12;"
                + "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + colorTexto + ";"
        );
    }

    private VBox crearGrupoFarmacia() {

        lblContadorFarmacias.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
                + "-fx-font-size: 11px;"
        );

        lblContadorFarmacias.setVisible(
                actor.esSuperAdmin()
        );

        lblContadorFarmacias.setManaged(
                actor.esSuperAdmin()
        );

        return new VBox(
                6,
                crearEtiqueta("Farmacia"),
                comboFarmacia,
                lblContadorFarmacias,
                lblFarmaciaFija
        );
    }

    private String estiloBotonPrimario() {

        return
                "-fx-background-color: " + COLOR_PRIMARY + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;";
    }

    private String estiloBotonPrimarioHover() {

        return
                "-fx-background-color: " + COLOR_PRIMARY_DARK + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;";
    }

    private String estiloBotonSecundario() {

        return
                "-fx-background-color: " + COLOR_PRIMARY_LIGHT + ";"
                + "-fx-text-fill: " + COLOR_PRIMARY_DARK + ";"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;";
    }

    private String estiloBotonSecundarioHover() {

        return
                "-fx-background-color: " + COLOR_BORDER + ";"
                + "-fx-text-fill: " + COLOR_PRIMARY_DARK + ";"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;";
    }

    private void ajustarAlto(
            Stage stage,
            ScrollPane scroll,
            VBox root
    ) {

        double alto =
                Math.min(
                        root.prefHeight(
                                ANCHO_VENTANA
                        ),
                        ALTO_MAXIMO
                );

        scroll.setPrefViewportWidth(
                ANCHO_VENTANA
        );

        scroll.setPrefViewportHeight(
                alto
        );

        stage.sizeToScene();
    }

    /* ===================== FARMACIAS ===================== */

    /**
     * Devuelve la representación visible de una farmacia.
     *
     * Ejemplo:
     * ID 3 · Farmacia Norte · SEDE-NORTE
     */
    private String describirFarmacia(
            Farmacia farmacia
    ) {

        String texto =
                "ID "
                + farmacia.getIdFarmacia()
                + " · "
                + farmacia.getNombre();

        if (
                farmacia.getCodigo() != null
                && !farmacia.getCodigo().isBlank()
        ) {

            texto +=
                    " · "
                    + farmacia.getCodigo();
        }

        return texto;
    }

    /**
     * Configura el ComboBox para mostrar:
     *
     * ID · Nombre · Código
     */
    private void configurarComboFarmacia() {

        comboFarmacia.setMaxWidth(
                Double.MAX_VALUE
        );

        estilizarCombo(false);

        comboFarmacia
                .focusedProperty()
                .addListener(
                        (
                                obs,
                                perdioFoco,
                                tieneFoco
                        ) ->
                                estilizarCombo(
                                        tieneFoco
                                )
                );

        comboFarmacia.setConverter(
                new StringConverter<Farmacia>() {

                    @Override
                    public String toString(
                            Farmacia farmacia
                    ) {

                        if (farmacia == null) {
                            return "";
                        }

                        return describirFarmacia(
                                farmacia
                        );
                    }

                    @Override
                    public Farmacia fromString(
                            String nombre
                    ) {

                        return null;
                    }
                }
        );
    }

    private void configurarPanelCrearFarmacia() {

        panelCrearFarmacia.setText(
                "+ Crear nueva farmacia"
        );

        panelCrearFarmacia.setExpanded(
                false
        );

        panelCrearFarmacia.setVisible(
                actor.esSuperAdmin()
        );

        panelCrearFarmacia.setManaged(
                actor.esSuperAdmin()
        );

        panelCrearFarmacia.setStyle(
                "-fx-text-fill: " + COLOR_PRIMARY_DARK + ";"
                + "-fx-font-weight: bold;"
        );

        configurarCampo(
                txtCodigoFarmacia,
                "Ej. SEDE-NORTE"
        );

        configurarCampo(
                txtNombreFarmacia,
                "Ej. Farmacia Norte"
        );

        Button btnCrearFarmacia =
                new Button(
                        "Crear farmacia"
                );

        btnCrearFarmacia.setPrefHeight(40);

        btnCrearFarmacia.setMaxWidth(
                Double.MAX_VALUE
        );

        btnCrearFarmacia.setStyle(
                estiloBotonPrimario()
        );

        btnCrearFarmacia.setOnMouseEntered(
                e ->
                        btnCrearFarmacia.setStyle(
                                estiloBotonPrimarioHover()
                        )
        );

        btnCrearFarmacia.setOnMouseExited(
                e ->
                        btnCrearFarmacia.setStyle(
                                estiloBotonPrimario()
                        )
        );

        btnCrearFarmacia.setOnAction(
                e -> crearNuevaFarmacia()
        );

        btnCrearFarmacia.setDisable(true);

        txtCodigoFarmacia
                .textProperty()
                .addListener(
                        (
                                obs,
                                old,
                                newVal
                        ) ->
                                actualizarEstadoBotonFarmacia(
                                        btnCrearFarmacia
                                )
                );

        txtNombreFarmacia
                .textProperty()
                .addListener(
                        (
                                obs,
                                old,
                                newVal
                        ) ->
                                actualizarEstadoBotonFarmacia(
                                        btnCrearFarmacia
                                )
                );

        Label lblNota =
                new Label(
                        "* Campos obligatorios"
                );

        lblNota.setStyle(
                "-fx-font-size: 10px;"
                + "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
        );

        VBox formularioFarmacia =
                new VBox(
                        10,
                        grupo(
                                "Código *",
                                txtCodigoFarmacia
                        ),
                        grupo(
                                "Nombre *",
                                txtNombreFarmacia
                        ),
                        btnCrearFarmacia,
                        lblNota
                );

        formularioFarmacia.setPadding(
                new Insets(12)
        );

        formularioFarmacia.setStyle(
                "-fx-background-color: " + COLOR_BG + ";"
                + "-fx-background-radius: 8;"
        );

        panelCrearFarmacia.setContent(
                formularioFarmacia
        );
    }

    private void actualizarEstadoBotonFarmacia(
            Button btnCrearFarmacia
    ) {

        String codigo =
                txtCodigoFarmacia.getText();

        String nombre =
                txtNombreFarmacia.getText();

        boolean camposCompletos =
                codigo != null
                && !codigo.isBlank()
                && nombre != null
                && !nombre.isBlank();

        btnCrearFarmacia.setDisable(
                !camposCompletos
        );
    }

    private void crearNuevaFarmacia() {

        notification.limpiar();

        try {

            Farmacia nuevaFarmacia =
                    farmaciaService
                            .crearFarmacia(
                                    txtCodigoFarmacia.getText(),
                                    txtNombreFarmacia.getText()
                            );

            List<Farmacia> activas =
                    farmaciaService
                            .listarActivas();

            comboFarmacia.setItems(
                    FXCollections
                            .observableArrayList(
                                    activas
                            )
            );

            actualizarContador(
                    activas.size()
            );

            /*
             * La nueva lista contiene instancias nuevas.
             * Se selecciona la farmacia comparando el ID.
             */
            activas.stream()
                    .filter(
                            f ->
                                    f.getIdFarmacia()
                                            == nuevaFarmacia
                                                    .getIdFarmacia()
                    )
                    .findFirst()
                    .ifPresent(
                            f ->
                                    comboFarmacia
                                            .getSelectionModel()
                                            .select(f)
                    );

            txtCodigoFarmacia.clear();

            txtNombreFarmacia.clear();

            panelCrearFarmacia.setExpanded(
                    false
            );

            notification.mostrarExito(
                    "La farmacia \""
                    + nuevaFarmacia.getNombre()
                    + "\" (ID "
                    + nuevaFarmacia.getIdFarmacia()
                    + ") fue creada correctamente."
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            notification.mostrarError(
                    e.getMessage()
            );

        } catch (SQLException e) {

            String mensaje =
                    e.getMessage() == null
                            ? ""
                            : e.getMessage();

            if (
                    mensaje
                            .toLowerCase()
                            .contains(
                                    "ya existe"
                            )
            ) {

                notification.mostrarError(
                        "Ya existe una farmacia "
                        + "con ese código."
                );

            } else {

                notification.mostrarError(
                        "No fue posible crear la farmacia. "
                        + "Verifique su conexión e intente nuevamente."
                );
            }
        }
    }

    private void configurarContexto() {

        if (actor.esSuperAdmin()) {

            lblRolDestino.setText(
                    "ADMINISTRADOR"
            );

            estilizarChip(
                    lblRolDestino,
                    COLOR_PRIMARY_DARK
            );

            comboFarmacia.setVisible(
                    true
            );

            comboFarmacia.setManaged(
                    true
            );

            lblFarmaciaFija.setVisible(
                    false
            );

            lblFarmaciaFija.setManaged(
                    false
            );

            cargarFarmacias();

            comboFarmacia.setPromptText(
                    "Seleccione una farmacia"
            );

            Tooltip tooltipFarmacia =
                    new Tooltip(
                            "Asigne la sede física principal "
                            + "para este administrador"
                    );

            tooltipFarmacia.setStyle(
                    "-fx-font-size: 12px;"
                    + "-fx-background-color: "
                    + COLOR_PRIMARY_DARK
                    + ";"
                    + "-fx-text-fill: white;"
            );

            comboFarmacia.setTooltip(
                    tooltipFarmacia
            );

        } else {

            lblRolDestino.setText(
                    "FARMACEUTICO"
            );

            estilizarChip(
                    lblRolDestino,
                    COLOR_PRIMARY_DARK
            );

            comboFarmacia.setVisible(
                    false
            );

            comboFarmacia.setManaged(
                    false
            );

            lblFarmaciaFija.setVisible(
                    true
            );

            lblFarmaciaFija.setManaged(
                    true
            );

            cargarFarmaciaAdministrador();
        }
    }

    private void actualizarContador(
            int cantidad
    ) {

        lblContadorFarmacias.setText(
                cantidad == 1
                        ? "1 farmacia activa disponible"
                        : cantidad
                        + " farmacias activas disponibles"
        );
    }

    /**
     * Carga las farmacias activas existentes.
     *
     * El StringConverter del ComboBox hará que se vean como:
     *
     * ID 1 · Farmacia Central · SEDE-CENTRAL
     * ID 2 · Farmacia Norte · SEDE-NORTE
     */
    private void cargarFarmacias() {

        try {

            List<Farmacia> farmaciasActivas =
                    farmaciaDAO
                            .listarActivas();

            comboFarmacia.setItems(
                    FXCollections
                            .observableArrayList(
                                    farmaciasActivas
                            )
            );

            actualizarContador(
                    farmaciasActivas.size()
            );

        } catch (SQLException e) {

            notification.mostrarError(
                    "No fue posible cargar "
                    + "las farmacias activas."
            );
        }
    }

    /**
     * Muestra la farmacia del administrador,
     * incluyendo su ID.
     */
    private void cargarFarmaciaAdministrador() {

        if (
                !actor.tieneFarmaciaAsignada()
        ) {

            lblFarmaciaFija.setText(
                    "Sin farmacia asignada"
            );

            estilizarChip(
                    lblFarmaciaFija,
                    COLOR_ERROR
            );

            return;
        }

        estilizarChip(
                lblFarmaciaFija,
                COLOR_PRIMARY_DARK
        );

        try {

            Farmacia farmacia =
                    farmaciaDAO.buscarPorId(
                            actor.getIdFarmacia()
                    );

            lblFarmaciaFija.setText(
                    (
                            farmacia != null
                                    ? describirFarmacia(
                                            farmacia
                                    )
                                    : "ID "
                                    + actor.getIdFarmacia()
                    )
                    + " · asignación automática"
            );

        } catch (SQLException e) {

            lblFarmaciaFija.setText(
                    "ID "
                    + actor.getIdFarmacia()
                    + " · asignación automática"
            );
        }
    }

    /* ===================== CREACIÓN DE USUARIO ===================== */

    private void intentarCrearUsuario(
            Stage stage
    ) {

        notification.limpiar();

        String nombre =
                txtNombre.getText();

        String email =
                txtEmail.getText();

        String password =
                txtPassword.getText();

        Integer idFarmaciaSeleccionada =
                null;

        /*
         * SUPER_ADMIN debe seleccionar
         * explícitamente la farmacia del administrador.
         */
        if (actor.esSuperAdmin()) {

            Farmacia farmacia =
                    comboFarmacia
                            .getSelectionModel()
                            .getSelectedItem();

            if (farmacia == null) {

                notification.mostrarError(
                        "Debe seleccionar la farmacia "
                        + "del nuevo administrador."
                );

                comboFarmacia.requestFocus();

                return;
            }

            /*
             * Se recupera el id_farmacia real
             * del objeto seleccionado.
             */
            idFarmaciaSeleccionada =
                    farmacia.getIdFarmacia();

            if (
                    idFarmaciaSeleccionada <= 0
            ) {

                notification.mostrarError(
                        "La farmacia seleccionada "
                        + "no es válida."
                );

                return;
            }
        }

        try {

            /*
             * El ID seleccionado continúa hacia:
             *
             * UsuarioService
             *      ↓
             * UsuarioDAO
             *      ↓
             * fn_crear_usuario_jerarquico
             *      ↓
             * USUARIOS.id_farmacia
             */
            usuarioService.crearUsuario(
                    nombre,
                    email,
                    password,
                    idFarmaciaSeleccionada
            );

            String rolCreado =
                    actor.esSuperAdmin()
                            ? "ADMINISTRADOR"
                            : "FARMACEUTICO";

            String farmaciaInfo =
                    "";

            if (
                    actor.esSuperAdmin()
                    && idFarmaciaSeleccionada != null
            ) {

                farmaciaInfo =
                        "\nFarmacia asignada: ID "
                        + idFarmaciaSeleccionada;
            }

            AlertUtil.mostrarExito(
                    "El usuario \""
                    + nombre
                    + "\" se creó correctamente como "
                    + rolCreado
                    + "."
                    + farmaciaInfo
            );

            stage.close();

        } catch (
                AccesoDenegadoException
                | IllegalStateException
                | IllegalArgumentException e
        ) {

            notification.mostrarError(
                    e.getMessage()
            );

        } catch (SQLException e) {

            String detalle =
                    e.getMessage() == null
                            ? ""
                            : e.getMessage()
                                    .toLowerCase();

            if (
                    detalle.contains(
                            "unique"
                    )
                    || detalle.contains(
                            "duplicate"
                    )
            ) {

                notification.mostrarError(
                        "Ya existe un usuario registrado "
                        + "con ese correo electrónico."
                );

            } else {

                notification.mostrarError(
                        "No fue posible crear el usuario. "
                        + "Verifique los datos ingresados."
                );
            }
        }
    }
}