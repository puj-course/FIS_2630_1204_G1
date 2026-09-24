package com.carestock.view;

import com.carestock.model.PreferenciaSesion;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

/**
 * Diálogo utilizado por cada usuario para configurar
 * sus preferencias de expiración por inactividad.
 */
public final class ConfiguracionSesionDialog
        extends Dialog<PreferenciaSesion> {

    public ConfiguracionSesionDialog(
            PreferenciaSesion preferencia
    ) {

        setTitle(
                "CareStock - Configuración de sesión"
        );

        setHeaderText(
                "Seguridad de la sesión"
        );

        CheckBox chkTimeout =
                new CheckBox(
                        "Cerrar automáticamente la sesión por inactividad"
                );

        chkTimeout.setSelected(
                preferencia.isTimeoutActivo()
        );

        Spinner<Integer> spinnerMinutos =
                new Spinner<>();

        spinnerMinutos.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        PreferenciaSesion.MIN_TIMEOUT_MINUTOS,
                        PreferenciaSesion.MAX_TIMEOUT_MINUTOS,
                        preferencia.getTimeoutMinutos()
                )
        );

        spinnerMinutos.setEditable(
                false
        );

        spinnerMinutos.setPrefWidth(
                100
        );

        spinnerMinutos
                .disableProperty()
                .bind(
                        chkTimeout
                                .selectedProperty()
                                .not()
                );

        Label lblMinutos =
                new Label(
                        "minutos"
                );

        Label lblAyuda =
                new Label(
                        "El temporizador se reinicia cada vez que "
                        + "mueves el mouse, haces clic o utilizas "
                        + "el teclado."
                );

        lblAyuda.setWrapText(
                true
        );

        lblAyuda.setMaxWidth(
                380
        );

        lblAyuda.setStyle(
                "-fx-text-fill: #607D8B;" +
                "-fx-font-size: 11px;"
        );

        GridPane contenido =
                new GridPane();

        contenido.setHgap(
                10
        );

        contenido.setVgap(
                15
        );

        contenido.setPadding(
                new Insets(10)
        );

        contenido.add(
                chkTimeout,
                0,
                0,
                3,
                1
        );

        contenido.add(
                new Label(
                        "Tiempo de inactividad:"
                ),
                0,
                1
        );

        contenido.add(
                spinnerMinutos,
                1,
                1
        );

        contenido.add(
                lblMinutos,
                2,
                1
        );

        contenido.add(
                lblAyuda,
                0,
                2,
                3,
                1
        );

        ButtonType btnGuardar =
                new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE
                );

        getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnGuardar,
                        ButtonType.CANCEL
                );

        getDialogPane()
                .setContent(
                        contenido
                );

        setResultConverter(
                button -> {

                    if (button != btnGuardar) {
                        return null;
                    }

                    return new PreferenciaSesion(
                            preferencia.getIdUsuario(),
                            chkTimeout.isSelected(),
                            spinnerMinutos.getValue()
                    );
                }
        );
    }
}
