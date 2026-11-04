package org.example.carestock.service;

import java.util.regex.Pattern;

/**
 * Validaciones del formulario de inicio de sesión.
 * No consulta la base de datos.
 */
public final class ValidacionFormularioLogin {

    private static final Pattern CORREO =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ValidacionFormularioLogin() { }

    public static String errorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "El correo institucional es obligatorio.";
        }

        if (!CORREO.matcher(correo.trim()).matches()) {
            return "Ingresa un correo válido (ej.: usuario@dominio.com).";
        }

        return "";
    }

    public static String errorContrasena(String contrasena) {
        return contrasena == null || contrasena.isBlank()
                ? "La contraseña es obligatoria." : "";
    }
}
