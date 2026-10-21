package org.example.carestock.dto;

import java.util.*;

public record DatosCatalogo(
        List<RegistroCatalogo<?>> productos,
        Map<Integer, String> categoriasMedicamento,
        long creadoNanos
) {
    public DatosCatalogo {
        productos = List.copyOf(productos);
        categoriasMedicamento =
                Collections.unmodifiableMap(
                        new LinkedHashMap<>(
                                categoriasMedicamento));
    }
}
