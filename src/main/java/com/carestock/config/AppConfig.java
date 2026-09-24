package com.carestock.config;

import com.carestock.session.UserSession;

/**
 * Configuración funcional de CareStock.
 *
 * El usuario actual se obtiene desde UserSession.
 */
public final class AppConfig {

    private AppConfig() {
    }

    /**
     * Devuelve el correo del usuario autenticado.
     */
    public static String getCurrentUserEmail() {

        UserSession session =
                UserSession.getInstance();

        if (!session.isLoggedIn()) {
            throw new IllegalStateException(
                    "No existe un usuario autenticado."
            );
        }

        return session
                .getCurrentUser()
                .getEmail();
    }

    /**
     * Indica si hay un usuario autenticado.
     */
    public static boolean hasAuthenticatedUser() {
        return UserSession
                .getInstance()
                .isLoggedIn();
    }

    /**
     * Limpia la sesión actual.
     */
    public static void clearCurrentUser() {
        UserSession
                .getInstance()
                .clearSession();
    }
}
