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

    private record Existencias(int stock, int minimo) { }

    private static Existencias obtenerExistencias(String categoria, Object producto) {
        if ("Aseo".equals(categoria) && producto instanceof Aseo aseo) {
            return new Existencias(aseo.getStock(), aseo.getStockMinimo());
        }
        if ("Maternidad".equals(categoria) && producto instanceof Maternidad maternidad) {
            return new Existencias(maternidad.getStock(), maternidad.getStockMinimo());
        }
        return null;
    }

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

            Existencias datos = obtenerExistencias(categoria, r.producto());
            if (datos != null && ids.add(r.id())) {
                total++;
                if (datos.stock() == 0) {
                    agotados++;
                } else if (datos.stock() > 0
                        && datos.minimo() > 0
                        && datos.stock() <= datos.minimo()) {
                    bajos++;
                }
            }
        }

        return new ResumenCategoriaCatalogo(
                total, bajos, agotados);
    }
}
