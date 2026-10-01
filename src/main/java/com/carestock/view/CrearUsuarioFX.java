package com.carestock.view;

import com.carestock.dao.FarmaciaDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Farmacia;
import com.carestock.security.AccessControl;
import com.carestock.service.FarmaciaService;
import com.carestock.service.UsuarioService;
import com.carestock.session.UserSession;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class CrearUsuarioFX {

    /* ===================== PALETA CLÍNICA ===================== */
    private static final String COLOR_PRIMARY       = "#0E7C7B";
    private static final String COLOR_PRIMARY_DARK  = "#0B615F";
    private static final String COLOR_BG            = "#F4FAF9";
    private static final String COLOR_TEXT_DARK     = "#20302F";
    private static final String COLOR_TEXT_MUTED    = "#6D8683";
    private static final String COLOR_BORDER        = "#D6E6E4";
    private static final String COLOR_ERROR         = "#C0392B";
    /* ============================================================ */

    private static final int LONGITUD_MINIMA_PASSWORD = 8;

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

    private final Label lblError =
            new Label();

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

        Label titulo =
                new Label(
                        "Crear nuevo usuario"
                );

        titulo.setStyle(
                "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        txtNombre.setPromptText(
                "Nombre completo"
        );

        txtNombre.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;"
                + "-fx-pref-height: 40;"
        );

        txtEmail.setPromptText(
                "Correo electrónico"
        );

        txtEmail.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;"
                + "-fx-pref-height: 40;"
        );

        txtPassword.setPromptText(
                "Contraseña (mínimo "
                + LONGITUD_MINIMA_PASSWORD
                + " caracteres)"
        );

        txtPassword.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;"
                + "-fx-pref-height: 40;"
        );

        configurarComboFarmacia();

        configurarContexto();

        configurarPanelCrearFarmacia();

        lblError.setStyle(
                "-fx-text-fill: " + COLOR_ERROR + ";"
                + "-fx-font-size: 12px;"
        );

        lblError.setWrapText(true);
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnCrear =
                new Button(
                        "Crear usuario"
                );

        btnCrear.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        );

        btnCrear.setOnMouseEntered(e -> btnCrear.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_DARK + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        ));

        btnCrear.setOnMouseExited(e -> btnCrear.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        ));

        Button btnCancelar =
                new Button(
                        "Cancelar"
                );

        btnCancelar.setStyle(
                "-fx-background-color: #E0E0E0;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        );

        btnCancelar.setOnAction(
                e -> stage.close()
        );

        btnCrear.setOnAction(
                e -> intentarCrearUsuario(
                        stage
                )
        );

        HBox filaBotones =
                new HBox(
                        10,
                        btnCrear,
                        btnCancelar
                );

        filaBotones.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label lblRol =
                new Label(
                        "Rol a crear:"
                );

        lblRol.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        Label lblFarmacia =
                new Label(
                        "Farmacia:"
                );

        lblFarmacia.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        lblContadorFarmacias.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
                + "-fx-font-size: 11px;"
        );

        VBox root =
                new VBox(
                        12,
                        titulo,
                        new Label(
                                "Nombre completo:"
                        ),
                        txtNombre,
                        new Label(
                                "Correo electrónico:"
                        ),
                        txtEmail,
                        new Label(
                                "Contraseña:"
                        ),
                        txtPassword,
                        lblRol,
                        lblRolDestino,
                        lblFarmacia,
                        comboFarmacia,
                        lblContadorFarmacias,
                        lblFarmaciaFija,
                        panelCrearFarmacia,
                        lblError,
                        filaBotones
                );

        root.setPadding(
                new Insets(25)
        );

        root.setPrefWidth(
                410
        );

        root.setStyle(
                "-fx-background-color: " + COLOR_BG + ";"
        );

        stage.setScene(
                new Scene(root)
        );

        stage.setResizable(false);

        stage.showAndWait();
    }

    private void configurarComboFarmacia() {

        comboFarmacia.setConverter(
                new StringConverter<Farmacia>() {

                    @Override
                    public String toString(
                            Farmacia farmacia
                    ) {

                        if (farmacia == null) {
                            return "";
                        }

                        return farmacia.getNombre();
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
                "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
                + "-fx-font-weight: bold;"
        );

        txtCodigoFarmacia.setPromptText(
                "Ej. SEDE-NORTE"
        );

        txtCodigoFarmacia.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;"
                + "-fx-pref-height: 40;"
        );

        txtNombreFarmacia.setPromptText(
                "Ej. Farmacia Norte"
        );

        txtNombreFarmacia.setStyle(
                "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-width: 1;"
                + "-fx-padding: 0 12 0 12;"
                + "-fx-pref-height: 40;"
        );

        Button btnCrearFarmacia =
                new Button(
                        "Crear farmacia"
                );

        btnCrearFarmacia.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        );

        btnCrearFarmacia.setOnMouseEntered(e -> btnCrearFarmacia.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_DARK + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        ));

        btnCrearFarmacia.setOnMouseExited(e -> btnCrearFarmacia.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-pref-height: 40;"
                + "-fx-cursor: hand;"
        ));

        btnCrearFarmacia.setOnAction(
                e -> crearNuevaFarmacia()
        );

        /* ==================== VALIDACIÓN VISUAL ==================== */
        btnCrearFarmacia.setDisable(true);

        txtCodigoFarmacia.textProperty().addListener((obs, old, newVal) -> {
            actualizarEstadoBotonFarmacia(btnCrearFarmacia);
        });

        txtNombreFarmacia.textProperty().addListener((obs, old, newVal) -> {
            actualizarEstadoBotonFarmacia(btnCrearFarmacia);
        });
        /* ============================================================ */

        Label lblCodigo =
                new Label(
                        "Código *"
                );

        lblCodigo.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
        );

        Label lblNombreFarmacia =
                new Label(
                        "Nombre *"
                );

        lblNombreFarmacia.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
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
                        8,
                        lblCodigo,
                        txtCodigoFarmacia,
                        lblNombreFarmacia,
                        txtNombreFarmacia,
                        btnCrearFarmacia,
                        lblNota
                );

        formularioFarmacia.setPadding(
                new Insets(10)
        );

        formularioFarmacia.setStyle(
                "-fx-background-color: " + COLOR_BG + ";"
                + "-fx-background-radius: 8;"
        );

        panelCrearFarmacia.setContent(
                formularioFarmacia
        );
    }

    /**
     * Actualiza el estado del botón "Crear farmacia" según
     * si los campos obligatorios están completos.
     */
    private void actualizarEstadoBotonFarmacia(Button btnCrearFarmacia) {

        String codigo = txtCodigoFarmacia.getText();
        String nombre = txtNombreFarmacia.getText();

        boolean camposCompletos =
                codigo != null && !codigo.isBlank()
                && nombre != null && !nombre.isBlank();

        btnCrearFarmacia.setDisable(!camposCompletos);
    }

    private void crearNuevaFarmacia() {

        ocultarError();

        try {

            Farmacia nuevaFarmacia =
                    farmaciaService
                            .crearFarmacia(
                                    txtCodigoFarmacia
                                            .getText(),
                                    txtNombreFarmacia
                                            .getText()
                            );

            comboFarmacia.setItems(
                    FXCollections
                            .observableArrayList(
                                    farmaciaService
                                            .listarActivas()
                            )
            );

            comboFarmacia
                    .getSelectionModel()
                    .select(
                            nuevaFarmacia
                    );

            txtCodigoFarmacia.clear();

            txtNombreFarmacia.clear();

            panelCrearFarmacia.setExpanded(
                    false
            );

            AlertUtil.mostrarExito(
                    "La farmacia \""
                    + nuevaFarmacia.getNombre()
                    + "\" fue creada correctamente."
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            mostrarError(
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

                mostrarError(
                        "Ya existe una farmacia con ese código."
                );

            } else {

                mostrarError(
                        "No fue posible crear la farmacia. "
                        + mensaje
                );
            }
        }
    }

    private void configurarContexto() {

        if (actor.esSuperAdmin()) {

            lblRolDestino.setText(
                    "ADMINISTRADOR"
            );

            lblRolDestino.setStyle(
                    "-fx-font-weight: bold;"
                    + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
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

            comboFarmacia.setStyle(
                    "-fx-background-radius: 8;"
                    + "-fx-border-radius: 8;"
                    + "-fx-border-color: " + COLOR_BORDER + ";"
                    + "-fx-border-width: 1;"
                    + "-fx-pref-height: 40;"
            );

            Tooltip tooltipFarmacia =
                    new Tooltip(
                            "Asigne la sede física principal "
                            + "para este administrador"
                    );

            tooltipFarmacia.setStyle(
                    "-fx-font-size: 12px;"
                    + "-fx-background-color: " + COLOR_PRIMARY_DARK + ";"
                    + "-fx-text-fill: white;"
            );

            comboFarmacia.setTooltip(
                    tooltipFarmacia
            );

        } else {

            lblRolDestino.setText(
                    "FARMACEUTICO"
            );

            lblRolDestino.setStyle(
                    "-fx-font-weight: bold;"
                    + "-fx-text-fill: " + COLOR_TEXT_DARK + ";"
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

    private void cargarFarmacias() {

        try {

            List<Farmacia> farmaciasActivas =
                    farmaciaDAO.listarActivas();

            comboFarmacia.setItems(
                    FXCollections.observableArrayList(
                            farmaciasActivas
                    )
            );

            lblContadorFarmacias.setText(
                    farmaciasActivas.size()
                    + " farmacias activas cargadas"
            );

        } catch (SQLException e) {

            mostrarError(
                    "No fue posible cargar las farmacias activas."
            );
        }
    }

    private void cargarFarmaciaAdministrador() {

        if (!actor.tieneFarmaciaAsignada()) {

            lblFarmaciaFija.setText(
                    "Sin farmacia asignada"
            );

            lblFarmaciaFija.setStyle(
                    "-fx-text-fill: " + COLOR_ERROR + ";"
                    + "-fx-font-weight: bold;"
            );

            return;
        }

        try {

            Farmacia farmacia =
                    farmaciaDAO.buscarPorId(
                            actor.getIdFarmacia()
                    );

            lblFarmaciaFija.setText(
                    farmacia != null
                            ? farmacia.getNombre()
                              + " · asignación automática"
                            : "Farmacia ID "
                              + actor.getIdFarmacia()
                              + " · asignación automática"
            );

        } catch (SQLException e) {

            lblFarmaciaFija.setText(
                    "Farmacia ID "
                    + actor.getIdFarmacia()
                    + " · asignación automática"
            );
        }
    }

    private void intentarCrearUsuario(
            Stage stage
    ) {

        ocultarError();

        String nombre =
                txtNombre.getText();

        String email =
                txtEmail.getText();

        String password =
                txtPassword.getText();

        Integer idFarmaciaSeleccionada =
                null;

        if (actor.esSuperAdmin()) {

            Farmacia farmacia =
                    comboFarmacia
                            .getSelectionModel()
                            .getSelectedItem();

            if (farmacia == null) {

                mostrarError(
                        "Debe seleccionar la farmacia "
                        + "del nuevo administrador."
                );

                return;
            }

            idFarmaciaSeleccionada =
                    farmacia.getIdFarmacia();
        }

        try {

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

            AlertUtil.mostrarExito(
                    "El usuario \""
                    + nombre
                    + "\" se creó correctamente como "
                    + rolCreado
                    + "."
            );

            stage.close();

        } catch (
                AccesoDenegadoException
                | IllegalStateException
                | IllegalArgumentException e
        ) {

            mostrarError(
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

                mostrarError(
                        "Ya existe un usuario registrado "
                        + "con ese correo electrónico."
                );

            } else {

                mostrarError(
                        "No fue posible crear el usuario. "
                        + "Verifique los datos ingresados."
                );
            }
        }
    }

    private void mostrarError(
            String mensaje
    ) {

        lblError.setText(
                mensaje
        );

        lblError.setVisible(
                true
        );

        lblError.setManaged(
                true
        );
    }

    private void ocultarError() {

        lblError.setVisible(
                false
        );

        lblError.setManaged(
                false
        );
    }
}
