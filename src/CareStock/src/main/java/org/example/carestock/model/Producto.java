package org.example.carestock.model;

/**
 * Interfaz Builder que declara los pasos comunes para la construcción de productos.
 * Referencia: Patrón Builder
 */
public interface Producto {
    void reset();
    Producto setCodigo(String codigo);
    Producto setNombre(String nombre);
    Producto setDescripcion(String descripcion);
    Producto setPrecio(double precio);
    Producto setStock(int stock);
}