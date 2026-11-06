package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Filtros reutilizables para la consulta y el CSV. */
public final class FiltroReposicion {

    private FiltroReposicion() { }

    public static List<RegistroReposicion> aplicar(
            List<RegistroReposicion> registros,
            Categoria categoria,
            Severidad severidad,
            String busqueda) {

        Objects.requireNonNull(registros,
                "Los registros son obligatorios");

        String texto = busqueda == null
                ? ""
                : busqueda.strip().toLowerCase(Locale.ROOT);

        return registros.stream()
                .filter(Objects::nonNull)
                .filter(r -> categoria == null
                        || r.categoria() == categoria)
                .filter(r -> severidad == null
                        || r.severidad() == severidad)
                .filter(r -> texto.isEmpty()
                        || r.codigo().toLowerCase(Locale.ROOT)
                                .contains(texto)
                        || r.nombre().toLowerCase(Locale.ROOT)
                                .contains(texto))
                .toList();
    }
}
