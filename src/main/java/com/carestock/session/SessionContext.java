package com.carestock.session;

/**
 * Contexto de seguridad para operaciones que requieren un usuario autenticado.
 *
 * Centraliza la extracción del usuario activo desde UserSession y evita que
 * los servicios de dominio reciban el usuario_id desde la interfaz o desde
 * parámetros manipulables por el cliente.
 */
public final class SessionContext {

    public static final String ERROR_SESION_REQUERIDA =
            "No existe un usuario autenticado para realizar la operación.";

    private final UserSession userSession;

    public SessionContext() {
        this(UserSession.getInstance());
    }

    SessionContext(UserSession userSession) {
        if (userSession == null) {
            throw new IllegalArgumentException(
                    "UserSession no puede ser nulo."
            );
        }

        this.userSession = userSession;
    }

    /**
     * Obtiene exclusivamente desde la sesión el ID del usuario autenticado.
     *
     * @return ID del usuario actualmente autenticado.
     * @throws IllegalStateException si no existe una sesión activa.
     */
    public int requireAuthenticatedUserId() {

        UserSession.CurrentUser currentUser =
                userSession.getCurrentUser();

        if (currentUser == null) {
            throw new IllegalStateException(
                    ERROR_SESION_REQUERIDA
            );
        }

        return currentUser.getId();
    }
}
