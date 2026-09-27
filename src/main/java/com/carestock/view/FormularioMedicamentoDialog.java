package com.carestock.view;

import com.carestock.model.Medicamento;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FormularioMedicamentoDialog
        extends Dialog<Medicamento> {

    private final TextField txtInvima =
            new TextField();

    private final TextField txtNombre =
            new TextField();

    private final TextField txtPrincipio =
            new TextField();

    private final TextField txtConcentracion =
            new TextField();

    private final TextField txtForma =
            new TextField();

    private final TextField txtPresentacion =
            new TextField();

    private final TextField txtCategoria =
            new TextField();

    private final TextField txtStockMinimo =
            new TextField();


    public FormularioMedicamentoDialog() {

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

        setTitle(
                "CareStock - Registrar medicamento"
        );

        setHeaderText(
                "Nuevo medicamento"
        );

        configurarCampos();

        VBox contenido =
                construirContenido();

        getDialogPane()
                .setContent(
                        contenido
                );

        getDialogPane()
                .setPrefWidth(
                        620
                );

        ButtonType btnGuardarType =
                new ButtonType(
                        "Guardar medicamento",
                        ButtonBar.ButtonData.OK_DONE
                );

        getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnGuardarType,
                        ButtonType.CANCEL
                );

        Button guardarButton =
                (Button) getDialogPane()
                        .lookupButton(
                                btnGuardarType
                        );

        guardarButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    String error =
                            validarFormulario();

                    if (error != null) {

                        event.consume();

                        AlertUtil
                                .mostrarAdvertencia(
                                        error
                                );
                    }
                }
        );

        setResultConverter(
                dialogButton -> {

                    if (
                            dialogButton
                            != btnGuardarType
                    ) {

                        return null;
                    }

                    int stockMinimo =
                            Integer.parseInt(
                                    txtStockMinimo
                                            .getText()
                                            .trim()
                            );

                    return new Medicamento(
                            null,
                            txtInvima
                                    .getText()
                                    .trim(),
                            txtNombre
                                    .getText()
                                    .trim(),
                            txtPrincipio
                                    .getText()
                                    .trim(),
                            txtConcentracion
                                    .getText()
                                    .trim(),
                            txtCategoria
                                    .getText()
                                    .trim(),
                            0,
                            stockMinimo,
                            txtForma
                                    .getText()
                                    .trim(),
                            txtPresentacion
                                    .getText()
                                    .trim()
                    );
                }
        );
    }


    private void configurarCampos() {

        txtInvima.setPromptText(
                "Ej: INVIMA-2026M-001"
        );

        txtNombre.setPromptText(
                "Ej: Acetaminofén"
        );

        txtPrincipio.setPromptText(
                "Ej: Paracetamol"
        );

        txtConcentracion.setPromptText(
                "Ej: 500 mg"
        );

        txtForma.setPromptText(
                "Ej: Tableta"
        );

        txtPresentacion.setPromptText(
                "Ej: Caja x 20 tabletas"
        );

        txtCategoria.setPromptText(
                "Ej: ANALGESICOS"
        );

        txtStockMinimo.setPromptText(
                "Ej: 10"
        );
    }


    private VBox construirContenido() {

        VBox root =
                new VBox(14);

        root.setPadding(
                new Insets(10)
        );

        Label ayuda =
                new Label(
                        "Registra primero la información general "
                        + "del medicamento. El stock inicia en 0 "
                        + "y aumenta únicamente mediante "
                        + "el ingreso de lotes."
                );

        ayuda.setWrapText(
                true
        );

        ayuda.setStyle(
                "-fx-text-fill: #607D8B;"
                + "-fx-font-size: 11px;"
        );

        Label tituloIdentificacion =
                crearTituloSeccion(
                        "1. Identificación del medicamento"
                );

        GridPane identificacion =
                new GridPane();

        identificacion.setHgap(
                15
        );

        identificacion.setVgap(
                10
        );

        agregarFila(
                identificacion,
                0,
                "Código INVIMA:",
                txtInvima
        );

        agregarFila(
                identificacion,
                1,
                "Nombre comercial:",
                txtNombre
        );

        agregarFila(
                identificacion,
                2,
                "Principio activo:",
                txtPrincipio
        );

        Label tituloPresentacion =
                crearTituloSeccion(
                        "2. Presentación y control de inventario"
                );

        GridPane presentacion =
                new GridPane();

        presentacion.setHgap(
                15
        );

        presentacion.setVgap(
                10
        );

        agregarFila(
                presentacion,
                0,
                "Concentración:",
                txtConcentracion
        );

        agregarFila(
                presentacion,
                1,
                "Forma farmacéutica:",
                txtForma
        );

        agregarFila(
                presentacion,
                2,
                "Presentación:",
                txtPresentacion
        );

        agregarFila(
                presentacion,
                3,
                "Categoría existente:",
                txtCategoria
        );

        agregarFila(
                presentacion,
                4,
                "Stock mínimo:",
                txtStockMinimo
        );

        Label notaCategoria =
                new Label(
                        "La categoría debe existir previamente "
                        + "en el catálogo de CareStock."
                );

        notaCategoria.setStyle(
                "-fx-text-fill: #78909C;"
                + "-fx-font-size: 10px;"
        );

        root
                .getChildren()
                .addAll(
                        ayuda,
                        tituloIdentificacion,
                        identificacion,
                        new Separator(),
                        tituloPresentacion,
                        presentacion,
                        notaCategoria
                );

        return root;
    }


    private Label crearTituloSeccion(
            String texto
    ) {

        Label label =
                new Label(
                        texto
                );

        label.setStyle(
                "-fx-font-weight: bold;"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: #263238;"
        );

        return label;
    }


    private void agregarFila(
            GridPane grid,
            int fila,
            String etiqueta,
            TextField campo
    ) {

        campo.setPrefWidth(
                330
        );

        grid.add(
                new Label(
                        etiqueta
                ),
                0,
                fila
        );

        grid.add(
                campo,
                1,
                fila
        );
    }


    private String validarFormulario() {

        if (
                vacio(txtInvima)
                || vacio(txtNombre)
                || vacio(txtPrincipio)
                || vacio(txtConcentracion)
                || vacio(txtForma)
                || vacio(txtPresentacion)
                || vacio(txtCategoria)
        ) {

            return "Complete todos los datos descriptivos "
                    + "del medicamento.";
        }

        try {

            int minimo =
                    Integer.parseInt(
                            txtStockMinimo
                                    .getText()
                                    .trim()
                    );

            if (minimo < 0) {

                return "El stock mínimo no puede ser negativo.";
            }

        } catch (NumberFormatException e) {

            return "El stock mínimo debe ser un número "
                    + "entero válido.";
        }

        return null;
    }


    private boolean vacio(
            TextField campo
    ) {

        return campo.getText() == null
                || campo.getText().isBlank();
    }
}
