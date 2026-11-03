package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * Calcula la cantidad sugerida a reponer para una alerta (HU-143).
 *
 * <p>Regla: se sugiere reponer hasta el doble del stock minimo, es decir
 * {@code 2 x minimo - existencias}, sin bajar de cero. Solo aplica a las alertas de
 * stock minimo; las de vencimiento no se reponen por minimo.</p>
 *
 * <p>No usa JavaFX, por eso se puede probar sin abrir ventanas.</p>
 */
public final class SugerenciaReposicion {

    public static final int FACTOR_STOCK_OBJETIVO = 2;
    public static final String NO_APLICA = "—";

    private SugerenciaReposicion() {
    }

    /** Cantidad sugerida, o vacio si la alerta no es de stock minimo o no tiene umbral. */
    public static OptionalInt cantidadSugerida(AlertaInventario alerta) {
        Objects.requireNonNull(alerta, "La alerta es obligatoria");

        if (alerta.tipoAlerta() != TipoAlerta.STOCK_MINIMO || alerta.umbral() == null) {
            return OptionalInt.empty();
        }

        int objetivo = FACTOR_STOCK_OBJETIVO * alerta.umbral();
        return OptionalInt.of(Math.max(0, objetivo - alerta.existencias()));
    }

    /** Texto para la tabla: la cantidad sugerida o "—" cuando no aplica. */
    public static String texto(AlertaInventario alerta) {
        OptionalInt cantidad = cantidadSugerida(alerta);
        return cantidad.isPresent() ? Integer.toString(cantidad.getAsInt()) : NO_APLICA;
    }
}
