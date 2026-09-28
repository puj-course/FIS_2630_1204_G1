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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;

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
                new Label("Rol a crear:");

        Label lblFarmacia =
                new Label("Farmacia:");

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
             * Recargar el ComboBox inmediatamente.
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
             * recién creada para facilitar la creación
             * del administrador.
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

            comboFarmacia.setVisible(true);
            comboFarmacia.setManaged(true);

            lblFarmaciaFija.setVisible(false);
            lblFarmaciaFija.setManaged(false);

            cargarFarmacias();

            comboFarmacia.setPromptText(
                    "Seleccione una farmacia"
            );

        } else {

            lblRolDestino.setText(
                    "FARMACEUTICO"
            );

            lblRolDestino.setStyle(
                    "-fx-font-weight: bold;"
            );

            comboFarmacia.setVisible(false);
            comboFarmacia.setManaged(false);

            lblFarmaciaFija.setVisible(true);
            lblFarmaciaFija.setManaged(true);

            cargarFarmaciaAdministrador();
        }
    }

    private void cargarFarmacias() {

        try {

            comboFarmacia.setItems(
                    FXCollections.observableArrayList(
                            farmaciaDAO.listarActivas()
                    )
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
                    detalle.contains("unique")
                    || detalle.contains("duplicate")
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

        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {

        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
