package com.carestock.view;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

public final class AlertUtil {

    private static final String ESTILO_EXITO =
            "-fx-background-color: #FDFBF7;" +
            "-fx-border-color: #A7D8D8;" +
            "-fx-border-width: 2px;";

    private static final String ESTILO_ERROR =
            "-fx-background-color: #FDFBF7;" +
            "-fx-border-color: #F0A8A8;" +
            "-fx-border-width: 2px;";

    private AlertUtil() {
    }

    public static void mostrarExito(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Operación exitosa", mensaje, ESTILO_EXITO, "#2E5E4E");
    }

    public static void mostrarError(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "No se pudo completar la operación", mensaje, ESTILO_ERROR, "#7A2E2E");
    }

    public static void mostrarAdvertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Revise la información", mensaje, ESTILO_ERROR, "#7A2E2E");
    }

    /**
     * Informa que una operación protegida no puede continuar
     * porque no existe una sesión autenticada activa.
     */
    public static void mostrarSesionExpirada() {
        mostrar(
                Alert.AlertType.WARNING,
                "Sesión no disponible",
                "La sesión ha expirado o no existe un usuario activo. Inicie sesión nuevamente para continuar.",
                ESTILO_ERROR,
                "#7A2E2E"
        );
    }

    private static void mostrar(Alert.AlertType type, String header, String mensaje,
                                String estiloFondo, String colorTexto) {
        Alert alert = new Alert(type);
        alert.setTitle("CareStock");
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        aplicarEstilo(alert, estiloFondo, colorTexto);
        alert.showAndWait();
    }

    private static void aplicarEstilo(Alert alert, String estiloFondo, String colorTexto) {
        DialogPane pane = alert.getDialogPane();
        pane.setStyle(estiloFondo);
        Label contentLabel = (Label) pane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-text-fill: " + colorTexto + "; -fx-font-size: 13px;");
        }
    }
}
