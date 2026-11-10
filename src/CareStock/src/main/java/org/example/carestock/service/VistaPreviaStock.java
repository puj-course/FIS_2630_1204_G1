package org.example.carestock.service;

import java.util.OptionalInt;

/**
 * Calcula el stock que quedaria tras un movimiento, antes de registrarlo (HU-153).
 *
 * <p>No usa JavaFX, por eso se puede probar sin abrir ventanas. La regla que valida
 * el servicio al guardar sigue en {@code ServicioMovimientosProducto}; esto es solo
 * una vista previa para el usuario.</p>
 */
public final class VistaPreviaStock {

    public static final String ENTRADA = "ENTRADA";
    public static final String SALIDA = "SALIDA";
    public static final String NO_APLICA = "—";

    private VistaPreviaStock() {
    }

    /**
     * Stock resultante, o vacio si no se puede calcular: tipo distinto de
     * ENTRADA/SALIDA, cantidad vacia, no numerica, cero o negativa, o una entrada
     * que desborda el maximo permitido. Una salida mayor que el stock devuelve un
     * valor negativo para poder avisar al usuario.
     */
    public static OptionalInt stockResultante(int stockActual, String tipo, String cantidadTexto) {
        if (cantidadTexto == null) {
            return OptionalInt.empty();
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto.trim());
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
        if (cantidad <= 0) {
            return OptionalInt.empty();
        }

        if (ENTRADA.equals(tipo)) {
            try {
                return OptionalInt.of(Math.addExact(stockActual, cantidad));
            } catch (ArithmeticException e) {
                return OptionalInt.empty();
            }
        }
        if (SALIDA.equals(tipo)) {
            return OptionalInt.of(stockActual - cantidad);
        }
        return OptionalInt.empty();
    }

    /** Indica si el movimiento dejaria el stock por debajo de cero. */
    public static boolean quedaNegativo(int stockActual, String tipo, String cantidadTexto) {
        OptionalInt resultado = stockResultante(stockActual, tipo, cantidadTexto);
        return resultado.isPresent() && resultado.getAsInt() < 0;
    }

    /** Texto para mostrar: el numero, "—" si no aplica o el aviso de stock insuficiente. */
    public static String texto(int stockActual, String tipo, String cantidadTexto) {
        OptionalInt resultado = stockResultante(stockActual, tipo, cantidadTexto);
        if (resultado.isEmpty()) {
            return "Stock resultante: " + NO_APLICA;
        }
        if (resultado.getAsInt() < 0) {
            return "Stock resultante: " + resultado.getAsInt() + " (stock insuficiente)";
        }
        return "Stock resultante: " + resultado.getAsInt();
    }
}
