package org.example.carestock.service;

import java.math.BigDecimal;

public final class ValidadorPrecioMedicamento {

    private ValidadorPrecioMedicamento() {
    }

    public static BigDecimal convertir(String texto) {

        // Los medicamentos historicos pueden no tener precio.
        if (texto == null || texto.isBlank()) {
            return null;
        }

        final BigDecimal valor;

        try {
            valor = new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Precio invalido. Use: 15000.00", e);
        }

        if (valor.signum() < 0
                || valor.scale() > 2
                || valor.precision() - valor.scale() > 10) {

            throw new IllegalArgumentException(
                    "El precio debe ser no negativo, "
                            + "con maximo dos decimales "
                            + "y 10 digitos enteros."
            );
        }

        return valor;
    }
}
