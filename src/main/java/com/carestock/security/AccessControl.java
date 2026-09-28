package com.carestock.security;

import com.carestock.exception.AccesoDenegadoException;
import com.carestock.session.UserSession;

import java.util.Arrays;

public final class AccessControl {

    public static final String ERROR_SESION_REQUERIDA =
            "No existe una sesión autenticada válida.";

    private AccessControl() {
    }

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

    public static void requireAuthenticated() {

        if (!hasValidSession()) {

            throw new IllegalStateException(
                    ERROR_SESION_REQUERIDA
            );
        }
    }

    public static void requireRole(String... rolesPermitidos) {

        requireAuthenticated();

        String rolActual =
                UserSession
                        .getInstance()
                        .getCurrentUser()
                        .getRol();

        boolean autorizado =
                Arrays.stream(rolesPermitidos)
                        .anyMatch(rolActual::equalsIgnoreCase);

        if (!autorizado) {

            throw new AccesoDenegadoException(
                    "Su rol (" + rolActual + ") no está "
                    + "autorizado para esta operación."
            );
        }
    }
}
