package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

/**
 * Datos compartidos por los productos con codigo, nombre,
 * descripcion, precio y existencias.
 * Las reglas particulares permanecen en cada subtipo.
 */
public abstract class ProductoBase implements Validable {

    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;

    protected final void validarCamposComunes(String tipo)
            throws ReglaNegocioException {

        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException(
                    "El codigo del producto de " + tipo + " es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException(
                    "El nombre del producto de " + tipo + " es obligatorio.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException(
                    "La descripcion del producto de " + tipo + " es obligatoria.");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new ReglaNegocioException(
                    "El precio del producto de " + tipo
                    + " debe ser valido y no negativo.");
        }
    }

    protected final void validarStockNoNegativo(String tipo)
            throws ReglaNegocioException {

        if (stock < 0) {
            throw new ReglaNegocioException(
                    "El stock del producto de " + tipo
                    + " no puede ser negativo.");
        }
    }

    protected final boolean esValidoYConStock() {
        try {
            validar();
            return stock > 0;
        } catch (ReglaNegocioException e) {
            return false;
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
