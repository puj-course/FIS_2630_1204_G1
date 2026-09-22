package com.carestock.view;

import javafx.scene.control.Label;

/**
 * Componente visual utilizado por LoginFX para mostrar
 * mensajes de validación y errores de autenticación.
 */
public final class LoginNotification {

    private final Label label = new Label();

    public LoginNotification() {

        label.setStyle(
                "-fx-text-fill: #7A2E2E;" +
                "-fx-font-size: 11px;" +
                "-fx-background-color: #FDECEC;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8;"
        );

        label.setWrapText(true);
        label.setMaxWidth(320);

        ocultar();
    }

    public Label getView() {
        return label;
    }

    public void mostrarError(String mensaje) {

        label.setText(mensaje);
        label.setVisible(true);
        label.setManaged(true);
    }

    public void limpiar() {

        label.setText("");
        ocultar();
    }

    private void ocultar() {

        label.setVisible(false);
        label.setManaged(false);
    }
}
