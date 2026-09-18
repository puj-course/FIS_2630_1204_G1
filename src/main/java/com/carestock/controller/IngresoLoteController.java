package com.carestock.controller;

import com.carestock.dao.MedicamentoDAO;
import com.carestock.dao.UbicacionDAO;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.service.IngresoLoteService;
import com.carestock.utils.IngresoLoteValidator;
import com.carestock.view.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class IngresoLoteController implements Initializable {

    @FXML private ComboBox<Medicamento> cmbMedicamento;
    @FXML private TextField txtNumeroLote;
    @FXML private TextField txtCantidad;
    @FXML private DatePicker dpFechaVencimiento;
    @FXML private ComboBox<Ubicacion> cmbUbicacion;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final UbicacionDAO ubicacionDAO = new UbicacionDAO();
    private final IngresoLoteService ingresoLoteService = new IngresoLoteService();
    private Runnable onSaved;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dpFechaVencimiento.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || !date.isAfter(LocalDate.now()));
            }
        });
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            cmbMedicamento.setItems(FXCollections.observableArrayList(medicamentoDAO.obtenerActivos()));
            cmbUbicacion.setItems(FXCollections.observableArrayList(ubicacionDAO.obtenerTodas()));
        } catch (SQLException e) {
            btnGuardar.setDisable(true);
            AlertUtil.mostrarError("No fue posible cargar medicamentos y ubicaciones. " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardarLote(ActionEvent event) {
        Medicamento medicamento = cmbMedicamento.getValue();
        Ubicacion ubicacion = cmbUbicacion.getValue();
        String numeroLote = txtNumeroLote.getText();
        String cantidad = txtCantidad.getText();
        LocalDate fecha = dpFechaVencimiento.getValue();

        String error = IngresoLoteValidator.validar(medicamento, numeroLote, cantidad, fecha, ubicacion);
        if (error != null) {
            AlertUtil.mostrarAdvertencia(error);
            return;
        }

        try {
            ingresoLoteService.registrar(medicamento, numeroLote, cantidad, fecha, ubicacion);
            AlertUtil.mostrarExito("El lote \"" + numeroLote.trim() + "\" se registró correctamente y el stock fue actualizado.");
            if (onSaved != null) {
                onSaved.run();
            }
            cerrarVentana();
        } catch (IllegalArgumentException e) {
            AlertUtil.mostrarAdvertencia(e.getMessage());
        } catch (SQLException e) {
            AlertUtil.mostrarError("No se pudo registrar el lote en PostgreSQL. " + mensajeSqlAmigable(e));
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana();
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private String mensajeSqlAmigable(SQLException e) {
        String mensaje = e.getMessage() == null ? "Error de base de datos." : e.getMessage();
        if ("23505".equals(e.getSQLState())) {
            return "Ya existe ese número de lote para el medicamento seleccionado.";
        }
        if ("23503".equals(e.getSQLState())) {
            return "Hay una referencia inválida de medicamento, ubicación o usuario.";
        }
        return mensaje;
    }
}
