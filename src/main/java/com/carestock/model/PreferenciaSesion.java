package com.carestock.model;

/**
 * Preferencias individuales de seguridad de sesión
 * asociadas a un usuario de CareStock.
 */
public final class PreferenciaSesion {

    public static final boolean DEFAULT_TIMEOUT_ACTIVO =
            true;

    public static final int DEFAULT_TIMEOUT_MINUTOS =
            15;

    public static final int MIN_TIMEOUT_MINUTOS =
            1;

    public static final int MAX_TIMEOUT_MINUTOS =
            120;

    private final int idUsuario;

    private final boolean timeoutActivo;

    private final int timeoutMinutos;

    public PreferenciaSesion(
            int idUsuario,
            boolean timeoutActivo,
            int timeoutMinutos
    ) {

        if (idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del usuario debe ser válido."
            );
        }

        if (
                timeoutMinutos < MIN_TIMEOUT_MINUTOS
                || timeoutMinutos > MAX_TIMEOUT_MINUTOS
        ) {

            throw new IllegalArgumentException(
                    "El tiempo de inactividad debe estar entre "
                    + MIN_TIMEOUT_MINUTOS
                    + " y "
                    + MAX_TIMEOUT_MINUTOS
                    + " minutos."
            );
        }

        this.idUsuario =
                idUsuario;

        this.timeoutActivo =
                timeoutActivo;

        this.timeoutMinutos =
                timeoutMinutos;
    }

    public static PreferenciaSesion porDefecto(
            int idUsuario
    ) {

        return new PreferenciaSesion(
                idUsuario,
                DEFAULT_TIMEOUT_ACTIVO,
                DEFAULT_TIMEOUT_MINUTOS
        );
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public boolean isTimeoutActivo() {
        return timeoutActivo;
    }

    public int getTimeoutMinutos() {
        return timeoutMinutos;
    }
}
