package com.carestock.session;

/**
 * Contexto de seguridad para operaciones que requieren un usuario autenticado.
 *
 * Centraliza la extracción del usuario activo desde UserSession y evita que
 * identificadores sensibles como usuario_id o id_farmacia lleguen desde la
 * interfaz o desde parámetros manipulables por el cliente.
 */
public final class SessionContext {

    public static final String ERROR_SESION_REQUERIDA =
            "No existe un usuario autenticado para realizar la operación.";

    public static final String ERROR_FARMACIA_REQUERIDA =
            "El usuario autenticado no tiene una farmacia asociada.";

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
     * Obtiene exclusivamente desde la sesión
     * el ID del usuario autenticado.
     */
    public int requireAuthenticatedUserId() {

        return requireCurrentUser().getId();
    }

    /**
     * Obtiene exclusivamente desde la sesión
     * el ID de la farmacia del usuario autenticado.
     *
     * El identificador nunca debe provenir de un TextField,
     * ComboBox u otro control modificable desde JavaFX.
     */
    public int requireAuthenticatedPharmacyId() {

        UserSession.CurrentUser currentUser =
                requireCurrentUser();

        if (!currentUser.tieneFarmaciaAsignada()) {

            throw new IllegalStateException(
                    ERROR_FARMACIA_REQUERIDA
            );
        }

        return currentUser.getIdFarmacia();
    }

    /**
     * Verifica que exista una sesión activa antes
     * de exponer información del usuario.
     */
    private UserSession.CurrentUser requireCurrentUser() {

        UserSession.CurrentUser currentUser =
                userSession.getCurrentUser();

        if (currentUser == null) {

            throw new IllegalStateException(
                    ERROR_SESION_REQUERIDA
            );
        }

        return currentUser;
    }
}
