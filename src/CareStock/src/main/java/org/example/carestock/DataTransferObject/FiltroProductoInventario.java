package org.example.carestock.DataTransferObject;

import java.util.Locale;

/** Filtro puro reutilizable y comprobable sin JavaFX ni conexion a PostgreSQL. */
public final class FiltroProductoInventario {

    public static final String TODAS = "Todas";

    private FiltroProductoInventario() { }

    public static boolean coincide(ProductoInventario producto, String categoria, String busqueda) {
        if (producto == null) return false;
        String tipo = categoria == null ? TODAS : categoria.trim();
        boolean coincideCategoria = tipo.isEmpty()
                || TODAS.equalsIgnoreCase(tipo)
                || producto.categoria().equalsIgnoreCase(tipo);
        if (!coincideCategoria) return false;
        String termino = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        return termino.isEmpty()
                || producto.codigo().toLowerCase(Locale.ROOT).contains(termino)
                || producto.nombre().toLowerCase(Locale.ROOT).contains(termino);
    }
}
