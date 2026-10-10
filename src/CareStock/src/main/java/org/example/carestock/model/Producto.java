package org.example.carestock.model;
/**
 * Contrato Builder para la construccion de productos.
 * Define los pasos comunes necesarios para configurar
 * productos de Aseo y Maternidad.
 * Patron de diseño: Builder (GoF).
 */
public interface Producto {
    void reset();
    Producto setCodigo(String codigo);
    Producto setNombre(String nombre);
    Producto setDescripcion(String descripcion);
    Producto setPrecio(double precio);
    Producto setStock(int stock);
}
