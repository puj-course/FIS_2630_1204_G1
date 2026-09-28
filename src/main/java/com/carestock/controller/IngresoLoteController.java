package com.carestock.controller;

import com.carestock.dao.MedicamentoDAO;
import com.carestock.dao.UbicacionDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.security.AccessControl;
import com.carestock.security.MedicamentoAccessPolicy;
import com.carestock.service.IngresoLoteService;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;
import com.carestock.utils.IngresoLoteValidator;
import com.carestock.view.AlertUtil;
import com.carestock.view.ProtectedNavigationGuard;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class IngresoLoteController
        implements Initializable {


    // =====================================================
    // MEDICAMENTO EXISTENTE
    // =====================================================

    @FXML
    private ComboBox<Medicamento> cmbMedicamento;

    @FXML
    private Button btnNuevoMedicamento;


    // =====================================================
    // FORMULARIO INLINE DE NUEVO MEDICAMENTO
    // =====================================================

    @FXML
    private VBox pnlNuevoMedicamento;

    @FXML
    private TextField txtNuevoInvima;

    @FXML
    private TextField txtNuevoNombre;

    @FXML
    private TextField txtNuevoPrincipio;

    @FXML
    private TextField txtNuevaConcentracion;

    @FXML
    private TextField txtNuevaForma;

    @FXML
    private TextField txtNuevaPresentacion;

    @FXML
    private ComboBox<String> cmbNuevaCategoria;

    @FXML
    private TextField txtNuevoStockMinimo;

    @FXML
    private Button btnGuardarNuevoMedicamento;


    // =====================================================
    // LOTE
    // =====================================================

    @FXML
    private TextField txtNumeroLote;

    @FXML
    private TextField txtCantidad;

    @FXML
    private DatePicker dpFechaVencimiento;

    @FXML
    private ComboBox<Ubicacion> cmbUbicacion;

    @FXML
    private Label lblUsuarioSesion;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;


    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final MedicamentoDAO medicamentoDAO =
            new MedicamentoDAO();

    private final UbicacionDAO ubicacionDAO =
            new UbicacionDAO();

    private final IngresoLoteService ingresoLoteService =
            new IngresoLoteService();

    private Runnable onSaved;

    private Runnable onSessionExpired;


    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        if (!AccessControl.hasValidSession()) {

            btnGuardar.setDisable(
                    true
            );

            Platform.runLater(
                    () -> {

                        Window owner =
                                btnCancelar.getScene() != null
                                        ? btnCancelar
                                                .getScene()
                                                .getWindow()
                                        : null;

                        ProtectedNavigationGuard
                                .ensureAuthenticated(
                                        owner
                                );
                    }
            );

            return;
        }


        /*
         * Nuevo medicamento inicia contraído.
         */
        mostrarFormularioMedicamento(
                false
        );


        /*
         * El botón solo se habilita para roles
         * autorizados.
         */
        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        boolean puedeRegistrarMedicamento =
                usuario != null
                && MedicamentoAccessPolicy
                        .puedeRegistrarMedicamento(
                                usuario.getRol()
                        );

        btnNuevoMedicamento.setVisible(
                puedeRegistrarMedicamento
        );

        btnNuevoMedicamento.setManaged(
                puedeRegistrarMedicamento
        );


        /*
         * Solo se permiten fechas futuras.
         */
        dpFechaVencimiento.setDayCellFactory(
                picker ->
                        new javafx.scene.control.DateCell() {

                            @Override
                            public void updateItem(
                                    LocalDate date,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        date,
                                        empty
                                );

                                setDisable(
                                        empty
                                        || !date.isAfter(
                                                LocalDate.now()
                                        )
                                );
                            }
                        }
        );


        if (!actualizarUsuarioSesion()) {

            btnGuardar.setDisable(
                    true
            );

            AlertUtil.mostrarSesionExpirada();

            return;
        }


        cargarDatos();
    }


    // =====================================================
    // SESIÓN
    // =====================================================

    private boolean actualizarUsuarioSesion() {

        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (usuario == null) {

            lblUsuarioSesion.setText(
                    "Responsable: sesión no disponible"
            );

            lblUsuarioSesion.setStyle(
                    "-fx-text-fill: #C62828;"
                    + "-fx-font-weight: bold;"
            );

            return false;
        }


        lblUsuarioSesion.setText(
                "Responsable: "
                + usuario.getNombre()
                + " ("
                + usuario.getRol()
                + ") · asignado automáticamente"
        );

        return true;
    }


    // =====================================================
    // CARGA DE DATOS
    // =====================================================

    private void cargarDatos() {

        try {

            cargarMedicamentos(
                    null
            );

            cmbUbicacion.setItems(
                    FXCollections.observableArrayList(
                            ubicacionDAO.obtenerTodas()
                    )
            );

            cmbNuevaCategoria.setItems(
                    FXCollections.observableArrayList(
                            medicamentoDAO.obtenerCategorias()
                    )
            );

        } catch (SQLException e) {

            btnGuardar.setDisable(
                    true
            );

            AlertUtil.mostrarError(
                    "No fue posible cargar los datos "
                    + "necesarios para el ingreso. "
                    + e.getMessage()
            );
        }
    }


    private void cargarMedicamentos(
            Medicamento seleccionar
    ) throws SQLException {

        int idFarmacia =
                new SessionContext()
                        .requireAuthenticatedPharmacyId();

        var medicamentos =
                medicamentoDAO
                        .obtenerActivosPorFarmacia(
                                idFarmacia
                        );

        cmbMedicamento.setItems(
                FXCollections.observableArrayList(
                        medicamentos
                )
        );


        if (seleccionar == null) {
            return;
        }


        medicamentos
                .stream()
                .filter(
                        medicamento ->
                                medicamento
                                        .getCodigoInvima()
                                        .equalsIgnoreCase(
                                                seleccionar
                                                        .getCodigoInvima()
                                        )
                )
                .findFirst()
                .ifPresent(
                        medicamento ->
                                cmbMedicamento
                                        .getSelectionModel()
                                        .select(
                                                medicamento
                                        )
                );
    }


    // =====================================================
    // MOSTRAR / OCULTAR NUEVO MEDICAMENTO
    // =====================================================

    @FXML
    private void handleNuevoMedicamento(
            ActionEvent event
    ) {

        if (!AccessControl.hasValidSession()) {

            manejarSesionExpirada();

            return;
        }


        boolean mostrar =
                !pnlNuevoMedicamento
                        .isVisible();

        mostrarFormularioMedicamento(
                mostrar
        );


        if (mostrar) {

            Platform.runLater(
                    () -> txtNuevoInvima
                            .requestFocus()
            );
        }
    }


    private void mostrarFormularioMedicamento(
            boolean mostrar
    ) {

        pnlNuevoMedicamento.setVisible(
                mostrar
        );

        pnlNuevoMedicamento.setManaged(
                mostrar
        );


        btnNuevoMedicamento.setText(
                mostrar
                        ? "− Ocultar formulario"
                        : "+ Registrar nuevo medicamento"
        );


        /*
         * Recalcular la ventana después del cambio
         * de contenido.
         */
        Platform.runLater(
                () -> {

                    if (
                            btnNuevoMedicamento
                                    .getScene()
                            != null
                    ) {

                        Window window =
                                btnNuevoMedicamento
                                        .getScene()
                                        .getWindow();

                        if (window instanceof Stage stage) {

                            stage.sizeToScene();
                        }
                    }
                }
        );
    }


    @FXML
    private void handleCancelarNuevoMedicamento(
            ActionEvent event
    ) {

        limpiarFormularioMedicamento();

        mostrarFormularioMedicamento(
                false
        );
    }


    // =====================================================
    // GUARDAR NUEVO MEDICAMENTO
    // =====================================================

    @FXML
    private void handleGuardarNuevoMedicamento(
            ActionEvent event
    ) {

        if (!AccessControl.hasValidSession()) {

            manejarSesionExpirada();

            return;
        }


        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (usuario == null) {

            manejarSesionExpirada();

            return;
        }


        try {

            MedicamentoAccessPolicy
                    .requireRegistrarMedicamento(
                            usuario.getRol()
                    );

        } catch (AccesoDenegadoException e) {

            AlertUtil.mostrarError(
                    e.getMessage()
            );

            return;
        }


        String error =
                validarNuevoMedicamento();

        if (error != null) {

            AlertUtil.mostrarAdvertencia(
                    error
            );

            return;
        }


        int stockMinimo =
                Integer.parseInt(
                        txtNuevoStockMinimo
                                .getText()
                                .trim()
                );


        Medicamento nuevo =
                new Medicamento(
                        null,
                        txtNuevoInvima
                                .getText()
                                .trim(),
                        txtNuevoNombre
                                .getText()
                                .trim(),
                        txtNuevoPrincipio
                                .getText()
                                .trim(),
                        txtNuevaConcentracion
                                .getText()
                                .trim(),
                        cmbNuevaCategoria
                                .getValue(),
                        0,
                        stockMinimo,
                        txtNuevaForma
                                .getText()
                                .trim(),
                        txtNuevaPresentacion
                                .getText()
                                .trim()
                );


        try {

            /*
             * insertar() permite conservar el detalle
             * de errores SQL en este flujo.
             */
            medicamentoDAO.insertar(
                    nuevo
            );


            cargarMedicamentos(
                    nuevo
            );


            limpiarFormularioMedicamento();

            mostrarFormularioMedicamento(
                    false
            );


            /*
             * Actualiza también los datos del Dashboard.
             */
            if (onSaved != null) {
                onSaved.run();
            }


            AlertUtil.mostrarExito(
                    "El medicamento \""
                    + nuevo.getNombreComercial()
                    + "\" fue registrado correctamente. "
                    + "Ya está seleccionado para continuar "
                    + "con el ingreso del lote."
            );

        } catch (AccesoDenegadoException e) {

            AlertUtil.mostrarError(
                    e.getMessage()
            );

        } catch (SQLException e) {

            if ("23505".equals(
                    e.getSQLState()
            )) {

                AlertUtil.mostrarAdvertencia(
                        "Ya existe un medicamento "
                        + "con ese código INVIMA."
                );

            } else {

                AlertUtil.mostrarError(
                        "No fue posible registrar "
                        + "el medicamento. "
                        + mensajeSqlAmigable(e)
                );
            }
        }
    }


    private String validarNuevoMedicamento() {

        if (vacio(txtNuevoInvima)) {

            return "Ingrese el código INVIMA.";
        }

        if (vacio(txtNuevoNombre)) {

            return "Ingrese el nombre comercial.";
        }

        if (vacio(txtNuevoPrincipio)) {

            return "Ingrese el principio activo.";
        }

        if (vacio(txtNuevaConcentracion)) {

            return "Ingrese la concentración.";
        }

        if (vacio(txtNuevaForma)) {

            return "Ingrese la forma farmacéutica.";
        }

        if (vacio(txtNuevaPresentacion)) {

            return "Ingrese la presentación.";
        }

        if (cmbNuevaCategoria.getValue() == null) {

            return "Seleccione una categoría.";
        }

        if (vacio(txtNuevoStockMinimo)) {

            return "Ingrese el stock mínimo.";
        }


        try {

            int minimo =
                    Integer.parseInt(
                            txtNuevoStockMinimo
                                    .getText()
                                    .trim()
                    );

            if (minimo < 0) {

                return "El stock mínimo no puede "
                        + "ser negativo.";
            }

        } catch (NumberFormatException e) {

            return "El stock mínimo debe ser "
                    + "un número entero válido.";
        }


        return null;
    }


    private boolean vacio(
            TextField campo
    ) {

        return campo.getText() == null
                || campo.getText().isBlank();
    }


    private void limpiarFormularioMedicamento() {

        txtNuevoInvima.clear();

        txtNuevoNombre.clear();

        txtNuevoPrincipio.clear();

        txtNuevaConcentracion.clear();

        txtNuevaForma.clear();

        txtNuevaPresentacion.clear();

        txtNuevoStockMinimo.clear();

        cmbNuevaCategoria
                .getSelectionModel()
                .clearSelection();
    }


    // =====================================================
    // GUARDAR LOTE
    // =====================================================

    @FXML
    private void handleGuardarLote(
            ActionEvent event
    ) {

        if (!AccessControl.hasValidSession()) {

            manejarSesionExpirada();

            return;
        }


        Medicamento medicamento =
                cmbMedicamento.getValue();

        Ubicacion ubicacion =
                cmbUbicacion.getValue();

        String numeroLote =
                txtNumeroLote.getText();

        String cantidad =
                txtCantidad.getText();

        LocalDate fecha =
                dpFechaVencimiento
                        .getValue();


        String error =
                IngresoLoteValidator
                        .validar(
                                medicamento,
                                numeroLote,
                                cantidad,
                                fecha,
                                ubicacion
                        );


        if (error != null) {

            AlertUtil.mostrarAdvertencia(
                    error
            );

            return;
        }


        try {

            ingresoLoteService.registrar(
                    medicamento,
                    numeroLote,
                    cantidad,
                    fecha,
                    ubicacion
            );


            AlertUtil.mostrarExito(
                    "El lote \""
                    + numeroLote.trim()
                    + "\" se registró correctamente "
                    + "y el stock fue actualizado."
            );


            if (onSaved != null) {
                onSaved.run();
            }


            cerrarVentana();

        } catch (IllegalStateException e) {

            manejarSesionExpirada();

        } catch (IllegalArgumentException e) {

            AlertUtil.mostrarAdvertencia(
                    e.getMessage()
            );

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No se pudo registrar el lote "
                    + "en PostgreSQL. "
                    + mensajeSqlAmigable(e)
            );
        }
    }


    // =====================================================
    // CANCELAR
    // =====================================================

    @FXML
    private void handleCancelar(
            ActionEvent event
    ) {

        cerrarVentana();
    }


    public void setOnSaved(
            Runnable onSaved
    ) {

        this.onSaved =
                onSaved;
    }


    public void setOnSessionExpired(
            Runnable onSessionExpired
    ) {

        this.onSessionExpired =
                onSessionExpired;
    }


    private void manejarSesionExpirada() {

        btnGuardar.setDisable(
                true
        );

        btnGuardarNuevoMedicamento.setDisable(
                true
        );


        actualizarUsuarioSesion();


        AlertUtil.mostrarSesionExpirada();


        cerrarVentana();


        if (onSessionExpired != null) {

            onSessionExpired.run();
        }
    }


    private void cerrarVentana() {

        Stage stage =
                (Stage) btnCancelar
                        .getScene()
                        .getWindow();

        if (stage != null) {

            stage.close();
        }
    }


    private String mensajeSqlAmigable(
            SQLException e
    ) {

        String mensaje =
                e.getMessage() == null
                        ? "Error de base de datos."
                        : e.getMessage();


        if ("23505".equals(
                e.getSQLState()
        )) {

            return "Ya existe un registro "
                    + "con esos datos.";
        }


        if ("23503".equals(
                e.getSQLState()
        )) {

            return "Existe una referencia inválida "
                    + "de medicamento, categoría, "
                    + "ubicación o usuario.";
        }


        return mensaje;
    }
}
