package org.example.carestock.service;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Lote;
import org.example.carestock.model.Medicamento;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Cuenta medicamentos unicos, no filas de lotes. */
public record IndicadoresCatalogo(
        int total,
        int conLotesVencidos,
        int agotados) {

    public static IndicadoresCatalogo calcular(
            List<RegistroCatalogo<Medicamento>> registros,
            List<Lote> lotes,
            LocalDate hoy) {

        Objects.requireNonNull(registros);
        Objects.requireNonNull(lotes);
        Objects.requireNonNull(hoy);

        Map<Integer, Medicamento> unicos = new HashMap<>();

        for (var registro : registros) {
            if (registro != null
                    && registro.id() > 0
                    && registro.producto() != null) {
                unicos.putIfAbsent(
                        registro.id(), registro.producto());
            }
        }

        Set<Integer> vencidos = new HashSet<>();

        for (var lote : lotes) {
            if (lote != null
                    && unicos.containsKey(lote.getIdMedicamento())
                    && lote.getFechaVencimiento() != null
                    && !lote.getFechaVencimiento().isAfter(hoy)) {

                vencidos.add(lote.getIdMedicamento());
            }
        }

        int agotados = (int) unicos.values().stream()
                .filter(m -> m.getStockTotal() == 0)
                .count();

        return new IndicadoresCatalogo(
                unicos.size(), vencidos.size(), agotados);
    }
}
