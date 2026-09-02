package com.carestock.view;

import com.carestock.model.Medicamento;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class FormularioMedicamentoDialog extends Dialog<Medicamento> {

    private TextField txtInvima = new TextField();
    private TextField txtNombre = new TextField();
    private TextField txtPrincipio = new TextField();
    private TextField txtStockInicial = new TextField();
    private TextField txtStockMinimo = new TextField();

    public FormularioMedicamentoDialog() {
        setTitle("CareStock - Agregar Nuevo Medicamento");
        setHeaderText("Ingrese los datos del nuevo medicamento.\nSiga los formatos de ejemplo mostrados en cada campo.");

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        txtInvima.setPromptText("Ej: INVIMA-2024M-057");
        txtNombre.setPromptText("Ej: Colchicina 0.5mg");
        txtPrincipio.setPromptText("Ej: Colchicina");
        txtStockInicial.setPromptText("Ej: 50");
        txtStockMinimo.setPromptText("Ej: 10");

        grid.add(new Label("Código INVIMA:"), 0, 0);
        grid.add(txtInvima, 1, 0);

        grid.add(new Label("Nombre Comercial:"), 0, 1);
        grid.add(txtNombre, 1, 1);

        grid.add(new Label("Principio Activo:"), 0, 2);
        grid.add(txtPrincipio, 1, 2);

        grid.add(new Label("Stock Inicial:"), 0, 3);
        grid.add(txtStockInicial, 1, 3);

        grid.add(new Label("Stock Mínimo:"), 0, 4);
        grid.add(txtStockMinimo, 1, 4);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                int stock = 0;
                int min = 0;
                try {
                    stock = Integer.parseInt(txtStockInicial.getText().trim());
                } catch (Exception ignored) {}
                try {
                    min = Integer.parseInt(txtStockMinimo.getText().trim());
                } catch (Exception ignored) {}

                return new Medicamento(
                    null,
                    txtInvima.getText().trim(),
                    txtNombre.getText().trim(),
                    txtPrincipio.getText().trim(),
                    "N/A",
                    "General",
                    stock,
                    min
                );
            }
            return null;
        });
    }
}
