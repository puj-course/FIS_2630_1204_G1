package org.example.carestock.model;

/**
 * HU-792: Modelo inmutable de ajustes de existencias.
 *
 * MEDICAMENTO identifica un lote.
 * ASEO y MATERNIDAD identifican un producto.
 */
public record AjusteExistencias(
        Categoria categoria,
        int idReferencia,
        int idUsuario,
        int idFarmacia,
        String motivo,
        int cantidadAnterior,
        int cantidadNueva
) {

    public enum Categoria {
        MEDICAMENTO,
        ASEO,
        MATERNIDAD
    }

    public AjusteExistencias {

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria."
            );
        }

        if (idReferencia <= 0
                || idUsuario <= 0
                || idFarmacia <= 0) {

            throw new IllegalArgumentException(
                    "Referencia, usuario y farmacia deben ser válidos."
            );
        }

        if (cantidadAnterior < 0 || cantidadNueva < 0) {
            throw new IllegalArgumentException(
                    "Las existencias anteriores y finales no pueden ser negativas."
            );
        }

        if (cantidadAnterior == cantidadNueva) {
            throw new IllegalArgumentException(
                    "El ajuste debe modificar las existencias."
            );
        }

        if (motivo == null
                || motivo.isBlank()
                || motivo.trim().length() > 250) {

            throw new IllegalArgumentException(
                    "El motivo es obligatorio y no puede superar 250 caracteres."
            );
        }

        motivo = motivo.trim();
    }

    /**
     * Una diferencia positiva representa aumento.
     * Una diferencia negativa representa disminución.
     */
    public int diferencia() {
        return cantidadNueva - cantidadAnterior;
    }
}
