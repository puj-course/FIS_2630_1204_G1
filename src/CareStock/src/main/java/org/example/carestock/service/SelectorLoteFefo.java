package org.example.carestock.service;

import org.example.carestock.model.Lote;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Elige el lote que conviene dispensar primero segun la regla FEFO: primero en
 * vencer, primero en salir (HU-152).
 *
 * <p>Solo se consideran los lotes del mismo medicamento que se pueden dispensar
 * (disponibles, con unidades y sin vencer). No usa JavaFX, por eso se puede probar
 * sin abrir ventanas.</p>
 */
public final class SelectorLoteFefo {

    private SelectorLoteFefo() {
    }

    /**
     * Devuelve el lote apto con la fecha de vencimiento mas cercana. Si dos lotes
     * vencen el mismo dia, gana el de menor identificador.
     */
    public static Optional<Lote> sugerir(List<Lote> lotes, int idMedicamento) {
        Objects.requireNonNull(lotes, "La lista de lotes es obligatoria");

        return lotes.stream()
                .filter(Objects::nonNull)
                .filter(lote -> lote.getIdMedicamento() == idMedicamento)
                .filter(Lote::esAptoParaDispensar)
                .min(Comparator.comparing(Lote::getFechaVencimiento)
                        .thenComparingInt(Lote::getIdLote));
    }
}
