package com.carestock.controller;

import com.carestock.session.UserSession;

/**
 * Controla las acciones asociadas al ciclo de vida de la sesión.
 */
public final class SessionController {

    private final UserSession userSession;

    public SessionController() {
        this(UserSession.getInstance());
    }

    SessionController(UserSession userSession) {

        if (userSession == null) {
            throw new IllegalArgumentException(
                    "UserSession no puede ser nulo."
            );
        }

        this.userSession = userSession;
    }

    /**
     * Cierra la sesión activa eliminando el usuario de memoria.
     */
    public void cerrarSesion() {
        userSession.clearSession();
    }
}
