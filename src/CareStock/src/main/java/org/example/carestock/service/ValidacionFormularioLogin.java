package org.example.carestock.service;

/**
 * Validaciones del formulario de inicio de sesion.
 * Sin consultas a BD y sin expresiones regulares.
 */
public final class ValidacionFormularioLogin {

    private static final int LONGITUD_MAXIMA_CORREO = 254;
    private static final int LONGITUD_MAXIMA_USUARIO = 64;

    private ValidacionFormularioLogin() { }

    public static String errorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "El correo institucional es obligatorio.";
        }

        if (!formatoCorreoValido(correo.trim())) {
            return "Ingresa un correo válido (ej.: usuario@dominio.com).";
        }

        return "";
    }

    public static String errorContrasena(String contrasena) {
        return contrasena == null || contrasena.isBlank()
                ? "La contraseña es obligatoria." : "";
    }

    private static boolean formatoCorreoValido(String correo) {
        int longitud = correo.length();

        // Rechazar entradas excesivas antes de procesarlas.
        if (longitud > LONGITUD_MAXIMA_CORREO) {
            return false;
        }

        int arroba = correo.indexOf('@');

        if (arroba < 1
                || arroba > LONGITUD_MAXIMA_USUARIO
                || arroba != correo.lastIndexOf('@')) {
            return false;
        }

        int inicioDominio = arroba + 1;
        int ultimoPunto = correo.lastIndexOf('.');

        if (inicioDominio >= longitud
                || correo.charAt(inicioDominio) == '.'
                || ultimoPunto <= inicioDominio
                || ultimoPunto == longitud - 1) {
            return false;
        }

        // Recorrido lineal: no existe backtracking.
        for (int i = 0; i < longitud; i++) {
            char caracter = correo.charAt(i);

            if (Character.isWhitespace(caracter)
                    || Character.isSpaceChar(caracter)
                    || Character.isISOControl(caracter)) {
                return false;
            }

            if (i > inicioDominio
                    && caracter == '.'
                    && correo.charAt(i - 1) == '.') {
                return false;
            }
        }

        return true;
    }
}
