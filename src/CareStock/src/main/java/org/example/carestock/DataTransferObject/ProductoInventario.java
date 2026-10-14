package org.example.carestock.DataTransferObject;

import java.util.Objects;

/** Fila de lectura de tres tablas distintas; no sustituye a los modelos de dominio. */
public record ProductoInventario(
        int id,
        String categoria,
        String codigo,
        String nombre,
        String presentacion,
        int existencia,
        String estado
) {
    public ProductoInventario {
        if (id <= 0) throw new IllegalArgumentException("ID de producto invalido");
        categoria = Objects.requireNonNull(categoria, "categoria");
        codigo = Objects.requireNonNull(codigo, "codigo");
        nombre = Objects.requireNonNull(nombre, "nombre");
        presentacion = Objects.requireNonNull(presentacion, "presentacion");
        estado = Objects.requireNonNull(estado, "estado");
        if (existencia < 0) throw new IllegalArgumentException("La existencia no puede ser negativa");
    }
}
