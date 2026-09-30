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

    /*
     * HU-68 / Issue #499
     *
     * El ComboBox mantiene objetos Farmacia completos.
     * Esto permite mostrar el nombre al usuario y conservar
     * internamente el id_farmacia de la sede seleccionada.
     */
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
        );

        txtNombre.setPromptText(
                "Nombre completo"
        );

        txtEmail.setPromptText(
                "Correo electrónico"
        );

        txtPassword.setPromptText(
                "Contraseña (mínimo "
                + LONGITUD_MINIMA_PASSWORD
                + " caracteres)"
        );

        /*
         * HU-68 / Issue #499
         *
         * Configura primero la forma en que se mostrarán
         * los objetos Farmacia dentro del ComboBox.
         */
        configurarComboFarmacia();

        /*
         * Configura el formulario según el rol de quien
         * está creando el nuevo usuario.
         *
         * Para SUPER_ADMIN también ejecutará la carga
         * dinámica de farmacias desde la base de datos.
         */
        configurarContexto();

        configurarPanelCrearFarmacia();

        lblError.setStyle(
                "-fx-text-fill: #C62828;"
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
                "-fx-background-color: #A3D9D2;"
                + "-fx-font-weight: bold;"
        );

        Button btnCancelar =
                new Button(
                        "Cancelar"
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

        Label lblFarmacia =
                new Label(
                        "Farmacia:"
                );

        lblContadorFarmacias.setStyle(
                "-fx-text-fill: #6B6862;"
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

        stage.setScene(
                new Scene(root)
        );

        stage.setResizable(false);

        stage.showAndWait();
    }

    /**
     * Configura la visualización del ComboBox de farmacias.
     *
     * El ComboBox continúa almacenando objetos Farmacia,
     * pero el usuario visualiza únicamente el nombre de
     * la sede.
     *
     * Esto permite conservar internamente:
     *
     * - id_farmacia
     * - código
     * - nombre
     * - estado
     *
     * mientras que en la interfaz únicamente aparece
     * Farmacia.getNombre().
     */
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

                        /*
                         * El ComboBox no es editable.
                         * Por lo tanto no necesitamos convertir
                         * texto escrito manualmente en Farmacia.
                         */
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

        txtCodigoFarmacia.setPromptText(
                "Ej. SEDE-NORTE"
        );

        txtNombreFarmacia.setPromptText(
                "Ej. Farmacia Norte"
        );

        Button btnCrearFarmacia =
                new Button(
                        "Crear farmacia"
                );

        btnCrearFarmacia.setStyle(
                "-fx-background-color: #B2DFDB;"
                + "-fx-font-weight: bold;"
        );

        btnCrearFarmacia.setOnAction(
                e -> crearNuevaFarmacia()
        );

        VBox formularioFarmacia =
                new VBox(
                        8,
                        new Label(
                                "Código:"
                        ),
                        txtCodigoFarmacia,
                        new Label(
                                "Nombre:"
                        ),
                        txtNombreFarmacia,
                        btnCrearFarmacia
                );

        formularioFarmacia.setPadding(
                new Insets(10)
        );

        panelCrearFarmacia.setContent(
                formularioFarmacia
        );
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

            /*
             * Recargar el ComboBox inmediatamente con
             * las farmacias activas provenientes de BD.
             */
            comboFarmacia.setItems(
                    FXCollections
                            .observableArrayList(
                                    farmaciaService
                                            .listarActivas()
                            )
            );

            /*
             * Seleccionar automáticamente la farmacia
             * recién creada.
             *
             * El SelectionModel conserva el objeto
             * Farmacia completo.
             */
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

            /*
             * HU-68 / Issue #499
             *
             * La lista ya no se construye con sedes
             * hardcodeadas.
             *
             * Se consulta directamente la base de datos.
             */
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
                    + "-fx-background-color: #2D6A4F;"
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

    /**
     * Carga dinámicamente las farmacias activas
     * registradas en la base de datos.
     *
     * No existen valores de sedes hardcodeados.
     */
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
                    "-fx-text-fill: #C62828;"
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

            /*
             * El SelectionModel devuelve el objeto
             * Farmacia completo que actualmente
             * está seleccionado.
             */
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

            /*
             * Aunque visualmente se muestra únicamente
             * el nombre, internamente se conserva y
             * recupera el id_farmacia real.
             */
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