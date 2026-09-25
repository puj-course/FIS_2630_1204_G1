package com.carestock.security;

import com.carestock.session.UserSession;

/**
 * Punto central de validación para el acceso a recursos
 * protegidos de CareStock.
 *
 * Una sesión se considera válida únicamente cuando existe
 * un usuario autenticado con identificador, correo y rol.
 */
public final class AccessControl {

    public static final String ERROR_SESION_REQUERIDA =
            "No existe una sesión autenticada válida.";

    private AccessControl() {
    }

    /**
     * Verifica que el contexto de sesión contenga
     * un usuario válido.
     *
     * @return true cuando la sesión puede acceder
     *         a recursos protegidos.
     */
    public static boolean hasValidSession() {

        UserSession.CurrentUser currentUser =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        return currentUser.getId() > 0
                && currentUser.getEmail() != null
                && !currentUser.getEmail().isBlank()
                && currentUser.getRol() != null
                && !currentUser.getRol().isBlank();
    }

    /**
     * Exige una sesión válida para continuar.
     *
     * @throws IllegalStateException si no existe
     *         una sesión autenticada válida.
     */
    public static void requireAuthenticated() {

        if (!hasValidSession()) {

            throw new IllegalStateException(
                    ERROR_SESION_REQUERIDA
            );
        }
    }
}
