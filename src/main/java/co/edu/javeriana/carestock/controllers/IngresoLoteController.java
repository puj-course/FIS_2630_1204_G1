package co.edu.javeriana.carestock.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import co.edu.javeriana.carestock.models.Medicamento;
import co.edu.javeriana.carestock.models.Ubicacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class IngresoLoteController implements Initializable {

    @FXML
    private ComboBox<Medicamento> cmbMedicamento;

    @FXML
    private TextField txtNumeroLote;

    @FXML
    private TextField txtCantidad;

    @FXML
    private DatePicker dpFechaVencimiento;

    @FXML
    private ComboBox<Ubicacion> cmbUbicacion;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarMedicamentosActivos();
        cargarUbicacionesDisponibles();
    }

    private void cargarMedicamentosActivos() {
        // En T4 se conectará con MedicamentoDAO / PostgreSQL
        ObservableList<Medicamento> medicamentosMock = FXCollections.observableArrayList(
            new Medicamento(1, "INVIMA-2024M-001", "Acetaminofén", "500 mg"),
            new Medicamento(2, "INVIMA-2024M-002", "Amoxicilina", "500 mg"),
            new Medicamento(3, "INVIMA-2024M-003", "Losartán", "50 mg")
        );
        cmbMedicamento.setItems(medicamentosMock);
    }

    private void cargarUbicacionesDisponibles() {
        // En T4 se conectará con UbicacionDAO / PostgreSQL
        ObservableList<Ubicacion> ubicacionesMock = FXCollections.observableArrayList(
            new Ubicacion(1, "Estante A", "Nivel 1", "Pasillo principal"),
            new Ubicacion(2, "Estante A", "Nivel 2", "Comprimidos"),
            new Ubicacion(3, "Estante B", "Nivel 1", "Jarabes")
        );
        cmbUbicacion.setItems(ubicacionesMock);
    }
@FXML
private void handleGuardarLote(ActionEvent event) {
    Medicamento medicamentoSel = cmbMedicamento.getValue();
    String numeroLote = txtNumeroLote.getText();
    String cantidadStr = txtCantidad.getText();
    LocalDate fechaVencimiento = dpFechaVencimiento.getValue();
    Ubicacion ubicacionSel = cmbUbicacion.getValue();

    String errorValidacion = IngresoLoteValidator.validar(
        medicamentoSel, numeroLote, cantidadStr, fechaVencimiento, ubicacionSel
    );

    if (errorValidacion != null) {
        mostrarAlerta("Error de Validación", errorValidacion, Alert.AlertType.WARNING);
        return;
    }

    int cantidad = Integer.parseInt(cantidadStr.trim());
    mostrarAlerta("Validación Exitosa", 
        "Datos válidos para el Lote: " + numeroLote.trim() + "\nListo para guardar en BD.", 
        Alert.AlertType.INFORMATION);
}
    @FXML
    private void handleCancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        if (stage != null) stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}