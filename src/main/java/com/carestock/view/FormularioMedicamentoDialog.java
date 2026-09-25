package com.carestock.view;

import com.carestock.model.Medicamento;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class FormularioMedicamentoDialog extends Dialog<Medicamento> {

    private final TextField txtInvima = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtPrincipio = new TextField();
    private final TextField txtConcentracion = new TextField();
    private final TextField txtForma = new TextField();
    private final TextField txtPresentacion = new TextField();
    private final TextField txtCategoria = new TextField();
    private final TextField txtStockMinimo = new TextField();

    public FormularioMedicamentoDialog() {

        /*
         * Bloquea la visualización del formulario si la
         * sesión deja de existir antes de mostrarlo.
         */
        setOnShowing(
                event -> {

                    if (
                            !ProtectedNavigationGuard
                                    .ensureAuthenticated(
                                            null
                                    )
                    ) {

                        event.consume();
                    }
                }
        );
        setTitle("CareStock - Agregar medicamento");
        setHeaderText("Registre el medicamento. El stock inicia en 0 y se actualiza mediante el ingreso de lotes.");

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        txtInvima.setPromptText("Ej: INVIMA-2026M-001");
        txtNombre.setPromptText("Ej: Acetaminofén");
        txtPrincipio.setPromptText("Ej: Paracetamol");
        txtConcentracion.setPromptText("Ej: 500 mg");
        txtForma.setPromptText("Ej: Tabletas");
        txtPresentacion.setPromptText("Ej: Caja x 20 tabletas");
        txtCategoria.setPromptText("Ej: ANALGESICOS");
        txtStockMinimo.setPromptText("Ej: 10");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        agregarFila(grid, 0, "Código INVIMA:", txtInvima);
        agregarFila(grid, 1, "Nombre comercial:", txtNombre);
        agregarFila(grid, 2, "Principio activo:", txtPrincipio);
        agregarFila(grid, 3, "Concentración:", txtConcentracion);
        agregarFila(grid, 4, "Forma farmacéutica:", txtForma);
        agregarFila(grid, 5, "Presentación:", txtPresentacion);
        agregarFila(grid, 6, "Categoría existente:", txtCategoria);
        agregarFila(grid, 7, "Stock mínimo:", txtStockMinimo);

        getDialogPane().setContent(grid);

        Button guardarButton = (Button) getDialogPane().lookupButton(btnGuardarType);
        guardarButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validarFormulario();
            if (error != null) {
                event.consume();
                AlertUtil.mostrarAdvertencia(error);
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton != btnGuardarType) return null;
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            return new Medicamento(
                null,
                txtInvima.getText().trim(),
                txtNombre.getText().trim(),
                txtPrincipio.getText().trim(),
                txtConcentracion.getText().trim(),
                txtCategoria.getText().trim(),
                0,
                stockMinimo,
                txtForma.getText().trim(),
                txtPresentacion.getText().trim()
            );
        });
    }

    private void agregarFila(GridPane grid, int fila, String etiqueta, TextField campo) {
        grid.add(new Label(etiqueta), 0, fila);
        grid.add(campo, 1, fila);
    }

    private String validarFormulario() {
        if (vacio(txtInvima) || vacio(txtNombre) || vacio(txtPrincipio) || vacio(txtConcentracion)
                || vacio(txtForma) || vacio(txtPresentacion) || vacio(txtCategoria)) {
            return "Todos los campos descriptivos del medicamento son obligatorios.";
        }
        try {
            int minimo = Integer.parseInt(txtStockMinimo.getText().trim());
            if (minimo < 0) return "El stock mínimo no puede ser negativo.";
        } catch (NumberFormatException e) {
            return "El stock mínimo debe ser un número entero válido.";
        }
        return null;
    }

    private boolean vacio(TextField campo) {
        return campo.getText() == null || campo.getText().isBlank();
    }
}
