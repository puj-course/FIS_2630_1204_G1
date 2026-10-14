package org.example.carestock.DataTransferObject;

import org.example.carestock.model.Lote;
import java.util.*;

public record DatosLotes(
        List<Lote> lotes,
        Map<Integer, String> categoriaPorMedicamento,
        Map<Integer, String> categorias,
        Map<String, Integer> conteoTipos
) {
    public DatosLotes {
        lotes = List.copyOf(lotes);
        categoriaPorMedicamento =
                Map.copyOf(categoriaPorMedicamento);
        categorias = Map.copyOf(categorias);
        conteoTipos = Map.copyOf(conteoTipos);
    }
}
