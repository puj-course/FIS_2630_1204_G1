
package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class AseoBuilder implements Producto {

    private Aseo aseo;

    public AseoBuilder() {
        reset();
    }

    @Override
    public void reset() {
        this.aseo = new Aseo();
    }

    @Override
    public AseoBuilder setCodigo(String codigo) {
        aseo.setCodigo(codigo);
        return this;
    }

    @Override
    public AseoBuilder setNombre(String nombre) {
        aseo.setNombre(nombre);
        return this;
    }

    @Override
    public AseoBuilder setDescripcion(
            String descripcion) {
        aseo.setDescripcion(descripcion);
        return this;
    }

    @Override
    public AseoBuilder setPrecio(double precio) {
        aseo.setPrecio(precio);
        return this;
    }

    @Override
    public AseoBuilder setStock(int stock) {
        aseo.setStock(stock);
        return this;
    }

    // Metodos especificos de aseo

    public AseoBuilder tipoAseo(String tipoAseo) {
        aseo.setTipoAseo(tipoAseo);
        return this;
    }

    public AseoBuilder biodegradable(
            boolean biodegradable) {
        aseo.setBiodegradable(biodegradable);
        return this;
    }

    public AseoBuilder componentesActivos(
            String componentesActivos) {
        aseo.setComponentesActivos(componentesActivos);
        return this;
    }

    // Construccion y validacion del producto

    public Aseo build() {

        try {
            aseo.validar();

        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(
                    "No se pudo construir el producto de aseo: "
                            + e.getMessage(), e
            );
        }

        Aseo productoFinal = this.aseo;

        reset();

        return productoFinal;
    }
}
