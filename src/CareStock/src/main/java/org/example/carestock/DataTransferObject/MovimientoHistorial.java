package org.example.carestock.DataTransferObject;

import java.time.OffsetDateTime;

/**
 * HU-795: Proyeccion de solo lectura del historial.
 * No modifica ni reemplaza los registros originales.
 */
public record MovimientoHistorial(
        String origen,
        long idOrigen,
        String categoria,
        String tipoMovimiento,
        int idReferencia,
        String producto,
        String referencia,
        long cantidad,
        String motivo,
        Integer stockAnterior,
        Integer stockPosterior,
        Long diferencia,
        int idFarmacia,
        int idUsuario,
        String usuario,
        OffsetDateTime fechaMovimiento
) {

    /**
     * Identifica el evento dentro de su tabla de origen.
     * Dos tablas pueden utilizar el mismo ID numerico.
     */
    public String claveUnica() {
        return origen + ":" + idOrigen;
    }
}
