
package org.example.carestock.model;
import org.example.carestock.exception.ReglaNegocioException;

public class MaternidadBuilder implements Producto {

    private Maternidad maternidad;

    public MaternidadBuilder() {
        reset();
    }

    @Override
    public void reset() {
        this.maternidad = new Maternidad();
    }

    @Override
    public MaternidadBuilder setCodigo(String codigo) {
        maternidad.setCodigo(codigo);
        return this;
    }

    @Override
    public MaternidadBuilder setNombre(String nombre) {
        maternidad.setNombre(nombre);
        return this;
    }

    @Override
    public MaternidadBuilder setDescripcion(
            String descripcion) {
        maternidad.setDescripcion(descripcion);
        return this;
    }

    @Override
    public MaternidadBuilder setPrecio(double precio) {
        maternidad.setPrecio(precio);
        return this;
    }

    @Override
    public MaternidadBuilder setStock(int stock) {
        maternidad.setStock(stock);
        return this;
    }

    // Metodos especificos de maternidad

    public MaternidadBuilder etapaRecomendada(
            String etapaRecomendada) {
        maternidad.setEtapaRecomendada(etapaRecomendada);
        return this;
    }

    public MaternidadBuilder hipoalergenico(
            boolean hipoalergenico) {
        maternidad.setHipoalergenico(hipoalergenico);
        return this;
    }

    public MaternidadBuilder edadGestacionalSugerida(
            int edadGestacionalSugerida) {
        maternidad.setEdadGestacionalSugerida(
                edadGestacionalSugerida);
        return this;
    }

    // Construccion y validacion del producto
    public Maternidad build() {

        try {
            maternidad.validar();

        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(
                    "No se pudo construir el producto de maternidad: "
                            + e.getMessage(), e
            );
        }

        Maternidad productoFinal = this.maternidad;

        reset();

        return productoFinal;
    }
}
