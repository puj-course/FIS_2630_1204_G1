package com.carestock.view;

import com.carestock.security.AccessControl;
import com.carestock.session.UserSession;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Guard central para vistas protegidas de CareStock.
 *
 * Antes de permitir el acceso verifica que exista una
 * sesión válida. Si no existe, invalida cualquier contexto
 * residual y redirige automáticamente al Login.
 */
public final class ProtectedNavigationGuard {

    private ProtectedNavigationGuard() {
    }

    /**
     * Verifica la sesión antes de permitir la navegación
     * hacia una vista protegida.
     *
     * @param preferredWindow ventana desde la cual
     *                        se intenta navegar.
     * @return true cuando el acceso está autorizado.
     */
    public static boolean ensureAuthenticated(
            Window preferredWindow
    ) {

        if (AccessControl.hasValidSession()) {
            return true;
        }

        /*
         * Defensa adicional:
         * elimina cualquier contexto residual o incompleto.
         */
        UserSession
                .getInstance()
                .clearSession();

        Stage targetStage =
                resolveStage(
                        preferredWindow
                );

        try {

            /*
             * La redirección ocurre inmediatamente.
             * No se permite cargar la vista protegida.
             */
            LoginFX login =
                    new LoginFX();

            login.start(
                    targetStage
            );

            /*
             * El usuario ya está en Login cuando recibe
             * la notificación de sesión inválida.
             */
            AlertUtil.mostrarSesionExpirada();

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    "No fue posible redirigir "
                    + "a la pantalla de inicio de sesión."
            );

            System.err.println(
                    "Error de navegación protegida: "
                    + e.getMessage()
            );
        }

        return false;
    }

    /**
     * Obtiene la ventana que será reutilizada para Login.
     * Si no existe una ventana principal disponible,
     * crea una nueva.
     */
    private static Stage resolveStage(
            Window preferredWindow
    ) {

        if (preferredWindow instanceof Stage) {

            return (Stage) preferredWindow;
        }

        for (
                Window window
                : Window.getWindows()
        ) {

            if (
                    window instanceof Stage
                    && window.isShowing()
            ) {

                return (Stage) window;
            }
        }

        return new Stage();
    }
}
