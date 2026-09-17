package com.carestock.view;
 
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
 
/**
 * Utilidad para mostrar mensajes de éxito y error con el estilo
 * oficial de CareStock (paleta pastel definida en DOCUMENTACION_UI.md).
 * HU-43: Mensajes de confirmación y error del registro de lote.
 *
 * Reutilizable para cualquier formulario de la app (medicamentos hoy,
 * registro de lotes cuando esté listo el backend de HU.37).
 */
public class AlertUtil {
 
    private static final String ESTILO_EXITO =
            "-fx-background-color: #FDFBF7;" +
            "-fx-border-color: #A7D8D8;" +
            "-fx-border-width: 2px;";
 
    private static final String ESTILO_ERROR =
            "-fx-background-color: #FDFBF7;" +
            "-fx-border-color: #F0A8A8;" +
            "-fx-border-width: 2px;";
 
    public static void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CareStock");
        alert.setHeaderText("Operación exitosa");
        alert.setContentText(mensaje);
        aplicarEstilo(alert, ESTILO_EXITO, "#2E5E4E");
        alert.showAndWait();
    }
 
    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("CareStock");
        alert.setHeaderText("No se pudo completar la operación");
        alert.setContentText(mensaje);
        aplicarEstilo(alert, ESTILO_ERROR, "#7A2E2E");
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
 
