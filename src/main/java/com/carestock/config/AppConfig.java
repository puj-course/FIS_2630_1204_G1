package com.carestock.config;

import com.carestock.session.UserSession;

public final class AppConfig {

    private AppConfig() {
    }

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

    
    public static String getCurrentUserRole() {

        UserSession session =
                UserSession.getInstance();

        if (!session.isLoggedIn()) {
            throw new IllegalStateException(
                    "No existe un usuario autenticado."
            );
        }

        return session
                .getCurrentUser()
                .getRol();
    }


    public static boolean hasAuthenticatedUser() {
        return UserSession
                .getInstance()
                .isLoggedIn();
    }

    public static void clearCurrentUser() {
        UserSession
                .getInstance()
                .clearSession();
    }
}
