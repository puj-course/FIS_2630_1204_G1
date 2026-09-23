package com.carestock.model;

/**
 * Representa una operación de despacho lista para persistencia.
 *
 * El usuario responsable se fija al crear el objeto y no puede
 * modificarse posteriormente.
 */
public final class DespachoLote {

    private final int idLote;
    private final int cantidad;
    private final int idUsuario;

    public DespachoLote(
            int idLote,
            int cantidad,
            int idUsuario
    ) {

        if (idLote <= 0) {
            throw new IllegalArgumentException(
                    "El lote debe ser válido."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad a despachar debe ser mayor que cero."
            );
        }

        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario responsable debe ser válido."
            );
        }

        this.idLote = idLote;
        this.cantidad = cantidad;
        this.idUsuario = idUsuario;
    }

    public int getIdLote() {
        return idLote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
}
