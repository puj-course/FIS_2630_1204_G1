package org.example.carestock.service;

/**
 * Validaciones y textos del modulo de dispensacion (HU-144).
 *
 * <p>No usa JavaFX, por eso se puede probar sin abrir ventanas. La regla de
 * farmacovigilancia sigue en {@code FarmacovigilanciaService}; esto solo revisa lo
 * que escribe el usuario antes de llamarla.</p>
 */
public final class ValidadorDispensacion {

    private ValidadorDispensacion() {
    }

    /**
     * Convierte el texto en una cantidad valida para el lote.
     *
     * @throws NumberFormatException    si el texto no es un numero entero
     * @throws IllegalArgumentException si la cantidad es cero, negativa o supera las
     *                                  existencias del lote
     */
    public static int validarCantidad(String texto, int existenciasLote) {
        int cantidad;
        try {
            cantidad = Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    "La cantidad a dispensar debe ser un número entero válido.");
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad a dispensar debe ser mayor que cero.");
        }
        if (cantidad > existenciasLote) {
            throw new IllegalArgumentException(
                    "La cantidad a dispensar (" + cantidad
                            + ") supera las existencias del lote ("
                            + existenciasLote + ").");
        }
        return cantidad;
    }

    public static int stockRestante(int existenciasLote, int cantidad) {
        return existenciasLote - cantidad;
    }

    public static String mensajeConfirmacion(String numeroLote, int cantidad, int existenciasLote) {
        return "Lote: " + numeroLote
                + "\nCantidad a dispensar: " + cantidad
                + "\nStock actual del lote: " + existenciasLote
                + "\nStock que quedará: " + stockRestante(existenciasLote, cantidad);
    }

    public static String mensajeExito(String numeroLote, int cantidad, int existenciasLote) {
        return "Se dispensaron " + cantidad + " unidades del lote " + numeroLote
                + " correctamente.\nStock restante del lote: "
                + stockRestante(existenciasLote, cantidad) + ".";
    }
}
