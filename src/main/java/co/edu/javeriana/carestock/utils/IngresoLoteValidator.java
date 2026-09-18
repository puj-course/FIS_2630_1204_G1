package co.edu.javeriana.carestock.utils;

import java.time.LocalDate;
import co.edu.javeriana.carestock.models.Medicamento;
import co.edu.javeriana.carestock.models.Ubicacion;

public class IngresoLoteValidator {

    // Regex: Alfanumérico y guiones, entre 3 y 50 caracteres
    private static final String LOTE_REGEX = "^[a-zA-Z0-9-]{3,50}$";

    public static String validar(Medicamento medicamento, String numeroLote, String cantidadStr, 
                                 LocalDate fechaVencimiento, Ubicacion ubicacion) {

        if (medicamento == null) {
            return "Debe seleccionar un medicamento activo.";
        }

        if (numeroLote == null || numeroLote.isBlank()) {
            return "El número de lote es obligatorio.";
        }

        if (!numeroLote.trim().matches(LOTE_REGEX)) {
            return "El número de lote solo debe contener letras, números y guiones (3 a 50 caracteres).";
        }

        if (cantidadStr == null || cantidadStr.isBlank()) {
            return "La cantidad de unidades es obligatoria.";
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr.trim());
            if (cantidad <= 0) {
                return "La cantidad debe ser un número entero mayor a cero.";
            }
        } catch (NumberFormatException e) {
            return "La cantidad ingresada debe ser un número entero válido.";
        }

        if (fechaVencimiento == null) {
            return "Debe seleccionar una fecha de vencimiento.";
        }

        if (!fechaVencimiento.isAfter(LocalDate.now())) {
            return "La fecha de vencimiento debe ser posterior a la fecha actual (" + LocalDate.now() + ").";
        }

        if (ubicacion == null) {
            return "Debe seleccionar una ubicación física disponible.";
        }

        return null; // Retorna null si la validación fue exitosa
    }
}