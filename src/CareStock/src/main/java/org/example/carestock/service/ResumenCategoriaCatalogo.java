package org.example.carestock.service;

import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Indicadores de la categoria consultada.
 * No modifica existencias ni consulta otras farmacias.
 */
public record ResumenCategoriaCatalogo(
        int total,
        int bajoMinimo,
        int agotados) {

    public static ResumenCategoriaCatalogo calcular(
            String categoria,
            List<RegistroCatalogo<?>> registros) {

        if (!"Aseo".equals(categoria)
                && !"Maternidad".equals(categoria)) {
            throw new IllegalArgumentException(
                    "Categoria no soportada: " + categoria);
        }

        if (registros == null) {
            throw new IllegalArgumentException(
                    "La lista no puede ser nula");
        }

        Set<Integer> ids = new HashSet<>();
        int total = 0;
        int bajos = 0;
        int agotados = 0;

        for (var r : registros) {
            if (r == null || r.id() <= 0) {
                continue;
            }

            int stock;
            int minimo;

            if ("Aseo".equals(categoria)
                    && r.producto() instanceof Aseo p) {
                stock = p.getStock();
                minimo = p.getStockMinimo();
            } else if ("Maternidad".equals(categoria)
                    && r.producto() instanceof Maternidad p) {
                stock = p.getStock();
                minimo = p.getStockMinimo();
            } else {
                continue;
            }

            if (!ids.add(r.id())) {
                continue;
            }

            total++;

            if (stock == 0) {
                agotados++;
            } else if (stock > 0
                    && minimo > 0
                    && stock <= minimo) {
                bajos++;
            }
        }

        return new ResumenCategoriaCatalogo(
                total, bajos, agotados);
    }
}
