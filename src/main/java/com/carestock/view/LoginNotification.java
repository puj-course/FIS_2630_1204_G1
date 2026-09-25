package com.carestock.view;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Componente visual utilizado por LoginFX para mostrar
 * mensajes de validación, autenticación y notificaciones
 * relacionadas con la sesión.
 */
public final class LoginNotification {

    private static final String ESTILO_ERROR =
            "-fx-text-fill: #7A2E2E;" +
            "-fx-font-size: 11px;" +
            "-fx-background-color: #FDECEC;" +
            "-fx-border-color: #F0A8A8;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8;";

    private static final String ESTILO_EXITO =
            "-fx-text-fill: #1B5E20;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #E8F5E9;" +
            "-fx-border-color: #A5D6A7;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8;";

    private final Label label =
            new Label();

    private PauseTransition ocultadoAutomatico;

    public LoginNotification() {

        label.setWrapText(true);
        label.setMaxWidth(320);

        ocultar();
    }

    /**
     * Retorna el componente visual que se integra
     * dentro de la pantalla de Login.
     */
    public Label getView() {
        return label;
    }

    /**
     * Muestra una notificación de error.
     */
    public void mostrarError(
            String mensaje
    ) {

        cancelarOcultadoAutomatico();

        label.setStyle(
                ESTILO_ERROR
        );

        label.setText(
                mensaje
        );

        mostrar();
    }

    /**
     * Muestra una notificación positiva y la oculta
     * automáticamente después de unos segundos.
     */
    public void mostrarExito(
            String mensaje
    ) {

        cancelarOcultadoAutomatico();

        label.setStyle(
                ESTILO_EXITO
        );

        label.setText(
                mensaje
        );

        mostrar();

        ocultadoAutomatico =
                new PauseTransition(
                        Duration.seconds(4)
                );

        ocultadoAutomatico.setOnFinished(
                event -> limpiar()
        );

        ocultadoAutomatico.play();
    }

    /**
     * Limpia y oculta cualquier notificación activa.
     */
    public void limpiar() {

        cancelarOcultadoAutomatico();

        label.setText("");

        ocultar();
    }

    private void mostrar() {

        label.setVisible(true);
        label.setManaged(true);
    }

    private void ocultar() {

        label.setVisible(false);
        label.setManaged(false);
    }

    private void cancelarOcultadoAutomatico() {

        if (
                ocultadoAutomatico
                != null
        ) {

            ocultadoAutomatico.stop();

            ocultadoAutomatico =
                    null;
        }
    }
}
