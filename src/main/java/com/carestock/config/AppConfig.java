package com.carestock.config;

/**
 * Mantiene la información funcional de la sesión actual de CareStock.
 */
public final class AppConfig {

    private static String currentUserEmail;

    private AppConfig() {
    }

    /**
     * Registra el usuario que inició sesión correctamente.
     */
    public static void setCurrentUserEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del usuario autenticado no puede estar vacío."
            );
        }

        currentUserEmail = email.trim();
    }

    /**
     * Devuelve el correo del usuario autenticado.
     */
    public static String getCurrentUserEmail() {

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new IllegalStateException(
                    "No existe un usuario autenticado en la sesión actual."
            );
        }

        return currentUserEmail;
    }

    /**
     * Indica si actualmente existe una sesión autenticada.
     */
    public static boolean hasAuthenticatedUser() {
        return currentUserEmail != null && !currentUserEmail.isBlank();
    }

    /**
     * Cierra la sesión actual.
     */
    public static void clearCurrentUser() {
        currentUserEmail = null;
    }
}
