package com.carestock.session;

public final class SessionContext {

    public static final String ERROR_SESION_REQUERIDA =
            "No existe un usuario autenticado para realizar la operación.";

    public static final String ERROR_FARMACIA_REQUERIDA =
            "El usuario autenticado no tiene una farmacia asociada.";

    private final UserSession userSession;

    public SessionContext() {
        this(
                UserSession.getInstance()
        );
    }

    SessionContext(
            UserSession userSession
    ) {

        if (userSession == null) {

            throw new IllegalArgumentException(
                    "UserSession no puede ser nulo."
            );
        }

        this.userSession =
                userSession;
    }

    public int requireAuthenticatedUserId() {

        return requireCurrentUser()
                .getId();
    }

    public int requireAuthenticatedPharmacyId() {

        UserSession.CurrentUser usuario =
                requireCurrentUser();

        if (!usuario.tieneFarmaciaAsignada()) {

            throw new IllegalStateException(
                    ERROR_FARMACIA_REQUERIDA
            );
        }

        return usuario.getIdFarmacia();
    }

    public UserSession.CurrentUser requireCurrentUser() {

        UserSession.CurrentUser usuario =
                userSession.getCurrentUser();

        if (usuario == null) {

            throw new IllegalStateException(
                    ERROR_SESION_REQUERIDA
            );
        }

        return usuario;
    }
}
