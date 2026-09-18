package com.carestock.config;

/** Configuración funcional de la aplicación. */
public final class AppConfig {

    private static final String DEFAULT_USER_EMAIL = "admin@carestock.com";

    private AppConfig() {
    }

    public static String getCurrentUserEmail() {
        String value = System.getProperty("carestock.user.email");
        if (value == null || value.isBlank()) {
            value = System.getenv("CARESTOCK_USER_EMAIL");
        }
        return (value == null || value.isBlank()) ? DEFAULT_USER_EMAIL : value.trim();
    }
}
