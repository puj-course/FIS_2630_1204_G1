
package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

/**
 * Constructor concreto del patrón Builder
 * para dispositivos médicos.
 *
 * Implementa los pasos definidos por Producto
 * y permite construir objetos DispositivoMedico.
 */
public class DispositivoMedicoBuilder implements Producto {

    // ==========================================
    // PRODUCTO EN CONSTRUCCIÓN
    // ==========================================

    private DispositivoMedico dispositivo;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DispositivoMedicoBuilder() {
        reset();
    }

    // ==========================================
    // REINICIAR CONSTRUCCIÓN
    // ==========================================

    @Override
    public void reset() {
        this.dispositivo = new DispositivoMedico();
    }

    // ==========================================
    // MÉTODOS DEL CONTRATO PRODUCTO
    // ==========================================

    @Override
    public DispositivoMedicoBuilder setCodigo(String codigo) {

        dispositivo.setCodigo(codigo);
        return this;
    }

    @Override
    public DispositivoMedicoBuilder setNombre(String nombre) {

        dispositivo.setNombre(nombre);
        return this;
    }

    @Override
    public DispositivoMedicoBuilder setDescripcion(String descripcion) {

        dispositivo.setDescripcion(descripcion);
        return this;
    }

    @Override
    public DispositivoMedicoBuilder setPrecio(double precio) {

        dispositivo.setPrecio(precio);
        return this;
    }

    @Override
    public DispositivoMedicoBuilder setStock(int stock) {

        dispositivo.setStock(stock);
        return this;
    }

    // ==========================================
    // ATRIBUTOS ESPECÍFICOS DEL DISPOSITIVO
    // ==========================================

    public DispositivoMedicoBuilder setClaseRiesgo(String claseRiesgo) {

        dispositivo.setClaseRiesgo(claseRiesgo);
        return this;
    }

    public DispositivoMedicoBuilder setRegistroSanitario(
            String registroSanitario
    ) {

        dispositivo.setRegistroSanitario(registroSanitario);
        return this;
    }

    // ==========================================
    // CONSTRUIR PRODUCTO FINAL
    // ==========================================

    public DispositivoMedico build()
            throws ReglaNegocioException {

        // Validar antes de entregar el producto
        dispositivo.validar();

        // Obtener el objeto construido
        DispositivoMedico productoFinal = dispositivo;

        // Preparar el Builder para otro producto
        reset();

        // Entregar el producto final
        return productoFinal;
    }
}
